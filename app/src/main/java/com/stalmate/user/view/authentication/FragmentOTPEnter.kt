package com.stalmate.user.view.authentication

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.stalmate.user.R
import com.stalmate.user.base.App
import com.stalmate.user.base.BaseFragment
import com.stalmate.user.databinding.FragmentOTPEnterBinding
import com.stalmate.user.databinding.SignUpSuccessPoppuBinding
import com.stalmate.user.utilities.PrefManager
import com.stalmate.user.utilities.ValidationHelper
import com.stalmate.user.view.dashboard.ActivityDashboard
import java.util.*


class FragmentOTPEnter : BaseFragment() {
    private lateinit var binding: FragmentOTPEnterBinding
    private lateinit var bindingdialog: SignUpSuccessPoppuBinding
    var email: String = ""
    var password: String = ""
    var first_name: String = ""
    var last_name: String = ""
    var user_name: String = ""
    var gender: String = ""
    var schoolandcollege: String = ""
    var dob: String = ""
    var device_token: String = ""
    var device_type: String = ""
    var forgetPasswordScreen: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_o_t_p_enter, container, false)
        binding = DataBindingUtil.bind<FragmentOTPEnterBinding>(view)!!


        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startTimer()

        email = requireArguments().getString("email").toString()
        password = requireArguments().getString("password").toString()
        first_name = requireArguments().getString("first_name").toString()
        last_name = requireArguments().getString("last_name").toString()
        gender = requireArguments().getString("gender").toString()
        dob = requireArguments().getString("dob").toString()
        device_token = requireArguments().getString("device_token").toString()
        device_type = requireArguments().getString("device_type").toString()
        forgetPasswordScreen = requireArguments().getString("signUp").toString()
        forgetPasswordScreen = requireArguments().getString("layout").toString()

        /*Common ToolBar SetUp*/
        toolbarSetUp()
        Log.d("emiasljkcn", forgetPasswordScreen)
        getOtpApiCall()
        getOtpRegistrationApiCall()


        binding.etUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.etUsername.text.toString().isNotEmpty()) {
                    hitApiCheckOldUsername()
                } else {
                    binding.ivUsername.visibility = View.GONE
                    binding.errorTaken.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
        binding.btnCrateAccount.setOnClickListener {
            if (ValidationHelper.isNull(binding.etUsername.text.toString())) {
                makeToast(getString(R.string.user_name_toast))
            } else if (isUsedUsername) {
                makeToast("Username is not available!")
            } else {
                createAccountApiCall()
            }
        }

        binding.btnProcess.setOnClickListener {

            /*Otp Verify Api Call*/

            if (binding.pinView.text.toString() == "1234") {

                if (forgetPasswordScreen == "SignUp") {
                    otpVerifyApiCall()
                } else if (forgetPasswordScreen == "ForgetPassword") {
                    otpVerifyForgotApiCall()
                }
            } else {
                makeToast("Please Enter Valid Otp")
            }
        }


        binding.pinView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                if (p0 != null && p0.length == 4) {
//                    hideKeyboard(binding.root)
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

    }

    var isUsedUsername = false
    fun hitApiCheckOldUsername() {
        networkViewModel.checkIfOldUsername(user_name = binding.etUsername.text.toString())
        networkViewModel.checkIfOldUsernameLiveData.observe(requireActivity()) {
            it?.reponse?.let {
                if (it.name_status == true) {
                    binding.ivUsername.visibility = View.VISIBLE
                    binding.errorTaken.visibility = View.GONE
                    isUsedUsername = false
                } else {
                    isUsedUsername = true
                    binding.ivUsername.visibility = View.GONE
                    binding.errorTaken.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun getOtpRegistrationApiCall() {
        val hashMap = HashMap<String, String>()

        hashMap["email"] = email

        binding.progressBar.visibility = View.VISIBLE
        networkViewModel.otpVerifyRegistration(hashMap, email = email, otp = "")

        networkViewModel.otpVerifyRegistarionData.observe(requireActivity()) {

            it?.let {

                if (it.status == true) {
                    binding.progressBar.visibility = View.GONE
                }
            }
            binding.progressBar.visibility = View.GONE
        }
    }


    private fun otpVerifyForgotApiCall() {
        val hashMap = HashMap<String, String>()

        hashMap["email"] = email

        binding.progressBar.visibility = View.VISIBLE
        networkViewModel.otpVerify(hashMap)

        networkViewModel.otpVerifyData.observe(requireActivity()) {

            it?.let {

                if (it.status == true) {
                    binding.progressBar.visibility = View.GONE
                    val bundle = Bundle()
                    bundle.putString("email", email)
                    bundle.putString("otp", "1234")
                    findNavController().navigate(R.id.fragmentPasswordReset, bundle)

                }
            }

        }
    }

    private fun getOtpApiCall() {
        val hashMap = HashMap<String, String>()

        hashMap["email"] = email

        binding.progressBar.visibility = View.VISIBLE
        networkViewModel.otpVerify(hashMap)

        networkViewModel.otpVerifyData.observe(requireActivity()) {

            it?.let {

                if (it.status == true) {
                    binding.progressBar.visibility = View.GONE
                }
            }
            binding.progressBar.visibility = View.GONE
        }
    }

    var successdialogBuilder: AlertDialog? = null
    private fun otpVerifyApiCall() {

        val hashMap = HashMap<String, String>()

        hashMap["email"] = email
        hashMap["otp"] = binding.pinView.text.toString()
        binding.progressBar.visibility = View.VISIBLE
        networkViewModel.otpVerifyRegistration(
            hashMap,
            email = email,
            otp = binding.pinView.text.toString()
        )
        networkViewModel.otpVerifyRegistarionData.observe(requireActivity()) {
            it?.let {
                val message = it.message
                if (it.status == true) {
                    //Update here
                    next()
                    makeToast("Otp verified successfully!")
                } else {
                    makeToast(message)
                }

            }
            binding.progressBar.visibility = View.GONE
        }


    }

    fun next() {
        binding.clOtp.visibility = View.GONE
        binding.clUsername.visibility = View.VISIBLE
        binding.btnCrateAccount.text = "Submit"
        binding.toolbar.toolBarCenterText.text = "Create Username"
    }

    private fun createAccountApiCall() {
        user_name = binding.etUsername.text.toString()
        val hashMap = HashMap<String, String>()
        hashMap["email"] = email
        hashMap["password"] = password
        hashMap["first_name"] = first_name
        hashMap["last_name"] = last_name
        hashMap["user_name"] = user_name
        hashMap["gender"] = gender
//        hashMap["schoolandcollege"] = schoolandcollege
        hashMap["dob"] = dob
        hashMap["device_id"] = ""
        hashMap["device_token"] = App.getInstance().firebaseToken.toString()
        hashMap["device_type"] = "android"
        binding.progressBar.visibility = View.VISIBLE
        networkViewModel.registration(hashMap)
        networkViewModel.registerData.observe(viewLifecycleOwner) {
            binding.progressBar.visibility = View.GONE
            it?.let {
                val message = it.message
                if (it.status) {
                    makeToast("Username is created!")
                    PrefManager.getInstance(requireContext())?.keyIsLoggedIn = true
                    PrefManager.getInstance(requireContext())?.userDetail = it
                    App.getInstance().setupApis()

                    successdialogBuilder =
                        AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog).create()
                    val view = layoutInflater.inflate(R.layout.sign_up_success_poppu, null)
                    successdialogBuilder?.setView(view)
                    successdialogBuilder?.setCanceledOnTouchOutside(false)
                    successdialogBuilder?.show()

                    Handler(Looper.myLooper() ?: Looper.getMainLooper()).postDelayed({
                        successdialogBuilder?.dismiss()
                        startActivity(Intent(requireContext(), ActivityDashboard::class.java))
                        (context as ActivityAuthentication).finish()
                    }, 3000)
                } else {
                    makeToast(message)
                }
            }
        }
    }

    private fun toolbarSetUp() {
        binding.toolbar.toolBarCenterText.visibility = View.VISIBLE
        if (forgetPasswordScreen == "SignUp") {
            binding.toolbar.toolBarCenterText.text = getString(R.string.text_verify)
        } else if (forgetPasswordScreen == "ForgetPassword") {
            binding.toolbar.toolBarCenterText.text = getString(R.string.forget_post)
        }
        binding.toolbar.back.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun startTimer() {
        val timeDuration = 30000L
        val interval = 1000L
        val timer = object : CountDownTimer(timeDuration, interval) {
            override fun onTick(millisUntilFinished: Long) {
                binding.otpCountDown.text = formatSeconds(millisUntilFinished)

            }

            override fun onFinish() {
                binding.otpCountDown.visibility = View.GONE
                binding.otpResent.visibility = View.VISIBLE
                binding.otpResent.setOnClickListener {
                    startTimer()
                    binding.otpResent.visibility = View.GONE
                    binding.otpCountDown.visibility = View.VISIBLE
                    getOtpApiCall()

                }
            }
        }

        timer.start()
    }

    fun formatSeconds(timeInSeconds: Long): String {
        val secondsLeft = timeInSeconds / 1000
        val ss = if (secondsLeft < 10) "0$secondsLeft" else "" + secondsLeft
        return "00:$ss"
    }
}





