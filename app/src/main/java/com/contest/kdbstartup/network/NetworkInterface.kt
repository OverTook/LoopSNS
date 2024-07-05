package com.contest.kdbstartup.network

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface NetworkInterface {

    @Multipart
    @POST("/get/category")
    fun retrieveCategory(
        @Part images: List<MultipartBody.Part>
    ): Call<CategoryResponse>

    @POST("/create/article")
    fun createArticle(
        @Query("cat1") cat1: String, @Query("cat2") cat2: String, @Query("keywords") keywords: List<String>, @Query("contents") content: String
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