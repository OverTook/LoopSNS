package com.hci.loopsns.view.fragment.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.hci.loopsns.ArticleDetailActivity
import com.hci.loopsns.R
import com.hci.loopsns.network.ArticleDetail
import com.hci.loopsns.network.ArticleDetailResponse
import com.hci.loopsns.network.MyArticleResponse
import com.hci.loopsns.network.NetworkManager
import com.hci.loopsns.recyclers.profile.ProfileRecyclerViewAdapter
import com.hci.loopsns.utils.hideDarkOverlay
import com.hci.loopsns.utils.showDarkOverlay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.HTTP

class ProfileMyLikeFragment : BaseProfileFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProfileRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.child_fragment_profile, container, false)

        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

        adapter = ProfileRecyclerViewAdapter(this, ArrayList())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        return view
    }

    override fun requestMoreArticle() {
        NetworkManager.apiService.retrieveLikeArticle(
            adapter.getLastArticle()
        ).enqueue(object : Callback<MyArticleResponse> {
            override fun onResponse(call: Call<MyArticleResponse>, response: Response<MyArticleResponse>) {
                if(!response.isSuccessful) return

                val articles = response.body()!!.articles
                if(articles.isNullOrEmpty()) {
                    return
                }
                adapter.insertArticle(articles)
            }

            override fun onFailure(call: Call<MyArticleResponse>, err: Throwable) {
                Log.e("RetrieveLikeArticle Failed", err.toString())
            }
        })
    }

    override fun onInitializeArticle() {
        NetworkManager.apiService.retrieveLikeArticle().enqueue(object : Callback<MyArticleResponse> {
            override fun onResponse(call: Call<MyArticleResponse>, response: Response<MyArticleResponse>) {
                if(!response.isSuccessful) return

                val articles = response.body()!!.articles
                if(articles.isNullOrEmpty()) {
                    return
                }
                adapter.insertArticle(articles)
            }

            override fun onFailure(call: Call<MyArticleResponse>, err: Throwable) {
                Log.e("RetrieveLikeArticle Failed", err.toString())
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
        requireActivity().showDarkOverlay()
        NetworkManager.apiService.retrieveArticleDetail(uid).enqueue(object : Callback<ArticleDetailResponse> {
            override fun onResponse(call: Call<ArticleDetailResponse>, response: Response<ArticleDetailResponse>) {
                requireActivity().hideDarkOverlay()
                if(!response.isSuccessful){
                    if(response.code() == 404) {
                        //게시글 찾을 수 없음
                        adapter.deleteArticle(uid)
                        Snackbar.make(requireActivity().findViewById(R.id.main), "이미 삭제된 게시글입니다.", Snackbar.LENGTH_SHORT).show()
                        return
                    }
                    Snackbar.make(requireActivity().findViewById(R.id.main), "게시글 정보를 불러올 수 없습니다.", Snackbar.LENGTH_SHORT).show()
                    return
                    return
                }

                val articleDetail = response.body()!!.article
                val comments = response.body()!!.comments

                if(articleDetail.writer == null) {
                    articleDetail.writer = "알 수 없는 사용자"
                    articleDetail.userImg = ""
                }
                comments.forEach { comment ->
                    if(comment.writer == null) {
                        comment.writer = "알 수 없는 사용자"
                        comment.userImg = ""
                    }
                }

                val intent = Intent(
                    requireActivity(),
                    ArticleDetailActivity::class.java
                )
                intent.putExtra("article", articleDetail)
                intent.putParcelableArrayListExtra("comments", ArrayList(comments))
                startActivity(intent)
            }

            override fun onFailure(call: Call<ArticleDetailResponse>, err: Throwable) {
                requireActivity().hideDarkOverlay()
                Log.e("ArticleDetailActivity", "게시글 불러오기 실패$err")
            }
        })
    }

    override fun onResume() {
        super.onResume()

        Log.e("OnResume", "ProfileMyLikeFragment")
    }
}