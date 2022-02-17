package com.example.youngm.Interface

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.youngm.Activity.MainActivity

class ProfileDataViewModel(context: Context?): ViewModel() {
    private val token = MutableLiveData<String>()
    private val login = MutableLiveData<String>()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    init {
        if (context != null){
            sharedPreferences = context.getSharedPreferences(MainActivity.PROFILE_SETTINGS, Context.MODE_PRIVATE)
            token.value = sharedPreferences.getString(MainActivity.KEY_TOKEN, "")
            login.value = sharedPreferences.getString(MainActivity.KEY_LOGIN, "")
        }
    }

    fun updateToken(token: String){
        this.token.value = token
        editor = sharedPreferences.edit()
        editor.putString(MainActivity.KEY_TOKEN, token)
        editor.apply()
    }

    fun updateLogin(login: String){
        this.login.value = login
        editor = sharedPreferences.edit()
        editor.putString(MainActivity.KEY_LOGIN, login)
        editor.apply()
    }

    fun updateProfileData(token: String, login: String){
        this.token.value = token
        this.login.value = login
        editor = sharedPreferences.edit()
        editor.putString(MainActivity.KEY_TOKEN, token)
        editor.putString(MainActivity.KEY_LOGIN, login)
        editor.apply()
    }

    fun getToken(): String?{
        return sharedPreferences.getString(MainActivity.KEY_TOKEN, "")
    }

    fun getLogin(): String?{
        return sharedPreferences.getString(MainActivity.KEY_LOGIN, "")
    }


}