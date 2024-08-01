package com.hci.loopsns.network

import okhttp3.Interceptor
import okhttp3.Response


class TokenInterceptor(private val token: NetworkManager.FirebaseToken) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        if(token.token.isEmpty()) {
            return chain.proceed(request)
        }

        val requestBuilder = request.newBuilder()
            .header("Authorization", "Bearer ${token.token}") //Bearer는 OAuth2.0 표준을 따르기 위해 추가하였음
            .header("User-ID", token.uuid) //서버단에서 토큰 정보로 조회한 유저 아이디와 이 헤더의 유저 아이디를 비교하여 검증
            .method(request.method, request.body)
        request = requestBuilder.build()
        return chain.proceed(request)
    }
}