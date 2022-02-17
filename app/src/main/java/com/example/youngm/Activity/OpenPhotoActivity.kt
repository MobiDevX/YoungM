package com.example.youngm.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.example.youngm.App.GlideApp
import com.example.youngm.Data.URLData
import com.example.youngm.R
import kotlinx.android.synthetic.main.activity_open_photo.*

class OpenPhotoActivity : AppCompatActivity() {

    private var imageURL = ""
    private var title = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_photo)
        imageURL = intent.extras?.getString("Image")!!
        title = intent.extras?.getString("Title")!!
        val toolbar: Toolbar = openPhotoToolbar
        toolbar.title = title
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        GlideApp.with(this).load(imageURL).into(imageViewOpenPhoto)


    }
}
