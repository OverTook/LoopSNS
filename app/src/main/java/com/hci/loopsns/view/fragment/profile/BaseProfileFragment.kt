package com.hci.loopsns.view.fragment.profile

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.hci.loopsns.network.ArticleDetail

abstract class BaseProfileFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onInitializeArticle()
    }

    abstract fun requestMoreArticle()
    abstract fun onInitializeArticle()
    abstract fun onArticleDelete(uid: String)
    abstract fun onArticleCreate(articleDetail: ArticleDetail)
}