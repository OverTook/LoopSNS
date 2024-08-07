package com.hci.loopsns.view.fragment.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.hci.loopsns.ArticleDetailActivity
import com.hci.loopsns.event.ArticleListener
import com.hci.loopsns.event.ArticleManager
import com.hci.loopsns.event.AutoRefresherInterface
import com.hci.loopsns.event.CommentListener
import com.hci.loopsns.event.CommentManager
import com.hci.loopsns.event.FavoriteListener
import com.hci.loopsns.event.FavoriteManager
import com.hci.loopsns.network.ArticleDetail
import com.hci.loopsns.network.Comment
import com.hci.loopsns.recyclers.profile.ProfileRecyclerViewAdapter

abstract class BaseProfileFragment : Fragment(), ArticleListener, FavoriteListener, CommentListener,
    AutoRefresherInterface {

    protected lateinit var recyclerView: RecyclerView
    protected lateinit var adapter: ProfileRecyclerViewAdapter

    abstract fun onInitializeArticle()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        FavoriteManager.getInstance().registerFavoriteListener(this)
        ArticleManager.getInstance().registerArticleListener(this)
        CommentManager.getInstance().registerCommentListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        FavoriteManager.getInstance().removeFavoriteListener(this)
        ArticleManager.getInstance().removeArticleListener(this)
        CommentManager.getInstance().removeCommentListener(this)
    }

    fun isInitialized(): Boolean {
        return this::adapter.isInitialized
    }

    fun onClickArticle(uid: String) {
        val intent = Intent(
            requireActivity(),
            ArticleDetailActivity::class.java
        )
        intent.putExtra("articleId", uid)
        startActivity(intent)
    }

    override fun onArticleCreated(article: ArticleDetail) {
        if(this !is ProfileMyArticleFragment) return
        //게시글 생성은 내가 작성한 게시글 목록에만 추가

        adapter.createArticle(article)
    }

    override fun onArticleDeleted(articleId: String) {
        //둘 다 해당
        adapter.deleteArticle(articleId)
    }

    override fun onFavoriteArticle(article: ArticleDetail) {
        //좋아요 갱신도 둘 다 해당
        if(this is ProfileMyArticleFragment) {
            adapter.updateLikeCount(article.uid, 1)
            return
        }

        //좋아요 목록에는 추가
        adapter.createArticle(article)
    }

    override fun onUnfavoriteArticle(articleId: String) {
        if(this is ProfileMyArticleFragment) {
            adapter.updateLikeCount(articleId, -1)
            return
        }

        adapter.deleteArticle(articleId)
    }


    override fun onCommentDeleted(uid: String) {
        adapter.updateCommentCount(uid, -1)
    }

    override fun onCommentCreated(comment: Comment) {
        adapter.updateCommentCount(comment.uid, +1)
    }
}