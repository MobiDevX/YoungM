package com.example.youngm.Adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.youngm.Fragment.ProfilePage.LastProfileFeedFragment
import com.example.youngm.Fragment.ProfilePage.LikedProfileFeedFragment
import com.example.youngm.Fragment.ProfilePage.PopularProfileFeedFragment

class ProfileFeedPagerAdapter(fm: FragmentManager, recent: String, popular: String, liked: String) : FragmentPagerAdapter(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private var fragmentList: ArrayList<String> = arrayListOf(recent, popular, liked)
    private val lastProfileFeedFragment: LastProfileFeedFragment =
        LastProfileFeedFragment()
    private val popularProfileFeedFragment: PopularProfileFeedFragment =
        PopularProfileFeedFragment()
    private val likedProfileFeedFragment: LikedProfileFeedFragment =
        LikedProfileFeedFragment()


    override fun getPageTitle(position: Int): CharSequence? {
        return fragmentList[position]
    }

    override fun getItem(position: Int): Fragment {
        when(position){
            0 -> return lastProfileFeedFragment
            1 -> return popularProfileFeedFragment
            else -> return likedProfileFeedFragment
        }
    }

    override fun getCount(): Int = fragmentList.size
}