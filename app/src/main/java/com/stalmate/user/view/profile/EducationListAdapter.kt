package com.stalmate.user.view.profile

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.common.util.SharedPreferencesUtils
import com.stalmate.user.R
import com.stalmate.user.databinding.ItemEducationprofileBinding
import com.stalmate.user.databinding.ItemGalleryBinding
import com.stalmate.user.model.*
import com.stalmate.user.view.language.AdapterLanguage
import com.stalmate.user.viewmodel.AppViewModel
import java.util.*
import kotlin.collections.ArrayList

class EducationListAdapter(
    val viewModel: AppViewModel,
    val context: Context,
    var callback: Callbackk
) : RecyclerView.Adapter<EducationListAdapter.AlbumViewHolder>() {

    val list = ArrayList<Education>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EducationListAdapter.AlbumViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_educationprofile, parent, false)
        return AlbumViewHolder(DataBindingUtil.bind(view)!!)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.bind(list.get(position))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun submitList(albumList: ArrayList<Education>) {
        list.clear()
        list.addAll(albumList)
        notifyDataSetChanged()
    }

    public interface Callbackk {
        fun onClickItemEdit(position: Education, index: Int)

    }

    inner class AlbumViewHolder(var binding: ItemEducationprofileBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(response: Education) {
            binding.tveducation.text = response.sehool
            binding.tvcource.text = response.course
            binding.tvcourcetype.text = response.branch

            binding.ivDelete.setOnClickListener {
                onClickItemDelete(
                    response._id,
                    bindingAdapterPosition,
                    (binding.root.context as? LifecycleOwner)!!
                )
            }

            binding.ivedit.setOnClickListener {
                callback.onClickItemEdit(list[bindingAdapterPosition], bindingAdapterPosition)
            }

        }
    }

    fun onClickItemDelete(id: String, position: Int, lifecycleObserver: LifecycleOwner) {
        val hashMap = HashMap<String, String>()

        hashMap["id"] = id
        hashMap["is_delete"] = "1"

        viewModel.educationData(
            (context as ActivityProfileEdit)?.prefManager?.access_token.toString(),hashMap)
        viewModel.educationData.observe(lifecycleObserver) {
            it?.let {
                if (it.status) {
                    list.removeAt(position)
                    notifyItemRemoved(position)
                }
            }
        }
    }
}