package com.example.youngm.Fragment.ProfilePage

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.youngm.Activity.FollowActivity
import com.example.youngm.Activity.ProfileActivity

import com.example.youngm.R
import kotlinx.android.synthetic.main.fragment_profile_description.*

class ProfileDescriptionFragment : Fragment() {


    private lateinit var TOKEN: String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_description, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val mainActivity = activity as ProfileActivity
        TOKEN = mainActivity.getToken()
        followingLinearLayout.setOnClickListener {
            val intent: Intent = Intent(context, FollowActivity::class.java)
            intent.putExtra("type", "following")
            intent.putExtra("LOGIN", profileUsername.text)
            intent.putExtra("TOKEN", TOKEN)
            startActivity(intent)
        }
        followersLinearLayout.setOnClickListener {
            val intent: Intent = Intent(context, FollowActivity::class.java)
            intent.putExtra("type", "followers")
            intent.putExtra("LOGIN", profileUsername.text)
            intent.putExtra("TOKEN", TOKEN)
            startActivity(intent)
        }

    }
}
