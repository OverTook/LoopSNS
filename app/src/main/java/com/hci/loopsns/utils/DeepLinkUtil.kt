package com.hci.loopsns.utils

import android.app.Activity

fun Activity.createDeepLink(articleId: String, commentId: String, subCommentId: String): String {
    return buildString {
        append("https://")
        append("overtook.github.io/?")
        append("articleId=")
        append(articleId)
        append("&commentId=")
        append(commentId)
        append("&subCommentId=")
        append(subCommentId)
    }
}