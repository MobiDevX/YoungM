package com.example.youngm.Fragment.OpenPost

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.youngm.Activity.MainActivity
import com.example.youngm.Activity.OpenPostActivity
import com.example.youngm.Activity.ProfileActivity
import com.example.youngm.Adapter.OpenPostMusicAdapter
import com.example.youngm.App.ArchLifecycleApp
import com.example.youngm.Data.MusicData
import com.example.youngm.Data.PostInformation
import com.example.youngm.Data.URLData
import com.example.youngm.R
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.fragment_open_post_description.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class OpenPostDescriptionFragment: Fragment() {
    private var ID: Int? = -1
    private val clientOkHttp = OkHttpClient()
    private var isEllipsize = false
    private var musicList = ArrayList<MusicData>()
    private lateinit var TOKEN: String
    private lateinit var LOGIN: String
    private var isPlay: Boolean = false
    private var isLiked: Boolean = false
    private lateinit var postInformation: PostInformation
    private var postPosition: Int = -1
    private lateinit var adapter: OpenPostMusicAdapter
    private val PLAY_FLAG = 1
    private lateinit var imageURL: String
    private var musicPosition = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_open_post_description, container, false)
    }



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val openPostActivity = activity as OpenPostActivity
        ID = openPostActivity.get_id()
        TOKEN = openPostActivity.getToken()
        LOGIN = openPostActivity.getLogin()
        isPlay = openPostActivity.getIsPlay()
        imageURL = openPostActivity.getImageURL()
        postPosition = openPostActivity.getMediaPostID()
        isLiked = openPostActivity.getIsLiked()
        musicPosition = openPostActivity.getMusicPostition()
        adapter = OpenPostMusicAdapter(context!!, ID!!, postPosition, imageURL, TOKEN, LOGIN, isLiked, postOpenLikeLayout)
        recyclerOpenPostMusic.adapter = adapter
        recyclerOpenPostMusic.layoutManager = LinearLayoutManager(context)
        activity!!.registerReceiver(adapter.broadcastReceiver, IntentFilter("MUSIC_ACTION"))
        loadData()
        postOpenLikeLayout.setOnClickListener {
            val job: Job = GlobalScope.launch(Dispatchers.Main) {
                postInformation.isLiked = !postInformation.isLiked
                if (postInformation.isLiked){
                    openPostImageLike.setImageResource(R.drawable.active_like)
                    openPostLikeCount.text = (openPostLikeCount.text.toString().toInt() + 1).toString()
                }else{
                    openPostImageLike.setImageResource(R.drawable.white_like)
                    openPostLikeCount.text = (openPostLikeCount.text.toString().toInt() - 1).toString()
                }
                updateLikeData()
            }
            job.start()
        }




        openPostUserProfileLayout.setOnClickListener {
            val intent: Intent = Intent(context, ProfileActivity::class.java)
            intent.putExtra("LOGIN", LOGIN)
            intent.putExtra("USERNAME", openPostUsernameAuthor.text.toString())
            intent.putExtra("TOKEN", TOKEN)
            context!!.startActivity(intent)
        }
    }

    private fun updateLikeData(){
        val url = URLData.MAIN_URL + URLData.LIKE_POST
        val body = mutableMapOf<String, Int>()
        body["post_id"] = postInformation.id
        val requestBody = Gson().toJson(body).toRequestBody(MainActivity.JSON)
        val request = Request.Builder().url(url).header("Authorization", "Token $TOKEN").post(requestBody).build()
        clientOkHttp.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("Fail", "True")
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("Success", "True")
            }

        })
    }


    override fun onPause() {
        super.onPause()
        try {
            val app = context!!.applicationContext as ArchLifecycleApp
            app.updateBroadcastReceiver(adapter.broadcastReceiver)
            activity!!.unregisterReceiver(adapter.broadcastReceiver)
        }catch (e:Exception){
            Log.d("ERROR", "RECEIVER")
        }
    }

    fun getReceiver(): BroadcastReceiver{
        return adapter.broadcastReceiver
    }

    fun getIsLiked(): Boolean{
        return postInformation.isLiked
    }


    private fun loadData(){
        progressBar.visibility = View.VISIBLE
        openPostDescriptionLayout.visibility = View.INVISIBLE
        val url = URLData.MAIN_URL + URLData.GET_DATA_OPEN_POST + ID
        val request = Request.Builder().url(url).addHeader("Authorization", "Token $TOKEN").build()
        clientOkHttp.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200) {
                    val job: Job = GlobalScope.launch(Dispatchers.Main) {
                        val body = response.body?.string()
                        val gson = GsonBuilder().create()
                        val postDescription = gson.fromJson(body, PostInformation::class.java)
                        postInformation = postDescription
                        if (postDescription.photo_author != "") {
                            Glide.with(context!!)
                                .load(postDescription.photo_author)
                                .into(openPostProfileImage)
                        } else {
                            Glide.with(context!!).load(R.mipmap.default_profile_image_round)
                                .into(openPostProfileImage)
                        }
                        openPostUsernameAuthor.text = postDescription.author
                        openPostLikeCount.text = postDescription.likes.toString()
                        val description = postDescription.description
                        if (description != "") {
                            val textViewDescription = TextView(context,null, 0, R.style.SecondaryTextTheme)
                            textViewDescription.text =
                                "${getString(R.string.hint_description)}: $description"
                            textViewDescription.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                            textViewDescription.maxLines = 2
                            textViewDescription.ellipsize = TextUtils.TruncateAt.END
                            textViewDescription.setOnClickListener {
                                if (isEllipsize) {
                                    textViewDescription.maxLines = 2
                                    isEllipsize = false
                                } else {
                                    textViewDescription.maxLines = description.length
                                    isEllipsize = true
                                }
                            }
                            linearLayoutDescriptionOpenPost.addView(textViewDescription)
                        }
                        val genre = postDescription.category
                        val textViewGenre = TextView(context, null, 0, R.style.SecondaryTextTheme)
                        textViewGenre.text = "${getString(R.string.header_gengre)}: $genre"
                        textViewGenre.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                        textViewGenre.context.setTheme(R.style.SecondaryTextTheme)
                        linearLayoutDescriptionOpenPost.addView(textViewGenre)
                        if (postDescription.isLiked) {
                            openPostImageLike.setImageResource(R.drawable.active_like)
                        } else {
                            openPostImageLike.setImageResource(R.drawable.white_like)
                        }

                        for (music in postDescription.music) {
                            musicList.add(
                                MusicData(
                                    postDescription.id,
                                    postDescription.author,
                                    music.music_name,
                                    music.music,
                                    music.music_time
                                )
                            )
                        }
                        adapter.refreshData(musicList)
                        progressBar.visibility = View.INVISIBLE
                        openPostDescriptionLayout.visibility = View.VISIBLE
                    }
                    job.start()


                }
            }

        })
    }

}