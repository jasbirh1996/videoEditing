package com.slatmate.user.view.Authentication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.slatmate.user.R
import com.slatmate.user.databinding.ActivityActivitytestBinding

class ActivityAuthentication : AppCompatActivity() {

    lateinit var navController: NavController
    lateinit var binding: ActivityActivitytestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= DataBindingUtil.setContentView(this, R.layout.activity_authentication)!!
        setUpNavigation()
    }


    fun setUpNavigation() {
        navController=findNavController(R.id.nav_host_fragment)
    }
}