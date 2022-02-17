package com.example.youngm.Service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class NotificationActionService: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context!!.sendBroadcast(Intent("MUSIC_ACTION").putExtra("name", intent!!.action)
            .putExtra("MUSIC_POSITION", intent.extras?.getInt("MUSIC_POSITION"))
            .putExtra("POST_ID", intent.extras?.getInt("POST_ID")))
    }
}