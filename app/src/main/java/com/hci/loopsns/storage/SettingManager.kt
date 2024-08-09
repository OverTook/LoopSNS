package com.hci.loopsns.storage

import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.hci.loopsns.MainActivity
import com.hci.loopsns.R
import java.util.Locale

object NightMode {
    const val DAY = 0
    const val NIGHT = 1
    const val SYSTEM = 2
}

//object NotificationType {
//    const val COMMENT = 1
//    const val HOT_ARTICLE = 2
//    const val FAVORITE_ARTICLE = 4
//}

enum class NotificationType(val code: Int, val string: Int, val enable: Boolean) {
    COMMENT(1, R.string.settings_notification_comment_setting, true),
    HOT_ARTICLE(2, R.string.settings_notification_hot_article_setting, true),
    FAVORITE_ARTICLE(4, R.string.settings_notification_favorite_setting, false)
}

class SettingManager {

    companion object {
        private const val PREF_NAME = "settings"
        private const val KEY_NIGHT_MODE = "night_mode"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_FCM_TOKEN = "fcm_token"
        private const val KEY_NOTIFICATION = "notification"


        val SupportedLanguage = listOf(
            Locale.ENGLISH,
            Locale.KOREAN
        )

        @Volatile
        private var instance: SettingManager? = null

        fun getInstance(context: Context): SettingManager {
            return instance ?: synchronized(this) {
                instance ?: SettingManager().also {
                    instance = it

                    val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                    instance!!.sharedPreferences = sp
                    instance!!.editor = sp.edit()
                }
            }
        }
        fun getInstance(): SettingManager {
            return instance!!
        }
    }

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    fun isSystemNightMode(context: Context): Boolean {
        val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        return uiModeManager.nightMode == UiModeManager.MODE_NIGHT_YES
    }

    fun getNightMode(): Int {
        return sharedPreferences.getInt(KEY_NIGHT_MODE, NightMode.SYSTEM)
    }

    fun setNightMode(context: Context, mode: Int) {
        editor.putInt(KEY_NIGHT_MODE, mode).apply()

//        when (mode) {
//            NightMode.DAY -> uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_NO)
//            NightMode.NIGHT -> uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_YES)
//            NightMode.SYSTEM -> uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_AUTO)
//        }

        when (mode) {
            NightMode.DAY -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            NightMode.NIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            NightMode.SYSTEM -> {
                when(isSystemNightMode(context)) {
                    true -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    false -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }
        }
    }

    fun isSameLanguage(language: String): Boolean {
        return Resources.getSystem().configuration.locales[0].language == language
    }

    fun getLocaleIndex(): Int {
        sharedPreferences.getString(KEY_LANGUAGE, null) ?: return SupportedLanguage.size

        val language = Locale.getDefault().language
        for(i in SupportedLanguage.indices) {
            if (SupportedLanguage[i].language == language) {
                return i
            }
        }
        return SupportedLanguage.size
    }

    fun setLocaleAuto(context: Context) {
        //이미 비어있으면 이미 Auto임
        val currentLocale = sharedPreferences.getString(KEY_LANGUAGE, null) ?: return
        val systemLocale = Resources.getSystem().configuration.locales[0]
        editor.remove(KEY_LANGUAGE).apply()

        //자동으로 했을 때랑 기존 언어가 일치하면 재시작 필요없음
        if(systemLocale.language == currentLocale) return

        //Lingver.getInstance().setLocale(context, systemLocale)
        Locale.setDefault(systemLocale)

        val refresh = Intent(context, MainActivity::class.java)
        refresh.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        context.startActivity(refresh)
    }

    fun getCurrentLocaleContext(context: Context): Context {
        val currentLocale = sharedPreferences.getString(KEY_LANGUAGE, null)
        Log.e("CURRENTLOCALE", currentLocale ?: "Auto")
        if(currentLocale == null) {
            val newConfig = Configuration()
            newConfig.setLocale(Resources.getSystem().configuration.locales[0])
            Locale.setDefault(Resources.getSystem().configuration.locales[0])
            return context.createConfigurationContext(newConfig)
        }
        val newConfig = Configuration()
        newConfig.setLocale(Locale(currentLocale))
        Locale.setDefault(Locale(currentLocale))
        return context.createConfigurationContext(newConfig)
    }

    fun setLocale(context: Context, language: String) {
        if(sharedPreferences.getString(KEY_LANGUAGE, null) == null) { //Auto인가?
            if(Resources.getSystem().configuration.locales[0].language == language) { //Auto 언어와 설정하고자 하는 언어가 같다면
                editor.putString(KEY_LANGUAGE, language).apply()
                return
            }
        }

        Locale.setDefault(Locale(language))
        editor.putString(KEY_LANGUAGE, language).apply()

        val refresh = Intent(context, MainActivity::class.java)
        refresh.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        context.startActivity(refresh)
    }

    fun removeFcmToken() {
        editor.remove(KEY_FCM_TOKEN).apply()
    }

    fun saveFcmToken(token: String, tokenChanged: Boolean = true) {
        editor.putString(KEY_FCM_TOKEN, token)
            .apply()
    }


    fun getFcmToken(): String? {
        return sharedPreferences.getString(KEY_FCM_TOKEN, null)
    }

    fun setNotification(type: NotificationType, state: Boolean) {
        var currentNotification = sharedPreferences.getInt(
            KEY_NOTIFICATION,
            NotificationType.entries
                .filter { it.enable }
                .fold(0) { acc, status ->
                acc or status.code
            }
        )

        currentNotification = if(state) {
            currentNotification or type.code
        } else {
            currentNotification and type.code.inv()
        }

        editor.putInt(KEY_NOTIFICATION, currentNotification).apply()
    }

    fun getNotification(type: NotificationType): Boolean {
        return (sharedPreferences.getInt(KEY_NOTIFICATION,
            NotificationType.entries
                .filter { it.enable }
                .fold(0) { acc, status ->
                acc or status.code
            }
        ) and type.code) == type.code
    }

    fun getNotifications(): Int {
        return sharedPreferences.getInt(KEY_NOTIFICATION,
            NotificationType.entries
                .filter { it.enable }
                .fold(0) { acc, status ->
                acc or status.code
            }
        )
    }
}