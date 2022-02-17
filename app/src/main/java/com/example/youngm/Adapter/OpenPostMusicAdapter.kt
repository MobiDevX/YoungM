package com.example.youngm.Adapter

import android.content.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.youngm.Activity.MainActivity
import com.example.youngm.Activity.OpenPostActivity
import com.example.youngm.Data.MusicData
import com.example.youngm.Data.Track
import com.example.youngm.Data.URLData
import com.example.youngm.Interface.MusicDataViewModel
import com.example.youngm.Interface.Playable
import com.example.youngm.Service.PlayService
import com.example.youngm.R
import com.example.youngm.Service.CreateNotification
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_open_post_description.view.*
import kotlinx.android.synthetic.main.open_post_music_item.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.lang.Exception

class OpenPostMusicAdapter(private val context: Context, private val postID: Int, private val thisPostPosition: Int, private val postPhotoURL: String,
                           private val TOKEN: String,
                           private val LOGIN: String, private var isLiked: Boolean,
                           private val openPostLikeLayout: LinearLayout?): RecyclerView.Adapter<OpenPostMusicAdapter.OpenPostMusicViewHolder>(), Playable{

    private var musicDataViewModel: MusicDataViewModel = MusicDataViewModel(context, "OpenPostMusicAdapter")
    private var musicList = ArrayList<MusicData>()
    private val clientOkHttp = OkHttpClient()
    private var imageArray: ArrayList<ImageView> = ArrayList()
    private var musicPosition = musicDataViewModel.getMusicPosition()
    private var mediaPostID = musicDataViewModel.getPostID()
    private var isPlaying = musicDataViewModel.getIsPlaying()
    private var imagePlay: ImageView? = null

    val broadcastReceiver = object: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.extras?.getString("name")!!
            val postID = intent.extras?.getInt("POST_ID")!!
            musicPosition = intent.extras?.getInt("MUSIC_POSITION")!!

            when(action){
                CreateNotification.ACTION_PLAY -> {
                    if (isPlaying){
                        onTrackPause()
                    }else{
                        onTrackPlay()
                    }
                }
                CreateNotification.ACTION_PREV -> {
                    imagePlay!!.setImageResource(R.drawable.white_play)
                    imagePlay = imageArray[musicPosition-1]
                    onTrackPrev()
                }
                CreateNotification.ACTION_NEXT -> {
                    if (musicPosition+1 != imageArray.size) {
                        try {
                            imagePlay!!.setImageResource(R.drawable.white_play)
                            imagePlay = imageArray[musicPosition + 1]
                        }catch (e: Exception){
                            Log.e("ERROR", "image null")
                        }
                    }
                    onTrackNext()
                }
                CreateNotification.ACTION_OPEN_POST -> openPost(musicPosition)
                CreateNotification.ACTION_LIKE -> likePost()
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OpenPostMusicViewHolder
    = OpenPostMusicViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.open_post_music_item, parent, false))

    override fun getItemCount(): Int = musicList.size

    override fun onBindViewHolder(holder: OpenPostMusicViewHolder, position: Int) {
        holder.bind(musicList[position])
        imageArray.add(holder.musicListLayout.openPostPlayer)
        if (position == musicPosition && postID == mediaPostID){
            imagePlay = holder.musicListLayout.openPostPlayer
            val playIcon = if (isPlaying) {
                imagePlay!!.setImageResource(R.drawable.white_pause)
                R.drawable.pause_mini
            }else{
                imagePlay!!.setImageResource(R.drawable.white_play)
                R.drawable.play_mini
            }
            val musicName = musicList[musicPosition].musicName
            val author = musicList[musicPosition].author
            val photo = postPhotoURL
            CreateNotification(context, Track(musicName, author, photo), playIcon, musicPosition, itemCount, isLiked)
            context.registerReceiver(broadcastReceiver, IntentFilter("MUSIC_ACTION"))
        }
        holder.musicListLayout.openPostPlayer.setOnClickListener {
            if (mediaPostID != postID || (musicPosition != position && imagePlay != null)){
                if (imagePlay != null) {
                    imagePlay!!.setImageResource(R.drawable.white_play)
                }
                mediaPostID = postID
                imagePlay = imageArray[position]
                musicPosition = position
                onTrackPlay()
            }else{
                imagePlay = imageArray[position]
                musicPosition = position
                if (isPlaying){
                    onTrackPause()
                }else{
                    onTrackPlay()
                }
            }
        }

    }




    class OpenPostMusicViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        lateinit var musicListLayout: LinearLayout
        fun bind(data: MusicData) = with(itemView){
            openPostMusicName.text = data.musicName
            openPostMusicAuthor.text = data.author
            openPostMusicTime.text = data.musicTime
            musicListLayout = musicListLinearLayout
        }

    }


    fun refreshData(musicList: ArrayList<MusicData>){
        this.musicList = musicList
        notifyDataSetChanged()
    }

    override fun onTrackPrev() {
        context.registerReceiver(broadcastReceiver, IntentFilter("MUSIC_ACTION"))
        musicPosition -= 1
        val musicName = musicList[musicPosition].musicName
        val author = musicList[musicPosition].author
        val photo = postPhotoURL
        imagePlay!!.setImageResource(R.drawable.white_pause)
        isPlaying = true
        updateMusicData(author, musicName, photo, isLiked, itemCount, isPlaying)
        val intent = Intent(context, PlayService::class.java)
        intent.putExtra("MUSIC", musicList[musicPosition].urlMusic)
        intent.putExtra("COMMAND", PlayService.ACTION_PREVIOUS)
        intent.putExtra("MUSIC_POSITION", musicPosition)
        intent.putExtra("MUSIC_COUNT", itemCount)
        intent.putExtra("author", author)
        intent.putExtra("musicName", musicName)
        intent.putExtra("photo", photo)
        intent.putExtra("isLiked", isLiked)
        intent.putExtra("size", itemCount)
        context.startService(intent)
    }

    override fun onTrackPlay() {
        context.registerReceiver(broadcastReceiver, IntentFilter("MUSIC_ACTION"))
        val musicName = musicList[musicPosition].musicName
        val author = musicList[musicPosition].author
        val photo = postPhotoURL
        imagePlay!!.setImageResource(R.drawable.white_pause)
        isPlaying = true
        updateMusicData(author, musicName, photo, isLiked, itemCount, isPlaying)
        val intent = Intent(context, PlayService::class.java)
        intent.putExtra("MUSIC", musicList[musicPosition].urlMusic)
        intent.putExtra("COMMAND", PlayService.ACTION_START)
        intent.putExtra("MUSIC_POSITION", musicPosition)
        intent.putExtra("MUSIC_COUNT", itemCount)
        intent.putExtra("author", author)
        intent.putExtra("musicName", musicName)
        intent.putExtra("photo", photo)
        intent.putExtra("isLiked", isLiked)
        intent.putExtra("size", itemCount)
        context.startService(intent)
    }

    override fun onTrackPause() {
        context.registerReceiver(broadcastReceiver, IntentFilter("MUSIC_ACTION"))
        val musicName = musicList[musicPosition].musicName
        val author = musicList[musicPosition].author
        val photo = postPhotoURL
//        CreateNotification(context, Track(musicName, author, photo), R.drawable.play_mini, postPosition, musicPosition, itemCount, isLiked)
        imagePlay!!.setImageResource(R.drawable.white_play)
        isPlaying = false
        updateMusicData(author, musicName, photo, isLiked, itemCount, isPlaying)
        val intent = Intent(context, PlayService::class.java)
        intent.putExtra("MUSIC", musicList[musicPosition].urlMusic)
        intent.putExtra("COMMAND", PlayService.ACTION_PAUSE)
        intent.putExtra("MUSIC_POSITION", musicPosition)
        intent.putExtra("MUSIC_COUNT", itemCount)
        intent.putExtra("author", author)
        intent.putExtra("musicName", musicName)
        intent.putExtra("photo", photo)
        intent.putExtra("isLiked", isLiked)
        intent.putExtra("size", itemCount)
        context.startService(intent)
    }

    override fun onTrackNext() {
        context.registerReceiver(broadcastReceiver, IntentFilter("MUSIC_ACTION"))
        musicPosition += 1
        if (musicPosition < itemCount) {
            val musicName = musicList[musicPosition].musicName
            val author = musicList[musicPosition].author
            val photo = postPhotoURL
            imagePlay!!.setImageResource(R.drawable.white_pause)
            isPlaying = true
            updateMusicData(author, musicName, photo, isLiked, itemCount, isPlaying)
            val intent = Intent(context, PlayService::class.java)
            intent.putExtra("MUSIC", musicList[musicPosition].urlMusic)
            intent.putExtra("COMMAND", PlayService.ACTION_NEXT)
            intent.putExtra("MUSIC_POSITION", musicPosition)
            intent.putExtra("MUSIC_COUNT", itemCount)
            intent.putExtra("author", author)
            intent.putExtra("musicName", musicName)
            intent.putExtra("photo", photo)
            intent.putExtra("isLiked", isLiked)
            intent.putExtra("size", itemCount)
            context.startService(intent)
        }
    }


    private fun openPost(position: Int){
        if (postID != -1) {
            val intent = Intent(context, OpenPostActivity::class.java)
            val id = postID
            intent.putExtra("ID", id)
            intent.putExtra("image", postPhotoURL)
            intent.putExtra("TOKEN", TOKEN)
            intent.putExtra("musicPosition", musicPosition)
            intent.putExtra("LOGIN", LOGIN)
            intent.putExtra("USERNAME", musicList[position].author)
            intent.putExtra("isLiked", isLiked)
            context.startActivity(intent)
        }
    }

    fun likePost(){
        if (postID != -1){
            val job: Job = GlobalScope.launch(Dispatchers.Main) {
                isLiked = !isLiked
                if (isLiked){
                    openPostLikeLayout!!.openPostImageLike.setImageResource(R.drawable.active_like)
                    openPostLikeLayout.openPostLikeCount.text = (openPostLikeLayout.openPostLikeCount.text.toString().toInt() + 1).toString()
                }else{
                    openPostLikeLayout!!.openPostImageLike.setImageResource(R.drawable.white_like)
                    openPostLikeLayout.openPostLikeCount.text = (openPostLikeLayout.openPostLikeCount.text.toString().toInt() - 1).toString()
                }
                updateLikeData()
                context.registerReceiver(broadcastReceiver, IntentFilter("MUSIC_ACTION"))
                val musicName = musicList[musicPosition].musicName
                val author =  musicList[musicPosition].author
                val photo = postPhotoURL
                val intent = Intent(context, PlayService::class.java)
                intent.putExtra("MUSIC", musicList[musicPosition].urlMusic)
                intent.putExtra("COMMAND", PlayService.ACTION_LIKE)
                intent.putExtra("MUSIC_POSITION", musicPosition)
                intent.putExtra("MUSIC_COUNT", itemCount)
                intent.putExtra("author", author)
                intent.putExtra("musicName", musicName)
                intent.putExtra("photo", photo)
                intent.putExtra("isLiked", isLiked)
                intent.putExtra("size", itemCount)
                context.startService(intent)

            }
            job.start()
        }
    }

    private fun updateMusicData(author: String, musicName: String, photo: String, isLiked: Boolean, size: Int, isPlaying: Boolean) {
        musicDataViewModel.updateMusicData(postID, musicPosition, isPlaying)
    }


    private fun updateLikeData(){
        val url = URLData.MAIN_URL + URLData.LIKE_POST
        val body = mutableMapOf<String, Int>()
        body["post_id"] = mediaPostID
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
}