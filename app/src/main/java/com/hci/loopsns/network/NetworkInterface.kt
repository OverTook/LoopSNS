package com.hci.loopsns.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

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
    fun deleteArticle(
        @Path("article_id") postId: String
    ): Call<ArticleDeleteResponse>

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
    fun retrieveArticleTimeline(
        @Query("articles") articles: List<String>
    ): Call<ArticleTimelineResponse>

    @GET("/get_article")
    fun retrieveArticle(
        @Query("uid") articleId: String
    ): Call<ArticleDetailResponse>

    @GET("/get_comment")
    fun retrieveComment(
        @Query("uid") articleId: String,
        @Query("comment_id") commentId: String
    ): Call<CommentResponse>

    @GET("/get_sub_comment")
    fun retrieveSubComment(
        @Query("uid") articleId: String,
        @Query("comment_id") commentId: String,
        @Query("sub_comment_id") subCommentId: String
    ): Call<CommentResponse>

    @GET("/get_comment_list")
    fun retrieveCommentList(
        @Query("uid") articleId: String,
        @Query("last_comment_id") lastCommentId: String = ""
    ): Call<CommentListResponse>

    @GET("/get_sub_comment_list")
    fun retrieveSubCommentList(
        @Query("uid") articleId: String,
        @Query("comment_id") commentId: String,
        @Query("last_sub_comment_id") lastSubCommentId: String = ""
    ): Call<CommentListResponse>

    @POST("/add_comment")
    fun createComment(
        @Body requestBody: CreateCommentRequest
    ): Call<CommentCreateResponse>

    @POST("/add_sub_comment")
    fun createSubComment(
        @Body requestBody: CreateSubCommentRequest
    ): Call<CommentCreateResponse>

    @DELETE("/delete_comment/{article_id}/{comment_id}")
    fun deleteComment(
        @Path("article_id") postId: String,
        @Path("comment_id") commentId: String
    ): Call<CommentDeleteResponse>

    @DELETE("/delete_sub_comment/{article_id}/{comment_id}/{sub_comment_id}")
    fun deleteSubComment(
        @Path("article_id") postId: String,
        @Path("comment_id") commentId: String,
        @Path("sub_comment_id") subCommentId: String
    ): Call<CommentDeleteResponse>

    @GET("/get_user_article_list")
    fun retrieveMyArticle(
        @Query("last_article_id") lastArticleId: String = ""
    ): Call<MyArticleResponse>

    @GET("/get_like_article_list")
    fun retrieveLikeArticle(
        @Query("last_article_id") lastArticleId: String = ""
    ): Call<MyArticleResponse>

    @GET("/login")
    fun createAccount(
        @Query("platform") platform: String,
        @Query("token") token: String
    ): Call<AccountCreateResponse>

    @POST("/add_article_like")
    fun likeArticle(
        @Body requestBody: LikeArticleRequest
    ): Call<LikeResponse>

    // FCM 토큰 추가
    @POST("/add_fcm_token")
    fun addFcmToken(
        @Body requestBody: AddFcmTokenRequest
    ): Call<FcmTokenResponse>

    // FCM 토큰 삭제
    @POST("/delete_fcm_token")
    fun deleteFcmToken(
        @Body requestBody: DeleteFcmTokenRequest
    ): Call<FcmTokenResponse>

    @GET("/get_center_addr")
    fun getAddress(
        @Query("latlng") latlng: String,
        @Query("language") language: String
    ): Call<AddressResponse>

    @Multipart
    @POST("/update_profile_img")
    fun updateProfileImage(
        @Part image: MultipartBody.Part?
    ): Call<UpdateProfileImageResponse>

    @DELETE("/delete_user")
    fun unregister(): Call<UnregisterResponse>

    @GET("/terms_of_use")
    fun getTermsOfUse(@Query("language") language: String): Call<TermsOfAnyResponse>

    @GET("/terms_of_information")
    fun getTermsOfInformation(@Query("language") language: String): Call<TermsOfAnyResponse>

    @GET("/terms_of_faq")
    fun getFAQ(@Query("language") language: String): Call<TermsOfAnyResponse>

    @GET("/search")
    fun search(
        @Query("c1") cat1: String,
        @Query("c2") cat2: String,
        @Query("query") query: String,
        @Query("search_type") searchType: String
    ): Call<SearchResponse>

    @POST("/report")
    fun report(
        @Query("article_id") articleId: String,
        @Query("comment_id") commentId: String,
        @Query("sub_comment_id") subCommentId: String,
    ): Call<ReportResponse>

    @GET("/load_addr_csv")
    fun getAllLocalities(): Call<LocalitiesResponse>

    @Streaming
    @GET("/report_download")
    fun downloadReport(
        @Query("intentions") intentions: List<String>,
        @Query("subjects") subjects: List<String>,
        @Query("sido") loc1: String,
        @Query("sigungu") loc2: String,
        @Query("eupmyeondong") loc3: String,
    ): Call<ResponseBody>

    @GET("/intention_subject")
    fun getIntentionsSubjects(@Query("language") language: String): Call<IntentionSubjectResponse>
}