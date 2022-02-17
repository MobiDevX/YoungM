package com.example.youngm.Service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.youngm.App.GlideApp
import com.example.youngm.Data.Track
import com.example.youngm.Interface.MusicDataViewModel
import com.example.youngm.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CreateNotification(context: Context, track: Track, playButton: Int, musicPosition: Int, size: Int, isLiked: Boolean){
    companion object{
        const val CHANNEL_ID = "channel1"

        const val ACTION_PREV = "action_prev"
        const val ACTION_PLAY = "action_play"
        const val ACTION_NEXT = "action_next"
        const val ACTION_OPEN_POST = "action_open_post"
        const val ACTION_LIKE = "action_like"
    }
    private val NOTIFY_ID = 101
    private lateinit var notification: Notification
    private val musicDataViewModel = MusicDataViewModel(context, "CreateNotification")
    private var postID = musicDataViewModel.getPostID()

    init{
        createNotification(context, track, playButton, musicPosition, size, isLiked)

    }

    private fun createNotification(context: Context, track: Track, playButton: Int, musicPosition: Int, size: Int, isLiked: Boolean){
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val mediaSessionCompat = MediaSessionCompat(context, "tag")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(CHANNEL_ID, "YoungM", NotificationManager.IMPORTANCE_LOW)
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            notificationChannel.enableLights(true)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        lateinit var icon: Bitmap
        val job: Job = GlobalScope.launch(Dispatchers.Main) {
            GlideApp.with(context).asBitmap().load(track.imageURL)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onLoadCleared(placeholder: Drawable?) {
                        icon = BitmapFactory.decodeResource(context.resources, R.drawable.white_play)
                    }

                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        icon = resource
                        val intentPlay = Intent(context, NotificationActionService::class.java)
                            .setAction(ACTION_PLAY)
                            .putExtra("MUSIC_POSITION", musicPosition)
                            .putExtra("POST_ID", postID)
                        val pendingIntentPlay = PendingIntent.getBroadcast(context, 0, intentPlay, PendingIntent.FLAG_UPDATE_CURRENT)


                        var pendingIntentPrev: PendingIntent? = null
                        val prevIcon = R.drawable.prev_mini
                        if (musicPosition != 0){
                            val intentPrev = Intent(context, NotificationActionService::class.java)
                                .setAction(ACTION_PREV)
                                .putExtra("MUSIC_POSITION", musicPosition)
                                .putExtra("POST_ID", postID)
                            pendingIntentPrev = PendingIntent.getBroadcast(context, 0, intentPrev, PendingIntent.FLAG_UPDATE_CURRENT)
                        }

                        var pendingIntentNext: PendingIntent? = null
                        val nextIcon = R.drawable.next_mini
                        if (musicPosition != size){
                            val intentNext = Intent(context, NotificationActionService::class.java)
                                .setAction(ACTION_NEXT)
                                .putExtra("MUSIC_POSITION", musicPosition)
                                .putExtra("POST_ID", postID)
                            pendingIntentNext = PendingIntent.getBroadcast(context, 0, intentNext, PendingIntent.FLAG_UPDATE_CURRENT)
                        }

                        val openPostIntent = Intent(context, NotificationActionService::class.java)
                            .setAction(ACTION_OPEN_POST)
                            .putExtra("MUSIC_POSITION", musicPosition)
                            .putExtra("POST_ID", postID)
                        val pendingIntentOpenPost = PendingIntent.getBroadcast(context, 0, openPostIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                        val likePostIntent = Intent(context, NotificationActionService::class.java)
                            .setAction(ACTION_LIKE)
                            .putExtra("MUSIC_POSITION", musicPosition)
                            .putExtra("POST_ID", postID)
                        val pendingIntentLike = PendingIntent.getBroadcast(context, 0, likePostIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                        val likeIcon = if (isLiked){
                            R.drawable.active_like_mini
                        }else{
                            R.drawable.like_mini
                        }
                        val ongoing = playButton != R.drawable.play_mini
                        notification = NotificationCompat.Builder(context, CHANNEL_ID)
                            .setSmallIcon(R.drawable.profile_bar2)
                            .setContentTitle(track.musicName)
                            .setContentText(track.author)
                            .setContentIntent(pendingIntentOpenPost)
                            .setLargeIcon(icon)
                            .setOnlyAlertOnce(true)
                            .setShowWhen(false)
                            .setOngoing(ongoing)
                            .addAction(prevIcon, "Previous", pendingIntentPrev)
                            .addAction(playButton, "Play", pendingIntentPlay)
                            .addAction(nextIcon, "Next", pendingIntentNext)
                            .addAction(likeIcon, "Like", pendingIntentLike)
                            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0,1,2)
                                .setMediaSession(mediaSessionCompat.sessionToken))
                            .setPriority(NotificationCompat.PRIORITY_LOW).build()
                        notificationManager.notify(NOTIFY_ID, notification)

                    }

                })
        }
        job.start()





    }
}