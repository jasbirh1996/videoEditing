package com.stalmate.user.model

data class ModelAlbumsResponse(
    val message: String,
    val results: List<ResultResponse>,
    val status: Boolean
)

data class ResultResponse(
    val _id: String,
//    val id: String,
    val name: String,
    val img : String
)