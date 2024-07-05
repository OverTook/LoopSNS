package com.contest.kdbstartup.network

import android.media.MediaDrm
import android.media.UnsupportedSchemeException
import okhttp3.Interceptor
import okhttp3.Response
import java.util.UUID


class TokenInterceptor(firebaseToken: String) : Interceptor {

    private var firebaseToken: String

    init {
        if (firebaseToken.isEmpty()) {
            throw IllegalArgumentException("Error")
        }
        this.firebaseToken = firebaseToken
    }
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val requestBuilder = original.newBuilder()
            .header("Authorization", "Bearer $firebaseToken") //Bearer는 OAuth2.0 표준을 따르기 위해 추가하였음
            .method(original.method, original.body)
        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}