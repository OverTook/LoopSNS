package com.contest.kdbstartup.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkManager {
    private const val BASE_URL = "http://csgpu.kku.ac.kr:5126/"

    private var retrofit: Retrofit = Retrofit.Builder()
                                        .baseUrl(BASE_URL)
                                        .addConverterFactory(GsonConverterFactory.create())
                                        .build()
    var apiService: NetworkInterface = retrofit.create(NetworkInterface::class.java)

    public fun initNetworkManager(firebaseToken: String, uuid: String) {
        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(TokenInterceptor(firebaseToken, uuid))
                    .build()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(NetworkInterface::class.java)
    }
}