package com.hci.loopsns

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.hci.loopsns.network.Article
import com.hci.loopsns.network.ArticleDetail
import com.hci.loopsns.network.ArticleDetailResponse
import com.hci.loopsns.network.Comment
import com.hci.loopsns.network.NetworkInterface
import com.hci.loopsns.network.NetworkManager
import com.hci.loopsns.recyclers.detail.ArticleRecyclerViewAdapter
import com.hci.loopsns.utils.showDarkOverlay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ArticleDetailActivity : AppCompatActivity() {

    private lateinit var adapter: ArticleRecyclerViewAdapter
    private lateinit var recyclerView: RecyclerView

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_article_detail)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val article: ArticleDetail? = IntentCompat.getParcelableExtra(intent, "article", ArticleDetail::class.java)
        val comments = IntentCompat.getParcelableArrayListExtra(intent, "comments", Comment::class.java)
        if (article == null) {
            Log.e("ArticleDetailActivity", "Article Null")
            return
        }

        recyclerView = findViewById(R.id.article_recycler_view)

        if (comments.isNullOrEmpty()) {
            adapter = ArticleRecyclerViewAdapter(this, article, emptyList())
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(this)
            return
        }

        adapter = ArticleRecyclerViewAdapter(this, article, comments)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val input: EditText = findViewById(R.id.comment_input)
        input.setOnKeyListener(View.OnKeyListener { _, keyCode, event -> //Enter key Action
            if ((event.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                if(input.text.isNullOrBlank()) {
                    return@OnKeyListener true
                }

                //sendAndWait(input.text.toString())
                input.setText("")

                return@OnKeyListener true
            }
            false
        })

    }

    private fun createComment() {

    }

}