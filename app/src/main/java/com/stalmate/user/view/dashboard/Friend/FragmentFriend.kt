package com.stalmate.user.view.dashboard.Friend

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayoutMediator
import com.stalmate.user.R
import com.stalmate.user.base.BaseFragment
import com.stalmate.user.commonadapters.FragmentViewPagerAdapter
import com.stalmate.user.databinding.FragmentFriendBinding
import com.stalmate.user.model.User
import com.stalmate.user.utilities.Constants

import com.stalmate.user.view.adapter.FriendAdapter
import com.stalmate.user.view.adapter.ProfileFriendAdapter

class FragmentFriend : BaseFragment(), FriendAdapter.Callbackk {
    lateinit var binding:FragmentFriendBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding=DataBindingUtil.bind<FragmentFriendBinding>(inflater.inflate(R.layout.fragment_friend, container, false))!!
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

   
        var list=ArrayList<Fragment>()
        list.add(FragmentFriendCategory(Constants.TYPE_FRIEND_REQUEST))
        list.add(FragmentFriendCategory(Constants.TYPE_FRIEND_SUGGESTIONS))
        list.add(FragmentFriendCategory(Constants.TYPE_MY_FRIENDS))
        var pagerAdapter=FragmentViewPagerAdapter(requireActivity(),requireContext())
        pagerAdapter.addFragments(list)
        binding.viewPager.adapter=pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
          if (position == 0) tab.text = "Friend Requests" else if (position == 1) {
                tab.text = "Suggestions"
            }else if (position == 2) {
              tab.text = "My Friends"
          }
        }.attach()
    }

    override fun onClickOnUpdateFriendRequest(friend: User, status: String) {

    }

    override fun onClickOnProfile(friend: User) {

    }
}