package com.example.youngm.Activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.youngm.App.GlideApp
import com.example.youngm.Data.AuthorPhoto
import com.example.youngm.Data.URLData
import com.example.youngm.Fragment.Navigation.FeedFragment
import com.example.youngm.Fragment.Navigation.MessageFragment
import com.example.youngm.Fragment.Navigation.SearchFragment
import com.example.youngm.Interface.MusicDataViewModel
import com.example.youngm.Interface.ProfileDataViewModel
import com.example.youngm.R
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private val clientOkHttp = OkHttpClient()
    val KEY_TOKEN = "SAVED_TOKEN"
    val KEY_LOGIN = "SAVED_LOGIN"
    lateinit var context: Context
    lateinit var TOKEN: String
    lateinit var LOGIN: String
    private lateinit var musicDataViewModel: MusicDataViewModel
    private lateinit var profileDataViewModel: ProfileDataViewModel




    companion object{
        val JSON: MediaType = "application/json; charset=utf-8".toMediaType()
        val MUSIC_SETTINGS = "music"
        val PROFILE_SETTINGS = "profile"
        val KEY_TOKEN = "TOKEN"
        val KEY_LOGIN = "LOGIN"
        val KEY_POST_ID = "MUSIC_POST_ID"
        val KEY_POST_POSITION = "MUSIC_POST_POSITION"
        val KEY_MUSIC_ID = "MUSIC_ID"
        val KEY_ISPLAYING = "MUSIC_ISPLAYING"
        val KEY_AUTHOR = "MUSIC_AUTHOR"
        val KEY_MUSIC_NAME = "MUSIC_NAME"
        val KEY_PHOTO = "MUSIC_PHOTO"
        val KEY_ISLIKED = "MUSIC_ISLIKED"
        val KEY_SIZE = "MUSIC_SIZE"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = mainActivityToolbar
        setSupportActionBar(toolbar)
        context = this
        profileDataViewModel = ProfileDataViewModel(context)
        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, FeedFragment()).commit()
        mainBottomNavigation.setOnNavigationItemSelectedListener {
            val selectedFragment = when(it.itemId){
                R.id.nav_item_home -> FeedFragment()
                R.id.nav_item_profile -> ProfileActivity()
                R.id.nav_item_share -> CreatePostActivity()
                R.id.nav_item_message -> MessageFragment()
                else -> SearchFragment()
            }
            if (selectedFragment is Fragment){
                supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, selectedFragment).commit()
                true
            }else{
                val intent = Intent(context, selectedFragment::class.java)
                intent.putExtra("USERNAME", LOGIN)
                startActivity(intent)
                false
            }

        }
        if (isInternetAvailable(context)) {
            checkAutorization()
        }
//        profileView.setOnClickListener {
//            val intent = Intent(context, ProfileActivity::class.java)
//            intent.putExtra("USERNAME", LOGIN)
//            startActivity(intent)
//        }
    }



    private fun checkAutorization(){
        val token: String? = profileDataViewModel.getToken()
        val login: String? = profileDataViewModel.getLogin()
        if (token != "") {
            TOKEN = token!!
            LOGIN = login!!
            checkToken(token)
        }else {
            loadLoginActivity()
        }
    }

    private fun checkToken(token: String?){
        val url = URLData.MAIN_URL + URLData.CHECK_TOKEN
        val body = mutableMapOf<String, String>()
        body["token"] = token.toString()
        val requestBody = Gson().toJson(body).toRequestBody(JSON)
        val request = Request.Builder().url(url).post(requestBody).build()
        clientOkHttp.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200) {
                    getImageProfile()
                }else{
                    loadLoginActivity()

                }
            }
        })
    }


    override fun finish() {
        super.finish()
        Log.d("FINISH_MAIN", "TRUE")
    }

    fun loadLoginActivity(){
        val intent = Intent(context, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.allNetworks.isNotEmpty()

    }

    fun getImageProfile(){
        val url = URLData.MAIN_URL + URLData.PROFILE_IMAGE + LOGIN
        val request = Request.Builder().url(url).header("Authorization", "Token $TOKEN").build()
        clientOkHttp.newCall(request).enqueue(object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Error", e.toString())
            }
            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200) {
                    val path_media = response.body?.string()
                    val json = GsonBuilder().create().fromJson(path_media, AuthorPhoto::class.java)
                    val job: Job = GlobalScope.launch(Dispatchers.Main) {
                        if (json.image != "") {
                            GlideApp.with(context).load(json.image)
                                .into(profileView)
                        } else {
                            GlideApp.with(context).load(R.mipmap.default_profile_image_round)
                                .into(profileView)
                        }
                    }
                    job.start()
                }

            }

        })
    }

    override fun onDestroy() {
        super.onDestroy()
        musicDataViewModel = MusicDataViewModel(context, "MainActivity")
        musicDataViewModel.updateIsPlaying(false)
        Log.d("UpdateIsPlaying", "TRUE")
    }
}
