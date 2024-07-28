package com.hci.loopsns

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.hci.loopsns.network.Article
import com.hci.loopsns.network.ArticleDeleteResponse
import com.hci.loopsns.network.ArticleDetail
import com.hci.loopsns.network.ArticleDetailResponse
import com.hci.loopsns.network.Comment
import com.hci.loopsns.network.CommentCreateResponse
import com.hci.loopsns.network.CommentDeleteResponse
import com.hci.loopsns.network.CreateCommentRequest
import com.hci.loopsns.network.DeleteArticleRequest
import com.hci.loopsns.network.NetworkInterface
import com.hci.loopsns.network.NetworkManager
import com.hci.loopsns.recyclers.detail.ArticleRecyclerViewAdapter
import com.hci.loopsns.utils.AuthAppCompatActivity
import com.hci.loopsns.utils.SharedPreferenceManager
import com.hci.loopsns.utils.formatTo
import com.hci.loopsns.utils.hideDarkOverlay
import com.hci.loopsns.utils.showDarkOverlay
import com.hci.loopsns.utils.toDate
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ArticleDetailActivity : AuthAppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {

    private lateinit var adapter: ArticleRecyclerViewAdapter
    private lateinit var recyclerView: RecyclerView

    private lateinit var article: ArticleDetail
    private lateinit var comments: ArrayList<Comment>

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContentView(R.layout.activity_article_detail)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        val input: EditText = findViewById(R.id.comment_input)
        input.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                hideKeyboard()
                createComment(input.text.toString())
                input.text.clear()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        findViewById<SwipeRefreshLayout>(R.id.swipeLayout).setOnRefreshListener(this)

        article = IntentCompat.getParcelableExtra(intent, "article", ArticleDetail::class.java)!!
        comments = IntentCompat.getParcelableArrayListExtra(intent, "comments", Comment::class.java)!!

        recyclerView = findViewById(R.id.article_recycler_view)

        if (comments.isEmpty()) {
            adapter = ArticleRecyclerViewAdapter(this, article, ArrayList<Comment>())
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(this)
            return
        }

        adapter = ArticleRecyclerViewAdapter(this, article, comments)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    fun deleteArticle() {
        NetworkManager.apiService.deleteArticle(article.uid).enqueue(object : Callback<ArticleDeleteResponse> {
            override fun onResponse(call: Call<ArticleDeleteResponse>, response: Response<ArticleDeleteResponse>) {
                if(!response.isSuccessful) return

                finish()
            }

            override fun onFailure(call: Call<ArticleDeleteResponse>, err: Throwable) {
                Log.e("ArticleDelete Failed", err.toString())
            }
        })
    }

    fun deleteComment(uid: String) {
        NetworkManager.apiService.deleteComment(article.uid, uid).enqueue(object : Callback<CommentDeleteResponse> {
            override fun onResponse(call: Call<CommentDeleteResponse>, response: Response<CommentDeleteResponse>) {
                if(!response.isSuccessful) return

                adapter.deleteComment(uid)
            }

            override fun onFailure(call: Call<CommentDeleteResponse>, err: Throwable) {
                Log.e("CommentDelete Failed", err.toString())
            }
        })
    }

    private fun createComment(comment: String) {
        NetworkManager.apiService.createComment(
            CreateCommentRequest(
                article.uid,
                comment
            )
        ).enqueue(object : Callback<CommentCreateResponse> {
            override fun onResponse(call: Call<CommentCreateResponse>, response: Response<CommentCreateResponse>) {
                if (!response.isSuccessful) return

                val time = response.body()!!.time.toDate().formatTo("yyyy-MM-dd HH:mm")
                val profile = SharedPreferenceManager(this@ArticleDetailActivity)

                adapter.addComment(
                    Comment(
                        response.body()!!.uid,
                        profile.getNickname()!!,
                        comment,
                        time,
                        profile.getImageURL()!!,
                        true
                    )
                )

                recyclerView.smoothScrollToPosition(1) //최신 댓글로 이동
            }

            override fun onFailure(call: Call<CommentCreateResponse>, err: Throwable) {
                Log.e("CreateComment Failed", err.toString())
            }

        })
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    override fun onRefresh() {
        showDarkOverlay()

        NetworkManager.apiService.retrieveArticleDetail(article.uid).enqueue(object :
            Callback<ArticleDetailResponse> {
            override fun onResponse(call: Call<ArticleDetailResponse>, response: Response<ArticleDetailResponse>) {
                hideDarkOverlay()
                findViewById<SwipeRefreshLayout>(R.id.swipeLayout).isRefreshing = false

                if(!response.isSuccessful){
                    Snackbar.make(findViewById(R.id.main), "게시글 정보를 불러올 수 없습니다.", Snackbar.LENGTH_SHORT).show()
                    return
                }

                article = response.body()!!.article
                comments = ArrayList(response.body()!!.comments)
                if(article.writer == null) {
                    article.writer = "알 수 없는 사용자"
                    article.userImg = ""
                }
                comments.forEach { comment ->
                    if(comment.writer == null) {
                        comment.writer = "알 수 없는 사용자"
                        comment.userImg = ""
                    }
                }

                if (comments.isEmpty()) {
                    adapter = ArticleRecyclerViewAdapter(this@ArticleDetailActivity, article, ArrayList<Comment>())
                    recyclerView.adapter = adapter
                    recyclerView.layoutManager = LinearLayoutManager(this@ArticleDetailActivity)
                    return
                }

                adapter = ArticleRecyclerViewAdapter(this@ArticleDetailActivity, article, comments)
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(this@ArticleDetailActivity)
            }

            override fun onFailure(call: Call<ArticleDetailResponse>, err: Throwable) {
                hideDarkOverlay()
                findViewById<SwipeRefreshLayout>(R.id.swipeLayout).isRefreshing = false
                Log.e("ArticleDetailActivity", "게시글 불러오기 실패$err")
            }
        })
    }
}