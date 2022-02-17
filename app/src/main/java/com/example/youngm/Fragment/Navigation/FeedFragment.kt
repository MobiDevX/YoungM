package com.example.youngm.Fragment.Navigation


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.example.youngm.Adapter.SlidesPagerAdapter
import com.example.youngm.Interface.UpdateData
import com.example.youngm.R
import kotlinx.android.synthetic.main.fragment_feed.*


class FeedFragment : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_feed, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        loadSettigs()
    }

    private fun loadSettigs(){
        if(isAdded) {
            val following = getString(R.string.header_following)
            val popular = getString(R.string.header_popular)
            val recommend = getString(R.string.header_recommend)
            val recent = getString(R.string.header_recent)
            val pagerAdapter =
                SlidesPagerAdapter(
                    requireActivity().supportFragmentManager,
                    following,
                    popular,
                    recommend,
                    recent
                )
            viewPagerSliders.adapter = pagerAdapter
            tabLayoutSliders.setupWithViewPager(viewPagerSliders)
            viewPagerSliders.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {
                }

                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                }

                override fun onPageSelected(position: Int) {
                    val fragment = pagerAdapter.getItem(position)
//                if (fragment is UpdateData) {
//                    fragment.updateData()
//                }
                }

            })
        }

    }










}


