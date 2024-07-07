package com.contest.kdbstartup

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.contest.kdbstartup.network.ArticleMarkersResponse
import com.contest.kdbstartup.network.NetworkManager
import com.google.android.material.snackbar.Snackbar
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MapOverviewActivity : AppCompatActivity() {

    lateinit var mapView: MapView
    lateinit var kakaoMap: KakaoMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_map_overview)



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mapView = findViewById<MapView>(R.id.kakaoMap)
        mapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
                // 지도 API 가 정상적으로 종료될 때 호출됨
            }

            override fun onMapError(error: Exception) {
                // 인증 실패 및 지도 사용 중 에러가 발생할 때 호출됨
            }
        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(kakaoMap: KakaoMap) {
                this@MapOverviewActivity.kakaoMap = kakaoMap

                initMap()
            }


        })
    }

    fun initMap(){
        kakaoMap.setOnCameraMoveEndListener { _, _, _ ->
            requestMaker()
        }
    }

    private fun requestMaker(){
        val rightTop = kakaoMap.fromScreenPoint(mapView.width, 0) !!//제일 높음
        val leftBottom = kakaoMap.fromScreenPoint(0, mapView.height)!! //제일 낮음

        NetworkManager.apiService.retrieveArticleMarker(leftBottom.latitude, leftBottom.longitude, rightTop.latitude, rightTop.longitude).enqueue(object : Callback<ArticleMarkersResponse> {
            override fun onResponse(call: Call<ArticleMarkersResponse>, response: Response<ArticleMarkersResponse>) {
                if(!response.isSuccessful) {
                    Snackbar.make(findViewById(R.id.main), "마커 요청에 실패했습니다.", Snackbar.LENGTH_SHORT).show();
                    return
                }

                if(!response.body()!!.success) {
                    Log.e("Marker Request Error", response.body()!!.msg)
                    Snackbar.make(findViewById(R.id.main), "마커 요청에 실패했습니다.", Snackbar.LENGTH_SHORT).show();
                    return
                }

                //마커 그리기 7월 8일 TODO
            }

            override fun onFailure(call: Call<ArticleMarkersResponse>, err: Throwable) {
                Log.e("Marker Request Error", err.toString())
                Snackbar.make(findViewById(R.id.main), "마커 요청에 실패했습니다.", Snackbar.LENGTH_SHORT).show();
            }
        })
    }
    public override fun onResume() {
        super.onResume()
        mapView.resume() // MapView 의 resume 호출
    }

    public override fun onPause() {
        super.onPause()
        mapView.pause() // MapView 의 pause 호출
    }
}