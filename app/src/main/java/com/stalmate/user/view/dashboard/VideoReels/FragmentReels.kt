package com.stalmate.user.view.dashboard.VideoReels

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.stalmate.user.R
import com.stalmate.user.base.BaseFragment
import com.stalmate.user.databinding.FragmentReelsBinding

class FragmentReels : BaseFragment() {


    private lateinit var binding : FragmentReelsBinding
    var tabLayout: TabLayout? = null
    var viewPager: ViewPager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
       var  view = inflater.inflate(R.layout.fragment_reels, container, false)
        binding = DataBindingUtil.bind<FragmentReelsBinding>(view)!!

        /*tab and ViewPager Layout id*/

        tabLayout = view.findViewById(R.id.tabLayout)
        viewPager = view.findViewById<ViewPager>(R.id.viewPager)

        /*tab layout setUp Data*/

        binding.tabLayout.addTab(tabLayout!!.newTab().setText(getString(R.string.live_videos)))
        binding.tabLayout.addTab(tabLayout!!.newTab().setText(getString(R.string.your_video)))
        binding.tabLayout.addTab(tabLayout!!.newTab().setText(getString(R.string.folllowing)))
        binding.tabLayout.addTab(tabLayout!!.newTab().setText(getString(R.string.saved)))
        tabLayout!!.tabGravity = TabLayout.GRAVITY_FILL

        /*TAb ViewPager Adapter*/
        viewPager!!.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))


        return binding.root
    }
}