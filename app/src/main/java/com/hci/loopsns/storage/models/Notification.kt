package com.hci.loopsns.storage.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import org.litepal.crud.LitePalSupport
import java.util.Date

@Parcelize
data class NotificationComment (
    @SerializedName("article_id")
    val articleId: String,
    @SerializedName("comment_id")
    val commentId: String,
    @SerializedName("sub_comment_id")
    val subCommentId: String,
    val writer: String,
    val contents: String,
    @SerializedName("user_img")
    val userImg: String,
    override val time: Date,
    override var readed: Boolean = false,
) : LitePalSupport(), NotificationInterface, Parcelable


@Parcelize
data class NotificationFavorite (
    @SerializedName("article_id")
    val articleId: String,
    @SerializedName("like_count")
    val likeCount: Int,
    override val time: Date,
    override var readed: Boolean = false,
) : LitePalSupport(), NotificationInterface, Parcelable

