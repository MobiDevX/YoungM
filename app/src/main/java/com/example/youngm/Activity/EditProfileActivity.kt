package com.example.youngm.Activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.marginTop
import com.bumptech.glide.Glide
import com.example.youngm.App.GlideApp
import com.example.youngm.Data.AuthorPhoto
import com.example.youngm.Data.URLData
import com.example.youngm.R
import com.google.gson.GsonBuilder
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.post_item.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

class EditProfileActivity : AppCompatActivity() {

    lateinit var TOKEN: String
    lateinit var USERNAME: String
    lateinit var context: Context
    private val PICK_FROM_GALLERY = 1
    private var pathNamePhoto = ""
    private lateinit var image: CircleImageView
    val JSON: MediaType = "multipart/form-data".toMediaType()
    val okHttpClient = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        TOKEN = intent.extras?.getString("TOKEN").toString()
        USERNAME = intent.extras?.getString("USERNAME").toString()
        context = this

        getImageProfile()

        editProfileImage.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            image = CircleImageView(this)
            image.setImageBitmap(editProfileImage.drawable.toBitmap())
            image.context.setTheme(R.style.EditProfileImageTheme)
            image.setOnClickListener {
                try {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(this@EditProfileActivity,
                            arrayOf(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ),
                            PICK_FROM_GALLERY
                        )
                    } else {
                        val galleryIntent = Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        )
                        startActivityForResult(galleryIntent, PICK_FROM_GALLERY)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            builder.setView(image)
                .setPositiveButton("Done"
                ) { dialog, which ->
                    editImage()
                }
                .setNegativeButton("Cancel") { dialog, which ->
                    Toast.makeText(context, "Cancel", Toast.LENGTH_LONG).show()
                }
            val dialog = builder.create()
            dialog.window!!.setBackgroundDrawableResource(R.color.cardColor)
            dialog.context.setTheme(R.style.CommentCommandTheme)
            dialog.show()
        }



    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    val imageUri = data!!.data!!
                    val projection = arrayOf(MediaStore.Images.Media.DATA)
                    val cursor = contentResolver.query(imageUri, projection, null, null, null)
                    if (cursor!!.moveToFirst()) {
                        val columnIndex = cursor
                            .getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                        pathNamePhoto = cursor.getString(columnIndex)
                        cursor.close()
                    }
                    // Найти решение подходящее для SDK 29
                    val job: Job = GlobalScope.launch(Dispatchers.Main) {
                        GlideApp.with(context).load(pathNamePhoto).into(image)
                    }
                    job.start()
                } catch (e: FileNotFoundException) {
                    val job: Job = GlobalScope.launch(Dispatchers.Main) {
                        Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show()
                    }
                    job.start()
                }
            }
        }
    }

    private fun editImage(){
        if (pathNamePhoto != ""){
            val url = URLData.MAIN_URL + URLData.EDIT_PHOTO
            val file = File(pathNamePhoto)
            val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("photo", file.name, file.asRequestBody(JSON))
                .build()
            val request = Request.Builder().url(url).post(requestBody).header("Authorization", "Token $TOKEN").build()
            okHttpClient.newCall(request).enqueue(object: Callback{
                override fun onFailure(call: Call, e: IOException) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.code == 200) {
                        val job: Job = GlobalScope.launch(Dispatchers.Main) {
                            Toast.makeText(context, response.body?.string(), Toast.LENGTH_SHORT)
                                .show()
                            editProfileImage.setImageBitmap(image.drawable.toBitmap())
                        }
                        job.start()
                    }
                    pathNamePhoto = ""
                }
            })
        }
    }

    fun getImageProfile(){
        val url = URLData.MAIN_URL + URLData.PROFILE_IMAGE + USERNAME
        val request = Request.Builder().url(url).header("Authorization", "Token $TOKEN").build()
        okHttpClient.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Error", e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val path_media = response.body?.string()
                val json = GsonBuilder().create().fromJson(path_media, AuthorPhoto::class.java)
                val job: Job = GlobalScope.launch(Dispatchers.Main) {
                    if (json.image != "") {
                        GlideApp.with(context).load(json.image)
                            .into(editProfileImage)
                    } else {
                        GlideApp.with(context).load(R.mipmap.default_profile_image_round)
                            .into(editProfileImage)
                    }
                }
                job.start()

            }
        })
    }
}


