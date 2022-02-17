package com.example.youngm.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.youngm.Activity.ProfileActivity
import com.example.youngm.App.GlideApp
import com.example.youngm.Data.FollowData
import com.example.youngm.R
import kotlinx.android.synthetic.main.follow_item.view.*

class FollowAdapter(val context: Context, val TOKEN: String, val LOGIN: String): RecyclerView.Adapter<FollowAdapter.FollowHolder>() {

    var followList: ArrayList<FollowData> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowHolder =
        FollowHolder(LayoutInflater.from(parent.context).inflate(R.layout.follow_item, parent, false))

    override fun getItemCount(): Int = followList.size

    override fun onBindViewHolder(holder: FollowHolder, position: Int) {
        holder.bind(followList[position])
        holder.linearLayout.setOnClickListener {
            val intent: Intent = Intent(context, ProfileActivity::class.java)
            intent.putExtra("USERNAME", followList[position].username)
            intent.putExtra("TOKEN", TOKEN)
            intent.putExtra("LOGIN", LOGIN)
            context.startActivity(intent)
        }
    }


    class FollowHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        lateinit var linearLayout: LinearLayout
        fun bind(item: FollowData) = with(itemView){
            followUsername.text = item.username
            if (item.photo != "") {
                GlideApp.with(context).load(item.photo).into(followPhoto)
            }
            linearLayout = followItemLinearLayout
        }



    }


    fun refreshData(followList: ArrayList<FollowData>){
        this.followList = followList
        notifyDataSetChanged()
    }

}