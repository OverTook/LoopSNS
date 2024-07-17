package com.hci.loopsns.recyclers.timeline

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.hci.loopsns.R
import com.hci.loopsns.network.Article
import com.hci.loopsns.network.KakaoResponse
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class HotArticleSheetFragment(private val articleClickAction: (Article) -> Unit, private val intent: Intent, private val article: Article, private val lat: Double, private val lng: Double) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.bottom_sheet_map_overview_timeline_short, container, false)


        val client = OkHttpClient()

        // 요청 URL과 파라미터 설정
        val baseUrl = "https://dapi.kakao.com/v2/local/geo/coord2address"
        val url = baseUrl.toHttpUrlOrNull()?.newBuilder()?.apply {
            addQueryParameter("x", lng.toString())
            addQueryParameter("y", lat.toString())
        }?.build()

        // 요청 생성
        val request = Request.Builder()
            .url(url!!)
            .header("Authorization", "KakaoAK ecac7e74ea9e428b92e9edaa3786e729")
            .build()

        // 요청 실행
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Kakao Local Error", e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()?.trimIndent() ?: return

                    val gson = Gson()
                    val kakaoResponse = gson.fromJson(responseBody, KakaoResponse::class.java)

                    val roadAddress = kakaoResponse.documents[0].roadAddress
                    val address = kakaoResponse.documents[0].address

                    view.rootView.post {
                        if(roadAddress != null) {
                            view.findViewById<TextView>(R.id.location_name).text = roadAddress.buildingName
                            view.findViewById<TextView>(R.id.location_address).text = roadAddress.addressName
                        } else if(address != null) {
                            view.findViewById<TextView>(R.id.location_name).text = address.addressName
                            view.findViewById<TextView>(R.id.location_address).text = "건물 없음"
                        } else {
                            view.findViewById<TextView>(R.id.location_name).text = "위치 조회 실패"
                            view.findViewById<TextView>(R.id.location_address).text = "위치 조회 실패"
                        }
                    }



                } else {
                    Log.e("Kakao Local Error", "Request failed: ${response.code}")
                }
            }
        })


        view.findViewById<TextView>(R.id.tag_1).text = article.cat1
        view.findViewById<TextView>(R.id.tag_2).text = article.cat2

        val keywordIds = listOf(
            R.id.keyword_1,
            R.id.keyword_2,
            R.id.keyword_3,
            R.id.keyword_4
        )

        for (i in 0 until article.keywords.size) {
            if (i < keywordIds.size) {
                view.findViewById<TextView>(keywordIds[i]).visibility = View.VISIBLE
                view.findViewById<TextView>(keywordIds[i]).text = article.keywords[i]
            }
        }

        view.findViewById<Button>(R.id.all_view_btn).setOnClickListener {
            dismiss()
            intent.putExtra("name", view.findViewById<TextView>(R.id.location_name).text)
            intent.putExtra("address", view.findViewById<TextView>(R.id.location_address).text)
            startActivity(intent)
        }

        view.findViewById<TextView>(R.id.content_text).text = article.contents

        view.findViewById<TextView>(R.id.like_count).text = article.likeCount.toString()
        view.findViewById<TextView>(R.id.comment_count).text = article.commentCount.toString()
        view.findViewById<ConstraintLayout>(R.id.hot_article_overview).setOnClickListener {
            articleClickAction(article)
        }
        return view
    }
}