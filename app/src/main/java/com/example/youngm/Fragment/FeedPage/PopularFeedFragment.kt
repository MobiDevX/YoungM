package com.example.youngm.Fragment.FeedPage

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.youngm.Fragment.Navigation.FeedFragment
import com.example.youngm.Activity.MainActivity
import com.example.youngm.Activity.OpenPostActivity
import com.example.youngm.Interface.UpdateData
import com.example.youngm.Adapter.PostsAdapter
import com.example.youngm.App.ArchLifecycleApp
import com.example.youngm.Data.MediaSerializable
import com.example.youngm.Data.PostData
import com.example.youngm.Data.PostsFeed
import com.example.youngm.Data.URLData
import com.example.youngm.Interface.MusicDataViewModel
import com.example.youngm.Interface.OnItemClickListener
import com.example.youngm.Interface.ProfileDataViewModel

import com.example.youngm.R
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.fragment_popular_feed.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException


class PopularFeedFragment : Fragment(), UpdateData, OnItemClickListener {


    private val clientOkHttp = OkHttpClient()
    private var postsList: ArrayList<PostData> = ArrayList()
    private val gson = Gson()
    lateinit var adapter: PostsAdapter
    private lateinit var TOKEN: String
    private lateinit var LOGIN: String
    private val REQUEST_ACCESS_TYPE = 15
    private lateinit var musicDataViewModel: MusicDataViewModel
    private lateinit var profileDataViewModel: ProfileDataViewModel
    private lateinit var progressBar: ProgressBar


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_popular_feed, container, false)
    }



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        profileDataViewModel = ProfileDataViewModel(context)
        TOKEN = profileDataViewModel.getToken()!!
        LOGIN = profileDataViewModel.getLogin()!!
        adapter = PostsAdapter(context!!, TOKEN, LOGIN)
        adapter.setOnItemClickListener(this)
        recyclerPopularFeed.adapter = adapter
        recyclerPopularFeed.layoutManager = LinearLayoutManager(context)
        musicDataViewModel = MusicDataViewModel(context, "PopularFeedFragment")
        progressBar = progressBarPopular
        updateData()

        if (swipeRefreshPopular != null) {
            swipeRefreshPopular.setOnRefreshListener {
                swipeRefreshPopular.isRefreshing = false
                updateData()
            }
        }
    }





    override fun onResume() {
        super.onResume()
        activity!!.registerReceiver(adapter.broadcastReceiver, IntentFilter("MUSIC_ACTION"))
        if (postsList.size > 0) {
//            updateData()
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

    fun loadPosts(){
        progressBar.visibility = View.VISIBLE
        recyclerPopularFeed.visibility = View.INVISIBLE
        val url = URLData.MAIN_URL + URLData.GET_POPULAR_POSTS
        val body = mutableMapOf<String, Int>()
        body["count"] = postsList.size
        val requestBody = gson.toJson(body).toRequestBody(MainActivity.JSON)
        val request = Request.Builder().url(url).header("Authorization", "Token $TOKEN").post(requestBody).build()
        val job: Job = GlobalScope.launch(Dispatchers.IO) {

            clientOkHttp.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("ERROR", e.toString())
                }

                override fun onResponse(call: Call, response: Response) {

                    if (response.code == 200) {


                        val playingPostID: Int = musicDataViewModel.getPostID()
                        val isPlayingPost: Boolean = musicDataViewModel.getIsPlaying()
                        val musicPositionPlaying: Int = musicDataViewModel.getMusicPosition()
                        var postSelected: Boolean = false
                        var isPlaying = false
                        val allPosts = response.body?.string()


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
                        val refreshJob: Job = GlobalScope.launch(Dispatchers.Main) {
                            adapter.refreshData(postsList)
                            progressBar.visibility = View.INVISIBLE
                            recyclerPopularFeed.visibility = View.VISIBLE
                        }
                        refreshJob.start()
                    }


                }


            })
        }
        job.start()
    }

    override fun updateData() {
        postsList = ArrayList()
        loadPosts()
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

    override fun getBR(): BroadcastReceiver {
        return adapter.broadcastReceiver
    }

}
