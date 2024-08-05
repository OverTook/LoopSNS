package com.hci.loopsns.storage

import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.google.protobuf.Internal.BooleanList
import com.hci.loopsns.MainActivity
import com.yariksoffice.lingver.Lingver
import java.util.Locale

object NightMode {
    const val DAY = 0
    const val NIGHT = 1
    const val SYSTEM = 2
}

class SettingManager {

    companion object {
        private const val PREF_NAME = "settings"
        private const val KEY_NIGHT_MODE = "night_mode"
        private const val KEY_LANGUAGE = "language"

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
        val systemLocale = Resources.getSystem().configuration.locales[0].language
        editor.remove(KEY_LANGUAGE).apply()

        //자동으로 했을 때랑 기존 언어가 일치하면 재시작 필요없음
        if(systemLocale == currentLocale) return

        Lingver.getInstance().setLocale(context, systemLocale)

        val refresh = Intent(context, MainActivity::class.java)
        refresh.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        context.startActivity(refresh)
    }

    fun setLocale(context: Context, language: String) {
        if(sharedPreferences.getString(KEY_LANGUAGE, null) == null) { //Auto인가?
            if(Resources.getSystem().configuration.locales[0].language == language) { //Auto 언어와 설정하고자 하는 언어가 같다면
                editor.putString(KEY_LANGUAGE, language).apply()
                return
            }
        }

        Lingver.getInstance().setLocale(context, language)
        editor.putString(KEY_LANGUAGE, language).apply()

        val refresh = Intent(context, MainActivity::class.java)
        refresh.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        context.startActivity(refresh)
    }
}