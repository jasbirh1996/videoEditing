package com.stalmate.user.view.profile

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.stalmate.user.intentHelper.IntentHelper
import com.stalmate.user.R
import com.stalmate.user.databinding.ItemProfileCoverBinding
import com.stalmate.user.model.Albums

import com.stalmate.user.utilities.ImageLoaderHelperGlide
import com.stalmate.user.viewmodel.AppViewModel

class ProfileAlbumAdapter(val viewModel: AppViewModel, val context: Context,var type:String)
    : RecyclerView.Adapter<ProfileAlbumAdapter.AlbumViewHolder>() {

    var list = ArrayList<Albums>()


    inner class AlbumViewHolder(var binding : ItemProfileCoverBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(response : Albums){
            ImageLoaderHelperGlide.setGlideCorner(context,binding.ivImage,response.files,R.drawable.user_placeholder)
            binding.ivImage.setOnClickListener {
                Log.d("alksjdasd",type)
                context.startActivity(IntentHelper.getPhotoGalleryAlbumScreen(context)!!.putExtra("albumId", type).putExtra("imageId",response.id).putExtra("viewType","viewFullScreen"))
            }
        }
    }

    fun submitList(albumList: List<Albums>) {
        list.clear()
        list.addAll(albumList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.item_profile_cover, parent, false)
        return AlbumViewHolder(DataBindingUtil.bind<ItemProfileCoverBinding>(view)!!)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.bind(list.get(position))
    }

    override fun getItemCount(): Int {
        return list.size
    }
}