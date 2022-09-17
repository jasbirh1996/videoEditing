package com.stalmate.user.view.dashboard.welcome

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnTouchListener
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.stalmate.user.Helper.IntentHelper
import com.stalmate.user.R
import com.stalmate.user.base.App
import com.stalmate.user.base.BaseActivity
import com.stalmate.user.databinding.ActivityWelcomeBinding
import com.stalmate.user.modules.contactSync.SyncService
import com.stalmate.user.utilities.Constants
import com.stalmate.user.utilities.PrefManager
import com.stalmate.user.view.dashboard.ActivityDashboard


class ActivityWelcome : BaseActivity() {
    lateinit var binding: ActivityWelcomeBinding
    lateinit var syncBroadcastreceiver: SyncBroadcasReceiver
    var count = 0
    override fun onClick(viewId: Int, view: View?) {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_welcome)
        val filter = IntentFilter()
        filter.addAction(Constants.ACTION_SYNC_COMPLETED)
        syncBroadcastreceiver = SyncBroadcasReceiver()
        registerReceiver(syncBroadcastreceiver, filter)
        var viewpagerAdapter = ViewPagerAdapter(supportFragmentManager)
        viewpagerAdapter.add(FragmentSync(), "title")
        viewpagerAdapter.add(FragmentWelcomePage(), "title")
        viewpagerAdapter.add(FragmentInformationSuggestions(), "title")
        // viewpagerAdapter.add(FragmentSync(),"title")
        viewpagerAdapter.add(FragmentGroupSuggestionList(), "title")
        viewpagerAdapter.add(FragmentPageSugggestionsList(), "title")
        viewpagerAdapter.add(FragmentEventSuggestionsList(), "title")
        viewpagerAdapter.add(FragmentInterestSuggestionList(), "title")

        binding.viewpager.adapter = viewpagerAdapter
        binding.indicator.setViewPager(binding.viewpager)


        count = binding.viewpager.currentItem
        binding.viewpager.setOnTouchListener(OnTouchListener { v, event -> true })
        /*  binding.viewpager.offscreenPageLimit = 0*/
        binding.toolbar.topAppBar.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.btnNext.setOnClickListener {
            var page = viewpagerAdapter.getItem(count)

            Log.d("asghdasd", page.toString())
            if (count == 6) {
                finish()
            } else {
                if (page is FragmentInformationSuggestions) {

                    /* count = count +1
                     binding.viewpager.setCurrentItem(count,true)
     */
                    if (page.isValid()) {
                        count = +1
                        binding.viewpager.setCurrentItem(count, true)
                    }
                } else {
                    count++
                    binding.viewpager.setCurrentItem(count, true)
                }


            }


        }

        binding.viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        toolbar(true, "Welcome")
                    }
                    1 -> {
                        toolbar(true, "Welcome")
                    }
                    2 -> {
                        toolbar(false, "Group")
                    }
                    3 -> {
                        toolbar(false, "Pages")
                    }
                    4 -> {
                        toolbar(false, "Events")

                    }


                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        /*ToolBar Set*/
        toolbar(true, "Welcome")
        var permissionArray = arrayOf(
            android.Manifest.permission.READ_CONTACTS,
        )
        if (isPermissionGranted(permissionArray)) {
            Log.d("alskjdasd", ";aosjldsad")



            startService(
                Intent(
                    this,
                    SyncService::class.java
                )
            )
        }


    }


    inner class SyncBroadcasReceiver : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            if (p1!!.action == Constants.ACTION_SYNC_COMPLETED) {
                makeToast("Synced")
                if (p1.extras!!.getString("contacts")!=null){
                    startActivity(IntentHelper.getSearchScreen(this@ActivityWelcome)!!.putExtra("contacts",p1.extras!!.getString("contacts").toString()))
                }
           }
        }

    }



    fun toolbar(isCenterVisible: Boolean, text: String) {

        if (isCenterVisible) {
            binding.toolbar.tvhead.visibility = View.GONE
            binding.toolbar.tvheadCenterHeadBold.visibility = View.VISIBLE
            binding.toolbar.tvheadCenterHeadBold.text = text
        } else {
            binding.toolbar.tvhead.visibility = View.VISIBLE
            binding.toolbar.tvheadCenterHeadBold.visibility = View.GONE
            binding.toolbar.tvhead.text = text
        }


    }

    override fun onDestroy() {
        unregisterReceiver(syncBroadcastreceiver)
        super.onDestroy()

    }


    class ViewPagerAdapter(@NonNull fm: FragmentManager?) :
        FragmentPagerAdapter(fm!!) {
        private val fragments: MutableList<Fragment> = ArrayList()
        private val fragmentTitle: MutableList<String> = ArrayList()
        fun add(fragment: Fragment, title: String) {
            fragments.add(fragment)
            fragmentTitle.add(title)
        }

        @NonNull
        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }

        @Nullable
        override fun getPageTitle(position: Int): CharSequence? {
            return fragmentTitle[position]
        }
    }


    override fun onBackPressed() {
        if (count != 0) {
            count--
            binding.viewpager.setCurrentItem(count, true)
        } else {
            super.onBackPressed()
        }
    }


}