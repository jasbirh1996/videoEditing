package com.stalmate.user.view.dashboard.funtime


import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.stalmate.user.R
import com.stalmate.user.base.App
import com.stalmate.user.databinding.ItemCommentBinding
import com.stalmate.user.model.Comment
import com.stalmate.user.modules.reels.utils.RealPathUtil
import com.stalmate.user.utilities.PrefManager
import com.stalmate.user.utilities.TimesAgo2
import com.stalmate.user.viewmodel.AppViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class CommentAdapterNew(
    var context: Context,
    var callback: Callback,
    var networkViewModel: AppViewModel,
    var funTimeId: String,
    var lifecyclerOwner: LifecycleOwner,
    val dialogFragment: DialogFragment,
    val accessToken: String
) : RecyclerView.Adapter<CommentAdapterNew.CommentViewHolder>() {
    var commentList = ArrayList<Comment>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {

        return CommentViewHolder(
            ItemCommentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(commentList[position], accessToken)
    }


    inner class CommentViewHolder(val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root), ChildCommentAdapter.Callback {
        fun bind(shortComment: Comment, accessToken: String) {
            if (shortComment.Created_date.isDigitsOnly())
                binding.tvDate.text = "${TimesAgo2.covertTimeToText(shortComment.Created_date, true)}"
            else
                binding.tvDate.text = "${shortComment.Created_date}"
            binding.tvUserName.text = "${shortComment.first_name} ${shortComment.last_name}"
            binding.tvComment.text = shortComment.comment
            binding.tvLikesCount.setText(shortComment.like_count.toString() + " likes")
            if (!shortComment.comment_image.isNullOrEmpty()) {
                binding.ivCommentImage.visibility = View.VISIBLE
                Glide.with(context).load(shortComment.comment_image).into(binding.ivCommentImage)
            } else
                binding.ivCommentImage.visibility = View.GONE
            Glide.with(context).load(shortComment.profile_img).circleCrop().into(binding.ivUserImage)
            binding.tvReply.setOnClickListener {
                callback.onClickOnReply(shortComment, bindingAdapterPosition, 0, false)
            }
            binding.btnMore.setOnClickListener {
                if (shortComment.isExpanded) {
                    shortComment.isExpanded = false
                    binding.btnMore.text = "Hide replies"
                    notifyItemChanged(bindingAdapterPosition)
                } else {
                    shortComment.isExpanded = true
                    //viewReplies(shortComment, bindingAdapterPosition, accessToken)
                    binding.rvChildReplies.visibility = View.VISIBLE
                }
            }

            val commentRepliedAdapter = ChildCommentAdapter(context, this, funTimeId, networkViewModel, lifecyclerOwner)
            commentRepliedAdapter.addToList(shortComment.replies)
            binding.rvChildReplies.adapter = commentRepliedAdapter
            binding.rvChildReplies.visibility = View.GONE

            if (shortComment.replies.isNullOrEmpty()) {
                binding.btnMore.visibility = View.GONE
                binding.view15.visibility = View.GONE
            } else {
                binding.btnMore.visibility = View.VISIBLE
                binding.view15.visibility = View.VISIBLE
                binding.btnMore.text = "Show ${shortComment.replies.size} replies"
                binding.rvChildReplies.visibility = View.GONE
            }

            if (shortComment.isLiked == "Yes") {
                binding.ivHearIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.heart_filled
                    )
                )
            } else {
                binding.ivHearIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_profile_heart_icon
                    )
                )
            }
            binding.tvLikesCount.text = shortComment.like_count.toString() + " likes"
            binding.ivHearIcon.setOnClickListener {
                likeComment(shortComment._id, bindingAdapterPosition)
            }
        }

        override fun onClickOnReply(shortComment: Comment, childPosition: Int) {
            callback.onClickOnReply(shortComment, bindingAdapterPosition, childPosition, true)
        }

        override fun onClickOnViewMoreReply(shortComment: Comment, position: Int) {
            callback.onClickOnViewMoreReply(shortComment, position)
        }
    }


    public interface Callback {
        fun onClickOnReply(
            shortComment: Comment,
            parentPosition: Int,
            childPosition: Int,
            isChild: Boolean
        )
        fun onClickOnViewMoreReply(shortComment: Comment, position: Int)
        fun onCommentAddedSucessfully()
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    fun addToList(comments: List<Comment>) {
        val size = commentList.size
        commentList.addAll(comments)
        val sizeNew = commentList.size
        notifyItemRangeChanged(size, sizeNew)
    }

    fun submitList(comments: List<Comment>) {
        commentList.clear()
        commentList.addAll(comments)
        notifyDataSetChanged()
    }

    fun addComment(data: String, accessToken: String) {
        val fromCameraCoverUri = when (dialogFragment) {
            is DialogFragmentComments -> {
                (dialogFragment as DialogFragmentComments).fromCameraCoverUri
            }
            is DialogFragmentCommentWithVideo -> {
                (dialogFragment as DialogFragmentCommentWithVideo).fromCameraCoverUri
            }
            else -> {
                null
            }
        }
        val images = try {
            if (!fromCameraCoverUri.isNullOrEmpty()) {
                File(
                    if (fromCameraCoverUri?.contains("file://", true) == true) {
                        RealPathUtil.getRealPath(
                            dialogFragment.requireActivity(),
                            fromCameraCoverUri.toString().toUri()
                        )
                    } else {
                        fromCameraCoverUri
                    }
                ).getMultipartBody(
                    keyName = "images",
                    type = "image/*"
                )
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        networkViewModel.addComment1(
            access_token = accessToken,
            funtime_id = funTimeId.getRequestBody(),
            comment = data.getRequestBody(),
            images = images
        )
        networkViewModel.addCommentLiveData.observe(lifecyclerOwner) {
            it.let {
                when (dialogFragment) {
                    is DialogFragmentComments -> {
                        (dialogFragment as DialogFragmentComments).hideImageView()
                    }
                    is DialogFragmentCommentWithVideo -> {
                        (dialogFragment as DialogFragmentCommentWithVideo).hideImageView()
                    }
                }
                if (it!!.status) {
                    it?.results?.replies = ArrayList<Comment>()
                    it?.results?.let { it1 -> commentList.add(it1) }
                    notifyItemInserted(commentList.size - 1)
                    callback.onCommentAddedSucessfully()
                }
            }
        }
    }

    fun replyOverComment(
        data: String,
        commentId: String,
        parentPosition: Int,
        childPosition: Int,
        isReplyisChildComment: Boolean,
        accessToken: String
    ) {
        val fromCameraCoverUri = when (dialogFragment) {
            is DialogFragmentComments -> {
                (dialogFragment as DialogFragmentComments).fromCameraCoverUri
            }
            is DialogFragmentCommentWithVideo -> {
                (dialogFragment as DialogFragmentCommentWithVideo).fromCameraCoverUri
            }
            else -> {
                null
            }
        }
        val images = try {
            if (!fromCameraCoverUri.isNullOrEmpty()) {
                File(
                    if (fromCameraCoverUri?.contains("file://", true) == true) {
                        RealPathUtil.getRealPath(
                            dialogFragment.requireActivity(),
                            fromCameraCoverUri.toString().toUri()
                        )
                    } else {
                        fromCameraCoverUri
                    }
                ).getMultipartBody(
                    keyName = "images",
                    type = "image/*"
                )
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        networkViewModel.addComment(
            access_token = accessToken,
            funtime_id = funTimeId.getRequestBody(),
            comment = data.getRequestBody(),
            id = commentId.getRequestBody(),
            comment_id = commentId.getRequestBody(),
            is_delete = "0".getRequestBody(),
            images = images
        )
        networkViewModel.addCommentLiveData.observe(lifecyclerOwner) {
            it.let {
                when (dialogFragment) {
                    is DialogFragmentComments -> {
                        (dialogFragment as DialogFragmentComments).hideImageView()
                    }
                    is DialogFragmentCommentWithVideo -> {
                        (dialogFragment as DialogFragmentCommentWithVideo).hideImageView()
                    }
                }
                if (it!!.status) {
                    if (isReplyisChildComment) {
                        it?.results?.replies = ArrayList<Comment>()
                        commentList[parentPosition].isExpanded = true
                        commentList[parentPosition].child_count++
                        it?.results?.let { it1 -> commentList[parentPosition].replies.add(it1) }
                        notifyItemChanged(parentPosition)
                    } else {
                        it?.results?.replies = ArrayList<Comment>()
                        commentList[parentPosition].isExpanded = true
                        commentList[parentPosition].child_count++
                        it?.results?.let { it1 -> commentList[parentPosition].replies.add(it1) }
                        notifyItemChanged(parentPosition)
                    }
                    callback.onCommentAddedSucessfully()
                }
            }
        }
    }

    private fun String.getRequestBody(): RequestBody =
        RequestBody.create("text/plain".toMediaTypeOrNull(), this)

    private fun File.getMultipartBody(
        keyName: String,
        type: String
    ): MultipartBody.Part? {
        return try {
            MultipartBody.Part.createFormData(
                keyName,
                this.name,
                this.asRequestBody(type.toMediaTypeOrNull())
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun viewReplies(comment: Comment, position: Int, accessToken: String) {
        val hashmap = HashMap<String, String>()
        hashmap.put("funtime_id", funTimeId)
        hashmap.put("comment_id", comment._id)
        hashmap.put("page", "1")

        networkViewModel.getRepliesList(accessToken, hashmap)
        networkViewModel.repliesLiveData.observe(lifecyclerOwner) { mainIt ->
            if (mainIt?.status == true) {
                mainIt.results.forEach {
                    it.replies = kotlin.collections.ArrayList<Comment>()
                }
                mainIt.results.let { commentList[position].replies.addAll(it) }
                notifyItemChanged(position)
            }
        }
    }

    fun likeComment(commentId: String, position: Int) {
        val hashmap = HashMap<String, String>()
        hashmap.put("funtime_id", funTimeId)
        hashmap.put("comment_id", commentId)
        networkViewModel.likeComment(
            PrefManager.getInstance(App.getInstance())?.userDetail?.results?.access_token.toString(),
            hashmap
        )
        networkViewModel.likeCommentLiveData.observe(lifecyclerOwner) { mainIt ->
            run {
                if (mainIt!!.status) {
                    if (commentList[position].isLiked == "Yes") {
                        commentList[position].isLiked = "No"
                        commentList[position].like_count--
                    } else {
                        commentList[position].isLiked = "Yes"
                        commentList[position].like_count++
                    }
                    notifyItemChanged(position)
                }
            }
        }
    }
}

