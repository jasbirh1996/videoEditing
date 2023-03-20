package com.stalmate.user.model

data class ChangePasswordResponse(
    val message: String,
    val reponse: Reponse? = null
) {
    data class Reponse(
        val Created_date: String,
        val Updated_date: String,
        val __v: Int,
        val _id: String,
        val about: String,
        val access_token: String,
        val branch_id: String,
        val category_id: String,
        val city: String,
        val company: String,
        val country: String,
        val countrycode: String,
        val cover_img_1: String,
        val deviceId: String,
        val deviceToken: String,
        val dob: String,
        val email: String,
        val first_name: String,
        val gender: String,
        val home_town: String,
        val is_delete: String,
        val last_name: String,
        val marital_status: String,
        val number: String,
        val otp: String,
        val password: String,
        val profile_img_1: String,
        val schoolandcollege: String,
        val show_to_about: String,
        val show_to_group: String,
        val show_to_profile: String,
        val show_to_profile_photo: String,
        val show_to_story: String,
        val state: String,
        val status: String,
        val university_id: String,
        val url: String,
        val who_can_like_my_post: String,
        val who_can_post_comment: String,
        val who_can_see_email: String,
        val who_can_see_my_future_post: String,
        val who_can_see_number: String,
        val who_can_see_people_page: String,
        val who_can_send_me_message: String,
        val who_can_send_you_friendrequest: String
    )
}