package com.hci.loopsns.utils

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.hci.loopsns.R
import com.hci.loopsns.network.NetworkManager
import java.util.concurrent.TimeUnit

open class AuthAppCompatActivity : AppCompatActivity() {

    object TimeHolder {
        private var lastUpdatedTime: Long = System.currentTimeMillis()

        fun setValue(value: Long) {
            this.lastUpdatedTime = value
        }

        // Long 값을 가져오는 메서드
        fun getValue(): Long {
            return lastUpdatedTime
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentTime = System.currentTimeMillis()
        val timeElapsed = currentTime - TimeHolder.getValue()
        if (timeElapsed >= TimeUnit.MINUTES.toMillis(50)) {
            TimeHolder.setValue(currentTime)
            refreshToken()
        }
    }

    override fun onResume() {
        super.onResume()

        val currentTime = System.currentTimeMillis()
        val timeElapsed = currentTime - TimeHolder.getValue()
        if (timeElapsed >= TimeUnit.MINUTES.toMillis(50)) {
            TimeHolder.setValue(currentTime)
            refreshToken()
        }
    }

    private fun refreshToken() {
        val mAuth = FirebaseAuth.getInstance()
        val user = mAuth.currentUser ?: return

        user.getIdToken(true)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val idToken = task.result.token
                    NetworkManager.initNetworkManager(idToken.toString(), user.uid)
                } else {
                    Snackbar.make(
                        findViewById(R.id.main),
                        "Token 값이 유효하지 않습니다.",
                        Snackbar.LENGTH_SHORT
                    ).show();
                }
            }
    }
}