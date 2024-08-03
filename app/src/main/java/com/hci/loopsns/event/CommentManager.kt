package com.hci.loopsns.event

import com.hci.loopsns.network.Comment

class CommentManager {

    private val observers = ArrayList<CommentListener>()
    private val subObservers = ArrayList<SubCommentListener>()

    companion object {
        @Volatile
        private var instance: CommentManager? = null

        fun getInstance(): CommentManager {
            return instance ?: synchronized(this) {
                instance ?: CommentManager().also { instance = it }
            }
        }
    }

    fun onCommentDeleted(uid: String) {
        observers.forEach {
            it.onCommentDeleted(uid)
        }
    }

    fun onCommentCreated(comment: Comment) {
        observers.forEach {
            it.onCommentCreated(comment)
        }
    }

    fun onSubCommentDeleted(parentUid: String, uid: String) {
        subObservers.forEach {
            it.onSubCommentDeleted(parentUid, uid)
        }
    }

    fun onSubCommentCreated(parentUid: String, comment: Comment) {
        subObservers.forEach {
            it.onSubCommentCreated(parentUid, comment)
        }
    }

    fun registerCommentListener(listener: CommentListener) {
        observers.add(listener)
    }

    fun removeCommentListener(listener: CommentListener) {
        observers.remove(listener)
    }

    fun registerSubCommentListener(listener: SubCommentListener) {
        subObservers.add(listener)
    }

    fun removeSubCommentListener(listener: SubCommentListener) {
        subObservers.remove(listener)
    }
}