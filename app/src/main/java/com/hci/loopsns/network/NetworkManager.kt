package com.hci.loopsns.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkManager {
    private const val BASE_URL = "http://csgpu.kku.ac.kr:5127/"

    class FirebaseToken (
        var token: String,
        var uuid: String
    )
    val token: FirebaseToken = FirebaseToken("", "")

    // 인증 안된 부분 //
    private var retrofit: Retrofit = Retrofit.Builder()
                                        .baseUrl(BASE_URL)
                                        .client(
                                            OkHttpClient.Builder()
                                                .connectTimeout(30, TimeUnit.SECONDS)
                                                .readTimeout(30, TimeUnit.SECONDS)
                                                .writeTimeout(30, TimeUnit.SECONDS)
                                                .addInterceptor(TokenRefreshInterceptor(token))
                                                .addInterceptor(TokenInterceptor(token))
                                                .build()
                                        )
                                        .addConverterFactory(GsonConverterFactory.create())
                                        .build()

    var apiService: NetworkInterface = retrofit.create(NetworkInterface::class.java)

    // 인증 이후 부분 //
    fun initNetworkManager(firebaseToken: String, uuid: String) {
        token.token = firebaseToken
        token.uuid = uuid
    }
}