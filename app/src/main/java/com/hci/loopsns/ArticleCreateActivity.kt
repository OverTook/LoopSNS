package com.hci.loopsns

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import com.hci.loopsns.network.KakaoResponse
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class ArticleCreateActivity : AppCompatActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_writing_wpicture_category)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        val x = intent.getDoubleExtra("x", 0.0)
        val y = intent.getDoubleExtra("y", 0.0)
        getLocation(x, y)
    }

    fun getLocation(x: Double, y: Double) {
        val baseUrl = "https://dapi.kakao.com/v2/local/geo/coord2address"
        val url = baseUrl.toHttpUrlOrNull()?.newBuilder()?.apply {
            addQueryParameter("x", x.toString())
            addQueryParameter("y", y.toString())
        }?.build()

        // 요청 생성
        val request = Request.Builder()
            .url(url!!)
            .header("Authorization", "KakaoAK ecac7e74ea9e428b92e9edaa3786e729")
            .build()

        // 요청 실행
        val client = OkHttpClient()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("Kakao Local Error", e.toString())
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()?.trimIndent() ?: return

                    val gson = Gson()
                    val kakaoResponse = gson.fromJson(responseBody, KakaoResponse::class.java)

                    runOnUiThread {
                        findViewById<TextView>(R.id.locationEditText).text = kakaoResponse.documents[0].address!!.addressName
                    }
                } else {
                    Log.e("Kakao Local Error", "Request failed: ${response.code}")
                }
            }
        })
    }
}