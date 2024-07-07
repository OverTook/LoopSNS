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
        @Part("contents") contents: RequestBody,
    ): Call<CategoryResponse>

    @Multipart
    @POST("/create/article")
    fun createArticle(
        @Part images: List<MultipartBody.Part>, //이미지
        @Part("title") title: RequestBody, //제목
        @Part("cat1") cat1: RequestBody, //1차 카테고리 (문자열)
        @Part("cat2") cat2: RequestBody, //2차 카테고리 (문자열)
        @Part("keywords") keywords: List<RequestBody>, //키워드 목록
        @Part("contents") contents: RequestBody, //내용
        //이하 위경도
        @Part("lat") lat: RequestBody,
        @Part("lng") lng: RequestBody
    ): Call<ArticleCreateResponse>

    //마커 가져오기
    @POST("/get/article/marker")
    fun retrieveArticleMarker(
        @Query("lat_from") latFrom: Double,
        @Query("lng_from") lngFrom: Double,
        @Query("lat_to") latTo: Double,
        @Query("lng_to") lngTo: Double
    ): Call<ArticleMarkersResponse>

    @POST("/get/article/timeline")
    fun retrieveArticleTimeline(
        @Query("article_ids") articles: List<String>
    ): Call<ArticleTimelineResponse>

    @POST("/get/article/comment")
    fun retrieveArticleComment(
        @Query("uid") uid: String
    ): Call<CommentResponse>

    @POST("/create/comment")
    fun createComment(
        @Query("uid") uid: String,
        @Query("contents") contents: String
    ): Call<CommentCreateResponse>

    @POST("/create/nickname")
    fun createNickname(
        @Query("nickname") nickname: String
    ): Call<NicknameResponse>

    @POST("/get/nickname")
    fun getNickname(): Call<NicknameResponse>

    @POST("/create/account")
    fun createAccount(
        @Query("platform") platform: String,
        @Query("token") token: String
    ): Call<AccountCreateResponse>
}