package com.stalmate.user.view.dashboard.HomeFragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.stalmate.user.Helper.IntentHelper
import com.stalmate.user.R
import com.stalmate.user.base.BaseFragment
import com.stalmate.user.commonadapters.AdapterFeed
import com.stalmate.user.databinding.FragmentProfileActivityLogBinding
import com.stalmate.user.databinding.FragmentSearchBinding
import com.stalmate.user.model.User
import com.stalmate.user.utilities.Constants
import com.stalmate.user.view.adapter.SearchedUserAdapter
import com.stalmate.user.view.adapter.SuggestedFriendAdapter
import com.stalmate.user.view.adapter.UserHomeStoryAdapter

class FragmentProfileActivityLog: BaseFragment(), AdapterFeed.Callbackk,
    SuggestedFriendAdapter.Callbackk {
    lateinit var feedAdapter: AdapterFeed
    lateinit var suggestedFriendAdapter: SuggestedFriendAdapter
    lateinit var binding:FragmentProfileActivityLogBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.bind<FragmentProfileActivityLogBinding>(
            inflater.inflate(
                R.layout.fragment_profile_activity_log,
                container,
                false
            )
        )!!
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getActivityLogs()
    }
    fun getActivityLogs() {
        feedAdapter = AdapterFeed(networkViewModel, requireContext(), this)
        binding.shimmerLayoutFeeds.startShimmer()
        binding.rvFeeds.adapter = feedAdapter
        binding.rvFeeds.layoutManager = LinearLayoutManager(context)
        networkViewModel.getFeedList("", HashMap())
        networkViewModel.feedLiveData.observe(viewLifecycleOwner, Observer {
            Log.d("asdasdasd", "oaspiasddsad")
            it.let {
                feedAdapter.submitList(it!!.results)
                binding.shimmerLayoutFeeds.stopShimmer()
                binding.rvFeeds.visibility = View.VISIBLE
            }
        })


        getFriendSuggestionListing()

    }

    private fun getFriendSuggestionListing() {

        var hashmap = HashMap<String, String>()
        hashmap.put("id_user", "")
        hashmap.put("type", Constants.TYPE_FRIEND_SUGGESTIONS)
        hashmap.put("sub_type", "")
        hashmap.put("search", "")
        hashmap.put("page", "1")
        hashmap.put("limit", "6")

        networkViewModel.getFriendList(hashmap)
        networkViewModel.friendLiveData.observe(viewLifecycleOwner, Observer {
            it.let {
                Log.d("asdasdasd", "asdasdasdasd")
                suggestedFriendAdapter =
                    SuggestedFriendAdapter(networkViewModel, requireContext(), this)
                binding.rvSuggestedFriends.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                binding.rvSuggestedFriends.adapter = suggestedFriendAdapter
                suggestedFriendAdapter.submitList(it!!.results)
            }
        })
    }

    override fun onClickOnViewComments(postId: Int) {

    }

    override fun onClickOnUpdateFriendRequest(friend: User, status: String) {

    }

    override fun onClickOnProfile(friend: User) {

    }

    override fun onResume() {
        super.onResume()
        binding.root.requestLayout()
    }


}