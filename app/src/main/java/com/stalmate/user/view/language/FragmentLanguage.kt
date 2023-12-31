package com.stalmate.user.view.language

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.stalmate.user.R
import com.stalmate.user.base.BaseFragment
import com.stalmate.user.commonadapters.AdapterFeed
import com.stalmate.user.databinding.FragmentLanguageBinding


class FragmentLanguage : BaseFragment(), AdapterLanguage.Callbackk {

    private lateinit var binding: FragmentLanguageBinding
    lateinit var languageAdapter: AdapterLanguage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_language, container, false)
        binding = DataBindingUtil.bind<FragmentLanguageBinding>(view)!!

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*Common ToolBar SetUp*/
        toolbarSetUp()

        languageAdapter = AdapterLanguage(networkViewModel, requireContext(), this)
        binding.rvLanguage.adapter = languageAdapter
        binding.rvLanguage.layoutManager = GridLayoutManager(requireContext(), 3)

        networkViewModel.languageLiveData(HashMap(), prefManager?.access_token.toString())
        networkViewModel.languageLiveData.observe(requireActivity()) {
            it.let {
                if (!it?.results.isNullOrEmpty())
                    it?.results?.let { it1 -> languageAdapter.submitList(it1) }
                else
                    it?.reponse?.let { it1 -> languageAdapter.submitList(it1) }
            }
        }

        binding.btnNext.setOnClickListener {
            findNavController().navigate(R.id.fragmentLogin)
        }
    }

    private fun toolbarSetUp() {
        binding.toolbar.toolBarCenterText.text = getString(R.string.choose_language)
        binding.toolbar.back.visibility = View.GONE
        binding.toolbar.toolBarCenterText.visibility = View.VISIBLE
        binding.toolbar.backButtonLeftText.visibility = View.GONE
        binding.toolbar.menuChat.visibility = View.GONE

        binding.toolbar.back.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    override fun onClickLanguageItem(postId: String, lang: String) {

    }


}