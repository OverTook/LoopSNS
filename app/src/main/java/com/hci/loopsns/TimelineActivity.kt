package com.hci.loopsns

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.hci.loopsns.network.Article
import com.hci.loopsns.network.ArticleDetailResponse
import com.hci.loopsns.network.NetworkManager
import com.hci.loopsns.recyclers.timeline.ArticleRecyclerViewAdapter
import com.hci.loopsns.utils.hideDarkOverlay
import com.hci.loopsns.utils.showDarkOverlay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class TimelineActivity : AppCompatActivity() {

    private lateinit var adapter: ArticleRecyclerViewAdapter
    private lateinit var recyclerView: RecyclerView

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_overview_timeline_long)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.longFragment)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.article_recycler)

        val articleList = IntentCompat.getParcelableArrayListExtra(intent, "articles", Article::class.java)


        if (articleList.isNullOrEmpty()) {
            adapter = ArticleRecyclerViewAdapter(::onClickArticle, emptyList())
            recyclerView.adapter = adapter
            return
        }

        adapter = ArticleRecyclerViewAdapter(::onClickArticle, articleList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<TextView>(R.id.location_name).text = intent.getStringExtra("name")
        findViewById<TextView>(R.id.location_address).text = intent.getStringExtra("address")
    }

    private fun onClickArticle(article: Article) {
        this.showDarkOverlay()
        NetworkManager.apiService.retrieveArticleDetail(article.uid).enqueue(object : Callback<ArticleDetailResponse> {
            override fun onResponse(call: Call<ArticleDetailResponse>, response: Response<ArticleDetailResponse>) {
                this@TimelineActivity.hideDarkOverlay()
                if(!response.isSuccessful){
                    Snackbar.make(findViewById(R.id.longFragment), "게시글 정보를 불러올 수 없습니다.", Snackbar.LENGTH_SHORT).show()
                    return
                }

                val articleDetail = response.body()!!.article
                val comments = response.body()!!.comments


                val intent = Intent(
                    this@TimelineActivity,
                    ArticleDetailActivity::class.java
                )
                intent.putExtra("article", articleDetail)
                intent.putParcelableArrayListExtra("comments", ArrayList(comments))
                startActivity(intent)
            }

            override fun onFailure(call: Call<ArticleDetailResponse>, err: Throwable) {
                this@TimelineActivity.hideDarkOverlay()
                Log.e("ArticleDetailActivity", "게시글 불러오기 실패$err")
            }
        })
    }
}