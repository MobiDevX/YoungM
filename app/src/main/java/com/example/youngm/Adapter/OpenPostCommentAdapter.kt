package com.example.youngm.Adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.youngm.Activity.MainActivity
import com.example.youngm.Activity.ProfileActivity
import com.example.youngm.App.GlideApp
import com.example.youngm.Data.CommentData
import com.example.youngm.Data.URLData
import com.example.youngm.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.comment_item.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class OpenPostCommentAdapter(val TOKEN: String, val context: Context, val LOGIN: String): RecyclerView.Adapter<OpenPostCommentAdapter.OpenPostCommentHolder>() {

    var commentList = ArrayList<CommentData>()
    private val clientOkHttp = OkHttpClient()
    private val myComment = arrayOf(context.resources.getString(R.string.comment_update), context.resources.getString(R.string.comment_delete))
    private val comment = arrayOf(context.resources.getString(R.string.comment_report))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OpenPostCommentHolder
    = OpenPostCommentHolder(LayoutInflater.from(parent.context).inflate(R.layout.comment_item, parent, false))

    override fun getItemCount(): Int = commentList.size

    override fun onBindViewHolder(holder: OpenPostCommentHolder, position: Int) {
        holder.bind(commentList[position])
        holder.commentLinearLayout.commentLikeLayout.setOnClickListener {
            val job: Job = GlobalScope.launch(Dispatchers.Main) {
                commentList[position].isLiked = !commentList[position].isLiked
                if (commentList[position].isLiked) {
                    holder.commentLinearLayout.commentLikeImage.setImageResource(R.drawable.active_like)
                    commentList[position].countLike += 1
                } else {
                    holder.commentLinearLayout.commentLikeImage.setImageResource(R.drawable.white_like)
                    commentList[position].countLike -= 1
                }
                updateLikeData(commentList[position])
                refreshData(commentList)
            }
            job.start()

        }


        holder.commentLinearLayout.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            lateinit var array: Array<String>
            if (LOGIN == holder.commentLinearLayout.commentUsername.text.toString()){
                array = myComment
            }else{
                array = comment
            }
            builder.setItems(array) { dialog, which ->
                when(array[which]){
                    myComment[0] -> updateComment(position)
                    myComment[1] -> deleteComment(position)
                    comment[0] -> reportComment()
                }

            }
            val dialog = builder.create()
            dialog.window!!.setBackgroundDrawableResource(R.color.cardColor)
            dialog.context.setTheme(R.style.CommentCommandTheme)
            dialog.show()

        }

        holder.commentLinearLayout.commentPhotoProfile.setOnClickListener {
            val intent: Intent = Intent(context, ProfileActivity::class.java)
            intent.putExtra("LOGIN", LOGIN)
            intent.putExtra("USERNAME", holder.commentLinearLayout.commentUsername.text.toString())
            intent.putExtra("TOKEN", TOKEN)
            context.startActivity(intent)
        }



    }

    class OpenPostCommentHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        lateinit var commentLinearLayout: LinearLayout
        fun bind(data: CommentData) = with(itemView){
            commentLinearLayout = commentLayout
            val job: Job = GlobalScope.launch(Dispatchers.Main) {
                if (data.userPhoto != "")
                    GlideApp.with(context).load(data.userPhoto).into(commentPhotoProfile)
                commentUsername.text = data.username
                commentText.text = data.commentText
                commentCountLike.text = data.countLike.toString()
                if (data.isLiked){
                    commentLikeImage.setImageResource(R.drawable.active_like)
                } else{
                    commentLikeImage.setImageResource(R.drawable.white_like)
                }
            }
            job.start()



        }
    }

    fun refreshData(commentList: ArrayList<CommentData>){
        this.commentList = commentList
        notifyDataSetChanged()
    }

    private fun updateComment(position: Int){
        val alertDialog = AlertDialog.Builder(context)
        val editTextUpdate = EditText(context)
        editTextUpdate.setTextColor(ResourcesCompat.getColor(context.resources,R.color.cardWhite,null))
        editTextUpdate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        editTextUpdate.setText(commentList[position].commentText)
        alertDialog
            .setView(editTextUpdate)
            .setPositiveButton(myComment[0]
            ) { dialog, which ->
                val url = URLData.MAIN_URL + URLData.UPDATE_COMMENT
                val body = mutableMapOf<String, String>()
                body["comment"] = editTextUpdate.text.toString()
                body["comment_id"] = commentList[position].id.toString()
                val requestBody = Gson().toJson(body).toRequestBody(MainActivity.JSON)
                val request = Request.Builder().url(url).post(requestBody).header("Authorization", "Token $TOKEN").build()
                clientOkHttp.newCall(request).enqueue(object: Callback{
                    override fun onFailure(call: Call, e: IOException) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if (response.code == 200){
                            val job: Job = GlobalScope.launch(Dispatchers.Main){
                                commentList[position].commentText = editTextUpdate.text.toString()
                                refreshData(commentList)
                            }
                            job.start()
                        }
                    }
                })
            }
            .setNegativeButton(context.resources.getString(R.string.btn_cancel)) {dialog, which ->
                alertDialog.create().cancel()
            }
        val dialog = alertDialog.create()
        dialog.context.setTheme(R.style.CommentCommandTheme)
        dialog.window!!.setBackgroundDrawableResource(R.color.cardColor)
        dialog.show()
    }

    private fun deleteComment(position: Int){
        val url = URLData.MAIN_URL + URLData.DELETE_COMMENT
        val body = mutableMapOf<String, Int>()
        body["comment"] = commentList[position].id
        val requestBody = Gson().toJson(body).toRequestBody(MainActivity.JSON)
        val request = Request.Builder().url(url).post(requestBody).header("Authorization", "Token $TOKEN").build()
        clientOkHttp.newCall(request).enqueue(object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200){
                    val job: Job = GlobalScope.launch(Dispatchers.Main) {
                        commentList.removeAt(position)
                        refreshData(commentList)
                    }
                    job.start()
                }
            }

        })

    }

    private fun reportComment(){
        Toast.makeText(context, "Report", Toast.LENGTH_LONG).show()
    }


    fun updateLikeData(comment: CommentData){
        val url = URLData.MAIN_URL + URLData.LIKE_COMMENT
        val body = mutableMapOf<String, Int>()
        body["comment"] = comment.id
        val requestBody = Gson().toJson(body).toRequestBody(MainActivity.JSON)
        val request = Request.Builder().url(url).header("Authorization", "Token $TOKEN").post(requestBody).build()
        clientOkHttp.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("Fail", "True")
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("Success", "True")
            }

        })
    }

}