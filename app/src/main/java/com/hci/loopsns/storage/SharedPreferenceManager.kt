package com.hci.loopsns.storage

import android.content.Context
import android.content.SharedPreferences

class SharedPreferenceManager(context: Context) {
    companion object {
        private const val PREF_NAME = "user_profile"
        private const val KEY_IMAGE_URL = "profile_url"
        private const val KEY_NICKNAME = "nickname"
        private const val KEY_EMAIL = "email"
        private const val KEY_FCM_TOKEN = "fcm_token"
        private const val KEY_FCM_TOKEN_CHANGED = "fcm_token_changed"
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    fun saveFcmToken(token: String, tokenChanged: Boolean = true) {
        editor.putString(KEY_FCM_TOKEN, token)
            .putBoolean(KEY_FCM_TOKEN_CHANGED, tokenChanged)
            .apply()
    }

    fun getFcmToken(): Pair<String?, Boolean> {
        return Pair(
            sharedPreferences.getString(KEY_FCM_TOKEN, null),
            sharedPreferences.getBoolean(KEY_FCM_TOKEN_CHANGED, false)
        )
    }

    fun saveProfileInfo(imageURL: String, nickname: String, email: String) {
        editor.putString(KEY_IMAGE_URL, imageURL)
            .putString(KEY_NICKNAME, nickname)
            .putString(KEY_EMAIL, email)
            .apply()
    }

    fun getImageURL(): String? {
        return sharedPreferences.getString(KEY_IMAGE_URL, null)
    }

    fun getNickname(): String? {
        return sharedPreferences.getString(KEY_NICKNAME, null)
    }

    fun getEmail(): String? {
        return sharedPreferences.getString(KEY_EMAIL, null)
    }

    fun clearProfileInfo() {
        editor.clear()
        editor.apply()
    }
}