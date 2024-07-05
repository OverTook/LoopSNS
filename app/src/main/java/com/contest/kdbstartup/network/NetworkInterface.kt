package com.contest.kdbstartup.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface NetworkInterface {

    @Multipart
    @POST("/get/category")
    fun retrieveCategory(
        @Part images: List<MultipartBody.Part>,
        @Part("contents") content: RequestBody,
    ): Call<CategoryResponse>

    @Multipart
    @POST("/create/article")
    fun createArticle(
        @Part images: List<MultipartBody.Part>,

        @Part("title") title: RequestBody,
        @Part("cat1") cat1: RequestBody,
        @Part("cat2") cat2: RequestBody,
        @Part("keywords") keywords: List<RequestBody>,

        @Part("contents") content: RequestBody,
    ): Call<ArticleCreateResponse>

    @POST("/create/nickname")
    fun createNickname(
        @Query("uid") uid: String, @Query("nickname") nickname: String
    ): Call<NicknameResponse>

    @POST("/get/nickname")
    fun getNickname(
        @Query("uid") uid: String
    ): Call<NicknameResponse>
}