package com.example.youngm.Service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import com.example.youngm.Data.Track
import com.example.youngm.Interface.MusicDataViewModel
import com.example.youngm.R


class PlayService : Service() {

    companion object{
        val ACTION_START = "action_start"
        val ACTION_RESUME = "action_resume"
        val ACTION_PAUSE = "action_pause"
        val ACTION_REWIND = "action_rewind"
        val ACTION_FAST_FORWARD = "action_fast_foward"
        val ACTION_NEXT = "action_next"
        val ACTION_PREVIOUS = "action_previous"
        val ACTION_STOP = "action_stop"
        val ACTION_LIKE = "action_like"
    }


    private var musicURL: String = ""
    private var resumePostID = -1
    private var resumeMusicPosition = -1
    private lateinit var command: String
    private var postID = -1
    private var musicPosition = -1
    private var musicCount = -1
    private lateinit var media: MediaPlayer
    private var author: String? = ""
    private var musicName: String? = ""
    private var photo: String? = ""
    private var size = -1
    private var isLiked = false
    lateinit var musicDataViewModel: MusicDataViewModel

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        media = MediaPlayer()
        musicDataViewModel = MusicDataViewModel(applicationContext, "PlayService")

    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        resumePostID = postID
        resumeMusicPosition = musicPosition
        postID = musicDataViewModel.getPostID()
        musicURL = intent?.extras?.getString("MUSIC")!!
        command = intent.extras?.getString("COMMAND")!!
        Log.d("COMMAND_PLAY_SERVICE", command)
        musicPosition = intent.extras?.getInt("MUSIC_POSITION")!!
        musicCount = intent.extras?.getInt("MUSIC_COUNT")!!
        author = intent.getStringExtra("author")
        musicName = intent.getStringExtra("musicName")
        photo = intent.getStringExtra("photo")
        isLiked = intent.getBooleanExtra("isLiked", false)
        size = intent.getIntExtra("size", -1)
        when(command){
            ACTION_START -> playMusic()
            ACTION_PAUSE -> pauseMusic()
            ACTION_NEXT -> nextOrPrevMusic()
            ACTION_PREVIOUS -> nextOrPrevMusic()
            ACTION_LIKE -> likePost()
        }

        return super.onStartCommand(intent, flags, startId)
    }



    private fun playMusic(){
        if ((resumePostID != postID || resumeMusicPosition != musicPosition) && media.isPlaying){
            media.stop()
            media.release()
        }
        if (resumePostID == postID && resumeMusicPosition == musicPosition && !media.isPlaying){
            CreateNotification(applicationContext, Track(musicName!!, author!!, photo!!), R.drawable.pause_mini, musicPosition, size, isLiked)
            media.start()
        }else if ((resumePostID != postID || resumeMusicPosition != musicPosition)) {
            CreateNotification(applicationContext, Track(musicName!!, author!!, photo!!), R.drawable.pause_mini, musicPosition, size, isLiked)
            media = MediaPlayer()
            media.setDataSource(musicURL)
            media.prepareAsync()
            media.setOnPreparedListener {
                it.start()
            }
            media.setOnCompletionListener {
                it.stop()
                if (musicPosition != musicCount - 1) {
                    val intentNext =
                        Intent(applicationContext, NotificationActionService::class.java)
                            .setAction(CreateNotification.ACTION_NEXT)
                            .putExtra("MUSIC_POSITION", musicPosition)
                            .putExtra("POST_ID", postID)
                    PendingIntent.getBroadcast(
                        applicationContext,
                        0,
                        intentNext,
                        PendingIntent.FLAG_UPDATE_CURRENT
                    ).send()
                } else {
                    val intentNext =
                        Intent(applicationContext, NotificationActionService::class.java)
                            .setAction(CreateNotification.ACTION_PLAY)
                            .putExtra("MUSIC_POSITION", musicPosition)
                            .putExtra("POST_ID", postID)
                    PendingIntent.getBroadcast(
                        applicationContext,
                        0,
                        intentNext,
                        PendingIntent.FLAG_UPDATE_CURRENT
                    ).send()
                }
            }
        }
    }


    private fun pauseMusic(){
        CreateNotification(applicationContext, Track(musicName!!, author!!, photo!!), R.drawable.play_mini, musicPosition, size, isLiked)
        if (media.isPlaying) {
            media.pause()
        }else{
            postID = -1
        }
    }

    private fun nextOrPrevMusic() {
        if (musicPosition > -1 && musicPosition < musicCount) {
            media.stop()
            media.release()
            CreateNotification(applicationContext, Track(musicName!!, author!!, photo!!), R.drawable.pause_mini, musicPosition, size, isLiked)
            media = MediaPlayer()
            media.setDataSource(musicURL)
            media.prepareAsync()
            media.setOnPreparedListener {
                it.start()
            }

            media.setOnCompletionListener {
                if (musicPosition != musicCount - 1) {
                    val intentNext =
                        Intent(applicationContext, NotificationActionService::class.java)
                            .setAction(CreateNotification.ACTION_NEXT)
                            .putExtra("MUSIC_POSITION", musicPosition)
                            .putExtra("POST_ID", postID)
                    PendingIntent.getBroadcast(
                        applicationContext,
                        0,
                        intentNext,
                        PendingIntent.FLAG_UPDATE_CURRENT
                    ).send()
                } else {
                    val intentNext =
                        Intent(applicationContext, NotificationActionService::class.java)
                            .setAction(CreateNotification.ACTION_PLAY)
                            .putExtra("MUSIC_POSITION", musicPosition)
                            .putExtra("POST_ID", postID)
                    PendingIntent.getBroadcast(
                        applicationContext,
                        0,
                        intentNext,
                        PendingIntent.FLAG_UPDATE_CURRENT
                    ).send()
                }
            }
        }
    }

    private fun likePost(){
        val icon = if (media.isPlaying){
            R.drawable.pause_mini
        }else{
            R.drawable.play_mini
        }
        CreateNotification(
            applicationContext,
            Track(musicName!!, author!!, photo!!),
            icon,
            musicPosition,
            size,
            isLiked
        )
    }



    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }




}
