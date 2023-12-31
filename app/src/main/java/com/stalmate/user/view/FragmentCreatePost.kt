package com.stalmate.user.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.stalmate.user.R
import com.stalmate.user.base.BaseFragment
import com.stalmate.user.commonadapters.AdapterFeed
import com.stalmate.user.databinding.FragmentCreatePostBinding
import com.stalmate.user.intentHelper.IntentHelper
import com.stalmate.user.model.Feed
import com.stalmate.user.model.User
import com.stalmate.user.view.adapter.SuggestedFriendAdapter
import com.stalmate.user.view.adapter.UserHomeStoryAdapter
import com.stalmate.user.view.dashboard.funtime.ResultFuntime


class FragmentCreatePost : BaseFragment(), AdapterFeed.Callbackk, UserHomeStoryAdapter.Callbackk,
    SuggestedFriendAdapter.Callbackk {

    private lateinit var binding: FragmentCreatePostBinding
    lateinit var feedAdapter: AdapterFeed
    lateinit var homeStoryAdapter: UserHomeStoryAdapter

    lateinit var suggestedFriendAdapter:  SuggestedFriendAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view=inflater.inflate(R.layout.fragment_create_post, container, false)
        binding=DataBindingUtil.bind<FragmentCreatePostBinding>(view)!!
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        feedAdapter = AdapterFeed(childFragmentManager,networkViewModel, requireContext(), requireActivity())
        homeStoryAdapter = UserHomeStoryAdapter(networkViewModel, requireContext(), this)
        suggestedFriendAdapter = SuggestedFriendAdapter(networkViewModel, requireContext(), this)



        networkViewModel.feedLiveData.observe(viewLifecycleOwner, Observer {
            Log.d("asdasdasd","oaspiasddsad")
            it.let {
                if (!it?.results.isNullOrEmpty()) {
                    it?.results?.let { it1 -> homeStoryAdapter.submitList(it1) }
                } else {
                    it?.reponse?.let { it1 -> homeStoryAdapter.submitList(it1) }
                }
            }
        })


    }

    override fun onClickOnViewComments(postId: Int) {

    }

    override fun onCLickItem(item: ResultFuntime) {

    }

    override fun onCLickUserProfile(item: String) {
        startActivity(IntentHelper.getOtherUserProfileScreen(this.requireContext())!!.putExtra("id", item))
    }

    override fun onClickOnLikeButtonReel(feed: ResultFuntime) {

    }

    override fun onClickOnFollowButtonReel(feed: ResultFuntime) {

    }

    override fun onSendComment(feed: ResultFuntime, comment: String) {

    }

    override fun onCaptureImage(feed: ResultFuntime, position: Int) {

    }

    override fun showCommentOverlay(feed: ResultFuntime, position: Int) {

    }

    override fun hideCommentOverlay(feed: ResultFuntime, position: Int) {

    }

    override fun onClickOnProfile(user: Feed) {

    }

    override fun onClickOnUpdateFriendRequest(friend: User, status: String) {

    }

    override fun onClickOnProfile(friend: User) {

    }

}