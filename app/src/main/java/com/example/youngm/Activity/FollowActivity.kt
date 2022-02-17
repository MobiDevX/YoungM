package com.example.youngm.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.youngm.Adapter.FollowAdapter
import com.example.youngm.Data.FollowData
import com.example.youngm.Data.FollowInformationData
import com.example.youngm.Data.URLData
import com.example.youngm.R
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_follow.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class FollowActivity : AppCompatActivity() {

    val followList: ArrayList<FollowData> = ArrayList()
    private val clientOkHttp = OkHttpClient()
    private lateinit var adapter: FollowAdapter
    private lateinit var TOKEN: String
    private lateinit var USERNAME: String
    private lateinit var LOGIN: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follow)
        val toolbar = followToolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val TYPE = intent.extras?.get("type").toString()
        LOGIN = intent.extras?.get("LOGIN").toString()
        TOKEN = intent.extras?.get("TOKEN").toString()
        adapter = FollowAdapter(this, TOKEN, LOGIN)
        followRecyclerView.adapter = adapter
        followRecyclerView.layoutManager = LinearLayoutManager(this)
        supportActionBar!!.title = LOGIN
        if (TYPE == "following"){
            loadFollowing()
        }else if (TYPE == "followers"){
            loadFollowers()
        }

    }


    fun loadFollowing(){
        val url = URLData.MAIN_URL + URLData.GET_FOLLOWING
        val body = mutableMapOf<String, String>()
        body["author"] = LOGIN
        body["count"] = followList.size.toString()
        val requestBody = Gson().toJson(body).toRequestBody(MainActivity.JSON)
        val request = Request.Builder().url(url).post(requestBody).header("Authorization",
            "Token $TOKEN"
        ).build()
        clientOkHttp.newCall(request).enqueue(object: Callback{
            override fun onResponse(call: Call, response: Response) {
                val followInformation = response.body!!.string()

                val gson = GsonBuilder().create()
                val information = gson.fromJson(followInformation, FollowInformationData::class.java)

                for (followers in information.information){
                    followList.add(FollowData(followers.username, followers.photo))
                }

                val job: Job = GlobalScope.launch(Dispatchers.Main){
                    adapter.refreshData(followList)
                }
                job.start()
            }

            override fun onFailure(call: Call, e: IOException) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })
    }

    fun loadFollowers(){
        val url = URLData.MAIN_URL + URLData.GET_FOLLOWERS
        var body = mutableMapOf<String, String>()
        body["author"] = LOGIN
        body["count"] = followList.size.toString()
        val requestBody = Gson().toJson(body).toRequestBody(MainActivity.JSON)
        val request = Request.Builder().url(url).post(requestBody).header("Authorization",
            "Token $TOKEN"
        ).build()
        clientOkHttp.newCall(request).enqueue(object: Callback{
            override fun onResponse(call: Call, response: Response) {
                val followInformation = response.body!!.string()

                val gson = GsonBuilder().create()
                val information = gson.fromJson(followInformation, FollowInformationData::class.java)

                for (followers in information.information){
                    followList.add(FollowData(followers.username, followers.photo))
                }

                val job: Job = GlobalScope.launch(Dispatchers.Main){
                    adapter.refreshData(followList)
                }
                job.start()
            }

            override fun onFailure(call: Call, e: IOException) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })
    }
}


