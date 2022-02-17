package com.example.youngm.Fragment.OpenPost

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.youngm.Activity.OpenPostActivity
import com.example.youngm.Adapter.OpenPostCommentAdapter
import com.example.youngm.Data.CommentData
import com.example.youngm.Data.CommentFromPost
import com.example.youngm.Data.PostCommentData
import com.example.youngm.Data.URLData
import okhttp3.RequestBody.Companion.toRequestBody

import com.example.youngm.R
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.fragment_open_post_comment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import java.io.IOException


class OpenPostCommentFragment : Fragment() {

    private var ID: Int? = -1
    private var commentList = ArrayList<CommentData>()
    private val JSON: MediaType = "application/json; charset=utf-8".toMediaType()
    private val clientOkHttp = OkHttpClient()
    private lateinit var adapter: OpenPostCommentAdapter
    private lateinit var TOKEN: String
    private lateinit var LOGIN: String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_open_post_comment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val openPostActivity = activity as OpenPostActivity
        ID = openPostActivity.get_id()
        TOKEN = openPostActivity.getToken()
        LOGIN = openPostActivity.getLogin()
        context
        adapter = OpenPostCommentAdapter(TOKEN, context!!, LOGIN)
        recyclerOpenPostComment.adapter = adapter
        recyclerOpenPostComment.layoutManager = LinearLayoutManager(context)
        loadData()
        btnSendComment.setOnClickListener {
            sendComment()
        }
    }


    private fun sendComment(){
        val url = URLData.MAIN_URL + URLData.ADD_COMMENT
        val data = mutableMapOf<String, String>()
        data["comment"] = textComment.text.toString()
        data["post"] = ID.toString()
        val requestBody = Gson().toJson(data).toRequestBody(JSON)
        val request = Request.Builder().url(url).post(requestBody).header("Authorization", "Token ${TOKEN}").build()
        clientOkHttp.newCall(request).enqueue(object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onResponse(call: Call, response: Response) {
                val job: Job = GlobalScope.launch(Dispatchers.Main){
                    val body = response.body?.string()
                    val comment = GsonBuilder().create().fromJson(body, CommentFromPost::class.java)
                    val countComment = getString(R.string.header_comment) + (commentList.size + 1).toString()
                    openPostCommentCountText.text = countComment
                    commentList.add(CommentData(comment.id, comment.author, comment.comment, comment.count_like, comment.user_photo, comment.check, comment.isLiked))
                    adapter.refreshData(commentList)
                    textComment.text.clear()
                    textComment.clearFocus()
                    val inputMethodManager = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(textComment.windowToken, 0)
                }
                job.start()

            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
    }


    private fun loadData(){
        progressBar.visibility = View.VISIBLE
        openPostCommentLayout.visibility = View.INVISIBLE
        val url = URLData.MAIN_URL + URLData.GET_COMMENT
        val data = mutableMapOf<String, String>()
        data["post"] = ID.toString()
        data["count"] = commentList.size.toString()
        val requestBody = Gson().toJson(data).toRequestBody(JSON)
        val request = Request.Builder().url(url).post(requestBody).header("Authorization", "Token $TOKEN").build()
        clientOkHttp.newCall(request).enqueue(object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onResponse(call: Call, response: Response) {
                val job: Job = GlobalScope.launch(Dispatchers.Main){
                    val body = response.body?.string()
                    val allComments = GsonBuilder().create().fromJson(body, PostCommentData::class.java)
                    val comment = getString(R.string.header_comment) + allComments.comment_count.toString()
                    openPostCommentCountText.text = comment

                    for (oneComment in allComments.comments){
                        commentList.add(CommentData(oneComment.id, oneComment.author, oneComment.comment, oneComment.count_like, oneComment.user_photo, oneComment.check, oneComment.isLiked))
                    }
                    adapter.refreshData(commentList)
                    progressBar.visibility = View.INVISIBLE
                    openPostCommentLayout.visibility = View.VISIBLE
                }
                job.start()


            }
        })
    }

}

