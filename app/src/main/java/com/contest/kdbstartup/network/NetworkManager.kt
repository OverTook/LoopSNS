package com.contest.kdbstartup.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit

object NetworkManager {
    private const val BASE_URL = "http://csgpu.kku.ac.kr:8080/"


    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(
            OkHttpClient.Builder()
            .addInterceptor(UIDInterceptor())
            .build()
        )
        .build()

    val apiService: NetworkInterface = retrofit.create(NetworkInterface::class.java)
}