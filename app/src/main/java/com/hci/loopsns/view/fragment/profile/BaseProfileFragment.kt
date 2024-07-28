package com.hci.loopsns.view.fragment.profile

import androidx.fragment.app.Fragment
import com.hci.loopsns.network.ArticleDetail
import com.hci.loopsns.utils.LikeArticleFactory
import com.hci.loopsns.utils.MyArticleFactory

abstract class BaseProfileFragment : Fragment() {

    abstract fun requestMoreArticle()
    abstract fun onInitializeArticle()
    abstract fun onArticleDelete(uid: String)
    abstract fun onArticleCreate(articleDetail: ArticleDetail)
    abstract fun isInitialized(): Boolean
    abstract fun onClickArticle(uid: String)

    fun onResumeSelf() {
        if(!isInitialized()) {
            return
        }

        when(this) {
            is ProfileMyArticleFragment -> {
                while(true) {
                    val createdArticle: ArticleDetail = MyArticleFactory.getCreatedArticle() ?: break

                    onArticleCreate(createdArticle)
                }

                while(true) {
                    val deletedArticle: String = MyArticleFactory.getDeletedArticles() ?: break

                    onArticleDelete(deletedArticle)
                }
            }
            is ProfileMyLikeFragment -> {
                while(true) {
                    val createdArticle: ArticleDetail = LikeArticleFactory.getLikedArticle() ?: break

                    onArticleCreate(createdArticle)
                }

                while(true) {
                    val deletedArticle: String = LikeArticleFactory.getDeletedArticles() ?: break

                    onArticleDelete(deletedArticle)
                }
            }
        }

    }
}