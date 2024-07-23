package com.hci.loopsns.network

import com.google.gson.annotations.SerializedName

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