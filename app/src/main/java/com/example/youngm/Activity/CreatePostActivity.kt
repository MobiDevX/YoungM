package com.example.youngm.Activity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.youngm.Adapter.OpenPostMusicAdapter
import com.example.youngm.Data.CategoryInformation
import com.example.youngm.Data.MusicData
import com.example.youngm.Data.URLData
import com.example.youngm.Interface.ProfileDataViewModel
import com.example.youngm.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_create_post.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.math.RoundingMode
import okhttp3.RequestBody.Companion.asRequestBody
import kotlin.time.ExperimentalTime
import kotlin.time.microseconds
import kotlin.time.milliseconds
import kotlin.time.minutes


class CreatePostActivity : AppCompatActivity() {

    private lateinit var TOKEN: String
    private lateinit var USERNAME: String
    private var genre: ArrayList<String> = ArrayList()
    private val okHttpClient = OkHttpClient()
    private val context: Context = this
    val PICK_FROM_GALLERY = 1
    val PICK_FROM_AUDIO = 2
    var pathNamePhoto = ""
    val JSON: MediaType = "multipart/form-data".toMediaType()
    var musicList = ArrayList<MusicData>()
    private val PLAY_FLAG = 2
    lateinit var adapter: OpenPostMusicAdapter
    private lateinit var profileDataViewModel: ProfileDataViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)
        profileDataViewModel = ProfileDataViewModel(this)
        TOKEN = profileDataViewModel.getToken()!!
        USERNAME = profileDataViewModel.getLogin()!!
        adapter = OpenPostMusicAdapter(this,-1, -1 , "", TOKEN, USERNAME,false, null)
        recyclerMusicList.adapter = adapter
        recyclerMusicList.layoutManager = LinearLayoutManager(this)


        loadCategory()


        createPostAddPhotoLayout.setOnClickListener {
            try {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(this@CreatePostActivity,
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

        btnAddMusic.setOnClickListener {
            try {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(this@CreatePostActivity,
                        arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ),
                        PICK_FROM_AUDIO
                    )
                } else {
                    val audioIntent = Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    )
                    startActivityForResult(audioIntent, PICK_FROM_AUDIO)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        btnCreatePost.setOnClickListener {
            createPost()
        }
    }


    @ExperimentalTime
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
                        Glide.with(context).load(pathNamePhoto).into(addPhotoImageView)
                        addPhotoTextView.visibility = View.INVISIBLE
                        addPhotoImageLayout.visibility = View.VISIBLE
                    }
                    job.start()
                } catch (e: FileNotFoundException) {
                    val job: Job = GlobalScope.launch(Dispatchers.Main) {
                        Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show()
                    }
                    job.start()
                }
            }
        }else if (requestCode == 2){
            if (resultCode == Activity.RESULT_OK) {
                try {
                    val job: Job = GlobalScope.launch(Dispatchers.Main) {
                        var path: String = ""
                        val musicUri = data!!.data!!
                        val projection = arrayOf(MediaStore.Images.Media.DATA)
                        val cursor = contentResolver.query(musicUri, projection, null, null, null)
                        if (cursor!!.moveToFirst()) {
                            val columnIndex = cursor
                                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                            path = cursor.getString(columnIndex)
                            cursor.close()
                        }
                        val name = File(path).nameWithoutExtension
                        val player = MediaPlayer()
                        player.setDataSource(path)
                        player.prepareAsync()
                        player.setOnPreparedListener { mp ->
                            val minutes = mp.duration.milliseconds.inMinutes.toBigDecimal().setScale(0, RoundingMode.DOWN).toString()
                            val seconds = mp.duration.milliseconds.inSeconds.toBigDecimal().setScale(0, RoundingMode.DOWN).toString().substring(0,2)
                            musicList.add(MusicData(musicList.size+1, USERNAME, name, path, "$minutes:$seconds"))
                            mp.stop()
                            mp.release()
                            adapter.refreshData(musicList)
                        }
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



    private fun createPost(){
        val url = URLData.MAIN_URL + URLData.CREATE_POST
        if (pathNamePhoto == ""){
            Toast.makeText(context, resources.getText(R.string.warning_create_post_photo), Toast.LENGTH_LONG).show()
        }else if(musicList.size == 0){
            Toast.makeText(context, resources.getText(R.string.warning_create_post_music), Toast.LENGTH_LONG).show()
        }else if (createPostGenre.text.isEmpty()){
            Toast.makeText(context, resources.getText(R.string.warning_create_post_genre), Toast.LENGTH_LONG).show()
        }else{
            lateinit var requestBody: RequestBody
            val file = File(pathNamePhoto)
            val multipartBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            for ((index, music) in musicList.withIndex()){
                val musicFile = File(music.urlMusic)
                multipartBody.addFormDataPart("music[$index]", musicFile.name, musicFile.asRequestBody(JSON))
                multipartBody.addFormDataPart("music_name[$index]", musicFile.nameWithoutExtension)
            }
            requestBody = multipartBody
                .addFormDataPart("photo", file.name, file.asRequestBody(JSON))
                .addFormDataPart("description", createPostDescription.text.toString())
                .addFormDataPart("category", createPostGenre.text.toString())
                .build()
            val request = Request.Builder().url(url).post(requestBody).header("Authorization", "Token $TOKEN").build()
            okHttpClient.newCall(request).enqueue(object: Callback{
                override fun onFailure(call: Call, e: IOException) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onResponse(call: Call, response: Response) {
                    finish()
                }
            })



        }

    }


    private fun loadCategory(){
        val url = URLData.MAIN_URL + URLData.GET_ALL_CATEGORY
        val request = Request.Builder().url(url).build()
        okHttpClient.newCall(request).enqueue(object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onResponse(call: Call, response: Response) {
                val job: Job = GlobalScope.launch(Dispatchers.Main) {
                    val body = response.body?.string()
                    val allGenre = Gson().fromJson(body, CategoryInformation::class.java)
                    for (oneGenre in allGenre.result) {
                        genre.add(oneGenre.name)
                    }
                    val arrayAdapter =
                        ArrayAdapter<String>(context, R.layout.auto_complete_text_item, genre)
                    createPostGenre.setAdapter(arrayAdapter)
                }
                job.start()

            }
        })
    }
}
