package com.example.youngm.Activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.youngm.Data.Token
import com.example.youngm.Data.URLData
import com.example.youngm.Fragment.Navigation.FeedFragment
import com.example.youngm.Interface.ProfileDataViewModel
import com.example.youngm.R
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    companion object{
        val KEY_TOKEN = "SAVED_TOKEN"
        val KEY_LOGIN = "SAVED_LOGIN"
    }

    val context: Context = this
    val JSON: MediaType = "application/json; charset=utf-8".toMediaType()
    private lateinit var profileDataViewModel: ProfileDataViewModel

    private val clientOkHttp = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        profileDataViewModel = ProfileDataViewModel(this)
        btnSignIn.setOnClickListener {
            signIn()
        }
        btnSignUp.setOnClickListener {
            signUp()
        }
    }

    private fun signIn() {
        val url = URLData.MAIN_URL + URLData.LOGIN
        val body = mutableMapOf<String, String>()
        body["username"] = textLogin.text.toString()
        body["password"] = textPassword.text.toString()
        val requestBody = Gson().toJson(body).toRequestBody(JSON)
        val request = Request.Builder().url(url).post(requestBody).build()
        clientOkHttp.newCall(request).enqueue(object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Error", e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200) {
                    val job: Job = GlobalScope.launch(Dispatchers.Main){
                        val gson = GsonBuilder().create()
                        val token = gson.fromJson(response.body?.string(), Token::class.java).token
                        saveTokenandLogin(token, body["username"].toString())
                        loadMainActivity(token, body["username"].toString())
                    }
                    job.start()

                }else{
                    val job: Job = GlobalScope.launch(Dispatchers.Main){
                        Toast.makeText(context, "Incorrect username or password", Toast.LENGTH_SHORT).show()
                    }
                    job.start()
                }


            }

        })

    }

    private fun signUp(){
        val intent = Intent(context, RegistrationActivity::class.java)
        startActivity(intent)
    }

    private fun saveTokenandLogin(token: String, login: String) {
        profileDataViewModel.updateProfileData(token, login)
    }




    private fun loadMainActivity(token: String, login: String){
        val intent: Intent = Intent(context, MainActivity::class.java)
        startActivity(intent)
        finish()
    }


}


