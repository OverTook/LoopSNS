package com.hci.loopsns.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface NetworkInterface {

    @Multipart
    @POST("/get_category")
    fun retrieveCategory(
        @Part images: List<MultipartBody.Part?>,
        @Part("contents") contents: RequestBody,
    ): Call<CategoryResponse>

    @POST("/add_article")
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
    @GET("/get_marker_clusterer")
    fun retrieveArticleMarker(
        @Query("lat_from") latFrom: Double,
        @Query("lng_from") lngFrom: Double,
        @Query("lat_to") latTo: Double,
        @Query("lng_to") lngTo: Double
    ): Call<ArticleMarkersResponse>

    @GET("/get_marker_timeline")
    fun retrieveArticleTimeline(@Query("articles") articles: List<String>): Call<ArticleTimelineResponse>

    @GET("/get_article_detail")
    fun retrieveArticleDetail(@Query("uid") articleId: String): Call<ArticleDetailResponse>

    @POST("/add_comment")
    fun createComment(@Body requestBody: CreateCommentRequest): Call<CommentCreateResponse>

//    @POST("/create/users/nickname")
//    fun createNickname(@Body nickname: String): Call<NicknameResponse>

//    @GET("/get/users/nickname")
//    fun retrieveNickname(): Call<NicknameResponse>

    @GET("/login")
    fun createAccount(
        @Query("platform") platform: String,
        @Query("token") token: String
    ): Call<AccountCreateResponse>

    @POST("/add_article_like")
    fun likeArticle(@Body requestBody: LikeArticleRequest): Call<LikeResponse>

    @GET("/user_liked_article_list")
    fun retrieveLikedArticles(): Call<LikedArticlesResponse>


}