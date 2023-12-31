package com.stalmate.user.view.dashboard.Friend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.stalmate.user.intentHelper.IntentHelper
import com.stalmate.user.R
import com.stalmate.user.base.BaseFragment
import com.stalmate.user.databinding.FragmentAllFollowersFollowingListBinding
import com.stalmate.user.model.User

import com.stalmate.user.view.adapter.FriendAdapter

class FragmentAllFollowersFollowingList(var type: String, var subtype: String) : BaseFragment(),
    FriendAdapter.Callbackk {
    lateinit var friendAdapter: FriendAdapter
    lateinit var binding: FragmentAllFollowersFollowingListBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.bind<FragmentAllFollowersFollowingListBinding>(
            inflater.inflate(
                R.layout.fragment_all_followers_following_list,
                container,
                false
            )
        )!!
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        friendAdapter = FriendAdapter(networkViewModel, requireContext(), this,type,subtype)
        binding.rvFriends.adapter = friendAdapter
        binding.rvFriends.layoutManager = LinearLayoutManager(context)
        var hashmap = HashMap<String, String>()
        hashmap.put("type", type)
        hashmap.put("sub_type", subtype)
        hashmap.put("search", "")
        hashmap.put("page", "1")
        hashmap.put("limit", "")
        networkViewModel.getFriendList(prefManager?.access_token.toString(), hashmap)
        networkViewModel.friendLiveData.observe(viewLifecycleOwner, Observer {
            it.let {
                it?.results?.let { it1 -> friendAdapter.submitList(it1) }
            }
        })
    }


    override fun onClickOnUpdateFriendRequest(friend: User, status: String) {

        var hashmap = HashMap<String, String>()
        hashmap.put("type", "")
        hashmap.put("search", "")
        hashmap.put("page", "1")


    }




    override fun onClickOnProfile(friend: User) {
        startActivity(
            IntentHelper.getOtherUserProfileScreen(requireContext())!!.putExtra("id", friend.id)
        )
    }
}