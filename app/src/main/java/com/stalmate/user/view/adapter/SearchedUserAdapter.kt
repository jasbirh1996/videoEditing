package com.stalmate.user.view.adapter


import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.stalmate.user.R

import com.stalmate.user.databinding.ItemFriendBinding
import com.stalmate.user.model.ModelGlobalSearch
import com.stalmate.user.model.User
import com.stalmate.user.utilities.Constants
import com.stalmate.user.utilities.Constants.TYPE_ALL_FOLLOWERS_FOLLOWING
import com.stalmate.user.utilities.Constants.TYPE_MY_FRIENDS
import com.stalmate.user.utilities.Constants.TYPE_USER_ACTION_ADD_FRIEND
import com.stalmate.user.utilities.Constants.TYPE_USER_ACTION_FOLLOW
import com.stalmate.user.utilities.ImageLoaderHelperGlide
import com.stalmate.user.utilities.ValidationHelper
import com.stalmate.user.viewmodel.AppViewModel

class SearchedUserAdapter(
    val viewModel: AppViewModel,
    val context: Context,
    var callback: Callbackk,
) :
    RecyclerView.Adapter<SearchedUserAdapter.FeedViewHolder>() {
    var list : ArrayList<ModelGlobalSearch.Reponse?>? = arrayListOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): SearchedUserAdapter.FeedViewHolder {
        var view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_friend, parent, false)
        return FeedViewHolder(DataBindingUtil.bind<ItemFriendBinding>(view)!!)
    }

    override fun onBindViewHolder(holder: SearchedUserAdapter.FeedViewHolder, position: Int) {
        holder.bind(list?.get(position))
    }

    override fun getItemCount(): Int {
        return (list?.size?:0)
    }

    inner class FeedViewHolder(var binding: ItemFriendBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(friend: ModelGlobalSearch.Reponse?) {
            setupViewsForAdapter(binding, friend, bindingAdapterPosition)
        }
    }

    fun addToList(users: ArrayList<ModelGlobalSearch.Reponse?>?) {
        if (users != null) {
            list?.addAll(users)
        }
        notifyDataSetChanged()
    }

    fun submitList(users: ArrayList<ModelGlobalSearch.Reponse?>?) {
        list?.clear()
        list?.addAll(users!!)
        notifyDataSetChanged()
    }

    public interface Callbackk {
        fun onClickOnUpdateFriendRequest(friend: User, status: String)
        fun onClickOnProfile(friend: ModelGlobalSearch.Reponse?)
    }


    fun updateFriendStatus(
        status: String,
        userId: String,
        lifecycleOwner: LifecycleOwner,
        binding: ItemFriendBinding,
        position: Int
    ) {
        var hashMap = HashMap<String, String>()
        hashMap.put("id_user", userId)
        if (status.equals(Constants.TYPE_USER_ACTION_ADD_FRIEND)) {
            viewModel.sendFriendRequest("", hashMap)
            viewModel.sendFriendRequestLiveData.observe(lifecycleOwner, Observer {
                it.let {
                    if (list?.get(position)?.isFriend == 0) {
                        if (!ValidationHelper.isNull(list!![position]?.request_status)) {
                            if (list!![position]?.request_status == Constants.FRIEND_CONNECTION_STATUS_PENDING) {
                                list!![position]?.request_status = ""
                            }
                        } else {
                            list!![position]?.request_status = Constants.FRIEND_CONNECTION_STATUS_PENDING
                        }
                    } else {
                        list?.get(position)?.isFriend = 1
                    }

                    notifyItemChanged(position)
                }
            })
        }


    }


    fun setupViewsForAdapter(
        binding: ItemFriendBinding,
        friend: ModelGlobalSearch.Reponse?,
        bindingAdapterPosition: Int
    ) {


        if (friend?.isFriend == 1) {
            binding.ivFriend.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_botm_menu_chat_inactive
                )
            )
        } else {

            if (friend?.request_status == "Pending") {
                binding.ivFriend.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.user_request_sent
                    )
                )
            } else {
                binding.ivFriend.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.user_add_request
                    )
                )
            }
        }

        binding.ivFriend.setOnClickListener {

            if (friend?.isFriend == 0) {
                updateFriendStatus(
                    TYPE_USER_ACTION_ADD_FRIEND,
                    friend.id.toString(),
                    (binding.root.context as? LifecycleOwner)!!,
                    binding,
                    bindingAdapterPosition
                )
            }


        }

        binding.root.setOnClickListener {
            callback.onClickOnProfile(friend)
        }

        ImageLoaderHelperGlide.setGlideCorner(
            context,
            binding.ivUserImage,
            friend?.img,
            R.drawable.user_placeholder
        )
        binding.tvUserName.text = friend?.first_name + " " + friend?.last_name
        if (friend?.profileData()?.profession?.isNotEmpty() == true) {
            binding.tvLineOne.text = friend?.profileData()?.profession?.get(0)?.designation
            binding.tvLineOne.visibility = View.VISIBLE
        } else if (friend?.profileData()?.education?.isNotEmpty() == true) {
            binding.tvLineOne.text = friend?.profileData()?.education?.get(0)?.sehool
            binding.tvLineOne.visibility = View.VISIBLE
        }

/*        if (friend.mutual_friend.isNotEmpty()){
            binding.tvMutualFirnds.text = friend.mutual_friend
            binding.tvMutualFirnds.visibility = View.VISIBLE
        }*/

        if (!ValidationHelper.isNull(friend?.profileData()?.home_town)) {
            binding.tvLineTwo.text = friend?.profileData()?.home_town
            binding.tvLineTwo.visibility = View.VISIBLE

        }
    }


}