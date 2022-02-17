package com.example.youngm.Interface

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.youngm.Activity.MainActivity.Companion.KEY_ISPLAYING
import com.example.youngm.Activity.MainActivity.Companion.KEY_MUSIC_ID
import com.example.youngm.Activity.MainActivity.Companion.KEY_POST_ID
import com.example.youngm.Activity.MainActivity.Companion.MUSIC_SETTINGS


class MusicDataViewModel(context: Context?, command: String): ViewModel() {
    private val author = MutableLiveData<String>()
    private val musicName = MutableLiveData<String>()
    private val photo = MutableLiveData<String>()
    private val isLiked = MutableLiveData<Boolean>()
    private val size = MutableLiveData<Int>()
    private val isPlaying = MutableLiveData<Boolean>()
    private val postID = MutableLiveData<Int>()
    private val musicPosition = MutableLiveData<Int>()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor



    init {
        if (context != null){
            sharedPreferences = context.getSharedPreferences(MUSIC_SETTINGS, Context.MODE_PRIVATE)
            isPlaying.value = sharedPreferences.getBoolean(KEY_ISPLAYING, false)
            postID.value = sharedPreferences.getInt(KEY_POST_ID, -1)
            musicPosition.value = sharedPreferences.getInt(KEY_MUSIC_ID, -1)
        }

    }


    fun updateIsPlaying(isPlaying: Boolean){
        this.isPlaying.value = isPlaying
        editor = sharedPreferences.edit()
        editor.putBoolean(KEY_ISPLAYING, isPlaying)
        editor.apply()
    }

    fun updatePostID(postID: Int, musicPosition: Int){
        this.postID.value = postID
        this.musicPosition.value = musicPosition
        editor = sharedPreferences.edit()
        editor.putInt(KEY_POST_ID, postID)
        editor.putInt(KEY_MUSIC_ID, musicPosition)
        editor.apply()
    }

    fun updateMusicPosition(musicPosition: Int){
        this.musicPosition.value = musicPosition
        editor = sharedPreferences.edit()
        editor.putInt(KEY_MUSIC_ID, musicPosition)
        editor.apply()
    }

    fun updateMusicData(postID: Int, musicPosition: Int, isPlaying: Boolean){
        this.postID.value = postID
        this.musicPosition.value = musicPosition
        this.isPlaying.value = isPlaying
        Log.d("UPDATE IS PLAYING", this.isPlaying.value.toString())
        editor = sharedPreferences.edit()
        editor.putInt(KEY_POST_ID, postID)
        editor.putInt(KEY_MUSIC_ID, musicPosition)
        editor.putBoolean(KEY_ISPLAYING, isPlaying)
        editor.apply()
    }

    fun getIsPlaying(): Boolean{
        return sharedPreferences.getBoolean(KEY_ISPLAYING, false)
    }

    fun getPostID(): Int{
        return sharedPreferences.getInt(KEY_POST_ID, -1)
    }

    fun getMusicPosition(): Int{
        return sharedPreferences.getInt(KEY_MUSIC_ID, -1)
    }



}