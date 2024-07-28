package com.hci.loopsns.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface NetworkInterface {

    @Multipart
    @POST("/get_category")
    fun retrieveCategory(
        @Part images: List<MultipartBody.Part?>,
        @Part("contents") contents: RequestBody,
    ): Call<CategoryResponse>

    @Multipart
    @JvmSuppressWildcards
    @POST("/add_article")
    fun createArticle(
        @Part images: List<MultipartBody.Part?>, //이미지
        @Part("categories") categories: List<RequestBody>, //카테고리 목록
        @Part("keywords") keywords: List<RequestBody>, //키워드 목록
        @Part("contents") contents: RequestBody, //내용
        //이하 위경도
        @Part("lat") lat: RequestBody,
        @Part("lng") lng: RequestBody
    ): Call<ArticleCreateResponse>

    @DELETE("/delete_article/{article_id}")
    fun deleteArticle(@Path("article_id") postId: String): Call<ArticleDeleteResponse>

    //마커 가져오기
    @GET("/get_marker_clusterer")
    fun retrieveArticleMarker(
        @Query("lat_from") latFrom: Double,
        @Query("lng_from") lngFrom: Double,
        @Query("lat_to") latTo: Double,
        @Query("lng_to") lngTo: Double,
        @Query("zoom_level") zoomLevel: Float
    ): Call<ArticleMarkersResponse>

    @GET("/get_marker_timeline")
    fun retrieveArticleTimeline(@Query("articles") articles: List<String>): Call<ArticleTimelineResponse>

    @GET("/get_article_detail")
    fun retrieveArticleDetail(@Query("uid") articleId: String): Call<ArticleDetailResponse>

    @POST("/add_comment")
    fun createComment(@Body requestBody: CreateCommentRequest): Call<CommentCreateResponse>

    @DELETE("/delete_comment/{article_id}/{comment_id}")
    fun deleteComment(@Path("article_id") postId: String, @Path("comment_id") commentId: String): Call<CommentDeleteResponse>

    @GET("/get_user_article_list")
    fun retrieveMyArticle(@Query("last_article_id") lastArticleId: String = ""): Call<MyArticleResponse>

    @GET("/get_like_article_list")
    fun retrieveLikeArticle(@Query("last_article_id") lastArticleId: String = ""): Call<MyArticleResponse>

    @GET("/login")
    fun createAccount(
        @Query("platform") platform: String,
        @Query("token") token: String
    ): Call<AccountCreateResponse>

    @POST("/add_article_like")
    fun likeArticle(@Body requestBody: LikeArticleRequest): Call<LikeResponse>

    // FCM 토큰 추가
    @POST("/add_fcm_token")
    fun addFcmToken(@Body requestBody: AddFcmTokenRequest): Call<FcmTokenResponse>

    // FCM 토큰 삭제
    @POST("/delete_fcm_token")
    fun deleteFcmToken(@Body requestBody: DeleteFcmTokenRequest): Call<FcmTokenResponse>
}