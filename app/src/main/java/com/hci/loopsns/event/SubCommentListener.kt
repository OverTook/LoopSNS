package com.hci.loopsns.event

import com.hci.loopsns.network.Comment

interface SubCommentListener {
    fun onSubCommentDeleted(parentUid: String, uid: String)
    fun onSubCommentCreated(parentUid: String, comment: Comment)
}