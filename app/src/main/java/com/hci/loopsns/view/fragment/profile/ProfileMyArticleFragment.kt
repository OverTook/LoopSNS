package com.hci.loopsns.view.fragment.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.hci.loopsns.ArticleDetailActivity
import com.hci.loopsns.event.AutoRefresherInterface
import com.hci.loopsns.R
import com.hci.loopsns.network.ArticleDetail
import com.hci.loopsns.network.MyArticleResponse
import com.hci.loopsns.network.NetworkManager
import com.hci.loopsns.recyclers.profile.ProfileRecyclerViewAdapter
import com.hci.loopsns.utils.registerAutoRefresh
import com.hci.loopsns.utils.requestEnd
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileMyArticleFragment() : BaseProfileFragment(), AutoRefresherInterface {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProfileRecyclerViewAdapter

    override var requested: Boolean = false
    override var noMoreData: Boolean = false
    override lateinit var requestAnimationView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.child_fragment_profile, container, false)

        requestAnimationView = view.findViewById(R.id.requestProgressBar)
        recyclerView = view.findViewById(R.id.recyclerView)

        adapter = ProfileRecyclerViewAdapter(this, ArrayList())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        onInitializeArticle()

        return view
    }

    override fun getRequestAnimation(): View = requestAnimationView

    override fun requestMoreData() {
        NetworkManager.apiService.retrieveMyArticle(
            adapter.getLastArticle()
        ).enqueue(object : Callback<MyArticleResponse> {
            override fun onResponse(call: Call<MyArticleResponse>, response: Response<MyArticleResponse>) {
                if(!response.isSuccessful) {
                    Snackbar.make(requireView().findViewById(R.id.main), "게시글 목록을 받아올 수 없습니다.", Snackbar.LENGTH_SHORT).show()
                    this@ProfileMyArticleFragment.requestEnd(true)
                    return
                }

                val articles = response.body()!!.articles
                if(articles.isNullOrEmpty()) {
                    this@ProfileMyArticleFragment.requestEnd(true)
                    return
                }

                adapter.insertArticles(articles)
                this@ProfileMyArticleFragment.requestEnd(false)
            }

            override fun onFailure(call: Call<MyArticleResponse>, err: Throwable) {
                Snackbar.make(requireView().findViewById(R.id.main), "게시글 목록을 받아올 수 없습니다.", Snackbar.LENGTH_SHORT).show()
                this@ProfileMyArticleFragment.requestEnd(true)
                Log.e("RetrieveMyArticle Failed", err.toString())
            }
        })
    }

    override fun onInitializeArticle() {
        if (noMoreData) return

        NetworkManager.apiService.retrieveMyArticle().enqueue(object : Callback<MyArticleResponse> {
            override fun onResponse(call: Call<MyArticleResponse>, response: Response<MyArticleResponse>) {
                if(!response.isSuccessful) return

                recyclerView.registerAutoRefresh(this@ProfileMyArticleFragment)

                val articles = response.body()!!.articles
                if(articles.isNullOrEmpty()) {
                    noMoreData = true
                    return
                }
                adapter.resetArticles(articles)
                recyclerView.scrollToPosition(0)
            }

            override fun onFailure(call: Call<MyArticleResponse>, err: Throwable) {
                Log.e("RetrieveMyArticle Failed", err.toString())
            }
        })
    }

    override fun onArticleDelete(uid: String) {
        adapter.deleteArticle(uid)
    }

    override fun onArticleCreate(articleDetail: ArticleDetail) {
        adapter.createArticle(articleDetail)
    }

    override fun isInitialized(): Boolean {
        return this::adapter.isInitialized
    }

    override fun onClickArticle(uid: String) {
        val intent = Intent(
            requireActivity(),
            ArticleDetailActivity::class.java
        )
        intent.putExtra("articleId", uid)
        startActivity(intent)
    }
}