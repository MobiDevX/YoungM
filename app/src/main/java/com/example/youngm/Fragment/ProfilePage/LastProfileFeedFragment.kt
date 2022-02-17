package com.example.youngm.Fragment.ProfilePage

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.youngm.Activity.MainActivity
import com.example.youngm.Activity.OpenPostActivity
import com.example.youngm.Activity.ProfileActivity
import com.example.youngm.Adapter.PostsAdapter
import com.example.youngm.App.ArchLifecycleApp
import com.example.youngm.Data.MediaSerializable
import com.example.youngm.Data.PostData
import com.example.youngm.Data.PostsFeed
import com.example.youngm.Data.URLData
import com.example.youngm.Interface.MusicDataViewModel
import com.example.youngm.Interface.OnItemClickListener
import com.example.youngm.Interface.UpdateData

import com.example.youngm.R
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.fragment_last_profile_feed.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException


class LastProfileFeedFragment : Fragment(), OnItemClickListener, UpdateData {


    lateinit var adapter: PostsAdapter
    private val clientOkHttp = OkHttpClient()
    private var postsList: ArrayList<PostData> = ArrayList()
    private lateinit var TOKEN: String
    private lateinit var LOGIN: String
    private lateinit var USERNAME: String
    private val REQUEST_ACCESS_TYPE = 15
    private lateinit var musicDataViewModel: MusicDataViewModel

    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_last_profile_feed, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val profileActivity = activity as ProfileActivity
        TOKEN = profileActivity.getToken()
        LOGIN = profileActivity.getLogin()
        USERNAME = profileActivity.getUsername()
        adapter = PostsAdapter(context!!, TOKEN, LOGIN)
        adapter.setOnItemClickListener(this)
        recyclerLastProfileFeed.adapter = adapter
        recyclerLastProfileFeed.layoutManager = LinearLayoutManager(context)
        musicDataViewModel = MusicDataViewModel(context, "LastFeedProfile")
        loadData()
    }



    override fun onResume() {
        super.onResume()
        activity!!.registerReceiver(adapter.broadcastReceiver, IntentFilter("MUSIC_ACTION"))
        if (postsList.size > 0) {
            val playingPostID: Int = musicDataViewModel.getPostID()
            val isPlayingPost: Boolean = musicDataViewModel.getIsPlaying()
            val musicPositionPlaying: Int = musicDataViewModel.getMusicPosition()
            val position = adapter.getPosition(playingPostID)
            if (position != -1) {
                adapter.updatePlay(position, isPlayingPost, musicPositionPlaying)
            }
        }
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

    private fun loadData(){
        progressBar.visibility = View.VISIBLE
        val url = URLData.MAIN_URL + URLData.GET_LAST_POST_AUTHOR
        val body = mutableMapOf<String, String>()
        body["count"] = postsList.size.toString()
        body["author"] = USERNAME
        val requestBody = gson.toJson(body).toRequestBody(MainActivity.JSON)
        val request = Request.Builder().url(url).post(requestBody).header("Authorization",
            "Token $TOKEN"
        ).build()
        val job: Job = GlobalScope.launch(Dispatchers.IO) {

            clientOkHttp.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("Error", e.toString())
                }

                override fun onResponse(call: Call, response: Response) {



                    val playingPostID: Int = musicDataViewModel.getPostID()
                    val isPlayingPost: Boolean = musicDataViewModel.getIsPlaying()
                    val musicPositionPlaying: Int = musicDataViewModel.getMusicPosition()
                    var postSelected: Boolean = false
                    var isPlaying = false
                    val allPosts = response.body?.string()

                    val refresJob: Job = GlobalScope.launch(Dispatchers.Main) {
                        if (allPosts!!.length > 2) {
                            val gson = GsonBuilder().create()

                            val postsFeed = gson.fromJson(allPosts, PostsFeed::class.java)

                            for (i in postsFeed.posts) {
                                val id = i.id
                                val author = i.author
                                val likesCount = i.likes
                                val commentCount = i.comment
                                val postPhoto = i.photo
                                val authorPhoto = i.photo_author
                                val category = i.category
                                val isLiked = i.isLiked
                                val music = ArrayList<String>()
                                val musicName = ArrayList<String>()
                                if (id == playingPostID){
                                    isPlaying = isPlayingPost
                                    postSelected = true
                                }else{
                                    isPlaying = false
                                    postSelected = false
                                }
                                for (mMusic in i.music){
                                    music.add(mMusic.music)
                                    musicName.add(mMusic.music_name)
                                }


                                val post = PostData(
                                    id,
                                    category,
                                    author,
                                    authorPhoto,
                                    postPhoto,
                                    likesCount,
                                    commentCount,
                                    isLiked,
                                    music,
                                    musicName,
                                    isPlaying,
                                    postSelected,
                                    musicPositionPlaying
                                )
                                if (postsList.indexOf(post) == -1) {
                                    postsList.add(post)
                                }

                            }
                        }



                        if (postsList.size > 0) {
                            adapter.refreshData(postsList)
                        } else {
                            textViewEmptyLastPosts.visibility = View.VISIBLE
                        }
                        progressBar.visibility = View.INVISIBLE
                    }
                    refresJob.start()
                }
            })
        }
        job.start()
    }

    override fun onItemClicked(view: View?, pos: Int, musicPosition: Int) {
        val intent = Intent(context, OpenPostActivity::class.java)
        val id = postsList[pos].id
        intent.putExtra("ID", id)
        intent.putExtra("image", postsList[pos].postPhoto)
        intent.putExtra("TOKEN", TOKEN)
        val mediaSerializable = MediaSerializable(postsList[pos].isPlaying, pos)
        intent.putExtra("media", mediaSerializable)
        intent.putExtra("musicPosition", musicPosition)
        intent.putExtra("LOGIN", LOGIN)
        intent.putExtra("USERNAME", postsList[pos].postUsername)
        intent.putExtra("isLiked", postsList[pos].isLiked)
        startActivityForResult(intent, REQUEST_ACCESS_TYPE)
    }

    override fun updateData() {
        postsList = ArrayList()
        loadData()
    }

    override fun getBR(): BroadcastReceiver {
        return adapter.broadcastReceiver
    }
}
