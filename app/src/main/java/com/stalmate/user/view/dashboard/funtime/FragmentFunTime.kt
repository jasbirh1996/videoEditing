package com.stalmate.user.view.dashboard.funtime

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.databinding.DataBindingUtil
import com.stalmate.user.Helper.IntentHelper
import com.stalmate.user.R
import com.stalmate.user.base.BaseFragment
import com.stalmate.user.databinding.FragmentFunTimeBinding
import com.stalmate.user.view.language.AdapterLanguage
import eightbitlab.com.blurview.RenderScriptBlur


class FragmentFunTime : BaseFragment() {

    lateinit var binding: FragmentFunTimeBinding

    lateinit var adapterFunTime: AdapterFunTime

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.bind<FragmentFunTimeBinding>(inflater.inflate(R.layout.fragment_fun_time, container, false))!!

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ivAddButton.setOnClickListener {
            //   makeToast("Under Development")
            startActivity(IntentHelper.getCreateReelsScreen(requireActivity()))
        }

      adapterFunTime = AdapterFunTime(networkViewModel, requireContext())

      binding.rvRecyclerView.adapter = adapterFunTime

    }

}