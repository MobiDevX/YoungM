package com.example.youngm.Activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import com.example.youngm.Adapter.ProfileFeedPagerAdapter
import com.example.youngm.App.GlideApp
import com.example.youngm.Data.AuthorPhoto
import com.example.youngm.Data.Information
import com.example.youngm.Data.SubscribeInformation
import com.example.youngm.Data.URLData
import com.example.youngm.Interface.ProfileDataViewModel
import com.example.youngm.Interface.UpdateData
import com.example.youngm.R
import com.google.gson.Gson
import com.google.gson.GsonBuilder

import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.fragment_profile_description.*
import kotlinx.android.synthetic.main.fragment_profile_feed.*
import kotlinx.android.synthetic.main.fragment_profile_top_bar.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException
import okhttp3.RequestBody.Companion.toRequestBody
import java.lang.Exception

class ProfileActivity : AppCompatActivity() {


    private val clientOkHttp = OkHttpClient()
    private lateinit var TOKEN: String
    private lateinit var LOGIN: String
    private lateinit var USERNAME: String
    private var openLikedPost: Boolean = true
    private lateinit var context: Context
    private var imageURL = ""
    private var broadcastReceiver: BroadcastReceiver? = null
    private lateinit var profileDataViewModel: ProfileDataViewModel




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        context = this
        profileDataViewModel = ProfileDataViewModel(context)
        LOGIN = profileDataViewModel.getLogin()!!
        TOKEN = profileDataViewModel.getToken()!!
        USERNAME = intent.extras?.getString("USERNAME").toString()
        val toolbar: Toolbar = topBar.profileToolBar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        btnSubscribe.setOnClickListener {
            subscribe()
        }

        loadProfileInformation()
        getImageProfile()



        val recent = getString(R.string.header_recent)
        val popular = getString(R.string.header_popular)
        val favourite = resources.getString(R.string.header_favourite)
        val profileFeedAdapter = ProfileFeedPagerAdapter(supportFragmentManager, recent, popular, favourite)
        viewPagerProfileFeed.adapter = profileFeedAdapter
        tabLayoutProfileFeed.setupWithViewPager(viewPagerProfileFeed)
        viewPagerProfileFeed.addOnPageChangeListener(object: ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                val fragment = profileFeedAdapter.getItem(position)
//                if (fragment is UpdateData) {
//                    fragment.updateData()
//                    broadcastReceiver = fragment.getBR()
//
//                }
            }
        })


        profileImage.setOnClickListener {
            if (imageURL != "") {
                val intent = Intent(this, OpenPhotoActivity::class.java)
                intent.putExtra("Image", imageURL)
                intent.putExtra("Title", USERNAME)
                startActivity(intent)
            }
//            if (LOGIN == USERNAME) {
//                val intent = Intent(this, CreatePostActivity::class.java)
//                intent.putExtra("TOKEN", TOKEN)
//                intent.putExtra("USERNAME", USERNAME)
//                startActivity(intent)
//            }
        }



    }

    override fun finish() {
        super.finish()
        try {
            unregisterReceiver(broadcastReceiver)
            Log.d("Unregister", "TRUE")
        }catch (e: Exception){
            e.printStackTrace()
        }
    }



    fun getToken(): String{
        return TOKEN
    }

    fun getLogin(): String{
        return LOGIN
    }

    fun getUsername():String{
        return USERNAME
    }

    fun getOpenLikedPost(): Boolean{
        return openLikedPost
    }

    private fun subscribe(){
        val url = URLData.MAIN_URL + URLData.SUBSCRIBE
        val body = mutableMapOf<String, String>()
        body["author"] = USERNAME
        val requestBody = Gson().toJson(body).toRequestBody(MainActivity.JSON)
        val request = Request.Builder().url(url).header("Authorization", "Token $TOKEN").post(requestBody).build()
        clientOkHttp.newCall(request).enqueue(object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Error", e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val result: String = response.body!!.string()
                val gson = GsonBuilder().create()
                val information = gson.fromJson(result, SubscribeInformation::class.java)
                val job: Job = GlobalScope.launch(Dispatchers.Main){
                    if (information.subscribe == "Subscription removed"){
                        countFollowers.text = (countFollowers.text.toString().toInt() - 1).toString()
                        btnSubscribe.text = getString(R.string.btn_subscribe)
                    }else{
                        countFollowers.text = (countFollowers.text.toString().toInt() + 1).toString()
                        btnSubscribe.text = getString(R.string.btn_unsubscirbe)
                    }
                }
                job.start()
            }

        })
    }


    private fun logout() {
        val url = URLData.MAIN_URL + URLData.LOGOUT
        val request = Request.Builder().url(url).header("Authorization", "Token $TOKEN").delete().build()
        clientOkHttp.newCall(request).enqueue(object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Error", e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                profileDataViewModel.updateProfileData("", "")
                val intent = Intent(context, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        })
    }


    private fun loadProfileInformation() {
        val url = URLData.MAIN_URL + URLData.PROFILE_INFORMANTION + "${USERNAME}/"
        val request = Request.Builder().url(url).header("Authorization", "Token $TOKEN").build()

        clientOkHttp.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Error", e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200) {
                    val allInformation = response.body!!.string()

                    val gson = GsonBuilder().create()
                    val information = gson.fromJson(allInformation, Information::class.java)


                    var userCategory: String = ""

                    val refreshJob: Job = GlobalScope.launch(Dispatchers.Main) {

                        if (information.favorite_categories.isNotEmpty()) {
                            for (index in information.favorite_categories.indices) {
                                userCategory += information.favorite_categories[index].category
                                if (index != information.favorite_categories.size - 1) {
                                    userCategory += ", "
                                }
                            }
                        } else {
                            userCategory = "-"
                        }

                        if (information.photo != "") {
                            GlideApp.with(context).load(information.photo)
                                .into(profileImage)
                            imageURL = information.photo
                        } else {
                            GlideApp.with(context).load(R.mipmap.default_profile_image_round)
                                .into(profileImage)
                            imageURL = ""
                        }
                        countPosts.text = information.count_posts.toString()
                        countFollowing.text = information.following.toString()
                        countFollowers.text = information.followers.toString()
                        nameInformationProfile.text = information.name
                        locationInformationProfile.text = information.location
                        aboutMeInformationProfile.text = information.about_me
                        profileUsername.text = information.username
                        categoryInformationProfile.text = userCategory
                        openLikedPost = information.open_liked_post
                        if (information.image_bar == 1) {
                            profileToolBar.background =
                                resources.getDrawable(R.drawable.profile_bar2, null)
                        } else if (information.image_bar == 2) {
                            profileToolBar.background =
                                resources.getDrawable(R.drawable.profile_bar3, null)
                        } else if (information.image_bar == 3) {
                            profileToolBar.background =
                                resources.getDrawable(R.drawable.profile_bar4, null)
                        }
                        if (profileUsername.text == LOGIN) {
                            btnSubscribe.text = getString(R.string.btn_edit)
                            btnSubscribe.setOnClickListener {
                                val intent = Intent(context, EditProfileActivity::class.java)
                                intent.putExtra("TOKEN", TOKEN)
                                intent.putExtra("USERNAME", USERNAME)
                                startActivity(intent)
                            }
                            btnSendMessage.text = getString(R.string.btn_logout)
                            btnSendMessage.setOnClickListener {
                                logout()
                            }
                        } else if (information.isFollower) {
                            btnSubscribe.text = getString(R.string.btn_unsubscirbe)
                        }
                    }
                    refreshJob.start()
                }

            }
        })
    }




    fun getImageProfile(){
        val url = URLData.MAIN_URL + URLData.PROFILE_IMAGE + USERNAME
        val request = Request.Builder().url(url).header("Authorization", "Token $TOKEN").build()
        clientOkHttp.newCall(request).enqueue(object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Error", e.toString())
            }
            override fun onResponse(call: Call, response: Response) {
                val path_media = response.body?.string()
                val json = GsonBuilder().create().fromJson(path_media, AuthorPhoto::class.java)
                val job: Job = GlobalScope.launch(Dispatchers.Main) {
                    if (json.image != "") {
                        GlideApp.with(context).load(json.image).into(profileImage)
                        imageURL = json.image
                    } else {
                        GlideApp.with(context).load(R.mipmap.default_profile_image_round)
                            .into(profileImage)
                        imageURL = ""
                    }
                }
                job.start()

            }

        })
    }

}

