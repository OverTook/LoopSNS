package com.hci.loopsns.event

import com.hci.loopsns.network.ArticleDetail

class FavoriteManager {

    private val observers = ArrayList<FavoriteListener>()

    companion object {
        @Volatile
        private var instance: FavoriteManager? = null

        fun getInstance(): FavoriteManager {
            return instance ?: synchronized(this) {
                instance ?: FavoriteManager().also { instance = it }
            }
        }
    }

    fun registerFavoriteListener(listener: FavoriteListener) {
        observers.add(listener)
    }

    fun removeFavoriteListener(listener: FavoriteListener) {
        observers.remove(listener)
    }

    fun onFavoriteStateChanged(article: ArticleDetail, state: Boolean) {
        if(state) {
            observers.forEach {
                it.onFavoriteArticle(article)
            }
            return
        }
        
        observers.forEach {
            it.onUnfavoriteArticle(article.uid)
        }
    }
}




interface FavoriteListener {
    fun onFavoriteArticle(article: ArticleDetail)
    fun onUnfavoriteArticle(articleId: String)
}
