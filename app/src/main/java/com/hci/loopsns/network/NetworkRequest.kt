package com.hci.loopsns.network

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Part

data class CreateCommentRequest (
    @SerializedName("uid")
    val articleId: String,
    @SerializedName("contents")
    val contents: String
)

data class LikeArticleRequest (
    @SerializedName("uid")
    val articleId: String,
    @SerializedName("like")
    val like: Boolean
)