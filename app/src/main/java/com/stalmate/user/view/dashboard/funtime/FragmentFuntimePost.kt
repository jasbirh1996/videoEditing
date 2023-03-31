package com.stalmate.user.view.dashboard.funtime

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.stalmate.user.R
import com.stalmate.user.base.BaseFragment
import com.stalmate.user.databinding.FragmentFuntimePostBinding
import com.stalmate.user.model.User
import com.stalmate.user.modules.reels.activity.EXTRA_SONG_ID
import com.stalmate.user.utilities.Constants
import com.stalmate.user.utilities.ValidationHelper
import com.stalmate.user.view.adapter.FriendAdapter
import com.stalmate.user.view.dashboard.ActivityDashboardNew
import com.stalmate.user.view.dashboard.funtime.viewmodel.TagPeopleViewModel
import com.stalmate.user.view.singlesearch.ActivitySingleSearch
import ly.img.android.pesdk.ui.model.state.UiConfigAudio
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.stream.Collectors

class FragmentFuntimePost : BaseFragment(), FriendAdapter.Callbackk {
    lateinit var binding: FragmentFuntimePostBinding
    var taggedPeople = ArrayList<User>()
    lateinit var tagPeopleViewModel: TagPeopleViewModel
    var mVideo = ""
    var city = ""
    var selectedPrivacy = "Public"
    var country = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        if (!::binding.isInitialized) {
            binding = DataBindingUtil.bind<FragmentFuntimePostBinding>(
                inflater.inflate(
                    R.layout.fragment_funtime_post,
                    container,
                    false
                )
            )!!
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("asdasdgkn", "xxxxxx")
        tagPeopleViewModel =
            ViewModelProvider(requireActivity()).get(TagPeopleViewModel::class.java)

        mVideo = (requireActivity() as ActivityFuntimePost).videoUri.toString()

        if ((requireActivity() as ActivityFuntimePost).isEdit) {
            Log.d("aklsjdasd", "oooooo")
            var funtime = (requireActivity() as ActivityFuntimePost).funtime
            Log.d("aklsjdasd", funtime.tag_user.size.toString())
            mVideo = funtime.file
            binding.editor.html = funtime.text
            binding.buttonPost.text = "Ok"
        }



        binding.toolbar.tvhead.text = ""
        binding.toolbar.topAppBar.setNavigationOnClickListener {
            requireActivity().finish()

        }
        setUpEditorbuttons()
        updateButtons()
        try {
            Log.d("asdasdasd", "hjkjh")
            Log.d("asdasdasd", requireActivity().intent.getStringExtra(EXTRA_SONG_ID).toString())
        } catch (e: java.lang.Exception) {

        }
        binding.layoutTagPeople.setOnClickListener { findNavController().navigate(R.id.action_fragmentFuntimePost_to_fragmentFuntimeTag) }
/*
        val bitmap = ThumbnailUtils.createVideoThumbnail(mVideo, MediaStore.Video.Thumbnails.MICRO_KIND)
        Glide.with(requireActivity()).load(bitmap).into(binding.thumbnail)
*/

        if (mVideo.endsWith("jpg", true) || mVideo.endsWith("jpeg", true) || mVideo.endsWith(
                "png",
                true
            )
        ) {
            Glide.with(requireContext())
                .load(File(mVideo))
                .placeholder(R.drawable.profileplaceholder)
                .error(R.drawable.profileplaceholder)
                .into(binding.thumbnail);
        } else {
            Glide.with(requireContext())
                .load(mVideo)
                .thumbnail(Glide.with(requireContext()).load(mVideo))
                .placeholder(R.drawable.profileplaceholder)
                .error(R.drawable.profileplaceholder)
                .into(binding.thumbnail);
        }

        binding.buttonPost.setOnClickListener {
            if ((requireContext() as ActivityFuntimePost).isEdit) {
                editPost()
            } else {
                apiPostReel(File(mVideo))
            }
        }

        tagPeopleViewModel.tagModelLiveData.observe(viewLifecycleOwner) {
            if (it.taggedPeopleList.isNotEmpty()) {
                binding.tvPeopleCount.text = it.taggedPeopleList.size.toString() + " People"
                binding.tvPeopleCount.visibility = View.VISIBLE
                taggedPeople = it.taggedPeopleList
            } else {
                binding.tvPeopleCount.visibility = View.GONE
            }

            setPolicyOnUi(it.policy)


        }

        binding.layoutAddLocation.setOnClickListener {
            var intentt = Intent(requireContext(), ActivitySingleSearch::class.java)
            intentt.putExtra("type", "autoCompleteCountries")
            startActivityForResult(intentt, 121)
        }

        binding.layoutPrivacy.setOnClickListener {
            setFragmentResultListener(SELECT_PRIVACY) { key, bundle ->
                clearFragmentResultListener(requestKey = SELECT_PRIVACY)
                selectedPrivacy = bundle.getString(SELECT_PRIVACY) as String
                setPolicyOnUi(selectedPrivacy)


            }
            findNavController().navigate(R.id.action_fragmentFuntimePost_to_FragmentFuntimePrivacyOptions)
        }

    }


    fun setPolicyOnUi(selectedPrivacyParameter: String) {
        when (selectedPrivacyParameter) {
            Constants.PRIVACY_TYPE_MY_FOLLOWER -> {
                binding.tvPrivacyData.text = "My Followers"

            }
            Constants.PRIVACY_TYPE_PRIVATE -> {
                binding.tvPrivacyData.text = "Private"
            }
            Constants.PRIVACY_TYPE_PUBLIC -> {
                binding.tvPrivacyData.text = "Public"
            }
            Constants.PRIVACY_TYPE_SPECIFIC -> {
                binding.tvPrivacyData.text = "Specific Friends"
            }

        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 121) {
            city = data!!.getSerializableExtra("city").toString()
            country = data.getSerializableExtra("country").toString()
            binding.tvAddLocation.text = city + "," + country
        }
    }


    override fun onClickOnUpdateFriendRequest(friend: User, status: String) {

    }

    override fun onClickOnProfile(friend: User) {

    }

/*    override fun onDestroy() {
        super.onDestroy()
        mPlayer!!.stop(true)
        mPlayer!!.playWhenReady = false
        mPlayer!!.release()
        mPlayer = null
    }*/


    private fun apiPostReel(file: File) {
        showLoader()
        var commaSeparatedStr = ""
        if (taggedPeople.isNotEmpty()) {
            commaSeparatedStr = taggedPeople
                .stream()
                .map {
                    it.id
                }
                .collect(Collectors.joining(","))
        }

        fun getRequestBody(str: String?): RequestBody =
            RequestBody.create("text/plain".toMediaTypeOrNull(), str.toString())

        val thumbnailBody: RequestBody = file.asRequestBody("video/*".toMediaTypeOrNull())
        val profile_image1: MultipartBody.Part = MultipartBody.Part.Companion.createFormData(
            "file",
            file.name,
            thumbnailBody
        ) //image[] for multiple image
        var data = ""
        if (!ValidationHelper.isNull(binding.editor.html)) {
            data = binding.editor.html.toString()
        }
        networkViewModel.postReel(
            file = profile_image1,
            cover_image = null,
            file_type = getRequestBody(".mp4"),
            text = getRequestBody(data),
            tag_id = getRequestBody(commaSeparatedStr),
            sound_id = getRequestBody(
                requireActivity().intent.getStringExtra(EXTRA_SONG_ID).toString()
            ),
            location = getRequestBody("$city, $country"),
            privacy = getRequestBody(selectedPrivacy),
            privacy_data = getRequestBody(""),
            deviceId = getRequestBody(""),
            deviceToken = getRequestBody("")
        )
        networkViewModel.postReelLiveData.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            it.let {
                dismissLoader()
                if (it?.status == true) {
                    val intent = Intent(context, ActivityDashboardNew::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    requireContext().startActivity(intent)
                    (context as Activity).finishAffinity()
                }
            }
        })
    }

    private var isBoldActive = false
    private var isItalicActive = false
    private var isUnderlineActive = false

    private fun setUpEditorbuttons() {
        binding.ivBold.setOnClickListener {
            isBoldActive = !isBoldActive
            updateButtons()
            binding.editor.setBold()
        }
        binding.ivItalic.setOnClickListener {
            isItalicActive = !isItalicActive
            updateButtons()
            binding.editor.setItalic()
        }
        binding.ivUnderLine.setOnClickListener {
            isUnderlineActive = !isUnderlineActive
            updateButtons()
            binding.editor.setUnderline()
        }
    }

    fun updateButtons() {

        if (isBoldActive) {
            binding.ivBold.setImageDrawable(
                ContextCompat.getDrawable(
                    requireActivity(),
                    R.drawable.ic_post_bold
                )
            )
        } else {
            binding.ivBold.setImageDrawable(
                ContextCompat.getDrawable(
                    requireActivity(),
                    R.drawable.ic_post_bold_gray
                )
            )
        }

        if (isItalicActive) {
            binding.ivItalic.setImageDrawable(
                ContextCompat.getDrawable(
                    requireActivity(),
                    R.drawable.ic_post_ittalic_blue
                )
            )
        } else {
            binding.ivItalic.setImageDrawable(
                ContextCompat.getDrawable(
                    requireActivity(),
                    R.drawable.ic_post_ittalic
                )
            )
        }

        if (isUnderlineActive) {
            binding.ivUnderLine.setImageDrawable(
                ContextCompat.getDrawable(
                    requireActivity(),
                    R.drawable.ic_post_underline_blue
                )
            )
        } else {
            binding.ivUnderLine.setImageDrawable(
                ContextCompat.getDrawable(
                    requireActivity(),
                    R.drawable.ic_post_underline
                )
            )
        }

    }


    private fun editPost() {
        val hashmap = HashMap<String, String>()
        hashmap.put("id", (requireActivity() as ActivityFuntimePost).funtime.id)
        hashmap.put("is_delete", "0")
        var data = ""
        if (!ValidationHelper.isNull(binding.editor.html)) {
            data = binding.editor.html.toString()
        }
        hashmap.put("text", data)
        networkViewModel.funtimUpdate(hashmap)
        networkViewModel.funtimeUpdateLiveData.observe(viewLifecycleOwner, Observer {
            it.let {
                if (it!!.status) {
                    val intent = Intent(context, ActivityDashboardNew::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    requireContext().startActivity(intent)
                    (context as Activity).finishAffinity()
                }
            }
        })
    }
}

const val SELECT_PRIVACY = "type"