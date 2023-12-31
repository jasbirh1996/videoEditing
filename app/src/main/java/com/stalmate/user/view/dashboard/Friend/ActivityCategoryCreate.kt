package com.stalmate.user.view.dashboard.Friend

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import com.stalmate.user.R
import com.stalmate.user.base.BaseActivity
import com.stalmate.user.databinding.ActivityCategoryCreateBinding
import com.stalmate.user.model.Education
import com.stalmate.user.model.Profession
import com.stalmate.user.view.dashboard.Friend.categoryadapter.AdapterCategory
import com.stalmate.user.view.dashboard.Friend.categorymodel.CategoryResponse
import com.stalmate.user.view.dashboard.Friend.categorymodel.ModelCategoryResponse
import com.stalmate.user.view.language.AdapterLanguage
import java.util.HashMap

class ActivityCategoryCreate : BaseActivity(), AdapterCategory.Callbackk {

    private lateinit var binding : ActivityCategoryCreateBinding
    lateinit var adapterCategory: AdapterCategory
    var isEdit : Boolean = false
    var isId : String = ""

    override fun onClick(viewId: Int, view: View?) {

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= DataBindingUtil.setContentView(this, R.layout.activity_category_create)

        /*toolbar*/
        setUpToolbar()

        /*getcateoryListing*/
        getCategoryListing()

        binding.toolbar.back.setOnClickListener {
            onBackPressed()
        }

        binding.btnAdd.setOnClickListener {
            hitAddEditApi()
            isId = ""
            isEdit = false
            binding.etCategoryName.setText(" ")
        }


        binding.popMenu.setOnClickListener {
            val popupmenu = PopupMenu(applicationContext, binding.popMenu)
            popupmenu.inflate(R.menu.menu_category)


            popupmenu.setOnMenuItemClickListener { item ->

                when(item!!.itemId){

                    R.id.category_A -> {

                    }

                }

                false
            }

            popupmenu.show()
        }

    }

    private fun hitAddEditApi() {

//        showLoader()
        val hashMap = HashMap<String, String>()

        if (isEdit) {
            hashMap["id"] = isId
        }
        hashMap["type"] = "Category A"
        hashMap["name"] = binding.etCategoryName.text.toString()

        networkViewModel.updateFriendCategoryData(hashMap)
        networkViewModel.updateFriendCategoryLiveData.observe(this){

                if (it!!.status){
                        getCategoryListing()
                }

            makeToast(it.message)
        }
    }


    private fun getCategoryListing() {

        adapterCategory = AdapterCategory(networkViewModel, this, this)
        binding.rvCategoryList.adapter = adapterCategory

        networkViewModel.categoryFriendLiveData()
        networkViewModel.categoryFriendLiveData.observe(this) {

           if (it?.status == true){
               it?.results?.let { it1 -> adapterCategory.submitList(it1) }
            }
        }

    }

    private fun setUpToolbar() {
        binding.toolbar.backButtonLeftText.visibility = View.VISIBLE
        binding.toolbar.backButtonLeftText.text = getString(R.string.category)
    }

    override fun onClickEditItem(categoryResponse: CategoryResponse, index: Int) {

        binding.etCategoryName.setText(categoryResponse.name)
        isEdit = true
        isId = categoryResponse.id
        binding.btnAdd.text = "Edit"

    }

    override fun onBackPressed() {
        super.onBackPressed()
    }


}