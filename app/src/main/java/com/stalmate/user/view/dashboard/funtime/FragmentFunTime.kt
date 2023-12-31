package com.stalmate.user.view.dashboard.funtime

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.stalmate.user.intentHelper.IntentHelper
import com.stalmate.user.R
import com.stalmate.user.base.BaseFragment
import com.stalmate.user.databinding.FragmentFunTimeBinding
import com.stalmate.user.modules.reels.player.ReelListFragment
import com.stalmate.user.view.dashboard.ActivityDashboard

class FragmentFunTime() : BaseFragment(), FragmentCallBack {
    var handler: Handler? = null
    var isPlusButtonActive = false
    lateinit var binding: FragmentFunTimeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        if (!this::binding.isInitialized) {
            binding = DataBindingUtil.bind<FragmentFunTimeBinding>(
                inflater.inflate(
                    R.layout.fragment_fun_time,
                    container,
                    false
                )
            )!!
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ivBack.setOnClickListener {
            (requireActivity() as ActivityDashboard).onBackPressed()
        }

        binding.ivAddButton.setOnClickListener {
            IntentHelper.getCreateReelsScreen(requireActivity())?.let { it1 -> startActivity(it1) }
        }
        loadFragment(ReelListFragment())
    }


    fun toggleButton() {
        if (isPlusButtonActive) {
            isPlusButtonActive = false
            binding.layoutImagenVideo.animate().alpha(0f).setDuration(500).start()
        } else {
            isPlusButtonActive = true
            binding.layoutImagenVideo.animate().alpha(1f).setDuration(500).start()
        }
    }


    private fun loadFragment(fragment: Fragment) {

        val backStateName = fragment.javaClass.name
        val fragmentTag = backStateName
        val manager: FragmentManager = childFragmentManager
        val fragmentPopped = manager.popBackStackImmediate(backStateName, 1)
        if (!fragmentPopped && manager.findFragmentByTag(fragmentTag) == null) { //fragment not in back stack, create it.
            val ft = manager.beginTransaction()
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.add(binding.frame.id, fragment, fragmentTag)
            ft.commit()
        }
    }

    override fun onResponce(bundle: Bundle?) {

    }

    fun pauseMusic() {
        var fragment = childFragmentManager.findFragmentById(binding.frame.id) as ReelListFragment
        fragment.onPause()
    }

    fun resumeMusic() {
        var fragment = childFragmentManager.findFragmentById(binding.frame.id) as ReelListFragment
        fragment.onStart()
    }


}













