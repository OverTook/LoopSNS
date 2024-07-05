package com.contest.kdbstartup.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit

object NetworkManager {
    private const val BASE_URL = "http://csgpu.kku.ac.kr:5126/"

    private lateinit var retrofit: Retrofit
    private lateinit var apiService: NetworkInterface
    public fun initNetworkManager(firebaseToken: String, uuid: String) {
        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(TokenInterceptor(firebaseToken, uuid))
                    .build()
            )
            .build()

        apiService = retrofit.create(NetworkInterface::class.java)
    }
}