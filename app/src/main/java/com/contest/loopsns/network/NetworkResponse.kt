package com.contest.loopsns.network

import android.os.Parcel
import android.os.Parcelable

//게시글 카테고리 자동 분석 결과 DTO
data class CategoryResponse (
    val success: Boolean, //응답 성공 여부
    val msg: String, //응답 실패 시 오류 메시지
    
    val cat1: String, //첫 번째 카테고리
    val cat2: String, //두 번째 카테고리
    val keywords: List<String>, //키워드
)

//게시글 작성 결과 DTO
data class ArticleCreateResponse (
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

data class Article (
    val uid: String, //게시글 유니크 아이디

    val writer: String, //글쓴이
    val title: String, //제목
    val contents: String, //내용
    val cat1: String, //첫 번째 카테고리
    val cat2: String, //두 번째 카테고리
    val keywords: List<String>, //키워드
    val time: String, //게시글 작성 시간
    val commentCount: Int, //댓글 수
    val likeCount: Int //좋아요 수
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createStringArrayList() ?: listOf(),
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(uid)
        parcel.writeString(writer)
        parcel.writeString(title)
        parcel.writeString(contents)
        parcel.writeString(cat1)
        parcel.writeString(cat2)
        parcel.writeStringList(keywords)
        parcel.writeString(time)
        parcel.writeInt(commentCount)
        parcel.writeInt(likeCount)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Article> {
        override fun createFromParcel(parcel: Parcel): Article {
            return Article(parcel)
        }

        override fun newArray(size: Int): Array<Article?> {
            return arrayOfNulls(size)
        }
    }
}

data class CommentResponse (
    val success: Boolean, //응답 성공 여부
    val msg: String, //응답 실패 시 오류 메시지

    val comments: List<Comment>
)

data class CommentCreateResponse (
    val success: Boolean, //응답 성공 여부
    val msg: String, //응답 실패 시 오류 메시지
)

data class Comment (
    val uid: String, //댓글 uid
    val writer: String, //글쓴이
    val contents: String, //내용
    val time: String //댓글 작성 시간
)

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

data class LikedArticlesResponse (
    val success: Boolean,
    val msg: String,

    val articles: List<LikedArticle>
)

data class LikedArticle (
    val uid: String, //게시글 유니크 아이디

    val contents: String, //내용
)