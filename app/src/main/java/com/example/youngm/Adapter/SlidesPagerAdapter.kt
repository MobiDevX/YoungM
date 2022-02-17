package com.example.youngm.Adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.youngm.Fragment.FeedPage.FollowingFeedFragment
import com.example.youngm.Fragment.FeedPage.PopularFeedFragment
import com.example.youngm.Fragment.FeedPage.RecentFeedFragment
import com.example.youngm.Fragment.FeedPage.RecommendFeedFragment

class SlidesPagerAdapter(fm: FragmentManager, following: String, popular: String, recommend: String, recent: String) : FragmentPagerAdapter(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {


    private val followingFeedFragment: FollowingFeedFragment =
        FollowingFeedFragment()
    private val popularFeedFragment: PopularFeedFragment =
        PopularFeedFragment()
    private val recommendFeedFragment: RecommendFeedFragment =
        RecommendFeedFragment()
    private val recentFeedFragment: RecentFeedFragment =
        RecentFeedFragment()




    override fun getPageTitle(position: Int): CharSequence? {
        return fragmentList[position]
    }

    private var fragmentList: ArrayList<String> = arrayListOf(following, popular, recommend, recent)

    override fun getItem(position: Int): Fragment{
        when(position) {
            0 -> return followingFeedFragment
            1 -> return popularFeedFragment
            2 -> return recommendFeedFragment
            else -> return recentFeedFragment
        }
    }

    override fun getCount(): Int = fragmentList.size
}