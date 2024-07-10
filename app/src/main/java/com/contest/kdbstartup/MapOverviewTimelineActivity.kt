package com.contest.kdbstartup

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.contest.kdbstartup.network.Article
import com.contest.kdbstartup.network.KakaoResponse
import com.contest.kdbstartup.timeline.ArticleRecyclerViewAdapter
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException


class MapOverviewTimelineActivity : AppCompatActivity() {

    private lateinit var adapter: ArticleRecyclerViewAdapter
    private lateinit var recyclerView: RecyclerView

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_map_overview_timeline_long)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.longFragment)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.article_recycler)

        val articleList = IntentCompat.getParcelableArrayListExtra(intent, "articles", Article::class.java)


        if (articleList.isNullOrEmpty()) {
            adapter = ArticleRecyclerViewAdapter(emptyList())
            recyclerView.adapter = adapter
            return
        }

        adapter = ArticleRecyclerViewAdapter(articleList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<TextView>(R.id.location_name).text = intent.getStringExtra("name")
        findViewById<TextView>(R.id.location_address).text = intent.getStringExtra("address")
    }

}