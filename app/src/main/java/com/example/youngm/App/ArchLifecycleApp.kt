package com.example.youngm.App

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner

class ArchLifecycleApp : Application(), LifecycleObserver {

    var broadcastReceiver: BroadcastReceiver? = null

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }


    fun updateBroadcastReceiver(receiver: BroadcastReceiver?){
        broadcastReceiver = receiver
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        Log.d("BACKGROUND", "STOP")
        try {
            registerReceiver(broadcastReceiver, IntentFilter("MUSIC_ACTION"))
        }catch (e: Exception){
            Log.d("REGISTER_RECEIVER", "NULL")
        }
    }



    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        Log.d("FOREGROUND", "START")
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver)
        }
    }

}