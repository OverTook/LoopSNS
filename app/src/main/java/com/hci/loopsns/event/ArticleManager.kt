package com.hci.loopsns.event

import com.hci.loopsns.network.ArticleDetail

class ArticleManager {

    private val observers = ArrayList<ArticleListener>()

    companion object {
        @Volatile
        private var instance: ArticleManager? = null

        fun getInstance(): ArticleManager {
            return instance ?: synchronized(this) {
                instance ?: ArticleManager().also { instance = it }
            }
        }
    }

    fun registerArticleListener(listener: ArticleListener) {
        observers.add(listener)
    }

    fun removeArticleListener(listener: ArticleListener) {
        observers.remove(listener)
    }

    fun onArticleCreated(article: ArticleDetail) {
        observers.forEach {
            it.onArticleCreated(article)
        }
    }

    fun onArticleDeleted(articleId: String) {
        observers.forEach {
            it.onArticleDeleted(articleId)
        }
    }
}




interface ArticleListener {
    fun onArticleCreated(article: ArticleDetail)
    fun onArticleDeleted(articleId: String)
}
