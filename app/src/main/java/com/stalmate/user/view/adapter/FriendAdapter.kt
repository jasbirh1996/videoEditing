package com.stalmate.user.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.stalmate.user.R
import com.stalmate.user.databinding.ItemFriendBinding
import com.stalmate.user.model.Feed
import com.stalmate.user.model.Friend
import com.stalmate.user.viewmodel.AppViewModel

class FriendAdapter(
    val viewModel: AppViewModel,
    val context: Context,
    var callback: Callbackk
) :
    RecyclerView.Adapter<FriendAdapter.FeedViewHolder>(){
    var list = ArrayList<Friend>()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): FriendAdapter.FeedViewHolder {

        var view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_friend, parent, false)
        return FeedViewHolder(DataBindingUtil.bind<ItemFriendBinding>(view)!!)
    }

    override fun onBindViewHolder(holder: FriendAdapter.FeedViewHolder, position: Int) {
        holder.bind(list.get(position))
    }
    override fun getItemCount(): Int {
        return list.size
    }
    inner class FeedViewHolder(var binding: ItemFriendBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(friend: Friend) {
            binding.buttonFollow.setOnClickListener {
              callback.onClickOnUpdateFriendRequest(friend,"Accept")
            }
        }
    }






    fun submitList(feedList: List<Friend>) {
        list.clear()
        list.addAll(feedList)
        notifyDataSetChanged()
    }

    public interface Callbackk {
        fun onClickOnUpdateFriendRequest(friend:Friend,status: String)
    }









}