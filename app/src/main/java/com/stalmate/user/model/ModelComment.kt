package com.stalmate.user.model

data class ModelComment(
    val status: Boolean,
    val results: Comment,
)

data class ModelGetComment(
    val status: Boolean = false,
    val results: ArrayList<Comment> = arrayListOf(),
)

data class Comment(
    val _id: String = "",
    val date: String = "",
    val Created_date: String = "",
    val author: String = "",
    val comment: String = "",
    val comment_image: String = "",
    var replies: ArrayList<Comment> = arrayListOf(),
    val parentId: String? = null,
    var isLiked: String? = null,
    val first_name: String = "",
    val last_name: String = "",
    var child_count: Int = 0,
    var profile_img: String = "",
    var like_count: Int = 0,
    var isExpanded: Boolean = false,
    var isShowingReplies: Boolean = false
)