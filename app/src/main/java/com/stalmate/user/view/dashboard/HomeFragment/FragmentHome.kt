package com.stalmate.user.view.dashboard.HomeFragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anggrayudi.storage.file.forceDelete
import com.bumptech.glide.Glide
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.stalmate.user.intentHelper.IntentHelper
import com.stalmate.user.R
import com.stalmate.user.base.App
import com.stalmate.user.base.BaseFragment
import com.stalmate.user.commonadapters.AdapterFeed
import com.stalmate.user.databinding.FragmentHomeNewBinding
import com.stalmate.user.model.Comment
import com.stalmate.user.model.Feed
import com.stalmate.user.model.User
import com.stalmate.user.modules.reels.player.VideoAutoPlayHelper
import com.stalmate.user.modules.reels.player.holders.VideoReelViewHolder
import com.stalmate.user.modules.reels.utils.RealPathUtil
import com.stalmate.user.networking.ApiInterface
import com.stalmate.user.utilities.Constants
import com.stalmate.user.utilities.NetworkUtils
import com.stalmate.user.utilities.PrefManager
import com.stalmate.user.view.adapter.SuggestedFriendAdapter
import com.stalmate.user.view.adapter.UserHomeStoryAdapter
import com.stalmate.user.view.dashboard.funtime.ActivityFuntimePost
import com.stalmate.user.view.dashboard.funtime.ResultFuntime
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.*
import kotlin.collections.HashMap

//getOtherUserProfileScreen

class FragmentHome(var callback: Callback? = null) : BaseFragment(),
    UserHomeStoryAdapter.Callbackk, SuggestedFriendAdapter.Callbackk {

    private var binding: FragmentHomeNewBinding? = null
    lateinit var feedAdapter: AdapterFeed
    lateinit var homeStoryAdapter: UserHomeStoryAdapter
    lateinit var suggestedFriendAdapter: SuggestedFriendAdapter
    var commentImagePosition = -1
    var videoAutoPlayHelper: VideoAutoPlayHelper? = null

    public interface Callback {
        fun onCLickOnMenuButton()
        fun onCLickOnProfileButton()
        fun onScoll(toHide: Boolean)
    }

    private var fragmentView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        if ((binding == null) && (fragmentView == null)) {
            // Inflate the layout for this fragment and store the view
            val view = inflater.inflate(R.layout.fragment_home_new, container, false)
            binding = DataBindingUtil.bind(view)
            fragmentView = binding?.root
        }
        // Return the view, whether it's newly created or the existing one
        return fragmentView!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeSetUp()
        binding?.refreshLayout?.setOnRefreshListener {
            binding?.refreshLayout?.isRefreshing = false
            if (isNetworkAvailable()) {
                isFirstApiHit = true
                page_count = 1
                callApi()
            } else {
                binding?.nointernet?.visibility = View.VISIBLE
            }
        }
        /*binding?.nestedScrollview.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (oldScrollY < scrollY) {//increase
                callback.onScoll(true)
            } else {
                callback.onScoll(false)
            }
        })*/
    }

    fun follow(feed: ResultFuntime) {
        val hashMap = HashMap<String, String>()
        hashMap.put("id_user", feed.user_id.toString())
        networkViewModel.sendFollowRequest(
            PrefManager.getInstance(App.getInstance())?.userDetail?.results?.access_token.toString(),
            hashMap
        )
        networkViewModel.followRequestLiveData.observe(this, Observer {
            it.let {
                Toast.makeText(this.requireContext(), "Success!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun likeApiHit(funtime: ResultFuntime) {
        var hashmap = HashMap<String, String>()
        hashmap.put("funtime_id", funtime.id.toString())
        networkViewModel.funtimeLiveLikeUnlikeData(prefManager?.access_token.toString(), hashmap)
        networkViewModel.funtimeLiveLikeUnlikeData.observe(this) {
            it.let {
                if (it?.status == true) {
                    feedAdapter.likeReelById()
                }
            }
        }
    }

    private var loading = true
    var pastVisiblesItems = 0
    var visibleItemCount: kotlin.Int = 0
    var totalItemCount: kotlin.Int = 0
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

    fun addComment(comment: String, feed: ResultFuntime) {
        //fromCameraCoverUri
        val images = try {
            if (!fromCameraCoverUri.isNullOrEmpty()) {
                File(
                    (if (fromCameraCoverUri?.contains("file://", true) == true) {
                        RealPathUtil.getRealPath(
                            this.requireActivity(),
                            fromCameraCoverUri.toString().toUri()
                        )
                    } else {
                        fromCameraCoverUri
                    }).toString()
                ).getMultipartBody(
                    keyName = "images",
                    type = "image/*"
                )
            } else
                null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        networkViewModel.addComment1(
            access_token = prefManager?.access_token.toString(),
            funtime_id = feed.id.toString().getRequestBody(),
            comment = comment.getRequestBody(),
            images = images
        )
        networkViewModel.addCommentLiveData.observe(this) {
            it.let {
                if (isNetworkAvailable()) {
                    isFirstApiHit = true
                    page_count = 1
                    callApi()
                } else {
                    binding?.nointernet?.visibility = View.VISIBLE
                }

                fromCameraCover = null
                fromCameraCoverUri = ""
                if (it!!.status) {
                    Toast.makeText(
                        this.requireContext(),
                        "Commented successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                    val hashmap = HashMap<String, String>()
                    hashmap.put("page", page_count.toString())
                    hashmap.put("id_user", "")
                    hashmap.put("fun_id", "")
                    hashmap.put("limit", "5")
                }
            }
        }
    }

    private var fromCameraCover: File? = null
    private var fromCameraCoverUri: String? = ""
    private var launchActivityForImageCaptureFromCamera =
        registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    // start picker to get image for cropping and then use the image in cropping activity
                    fromCameraCoverUri?.toUri()?.let {
                        cropImage.launch(
                            options(
                                uri = fromCameraCoverUri?.toUri(),
                                builder = {
                                    this.setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                                })
                        )
                    }
                }
                Activity.RESULT_CANCELED -> {
                    // User Cancelled the action
                }
                else -> {
                    // Error
                }
            }
        }

    /*Cover Image Picker */
    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            // use the returned uri
            val uriContent = result.uriContent
            val uriFilePath = result.getUriFilePath(requireContext()) // optional usage
            val imageFile = File(result.getUriFilePath(requireContext(), true)!!)
            Log.d("imageUrl======", uriContent.toString())
            Log.d("imageUrl======", uriFilePath.toString())
            fromCameraCoverUri = Uri.fromFile(imageFile).toString()
            if (commentImagePosition != -1) {
                if (commentImagePosition < feedAdapter.list.size) {
                    if (!feedAdapter.list.get(commentImagePosition).topcomment.isNullOrEmpty()) {
                        feedAdapter.list.get(commentImagePosition).topcomment?.get(0)?.new_comment_image =
                            fromCameraCoverUri.toString()
                        feedAdapter.notifyDataSetChanged()
                    } else {
                        feedAdapter.list.get(commentImagePosition).topcomment?.clear()
                        feedAdapter.list.get(commentImagePosition).topcomment?.add(
                            ResultFuntime.TopComment(
                                comment = "",
                                comment_image = "",
                                new_comment_image = fromCameraCoverUri.toString(),
                                comment_id = "",
                                Created_date = "",
                                Updated_date = "",
                                is_delete = "",
                                _id = "",
                                funtime_id = "",
                                user_id = null,
                                __v = ""
                            )
                        )
                        feedAdapter.notifyDataSetChanged()
                    }
                }
            }
        } else {
            // an error occurred
            val exception = result.error
        }
    }

    private fun homeSetUp() {
        setupSearchBox()
        homeStoryAdapter = UserHomeStoryAdapter(networkViewModel, requireContext(), this)
        binding?.shimmerViewContainer?.startShimmer()
        binding?.rvStory?.adapter = homeStoryAdapter

        feedAdapter = AdapterFeed(
            childFragmentManager,
            networkViewModel,
            requireContext(),
            requireActivity(),
            object : AdapterFeed.Callbackk {
                override fun onClickOnViewComments(postId: Int) {

                }

                override fun onCLickItem(item: ResultFuntime) {
                    startActivity(
                        IntentHelper.getFullViewReelActivity(context)!!.putExtra("data", item)
                    )
                }

                override fun onCLickUserProfile(item: String) {
                    startActivity(
                        IntentHelper.getOtherUserProfileScreen(this@FragmentHome.requireContext())!!
                            .putExtra("id", item)
                    )
                }

                override fun onClickOnLikeButtonReel(feed: ResultFuntime) {
                    likeApiHit(feed)
                }

                override fun onClickOnFollowButtonReel(feed: ResultFuntime) {
                    follow(feed)
                }

                override fun onSendComment(feed: ResultFuntime, comment: String) {
                    addComment(comment, feed)
                }

                override fun onCaptureImage(feed: ResultFuntime, position: Int) {
                    commentImagePosition = position
                    if (commentImagePosition != -1) {
                        if (fromCameraCover != null)
                            fromCameraCover?.forceDelete()
                        val now = DateFormat.format("yyyy_MM_dd_hh_mm_ss", Date())
                        fromCameraCover = File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/Stalmate"),
                            "image_$now.jpg"
                        )
                        fromCameraCoverUri =
                            FileProvider.getUriForFile(
                                this@FragmentHome.requireContext().applicationContext,
                                "${this@FragmentHome.requireContext().applicationContext.packageName}.provider",
                                fromCameraCover!!
                            ).toString()
                        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        cameraIntent.putExtra(
                            MediaStore.EXTRA_OUTPUT,
                            fromCameraCoverUri?.toUri()
                        )
                        launchActivityForImageCaptureFromCamera.launch(cameraIntent)
                    } else {
                        fromCameraCover = null
                        fromCameraCoverUri = ""
                    }
                }

                override fun showCommentOverlay(feed: ResultFuntime, position: Int) {
                    try {
                        if ((videoAutoPlayHelper != null)) {
                            binding?.rvFeeds?.postDelayed({
                                val viewholder =
                                    binding?.rvFeeds?.findViewHolderForAdapterPosition(
                                        videoAutoPlayHelper!!.currentPlayingVideoItemPos
                                    );
                                if (viewholder != null) {
                                    val viewMainHolder = (viewholder as AdapterFeed.FeedViewHolder)
                                    if (viewMainHolder.isVideo && viewMainHolder.customPlayerView.getPlayer()?.isPlaying == true)
                                        viewMainHolder.customPlayerView.removePlayer()
                                }
                            }, 500)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun hideCommentOverlay(feed: ResultFuntime, position: Int) {
                    try {
                        getUserProfileData()
                        if ((videoAutoPlayHelper != null)) {
                            binding?.rvFeeds?.postDelayed({
                                val viewholder =
                                    binding?.rvFeeds?.findViewHolderForAdapterPosition(
                                        videoAutoPlayHelper!!.currentPlayingVideoItemPos
                                    );
                                if (viewholder != null) {
                                    val viewMainHolder = (viewholder as AdapterFeed.FeedViewHolder)
                                    if (viewMainHolder.isVideo && viewMainHolder.customPlayerView.getPlayer()?.isPlaying == false)
                                        viewMainHolder.customPlayerView.startPlaying()
                                }
                            }, 500)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
        binding?.rvFeeds?.adapter = feedAdapter
        (binding?.rvFeeds?.adapter as AdapterFeed).stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        binding?.shimmerLayoutFeeds?.startShimmer()
        /*Helper class to provide AutoPlay feature inside cell*/
        if (videoAutoPlayHelper == null) {
            binding?.rvFeeds?.let {
                videoAutoPlayHelper = VideoAutoPlayHelper(recyclerView = it)
                videoAutoPlayHelper?.startObserving()
            }
        }

        binding?.rvFeeds?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) { //check for scroll down
                    callback?.onScoll(true)
                    visibleItemCount =
                        (binding?.rvFeeds?.layoutManager as LinearLayoutManager).getChildCount()
                    totalItemCount =
                        (binding?.rvFeeds?.layoutManager as LinearLayoutManager).getItemCount()
                    pastVisiblesItems =
                        (binding?.rvFeeds?.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    if (loading) {
                        if (visibleItemCount + pastVisiblesItems >= totalItemCount) {
                            loading = false
                            Log.v("...", "Last Item Wow !")
                            // Do pagination.. i.e. fetch new data
                            if (!isApiRuning) {
                                page_count++
                                callApi()
                            }
                            loading = true
                        }
                    }
                    Handler(Looper.getMainLooper()).postDelayed({
                        binding?.toolbar?.clTool?.visibility = View.GONE
                    }, 500)
                } else {
                    callback?.onScoll(false)
                    Handler(Looper.getMainLooper()).postDelayed({
                        binding?.toolbar?.clTool?.visibility = View.VISIBLE
                    }, 500)
                }
            }
        })


        getFriendSuggestionListing()
        binding?.postContant?.userImage?.setOnClickListener {
            callback?.onCLickOnProfileButton()
        }

        binding?.layoutNewUser?.setOnClickListener {
            IntentHelper.getActivityWelcomeScreen(requireContext())
                ?.let { it1 -> startActivity(it1) }
        }

        binding?.toolbar?.ivButtonMenu?.setOnClickListener {
//            startActivity(Intent(requireContext(), ActivitySideDawer::class.java))
//            callback.onCLickOnMenuButton()
            callback?.onCLickOnProfileButton()
        }

        binding?.nointernet?.visibility = View.GONE
    }

    var page_count = 1
    var isFirstApiHit = true
    var isSelfVideos = false
    var isApiRuning = false
    var handler: Handler? = null
    fun callApi() {
        isApiRuning = true
        val hashmap = HashMap<String, String>()
        hashmap.put("page", page_count.toString())
        hashmap.put("id_user", "")
        hashmap.put("fun_id", "")
        hashmap.put("limit", "50")
        hashmap.put("is_video", "2")
        networkViewModel.funtimeLiveData(prefManager?.access_token.toString(), hashmap)
        networkViewModel.funtimeLiveData.observe(viewLifecycleOwner, Observer {
            /*
            binding?.shimmerViewContainer.stopShimmer()
                binding?.storyView.visibility = View.GONE
                if (!it?.results.isNullOrEmpty())
                    it?.results?.let { it1 -> homeStoryAdapter.submitList(it1) }
            */
            isApiRuning = false
            if (!it?.results.isNullOrEmpty()) {
                binding?.shimmerLayoutFeeds?.stopShimmer()
                binding?.rvFeeds?.visibility = View.VISIBLE
                it?.results?.let {
                    if (isFirstApiHit) {
                        it.forEach {
                            it.isDataUpdated = false
                        }
                        feedAdapter.submitList(it)
                    } else {
                        it.forEach {
                            it.isDataUpdated = false
                        }
                        feedAdapter.addToList(it)
                    }
                }
                isFirstApiHit = false
            }
        })
    }

    override fun onPause() {
        Log.d("#HomeTag", "onPause")
        try {
            if ((videoAutoPlayHelper != null)) {
                binding?.rvFeeds?.postDelayed({
                    val viewholder =
                        binding?.rvFeeds?.findViewHolderForAdapterPosition(videoAutoPlayHelper!!.currentPlayingVideoItemPos);
                    if (viewholder != null) {
                        val viewMainHolder = (viewholder as AdapterFeed.FeedViewHolder)
                        if (viewMainHolder.isVideo && viewMainHolder.customPlayerView.getPlayer()?.isPlaying == true)
                            viewMainHolder.customPlayerView.removePlayer()
                    }
                }, 500)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onPause()
    }

    override fun onResume() {
        Log.d("#HomeTag", "onResume")
        try {
            if (isNetworkAvailable()) {
                isFirstApiHit = true
                page_count = 1
                callApi()
            } else {
                binding?.nointernet?.visibility = View.VISIBLE
            }
            getUserProfileData()
            if ((videoAutoPlayHelper != null)) {
                binding?.rvFeeds?.postDelayed({
                    val viewholder =
                        binding?.rvFeeds?.findViewHolderForAdapterPosition(videoAutoPlayHelper!!.currentPlayingVideoItemPos);
                    if (viewholder != null) {
                        val viewMainHolder = (viewholder as AdapterFeed.FeedViewHolder)
                        if (viewMainHolder.isVideo && viewMainHolder.customPlayerView.getPlayer()?.isPlaying == false)
                            viewMainHolder.customPlayerView.startPlaying()
                    }
                }, 500)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        binding?.toolbar?.ivButtonMenu?.let {
            Glide.with(this).load(prefManager?.profile_img_1.toString())
                .placeholder(R.drawable.user_placeholder).error(R.drawable.user_placeholder)
                .into(it)
        }
        super.onResume()
    }

    private fun getUserProfileData() {
        val hashMap = HashMap<String, String>()
        networkViewModel.getProfileData(hashMap, prefManager?.access_token.toString())
        networkViewModel.profileLiveData.observe(requireActivity(), Observer {
            it.let {
                if (it != null) {
                    PrefManager.getInstance(requireContext())!!.userProfileDetail = it
                }
                binding?.postContant?.userImage?.let { it1 ->
                    Glide.with(requireContext())
                        .load(PrefManager.getInstance(requireContext())?.userProfileDetail?.results?.profile_img1)
                        .placeholder(R.drawable.profileplaceholder).circleCrop()
                        .into(it1)
                }

                binding?.toolbar?.ivButtonMenu?.let { it1 ->
                    Glide.with(requireContext())
                        .load(PrefManager.getInstance(requireContext())?.userProfileDetail?.results?.profile_img1)
                        .placeholder(R.drawable.user_placeholder).error(R.drawable.user_placeholder)
                        .into(it1)
                }

                binding?.postContant?.appCompatEditText?.hint =
                    "${PrefManager.getInstance(requireContext())?.userProfileDetail?.results?.first_name}, What's in your mind?"
            }
        })
    }

    override fun onClickOnProfile(user: Feed) {

    }

    override fun onClickOnUpdateFriendRequest(friend: User, status: String) {

    }

    override fun onClickOnProfile(friend: User) {
        startActivity(
            IntentHelper.getOtherUserProfileScreen(requireContext())!!.putExtra("id", friend.id)
        )
    }

    private fun isNetworkAvailable(): Boolean {
        return NetworkUtils.isNetworkAvailable()
    }

    private fun getFriendSuggestionListing() {
        networkViewModel.getFriendListBody(
            prefManager?.access_token.toString(),
            ApiInterface.UsersListResponse(
                limit = "6",
                page = "1",
                type = Constants.NEW_Type_Friend_List,
                user_id = ""
            )
        )
        networkViewModel.friendLiveData.observe(this.requireActivity(), Observer {
            it.let {
                suggestedFriendAdapter =
                    SuggestedFriendAdapter(networkViewModel, requireContext(), this)
                binding?.rvSuggestedFriends?.adapter = suggestedFriendAdapter
                it?.results?.let { it1 -> suggestedFriendAdapter.submitList(it1) }
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupSearchBox() {
        binding?.toolbar?.ivButtonSearch?.setImageResource(R.drawable.ic_profile_searchbar)
        binding?.toolbar?.ivButtonSearch?.setOnClickListener {
            IntentHelper.getSearchScreen(requireContext())?.let { it1 -> startActivity(it1) }
        }
    }
}