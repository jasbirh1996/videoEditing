package com.stalmate.user.view.dashboard.funtime

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.stalmate.user.R
import com.stalmate.user.base.BaseFragment
import com.stalmate.user.databinding.FragmentFuntimePrivacyOptionsBinding
import com.stalmate.user.utilities.Constants
import com.stalmate.user.view.dashboard.funtime.viewmodel.TagPeopleViewModel
import com.stalmate.user.view.dialogs.CommonConfirmationDialog


class FragmentFuntimePrivacyOptions : BaseFragment() {
    var selectedType=""
    lateinit var tagPeopleViewModel: TagPeopleViewModel
    lateinit var binding: FragmentFuntimePrivacyOptionsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        var view = inflater.inflate(R.layout.fragment_funtime_privacy_options, container, false)
        binding = DataBindingUtil.bind<FragmentFuntimePrivacyOptionsBinding>(view)!!
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tagPeopleViewModel= ViewModelProvider(requireActivity()).get(TagPeopleViewModel::class.java)
        multiUserSelectFragment = FragmentMultiUserSelector(networkViewModel,tagPeopleViewModel)

        tagPeopleViewModel.tagModelLiveData.observe(viewLifecycleOwner) {
            Log.d("aklsdasd",it.policy)
            selectedType=it.policy
            selectedOption(selectedType)
        }
        binding.layoutMyFollower.setOnClickListener {


            if (tagPeopleViewModel.taggedModelObject.taggedPeopleList.size>0){
                var custumConfirmDialog= CommonConfirmationDialog(requireContext(),"","Tagged people removed when you select My Followers, Private or Specific friend","Yes","Cancel",object :CommonConfirmationDialog.Callback{
                    override fun onDialogResult(isPermissionGranted: Boolean) {
                        if (isPermissionGranted){
                            tagPeopleViewModel.clearList()
                            proceed(Constants.PRIVACY_TYPE_MY_FOLLOWER)
                        }
                    }
                })
                custumConfirmDialog.show()
            }else{
                proceed(Constants.PRIVACY_TYPE_MY_FOLLOWER)
            }
        }

        binding.layoutPublic.setOnClickListener {
            proceed(Constants.PRIVACY_TYPE_PUBLIC)
        }

        binding.layoutSpecific.setOnClickListener {
            if (tagPeopleViewModel.taggedModelObject.policy!=Constants.PRIVACY_TYPE_SPECIFIC && tagPeopleViewModel.taggedModelObject.taggedPeopleList.size!=0){
               var custumConfirmDialog= CommonConfirmationDialog(requireContext(),"","Tagged people removed when you select My Followers, Private or Specific friend","Yes","Cancel",object :CommonConfirmationDialog.Callback{
                    override fun onDialogResult(isPermissionGranted: Boolean) {
                        if (isPermissionGranted){
                            tagPeopleViewModel.clearList()
                            openBottomSheetDialog()
                        }
                    }
                })

                custumConfirmDialog.show()
            }else{
                openBottomSheetDialog()
            }

        }
        binding.layoutPrivate.setOnClickListener {
            if (tagPeopleViewModel.taggedModelObject.taggedPeopleList.size>0){
                var custumConfirmDialog= CommonConfirmationDialog(requireContext(),"","Tagged People Removed when you Select Private","Yes","Cancel",object :CommonConfirmationDialog.Callback{
                    override fun onDialogResult(isPermissionGranted: Boolean) {
                        if (isPermissionGranted){
                           tagPeopleViewModel.clearList()
                            tagPeopleViewModel.clearSpecificFriendList()
                            proceed(Constants.PRIVACY_TYPE_PRIVATE)
                        }
                    }
                })
                custumConfirmDialog.show()
            }else{
                tagPeopleViewModel.clearList()
                tagPeopleViewModel.clearSpecificFriendList()
                proceed(Constants.PRIVACY_TYPE_PRIVATE)
            }

        }
        binding.buttonOk.setOnClickListener {
            tagPeopleViewModel.setPolicy(selectedType)
            var bundle = Bundle()
            bundle.putString(SELECT_PRIVACY, selectedType)
            setFragmentResult(
                SELECT_PRIVACY, bundle
            )
            findNavController().popBackStack()
        }

        binding.toolbar.tvhead.text = "Privacy"
        binding.toolbar.topAppBar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

    }

    private fun proceed(type: String) {
        selectedType=type
        tagPeopleViewModel.setPolicy(selectedType)
        selectedOption(type)



    }
    private lateinit var multiUserSelectFragment: FragmentMultiUserSelector
    fun openBottomSheetDialog(){
        proceed(Constants.PRIVACY_TYPE_SPECIFIC)
        if (multiUserSelectFragment.isAdded) {
            return
        }
        multiUserSelectFragment!!.show(
            childFragmentManager,
            multiUserSelectFragment!!.tag
        )
    }
    



    fun selectedOption(type: String){
       when(type){
           Constants.PRIVACY_TYPE_MY_FOLLOWER->{
               binding.layoutMyFollower.background=ContextCompat.getDrawable(requireContext(),R.drawable.primary_small_corner_border)
               binding.layoutPrivate.background=ContextCompat.getDrawable(requireContext(),R.drawable.white_small_corner_border)
               binding.layoutPublic.background=ContextCompat.getDrawable(requireContext(),R.drawable.white_small_corner_border)
               binding.layoutSpecific.background=ContextCompat.getDrawable(requireContext(),R.drawable.white_small_corner_border)
           }
           Constants.PRIVACY_TYPE_PRIVATE->{
               binding.layoutMyFollower.background=ContextCompat.getDrawable(requireContext(),R.drawable.white_small_corner_border)
               binding.layoutPrivate.background=ContextCompat.getDrawable(requireContext(),R.drawable.primary_small_corner_border)
               binding.layoutPublic.background=ContextCompat.getDrawable(requireContext(),R.drawable.white_small_corner_border)
               binding.layoutSpecific.background=ContextCompat.getDrawable(requireContext(),R.drawable.white_small_corner_border)
           }
           Constants.PRIVACY_TYPE_PUBLIC->{
               binding.layoutMyFollower.background=ContextCompat.getDrawable(requireContext(),R.drawable.white_small_corner_border)
               binding.layoutPrivate.background=ContextCompat.getDrawable(requireContext(),R.drawable.white_small_corner_border)
               binding.layoutPublic.background=ContextCompat.getDrawable(requireContext(),R.drawable.primary_small_corner_border)
               binding.layoutSpecific.background=ContextCompat.getDrawable(requireContext(),R.drawable.white_small_corner_border)
           }
           Constants.PRIVACY_TYPE_SPECIFIC->{
               binding.layoutMyFollower.background=ContextCompat.getDrawable(requireContext(),R.drawable.white_small_corner_border)
               binding.layoutPrivate.background=ContextCompat.getDrawable(requireContext(),R.drawable.white_small_corner_border)
               binding.layoutPublic.background=ContextCompat.getDrawable(requireContext(),R.drawable.white_small_corner_border)
               binding.layoutSpecific.background=ContextCompat.getDrawable(requireContext(),R.drawable.primary_small_corner_border)
           }
       }
    }




}