package com.hci.loopsns.storage.models

import com.google.gson.annotations.SerializedName
import org.litepal.annotation.Column
import org.litepal.crud.LitePalSupport
import java.util.Date

data class NotificationComment (
    @SerializedName("article_id")
    val articleId: String,
    @SerializedName("comment_id")
    val commentId: String,
    val writer: String,
    val contents: String,
    @SerializedName("user_img")
    val userImg: String,
    override val time: Date,
    override var readed: Boolean = false,
) : LitePalSupport(), NotificationInterface


data class NotificationHotArticle (
    val writer: String,
    val contents: String,
    val picture: String,
    override val time: Date,
    override var readed: Boolean = false,
) : LitePalSupport(), NotificationInterface

