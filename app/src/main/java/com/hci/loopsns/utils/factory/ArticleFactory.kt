package com.hci.loopsns.utils.factory

import com.hci.loopsns.network.ArticleDetail
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue

object MyArticleFactory {

    private val createdArticles: Queue<ArticleDetail> = ConcurrentLinkedQueue<ArticleDetail>()
    private val deletedArticles: Queue<String> = ConcurrentLinkedQueue<String>()

    fun addCreatedArticle(articleDetail: ArticleDetail) {
        createdArticles.add(articleDetail)
    }

    fun getCreatedArticle(): ArticleDetail? {
        return createdArticles.poll()
    }

    fun addDeletedArticles(uid: String) {
        deletedArticles.add(uid)
    }

    fun getDeletedArticles(): String? {
        return deletedArticles.poll()
    }
}

object LikeArticleFactory {

    private val likedArticles: Queue<ArticleDetail> = ConcurrentLinkedQueue<ArticleDetail>()
    private val deletedArticles: Queue<String> = ConcurrentLinkedQueue<String>()

    fun addLikedArticle(articleDetail: ArticleDetail) {
        likedArticles.add(articleDetail)
    }

    fun getLikedArticle(): ArticleDetail? {
        return likedArticles.poll()
    }

    fun addDeletedArticles(uid: String) {
        deletedArticles.add(uid)
    }

    fun getDeletedArticles(): String? {
        return deletedArticles.poll()
    }
}