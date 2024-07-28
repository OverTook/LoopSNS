package com.hci.loopsns.view.fragment.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hci.loopsns.R
import com.hci.loopsns.network.ArticleDetail
import com.hci.loopsns.network.MyArticleResponse
import com.hci.loopsns.network.NetworkManager
import com.hci.loopsns.recyclers.profile.ProfileRecyclerViewAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
                adapter.insertArticle(articles)
            }

            override fun onFailure(call: Call<MyArticleResponse>, err: Throwable) {
                Log.e("RetrieveLikeArticle Failed", err.toString())
            }
        })
    }

    override fun onInitializeArticle() {
        NetworkManager.apiService.retrieveLikeArticle(
            adapter.getLastArticle()
        ).enqueue(object : Callback<MyArticleResponse> {
            override fun onResponse(call: Call<MyArticleResponse>, response: Response<MyArticleResponse>) {
                if(!response.isSuccessful) return

                val articles = response.body()!!.articles
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
}