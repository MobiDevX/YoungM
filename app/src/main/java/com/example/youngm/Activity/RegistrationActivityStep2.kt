package com.example.youngm.Activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.youngm.R
import kotlinx.android.synthetic.main.activity_registration_step2.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.youngm.Adapter.ChooseCategoryAdapter
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.choose_category.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.*
import okhttp3.RequestBody.Companion.asRequestBody
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager

import com.bumptech.glide.Glide
import com.example.youngm.Data.CategoryInformation
import com.example.youngm.Data.URLData


class RegistrationActivityStep2 : AppCompatActivity() {


    val PICK_FROM_GALLERY = 1
    val JSON: MediaType = "multipart/form-data".toMediaType()
    private val clientOkHttp = OkHttpClient()
    val context: Context = this
    val PICK_IMAGE: Int = 1
    var allCategory: ArrayList<String> = ArrayList()
    var selectedUserCategory: ArrayList<String> = ArrayList()
    val chooseCategoryAdapter = ChooseCategoryAdapter()
    var pathName: String = ""
    private var themeImage = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration_step2)
        val firstName = intent.extras?.get("first_name").toString()
        val lastName = intent.extras?.get("last_name").toString()
        val username = intent.extras?.get("username").toString()
        val password = intent.extras?.get("password").toString()
        val email = intent.extras?.get("email").toString()


        themeRbtn1.setOnClickListener {
            if (themeImage != 1){
                themeImage = 1
                themeRbtn1.setBackgroundResource(R.drawable.profile_bar2_active)
                themeRbtn2.setBackgroundResource(R.drawable.profile_bar3)
                themeRbtn3.setBackgroundResource(R.drawable.profile_bar4)
            }
        }

        themeRbtn2.setOnClickListener {
            if (themeImage != 2){
                themeImage = 2
                themeRbtn1.setBackgroundResource(R.drawable.profile_bar2)
                themeRbtn2.setBackgroundResource(R.drawable.profile_bar3_active)
                themeRbtn3.setBackgroundResource(R.drawable.profile_bar4)
            }
        }

        themeRbtn3.setOnClickListener {
            if (themeImage != 3){
                themeImage = 3
                themeRbtn1.setBackgroundResource(R.drawable.profile_bar2)
                themeRbtn2.setBackgroundResource(R.drawable.profile_bar3)
                themeRbtn3.setBackgroundResource(R.drawable.profile_bar4_active)
            }
        }




        loadCategory()


        btnFinishReg.setOnClickListener {
            finishRegistration(firstName, lastName, username, password, email)
        }


        btnChooseImage.setOnClickListener{
            try {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(this@RegistrationActivityStep2,
                        arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ),
                        PICK_FROM_GALLERY
                    )
                } else {
                    val galleryIntent = Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )
                    startActivityForResult(galleryIntent, PICK_FROM_GALLERY)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        btnChooseCategory.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder
                .setPositiveButton("Done", object: DialogInterface.OnClickListener{
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        updateSelectedData()
                    }

                })
                .setView(R.layout.choose_category)
            var alert = builder.create()
            alert.show()
            alert.recyclerChooseCategory.adapter = chooseCategoryAdapter
            alert.recyclerChooseCategory.layoutManager = LinearLayoutManager(context)
            val job: Job = GlobalScope.launch(Dispatchers.Main) {
                chooseCategoryAdapter.refreshData(allCategory)
            }
            job.start()

        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PICK_FROM_GALLERY -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val galleryIntent = Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )
                    startActivityForResult(galleryIntent, PICK_FROM_GALLERY)
                    val photoPickerIntent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI)
                    startActivityForResult(photoPickerIntent, PICK_IMAGE)
                } else {
                    //do something like displaying a message that he didn't allow the app to access gallery and you wont be able to let him select from gallery
                }
            }
        }
    }

    private fun updateSelectedData(){
        selectedUserCategory = chooseCategoryAdapter.selectedCategory
    }


    private fun loadCategory(){
        val url = URLData.MAIN_URL + URLData.GET_ALL_CATEGORY
        val request = Request.Builder().url(url).build()
        clientOkHttp.newCall(request).enqueue(object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ERROR", e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val categories = GsonBuilder().create().fromJson(body, CategoryInformation::class.java)
                for (category in categories.result){
                    allCategory.add(category.name)
                }
            }
        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            try{
                val imageUri = data!!.data!!
                pathName = imageUri.lastPathSegment!!
                val job: Job = GlobalScope.launch(Dispatchers.Main){
                    Glide.with(context).load(pathName).into(imageViewPhotoReg)
                }
                job.start()
            }catch (e: FileNotFoundException){
                val job: Job = GlobalScope.launch(Dispatchers.Main) {
                    Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show()
                }
                job.start()
            }
        }

    }



    private fun finishRegistration(firstName: String, lastName: String, username: String, password: String, email: String){
        val url = URLData.MAIN_URL + URLData.CREATE_USER
        lateinit var requestBody: RequestBody
        var openLikedPost = true
        if (rbtnFavouriteJustMe.isChecked){
            openLikedPost = false
        }
        if (pathName != "") {
            val file: File = File(pathName)
            requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("username", username)
                .addFormDataPart("password", password)
                .addFormDataPart("email", email)
                .addFormDataPart("first_name", firstName)
                .addFormDataPart("last_name", lastName)
                .addFormDataPart("location", editTextLocationReg.text.toString())
                .addFormDataPart("favorite_categories", selectedUserCategory.toString())
                .addFormDataPart("about_me", editTextAboutMeReg.text.toString())
                .addFormDataPart("photo", file.name, file.asRequestBody(JSON))
                .addFormDataPart("image_bar", themeImage.toString())
                .addFormDataPart("open_liked_post", openLikedPost.toString())
                .build()
        }else {
            requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("username", username)
                .addFormDataPart("password", password)
                .addFormDataPart("email", email)
                .addFormDataPart("first_name", firstName)
                .addFormDataPart("last_name", lastName)
                .addFormDataPart("location", editTextLocationReg.text.toString())
                .addFormDataPart("favorite_categories", selectedUserCategory.toString())
                .addFormDataPart("about_me", editTextAboutMeReg.text.toString())
                .addFormDataPart("photo", "")
                .addFormDataPart("image_bar", themeImage.toString())
                .addFormDataPart("open_liked_post", openLikedPost.toString())
                .build()
        }

        val request = Request.Builder().url(url).post(requestBody).build()
        clientOkHttp.newCall(request).enqueue(object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ERROR", e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val intent = Intent(context, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        })
    }




}


