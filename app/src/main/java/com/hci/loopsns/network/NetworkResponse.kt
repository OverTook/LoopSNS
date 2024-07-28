package com.hci.loopsns.network

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.PropertyName
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
//게시글 카테고리 자동 분석 결과 DTO
data class CategoryResponse (
    val success: Boolean, //응답 성공 여부
    val msg: String, //응답 실패 시 오류 메시지

    val categories: List<String>?, //카테고리
    val keywords: List<String>?, //키워드
)

//게시글 작성 결과 DTO
data class ArticleCreateResponse (
    val success: Boolean, //응답 성공 여부
    val msg: String, //응답 실패 시 오류 메시지
    val article: ArticleDetail
)

data class ArticleDeleteResponse (
    val success: Boolean, //응답 성공 여부
    val msg: String //응답 실패 시 오류 메시지
)

data class ArticleMarkersResponse (
    val success: Boolean, //응답 성공 여부
    val msg: String, //응답 실패 시 오류 메시지

    val markers: List<MarkerItem>, //마커 목록
)

data class MarkerItem (
    val articles: List<String>, //클러스터링된 게시글 아이디 목록
    val lat: Double,
    val lng: Double
)

data class ArticleTimelineResponse (
    val success: Boolean, //응답 성공 여부
    val msg: String, //응답 실패 시 오류 메시지

    val articles: List<Article> //게시글 목록
)

@Parcelize
data class Article (
    val uid: String, //게시글 유니크 아이디

    val writer: String, //글쓴이
    val contents: String, //내용
    val cat1: String, //첫 번째 카테고리
    val cat2: String, //두 번째 카테고리
    val keywords: List<String>, //키워드
    val time: String, //게시글 작성 시간
    @SerializedName("comment_count")
    val commentCount: Int, //댓글 수
    @SerializedName("like_count")
    val likeCount: Int, //좋아요 수

    @SerializedName("image_urls")
    val images: List<String>,
) : Parcelable

data class ArticleDetailResponse (
    val success: Boolean, //응답 성공 여부
    val msg: String, //응답 실패 시 오류 메시지

    val article: ArticleDetail,
    val comments: List<Comment>
)

data class MyArticleResponse (
    val success: Boolean, //응답 성공 여부
    val msg: String, //응답 실패 시 오류 메시지

    val articles: List<ArticleDetail> //게시글 목록
)

@Parcelize
data class ArticleDetail (
    val uid: String, //게시글 유니크 아이디

    var writer: String?, //글쓴이
    val contents: String, //내용
    val cat1: String, //첫 번째 카테고리
    val cat2: String, //두 번째 카테고리
    val keywords: List<String>, //키워드
    val time: String, //게시글 작성 시간
    @SerializedName("comment_count")
    var commentCount: Int, //댓글 수
    @SerializedName("like_count")
    val likeCount: Int, //좋아요 수
    @SerializedName("image_urls")
    val images: List<String>,
    @SerializedName("user_img")
    var userImg: String,
    @SerializedName("can_delete")
    val canDelete: Boolean
) : Parcelable

data class CommentCreateResponse (
    val success: Boolean, //응답 성공 여부
    val msg: String, //응답 실패 시 오류 메시지
    val time: String,
    @SerializedName("comment_id")
    val uid: String
)

data class CommentDeleteResponse (
    val success: Boolean, //응답 성공 여부
    val msg: String //응답 실패 시 오류 메시지
)

@Parcelize
data class Comment (
    @SerializedName("comment_id")
    val uid: String, //댓글 유니크 아이디
    var writer: String?, //글쓴이
    val contents: String, //내용
    val time: String, //댓글 작성 시간
    @SerializedName("user_img")
    var userImg: String,
    @SerializedName("can_delete")
    val canDelete: Boolean
) : Parcelable

//닉네임 요청&생성 응답 DTO
data class NicknameResponse (
    val success: Boolean,
    val msg: String,

    val nickname: String
)

data class AccountCreateResponse (
    val success: Boolean,
    val msg: String,

    val token: String
)

//좋아요
data class LikeResponse (
    val success: Boolean,
    val msg: String
)