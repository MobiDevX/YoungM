package com.example.youngm.Fragment.FeedPage

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.example.youngm.R


class TopBarFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_top_bar, container, false)

    }




}
