package com.stalmate.user.view.profile

import android.accounts.Account
import android.accounts.AccountManager
import android.app.ActivityOptions
import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.google.android.material.shape.CornerFamily
import com.google.android.material.tabs.TabLayout
import com.stalmate.user.intentHelper.IntentHelper
import com.stalmate.user.R
import com.stalmate.user.base.BaseActivity
import com.stalmate.user.commonadapters.AdapterFeed
import com.stalmate.user.databinding.ActivityProfileBinding
import com.stalmate.user.model.AboutProfileLine
import com.stalmate.user.model.User
import com.stalmate.user.utilities.Constants
import com.stalmate.user.utilities.ImageLoaderHelperGlide
import com.stalmate.user.utilities.PrefManager
import com.stalmate.user.utilities.ValidationHelper
import com.stalmate.user.view.adapter.ProfileAboutAdapter
import com.stalmate.user.view.adapter.ProfileFriendAdapter
import com.stalmate.user.view.dashboard.funtime.ResultFuntime
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File


class ActivityProfile : BaseActivity(), AdapterFeed.Callbackk, ProfileFriendAdapter.Callbackk,
    ProfileAboutAdapter.Callbackk {

    lateinit var binding: ActivityProfileBinding
    lateinit var friendAdapter: ProfileFriendAdapter

    val PICK_IMAGE_PROFILE = 1
    val PICK_IMAGE_COVER = 1
    var imageFile: File? = null
    var isCoverImage = false
    lateinit var userData: User
    var albumTabType = ""
    private lateinit var mAccount: Account
    private lateinit var albumImageAdapter: ProfileAlbumImageAdapter
    private lateinit var albumAdapter: SelfProfileAlbumAdapter


    override fun onClick(viewId: Int, view: View?) {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile)


        binding.layout.buttonEditProfile.visibility = View.VISIBLE


        val radius = resources.getDimension(R.dimen.dp_10)

        binding.ivBackground.shapeAppearanceModel = binding.ivBackground.shapeAppearanceModel
            .toBuilder()
            .setBottomLeftCorner(CornerFamily.ROUNDED, radius)
            .setBottomRightCorner(CornerFamily.ROUNDED, radius)
            .build()
        getUserProfileData()

        friendAdapter = ProfileFriendAdapter(networkViewModel, this, this)
        binding.layout.rvFriends.adapter = friendAdapter
        binding.layout.rvFriends.setNestedScrollingEnabled(false);
        binding.layout.rvFriends.layoutManager = GridLayoutManager(this, 3)

        var hashmap = HashMap<String, String>()
        hashmap.put("type", Constants.TYPE_PROFILE_FRIENDS)
        hashmap.put("sub_type", "")
        hashmap.put("search", "")
        hashmap.put("page", "1")
        hashmap.put("limit", "6")
        networkViewModel.getFriendList(prefManager?.access_token.toString(), hashmap)
        networkViewModel.friendLiveData.observe(this, Observer {
            it.let {
                friendAdapter.submitList(it!!.results)
            }
        })

        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.syncContacts.setOnClickListener {
            //startActivity(IntentHelper.getSyncContactsScreen(this))
            //retreiveGoogleContacts()
            Toast.makeText(this, "sdfsffdasfsf", Toast.LENGTH_SHORT).show()
        }
        binding.layout.tvAlbumPhotoSeeMore.setOnClickListener {
            if (binding.layout.photoTab.selectedTabPosition == 0) {
                startActivity(
                    IntentHelper.getPhotoGalleryAlbumScreen(this)!!
                        .putExtra("viewType", "viewNormal").putExtra("type", "photos")
                )
            } else {
                startActivity(
                    IntentHelper.getPhotoGalleryAlbumScreen(this)!!
                        .putExtra("viewType", "viewNormal").putExtra("type", "albums")
                )
            }
        }

        binding.layout.photoTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val position = tab!!.position
                if (position == 0) {
                    setUpAboutUI("Photos")
                } else if (position == 1) {
                    setUpAboutUI("Albums")
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })
        setupData()
    }


    override fun onResume() {
        super.onResume()
        getUserProfileData()
    }

    fun removeAccount() {
        // Get an instance of the Android account manager
        val accountManager = this.getSystemService(
            ACCOUNT_SERVICE
        ) as AccountManager


        if (isAccountAdded()) {

            var acc = Account(Constants.ACCOUNT_NAME, Constants.ACCOUNT_TYPE)
            accountManager.removeAccountExplicitly(acc)
        }
    }

    fun isAccountAdded(): Boolean {

        // Get an instance of the Android account manager
        val accountManager = this.getSystemService(ACCOUNT_SERVICE) as AccountManager

        for (i in 0 until accountManager.accounts.size) {
            if (accountManager.accounts[i].type == Constants.ACCOUNT_TYPE) {
                return true

            }
        }
        return false
    }


    fun setupData() {
        binding.layout.layoutFollowers.setOnClickListener {
            startActivity(
                IntentHelper.getFollowersFollowingScreen(this)!!.putExtra("id", userData.id)
                    .putExtra("type", Constants.TYPE_USER_TYPE_FOLLOWERS)
            )
        }
        binding.layout.layoutFollowing.setOnClickListener {
            startActivity(
                IntentHelper.getFollowersFollowingScreen(this)!!.putExtra("id", userData.id)
                    .putExtra("type", Constants.TYPE_USER_TYPE_FOLLOWINGS)
            )
        }

        binding.idCoverPhoto.setOnClickListener {
            isCoverImage = true
            startCrop()
        }

        binding.idCameraProfile.setOnClickListener {
            isCoverImage = false
            startCrop()
        }

        binding.layout.btnphoto.setOnClickListener {
            startActivity(
                IntentHelper.getPhotoGalleryAlbumScreen(this)!!.putExtra("viewType", "viewNormal")
            )
        }

        binding.layout.buttonEditProfile.setOnClickListener {

            // create an options object that defines the transition
            val options = ActivityOptions.makeSceneTransitionAnimation(
                this,
                binding.layoutChangeBackgroundImage,
                "image"
            )
            // start the activity with transition
            startActivity(IntentHelper.getProfileEditScreen(this), options.toBundle())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var filePath: String? = ""
        if (requestCode == PICK_IMAGE_PROFILE && resultCode == RESULT_OK) {
        } else if (requestCode == PICK_IMAGE_COVER && resultCode == RESULT_OK) {
        }
    }

    /*Cover Image Picker */
    private val cropImage =
        (this as ComponentActivity).registerForActivityResult(CropImageContract()) { result ->
            if (result.isSuccessful) {
                // use the returned uri
                val uriContent = result.uriContent
                var uriFilePath = result.getUriFilePath(this) // optional usage
                imageFile = File(result.getUriFilePath(this, true))
                Log.d("imageUrl======", uriContent.toString())
                Log.d("imageUrl======", uriFilePath.toString())

                /*if (isCoverImage) {
                    Glide.with(this).load(uriContent).into(binding.ivBackground)
                } else {
                    Glide.with(this).load(uriContent).into(binding.ivUserThumb)
                }*/

                updateProfileImageApiHit()

            } else {
                // an error occurred
                val exception = result.error
            }
        }

    private fun startCrop() {
        // start picker to get image for cropping and then use the image in cropping activity
        cropImage.launch(
            options {
                setGuidelines(CropImageView.Guidelines.ON)
            }
        )
    }


    override fun onClickOnProfile(user: User) {
        startActivity(IntentHelper.getOtherUserProfileScreen(this)!!.putExtra("id", user.id))
    }

    override fun onClickOnViewComments(postId: Int) {

    }

    override fun onCLickItem(item: ResultFuntime) {

    }

    override fun onCLickUserProfile(item: String) {
        startActivity(IntentHelper.getOtherUserProfileScreen(this)!!.putExtra("id", item))
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

    private fun updateProfileImageApiHit() {
        val thumbnailBody: RequestBody =
            RequestBody.create("image/*".toMediaTypeOrNull(), imageFile!!)
        val profile_image1: MultipartBody.Part = MultipartBody.Part.Companion.createFormData(
            "cover_img".takeIf { isCoverImage } ?: "profile_img",
            imageFile!!.name,
            thumbnailBody
        ) //image[] for multiple image

        networkViewModel.etsProfileApi(prefManager?.access_token.toString(), profile_image1)
        networkViewModel.UpdateProfileLiveData.observe(this, Observer {
            it.let {
                makeToast(it!!.message)
                getUserProfileData()
            }
        })
    }

    private fun getUserProfileData() {
        val hashMap = HashMap<String, String>()
        networkViewModel.getProfileData(hashMap, prefManager?.access_token.toString())
        networkViewModel.profileLiveData.observe(this, Observer {
            it?.let {
                userData = it.results!!
                setUpAboutUI("Photos")
                PrefManager.getInstance(this)!!.userProfileDetail = it
            }
        })
    }


    fun setUpAboutUI(tabType: String) {

        Log.d("ajkbcb", tabType)

        if (userData.about!!.isEmpty()) {
            binding.tvUserAbout.visibility = View.GONE
        }

        binding.tvUserName.text = userData.first_name + " " + userData.last_name
        binding.layout.tvFollowerCount.text = userData.follower_count.toString()
        binding.layout.tvFollowingCount.text = userData.following_count.toString()
        binding.tvUserAbout.text = userData.about
        binding.layout.tvFriendCount.text = userData.friends_count.toString()
        ImageLoaderHelperGlide.setGlide(
            this,
            binding.ivBackground,
            userData.cover_img1,
            R.drawable.user_placeholder
        )
        //   Glide.with(this).load(userData.img_url+userData.profile_img1).into(binding.ivUserThumb)
        ImageLoaderHelperGlide.setGlide(
            this,
            binding.ivUserThumb,
            userData.profile_img1,
            R.drawable.user_placeholder
        )
        var aboutArrayList = ArrayList<AboutProfileLine>()

        if (tabType == "Photos") {
            albumImageAdapter = ProfileAlbumImageAdapter(networkViewModel, this, "")
            binding.layout.rvPhotoAlbumData.adapter = albumImageAdapter
            userData.photos?.let { albumImageAdapter.submitList(it) }
        } else if (tabType == "Albums") {
            albumAdapter = SelfProfileAlbumAdapter(networkViewModel, this, "")
            binding.layout.rvPhotoAlbumData.adapter = albumAdapter
            userData.albums?.let { albumAdapter.submitList(it) }

        }

        if (!userData.profileData()?.profession.isNullOrEmpty()) {
            aboutArrayList.add(
                AboutProfileLine(
                    R.drawable.ic_profile_job,
                    userData.profileData()?.profession?.get(0)?.designation ?: "",
                    userData.profileData()?.profession?.get(0)?.company_name ?: "",
                    "at"
                )
            )
        }

        if (!userData.profileData()?.education.isNullOrEmpty()) {
            aboutArrayList.add(
                AboutProfileLine(
                    R.drawable.ic_profile_graduation,
                    "Student",
                    userData.profileData()?.education?.get(0)?.sehool ?: "",
                    "at"
                )
            )
        }

        aboutArrayList.add(
            AboutProfileLine(
                R.drawable.ic_profile_location,
                "From",
                userData.profileData()?.home_town.toString(),
                ""
            )
        )

        aboutArrayList.add(
            AboutProfileLine(
                R.drawable.ic_profile_status,
                "",
                userData.profileData()?.marital_status.toString(),
                ""
            )
        )

        binding.layout.rvAbout.layoutManager = LinearLayoutManager(this)
        var profileAboutAdapter = ProfileAboutAdapter(networkViewModel, this, this)
        profileAboutAdapter.submitList(aboutArrayList)
        binding.layout.rvAbout.adapter = profileAboutAdapter

        if (!ValidationHelper.isNull(userData.company)) {
            binding.layout.tvWebsite.text = userData.company
            binding.layout.layoutWebsite.visibility = View.VISIBLE
        }
    }


    override fun onDestroy() {

        super.onDestroy()
    }
}