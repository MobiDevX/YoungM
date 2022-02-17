package com.example.youngm.Activity

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.example.youngm.Data.MediaSerializable
import com.example.youngm.R
import kotlinx.android.synthetic.main.activity_open_post.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import android.content.Intent
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.youngm.App.GlideApp
import com.example.youngm.Data.URLData
import com.example.youngm.Fragment.OpenPost.OpenPostDescriptionFragment
import com.example.youngm.Interface.MusicDataViewModel
import okhttp3.*
import java.io.IOException


class OpenPostActivity : AppCompatActivity() {

    var ID: Int = -1
    val context: Context = this
    private lateinit var TOKEN: String
    private lateinit var LOGIN: String
    private lateinit var USERNAME: String
    private var isLiked = false
    private var isPlay: Boolean = false
    private var mediaPostID: Int = -1
    private var musicPosition: Int = -1
    private var imageURL = ""
    private val okHttpClient = OkHttpClient()
    private lateinit var musicDataViewModel: MusicDataViewModel



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_post)
        musicDataViewModel = MusicDataViewModel(context, "OpenPostActivity")




        val toolbar: Toolbar = openPostTopBar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
//        toolbar.overflowIcon = ContextCompat.getDrawable(applicationContext,android.R.drawable)

        ID = intent.extras?.getInt("ID")!!
        TOKEN = intent.extras?.getString("TOKEN")!!
        LOGIN = intent.extras?.getString("LOGIN")!!
        USERNAME = intent.extras?.getString("USERNAME")!!
        isLiked = intent.extras?.getBoolean("isLiked")!!
        musicPosition = intent.extras?.getInt("musicPosition")!!

        openPostImageBar.setOnClickListener {
            val intent = Intent(this, OpenPhotoActivity::class.java)
            intent.putExtra("Image", imageURL)
            intent.putExtra("Title", getString(R.string.title_post))
            startActivity(intent)

        }


        val mediaSerializable = intent.extras?.getSerializable("media") as MediaSerializable
        isPlay = musicDataViewModel.getIsPlaying()
        mediaPostID = musicDataViewModel.getPostID()
        imageURL = intent.extras?.getString("image")!!
        loadPostPhoto(imageURL)

    }


    fun loadPostPhoto(postPhoto: String){
        val job: Job = GlobalScope.launch(Dispatchers.Main) {
            GlideApp.with(context).load(postPhoto).into(openPostImageBar)
        }
        job.start()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (LOGIN != USERNAME) {
            menuInflater.inflate(R.menu.open_post_guest_menu, menu)
        }else{
            menuInflater.inflate(R.menu.open_post_author_menu, menu)
        }
        return true

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when(id){
            R.id.report_settings -> Toast.makeText(context, "REPORT", Toast.LENGTH_LONG).show()
            R.id.edit_settings ->   Toast.makeText(context, "EDIT", Toast.LENGTH_LONG).show()
            R.id.delete_settings -> {
                val builder = AlertDialog.Builder(context)
                builder.setMessage("Вы дейстивтельно хотите удалить пост?")
                    .setPositiveButton("Да"
                    ) { dialog, which ->
                        deletePost()
                    }
                    .setNegativeButton("Нет", null)
                val dialog = builder.create()
                dialog.window!!.setBackgroundDrawableResource(R.color.cardColor)
                dialog.context.setTheme(R.style.CommentCommandTheme)
                dialog.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }



    fun get_id(): Int{
        return ID
    }

    fun getToken(): String{
        return TOKEN
    }

    fun getIsPlay(): Boolean{
        return musicDataViewModel.getIsPlaying()
    }

    fun getMediaPostID(): Int{
        return musicDataViewModel.getPostID()
    }


    fun getLogin(): String{
        return LOGIN
    }

    fun getImageURL(): String{
        return imageURL
    }

    fun getIsLiked(): Boolean{
        return isLiked
    }

    fun getMusicPostition(): Int{
        return musicDataViewModel.getMusicPosition()
    }


    override fun finish() {
        val dataIntent = Intent()
        val musicDataViewModel: MusicDataViewModel = MusicDataViewModel(context, "OpenPostActivityFinish")
        val description = openPostDescriptionFragment as OpenPostDescriptionFragment
        dataIntent.putExtra("isLiked", description.getIsLiked())
        dataIntent.putExtra("isPlaying", musicDataViewModel.getIsPlaying())
        dataIntent.putExtra("Position", mediaPostID)
        dataIntent.putExtra("MusicPosition", musicDataViewModel.getMusicPosition())
        try {
            unregisterReceiver(description.getReceiver())
        }catch (e:Exception){
            
        }
        setResult(Activity.RESULT_OK, dataIntent)
        super.finish()
    }




    private fun deletePost(){
        val url = URLData.MAIN_URL + URLData.DELETE_POST + ID
        val request = Request.Builder().url(url).header("Authorization", "Token $TOKEN").build()
        okHttpClient.newCall(request).enqueue(object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200){
                    finish()
                }
            }
        })

    }
}
