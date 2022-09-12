package com.stalmate.user.view.photoalbum

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.stalmate.user.R
import com.stalmate.user.databinding.ItemPhotoLayoutBinding
import com.stalmate.user.model.Result
import com.stalmate.user.viewmodel.AppViewModel

class PhotoAdapter(val viewModel: AppViewModel, val context: Context
) : RecyclerView.Adapter<PhotoAdapter.ViewHolder>(){




    var list = ArrayList<ResultImage>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.item_photo_layout, parent, false)
        return ViewHolder(DataBindingUtil.bind<ItemPhotoLayoutBinding>(view)!!)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list.get(position))
    }

    override fun getItemCount(): Int {
       return  list.size
    }


    fun submitList(languageList: List<ResultImage>) {
        list.clear()
        list.addAll(languageList)
        notifyDataSetChanged()
    }

    inner class ViewHolder(var binding : ItemPhotoLayoutBinding): RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("ResourceAsColor", "ResourceType")
        fun bind(feed: ResultImage) {


            Glide.with(context).load(feed.url+feed.files)
                .placeholder(R.drawable.profileplaceholder)
                .into(binding.image)

        }
    }

}