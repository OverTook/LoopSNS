package com.hci.loopsns.network

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GetTokenResult
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit


class TokenRefreshInterceptor(private val token: NetworkManager.FirebaseToken) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        try {

            if (request.url.encodedPath.contains("/login") || request.method == "GET") {
                return chain.proceed(request)
            }

            val user = FirebaseAuth.getInstance().currentUser ?: return chain.proceed(request)
            val task: Task<GetTokenResult> = user.getIdToken(false)
            val tokenResult = Tasks.await(task, 20, TimeUnit.SECONDS)
            val token = tokenResult.token

            this.token.token = token!!
            this.token.uuid = user.uid

            return chain.proceed(request)
        } catch (e: Exception) {
            return chain.proceed(request)
        }
    }
}