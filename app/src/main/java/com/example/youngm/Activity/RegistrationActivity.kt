package com.example.youngm.Activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.youngm.Data.URLData
import com.example.youngm.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_registration.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class RegistrationActivity : AppCompatActivity() {

    val JSON: MediaType = "application/json; charset=utf-8".toMediaType()
    private val clientOkHttp = OkHttpClient()
    val context: Context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        btnNext.setOnClickListener {
            val firstName: String = editTextFirstNameReg.text.toString()
            val lastName: String = editTextLastNameReg.text.toString()
            val username = editTextUsernameReg.text.toString()
            val password = editTextPasswordReg.text.toString()
            val confPassword = editTextConfirmPasswordReg.text.toString()
            val email: String = editTextEmailReg.text.toString()
            if (username == "") {
                Toast.makeText(this, "Username can't be blank", Toast.LENGTH_LONG).show()
            }else if (password == "" || confPassword == ""){
                Toast.makeText(this, "Password can't be blank", Toast.LENGTH_LONG).show()
            }else if (password != confPassword){
                Toast.makeText(this, "Passwords don't match", Toast.LENGTH_LONG).show()
            }else{
                val intent = Intent(this, RegistrationActivityStep2::class.java)
                intent.putExtra("first_name", firstName)
                intent.putExtra("last_name", lastName)
                intent.putExtra("username", username)
                intent.putExtra("password", password)
                intent.putExtra("email", email)
                testUsername(username, intent)


            }
        }
    }


    fun testUsername(username: String, intent: Intent){
        val url = URLData.MAIN_URL + URLData.CHECK_USERNAME
        var body = mutableMapOf<String, String>()
        body["username"] = username
        val requestBody = Gson().toJson(body).toRequestBody(JSON)
        val request = Request.Builder().url(url).post(requestBody).build()
        clientOkHttp.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Error", e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200) {
                    startActivity(intent)
                    finish()
                }else{
                    val job: Job = GlobalScope.launch(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "This user name is already taken",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    job.start()
                }
            }
        })

    }
}
