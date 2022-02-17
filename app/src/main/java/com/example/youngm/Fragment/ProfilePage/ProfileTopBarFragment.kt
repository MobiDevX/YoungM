package com.example.youngm.Fragment.ProfilePage

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.youngm.R
import kotlinx.android.synthetic.main.fragment_profile_top_bar.*


class ProfileTopBarFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile_top_bar, container, false)
    }

}
