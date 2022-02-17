package com.example.youngm.Adapter

import android.content.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.youngm.Activity.MainActivity
import com.example.youngm.Activity.OpenPostActivity
import com.example.youngm.Activity.ProfileActivity
import com.example.youngm.App.GlideApp
import com.example.youngm.Data.MediaSerializable
import com.example.youngm.Data.PostData
import com.example.youngm.Data.URLData
import com.example.youngm.Interface.MusicDataViewModel
import com.example.youngm.Interface.OnItemClickListener
import com.example.youngm.Interface.Playable
import com.example.youngm.R
import com.example.youngm.Service.CreateNotification
import com.example.youngm.Service.PlayService
import com.google.gson.Gson
import kotlinx.android.synthetic.main.post_item.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException


class PostsAdapter(val context: Context, val TOKEN: String, val LOGIN: String): RecyclerView.Adapter<PostsAdapter.PostsHolder>(), Playable {

    var postsList: ArrayList<PostData> = ArrayList()
    private val clientOkHttp = OkHttpClient()
    private var mediaPostID: Int = -1
    private var imagePlay: ImageView? = null
    private var imageLike: ImageView? = null
    private var notificationCreated: Boolean = false
    private var postPosition = -1
    private var musicPosition = -1
    private var mOnItemClickLister: OnItemClickListener? = null
    private var musicDataViewModel: MusicDataViewModel? = null



    fun setOnItemClickListener(listener: OnItemClickListener) {
        mOnItemClickLister = listener
    }

    val broadcastReceiver = object: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.extras?.getString("name")!!
            val postID = intent.extras?.getInt("POST_ID")!!
            postPosition = getPosition(postID)
            musicPosition = intent.extras?.getInt("MUSIC_POSITION")!!
            when(action){
                CreateNotification.ACTION_PLAY -> {
                    if (postsList[postPosition].isPlaying){
                        onTrackPause()
                    }else{
                        onTrackPlay()
                    }
                }
                CreateNotification.ACTION_PREV -> onTrackPrev()
                CreateNotification.ACTION_NEXT -> onTrackNext()
                CreateNotification.ACTION_OPEN_POST -> openPost(postPosition)
                CreateNotification.ACTION_LIKE -> {
                    likePost(postPosition)
                    try {
                        if (postsList[postPosition].isLiked) {
                            imageLike!!.setImageResource(R.drawable.active_like)
                        } else {
                            imageLike!!.setImageResource(R.drawable.white_like)
                        }
                    }catch (e: Exception){
                        Log.d("LIKE_POST", "NULL")
                    }

                }
            }
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostsHolder =
        PostsHolder(LayoutInflater.from(parent.context).inflate(R.layout.post_item, parent, false))

    override fun getItemCount(): Int = postsList.size

    override fun onBindViewHolder(holder: PostsHolder, position: Int) {
        holder.bind(postsList[position])
        if (postsList[position].isPlaying){
            imagePlay = holder.postCard.playerLayout.imagePlay
            imagePlay!!.setImageResource(R.drawable.white_pause)
            Log.d("NESCHETPOSTPOSITION", "$position and $postPosition")
            postPosition = position
            musicPosition = postsList[position].musicPositionPlaying
            onTrackPlay()
        }else if(postsList[position].isSelected){
            imagePlay = holder.postCard.playerLayout.imagePlay
            imagePlay!!.setImageResource(R.drawable.white_play)
            Log.d("NESCHETPOSTPOSITION", "$position and $postPosition")
            postPosition = position
            musicPosition = postsList[position].musicPositionPlaying
        }else{
            holder.postCard.playerLayout.imagePlay.setImageResource(R.drawable.white_play)
        }
        holder.postCard.likeLayout.setOnClickListener {
            val job: Job = GlobalScope.launch(Dispatchers.Main) {
                likePost(position)
                if (postsList[position].isLiked) {
                    holder.postCard.imageLike.setImageResource(R.drawable.active_like)
                } else {
                    holder.postCard.imageLike.setImageResource(R.drawable.white_like)
                }
            }
            job.start()

        }

        holder.postCard.imageComment.setOnClickListener {
            refreshData(postsList)
        }

        holder.postCard.setOnClickListener {
            mOnItemClickLister!!.onItemClicked(it, position, musicPosition)
//            openPost(position)
        }

        holder.postCard.playerLayout.setOnClickListener {
            notificationCreated = true

            if (imagePlay != null && postPosition != position) {
                imagePlay!!.setImageResource(R.drawable.white_play)
            }
            if (musicPosition == -1){
                musicPosition = 0
            }
            imagePlay = it.imagePlay
            if (postPosition != position){
                if (postPosition != -1) {
                    postsList[postPosition].isPlaying = false
                }
                postPosition = position
                musicPosition = 0
                imageLike = holder.postCard.imageLike
                onTrackPlay()
            }else{
                if (postsList[position].isPlaying){
                    onTrackPause()
                }else{
                    onTrackPlay()
                }
            }



        }

        holder.postCard.postUserPhoto.setOnClickListener {
            val intent: Intent = Intent(context, ProfileActivity::class.java)
            intent.putExtra("LOGIN", LOGIN)
            intent.putExtra("USERNAME", postsList[position].postUsername)
            intent.putExtra("TOKEN", TOKEN)
            context.startActivity(intent)
        }
    }



    fun refreshData(postsList: ArrayList<PostData>){
        this.postsList = postsList
        notifyDataSetChanged()
    }





    class PostsHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        lateinit var postCard: ConstraintLayout
        fun bind(item: PostData) = with(itemView){
            musicCategory.text = item.musicCategory
            postUsername.text = item.postUsername
            likeCount.text = item.likeCount.toString()
            commentCount.text = item.commentCount.toString()
            val job: Job = GlobalScope.launch(Dispatchers.Main) {
                GlideApp.with(context).load(item.postPhoto).into(postPhoto)
                if (item.postUserPhoto != "") {
                    GlideApp.with(context).load(item.postUserPhoto)
                        .into(postUserPhoto)

                }else{
                    GlideApp.with(context).load(R.mipmap.default_profile_image_round).into(postUserPhoto)

                }
            }
            job.start()
            if (item.isLiked){
                imageLike.setImageResource(R.drawable.active_like)
            }else{
                imageLike.setImageResource(R.drawable.white_like)
            }
            postCard = postItemCard


        }

    }



    fun updateLikeData(post: PostData){
        val url = URLData.MAIN_URL + URLData.LIKE_POST
        val body = mutableMapOf<String, Int>()
        body["post_id"] = post.id
        val requestBody = Gson().toJson(body).toRequestBody(MainActivity.JSON)
        val request = Request.Builder().url(url).header("Authorization", "Token $TOKEN").post(requestBody).build()
        clientOkHttp.newCall(request).enqueue(object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                Log.d("Fail", "True")
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("Success", "True")
            }

        })
    }

    override fun onTrackPrev() {
        musicPosition -= 1
        val musicName = postsList[postPosition].musicName[musicPosition]
        val author = postsList[postPosition].postUsername
        val photo = postsList[postPosition].postPhoto
        imagePlay!!.setImageResource(R.drawable.white_pause)
        postsList[postPosition].isPlaying = true
        updateMusicData(author, musicName, photo, postsList[postPosition].isLiked, postsList[postPosition].music.size, true)
        val intent = Intent(context, PlayService::class.java)
        intent.putExtra("MUSIC", postsList[postPosition].music[musicPosition])
        intent.putExtra("COMMAND", PlayService.ACTION_PREVIOUS)
        intent.putExtra("MUSIC_POSITION", musicPosition)
        intent.putExtra("MUSIC_COUNT", postsList[postPosition].music.size)
        intent.putExtra("author", author)
        intent.putExtra("musicName", musicName)
        intent.putExtra("photo", photo)
        intent.putExtra("isLiked", postsList[postPosition].isLiked)
        intent.putExtra("size", postsList[postPosition].music.size)
        context.startService(intent)
    }

    override fun onTrackPlay() {
//
        val musicName = postsList[postPosition].musicName[musicPosition]
        val author = postsList[postPosition].postUsername
        val photo = postsList[postPosition].postPhoto
        imagePlay!!.setImageResource(R.drawable.white_pause)
        postsList[postPosition].isPlaying = true
        updateMusicData(author, musicName, photo, postsList[postPosition].isLiked, postsList[postPosition].music.size, true)
        val intent = Intent(context, PlayService::class.java)
        intent.putExtra("MUSIC", postsList[postPosition].music[musicPosition])
        intent.putExtra("COMMAND", PlayService.ACTION_START)
        intent.putExtra("MUSIC_POSITION", musicPosition)
        intent.putExtra("MUSIC_COUNT", postsList[postPosition].music.size)
        intent.putExtra("author", author)
        intent.putExtra("musicName", musicName)
        intent.putExtra("photo", photo)
        intent.putExtra("isLiked", postsList[postPosition].isLiked)
        intent.putExtra("size", postsList[postPosition].music.size)
        context.startService(intent)

    }

    private fun updateMusicData(author: String, musicName: String, photo: String, isLiked: Boolean, size: Int, isPlaying: Boolean) {
        if (musicDataViewModel == null){
            musicDataViewModel = MusicDataViewModel(context, "PostAdapter")
        }
        musicDataViewModel!!.updateMusicData(postsList[postPosition].id, musicPosition, isPlaying)
    }

    override fun onTrackPause() {
        val musicName = postsList[postPosition].musicName[musicPosition]
        val author = postsList[postPosition].postUsername
        val photo = postsList[postPosition].postPhoto
        imagePlay!!.setImageResource(R.drawable.white_play)
        postsList[postPosition].isPlaying = false
        updateMusicData(author, musicName, photo, postsList[postPosition].isLiked, postsList[postPosition].music.size, false)
        val intent = Intent(context, PlayService::class.java)
        intent.putExtra("MUSIC", postsList[postPosition].music[musicPosition])
        intent.putExtra("COMMAND", PlayService.ACTION_PAUSE)
        intent.putExtra("MUSIC_POSITION", musicPosition)
        intent.putExtra("MUSIC_COUNT", postsList[postPosition].music.size)
        intent.putExtra("author", author)
        intent.putExtra("musicName", musicName)
        intent.putExtra("photo", photo)
        intent.putExtra("isLiked", postsList[postPosition].isLiked)
        intent.putExtra("size", postsList[postPosition].music.size)
        context.startService(intent)
    }

    override fun onTrackNext() {
        Log.d("MUSIC_NEXT", "$musicPosition, $postPosition")
        if (musicPosition+1 < postsList[postPosition].music.size) {
            musicPosition += 1
            val musicName = postsList[postPosition].musicName[musicPosition]
            val author = postsList[postPosition].postUsername
            val photo = postsList[postPosition].postPhoto
            imagePlay!!.setImageResource(R.drawable.white_pause)
            postsList[postPosition].isPlaying = true
            updateMusicData(author, musicName, photo, postsList[postPosition].isLiked, postsList[postPosition].music.size, true)
            val intent = Intent(context, PlayService::class.java)
            intent.putExtra("MUSIC", postsList[postPosition].music[musicPosition])
            intent.putExtra("COMMAND", PlayService.ACTION_NEXT)
            intent.putExtra("MUSIC_POSITION", musicPosition)
            intent.putExtra("MUSIC_COUNT", postsList[postPosition].music.size)
            intent.putExtra("author", author)
            intent.putExtra("musicName", musicName)
            intent.putExtra("photo", photo)
            intent.putExtra("isLiked", postsList[postPosition].isLiked)
            intent.putExtra("size", postsList[postPosition].music.size)
            context.startService(intent)
        }

    }

    fun openPost(position: Int){
        val intent = Intent(context, OpenPostActivity::class.java)
        val id = postsList[position].id
        intent.putExtra("ID", id)
        intent.putExtra("image", postsList[position].postPhoto)
        intent.putExtra("TOKEN", TOKEN)
        val mediaSerializable = MediaSerializable(postsList[position].isPlaying, position)
        intent.putExtra("media", mediaSerializable)
        intent.putExtra("musicPosition", musicPosition)
        intent.putExtra("LOGIN", LOGIN)
        intent.putExtra("USERNAME", postsList[position].postUsername)
        intent.putExtra("isLiked", postsList[position].isLiked)
        context.startActivity(intent)
    }

    private fun likePost(position: Int){
        postsList[position].isLiked = !postsList[position].isLiked
        if (postsList[position].isLiked) {
            postsList[position].likeCount += 1
        } else {
            postsList[position].likeCount -= 1
        }
        updateLikeData(postsList[position])
        refreshData(postsList)
        try {
            val musicName = postsList[postPosition].musicName[musicPosition]
            val author = postsList[postPosition].postUsername
            val photo = postsList[postPosition].postPhoto
            val intent = Intent(context, PlayService::class.java)
            intent.putExtra("MUSIC", postsList[postPosition].music[musicPosition])
            intent.putExtra("COMMAND", PlayService.ACTION_LIKE)
            intent.putExtra("POST_POSITION", postPosition)
            intent.putExtra("MUSIC_POSITION", musicPosition)
            intent.putExtra("MUSIC_COUNT", postsList[postPosition].music.size)
            intent.putExtra("author", author)
            intent.putExtra("musicName", musicName)
            intent.putExtra("photo", photo)
            intent.putExtra("isLiked", postsList[postPosition].isLiked)
            intent.putExtra("size", postsList[postPosition].music.size)
            context.startService(intent)
        }catch (e: Exception){
            Log.e("ERROR", "NULL NOTIFICATION")
        }

    }


    fun updatePlay(position: Int, isPlaying: Boolean, musicPosition: Int){
        if(isPlaying){
            if (position != postPosition) {
                postsList[position].isPlaying = true
                postsList[position].isSelected = true
                if (postPosition != -1){
                    postsList[postPosition].isPlaying = false
                    postsList[postPosition].isSelected = false
                }
                postPosition = position
                postsList[position].musicPositionPlaying = musicPosition
            }else{
                postPosition = position
                postsList[position].isPlaying = true
                postsList[position].isSelected = true
                postsList[position].musicPositionPlaying = musicPosition
            }
        }else{
            postsList[postPosition].isPlaying = false
            postsList[postPosition].isSelected = false
            postsList[position].isPlaying = false
            postsList[position].isSelected = true
            postPosition = position

        }
        refreshData(postsList)

    }

    fun getPosition(postID: Int): Int{
        var result = -1
        for (i in postsList){
            if(i.id == postID){
                result = postsList.indexOf(i)
            }
        }
        return result
    }


}