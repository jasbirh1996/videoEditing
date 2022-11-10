package com.stalmate.user.view.dashboard.funtime

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.stalmate.user.R
import com.stalmate.user.base.BaseActivity
import com.stalmate.user.databinding.ActivityReelsByAudioBinding

class ActivityReelsByAudio : BaseActivity() {
    lateinit var binding:ActivityReelsByAudioBinding
    override fun onClick(viewId: Int, view: View?) {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       binding=DataBindingUtil. setContentView(this,R.layout.activity_reels_by_audio)

       var reelData= intent.getParcelableExtra<ResultFuntime>("data") as ResultFuntime
        loadDrawerFragment(FragmentReelUsedAudio(reelData))
    }


    private fun loadDrawerFragment(fragment: Fragment) {
        Log.d(":lkasdasd","ppopp")
        val backStateName = fragment.javaClass.name
        val fragmentTag = backStateName
        val manager: FragmentManager = supportFragmentManager
        val fragmentPopped = manager.popBackStackImmediate(backStateName, 1)
        if (!fragmentPopped && manager.findFragmentByTag(fragmentTag) == null) { //fragment not in back stack, create it.
            val ft = manager.beginTransaction()
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.add(binding.frame.id, fragment, fragmentTag)
            ft.commit()
        }
    }
}