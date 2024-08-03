package com.hci.loopsns.event

import com.hci.loopsns.network.Comment

interface CommentListener {
    fun onCommentDeleted(uid: String)
    fun onCommentCreated(comment: Comment)
}