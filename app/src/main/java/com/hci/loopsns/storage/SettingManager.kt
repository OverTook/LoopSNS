package com.hci.loopsns.storage

import android.app.UiModeManager
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import com.yariksoffice.lingver.Lingver
import java.util.Locale

object NightMode {
    val DAY = 0
    val NIGHT = 1
    val SYSTEM = 2
}

class SettingManager {

    companion object {
        private const val PREF_NAME = "settings"
        private const val KEY_NIGHT_MODE = "night_mode"

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

    private val nightModeListeners: ArrayList<OnNightModeChangeListener> = ArrayList()

    fun registerNightModeCallback(listener: OnNightModeChangeListener) {
        nightModeListeners.add(listener)
    }

    fun unregisterNightModeCallback(listener: OnNightModeChangeListener) {
        nightModeListeners.remove(listener)
    }

    fun isNightMode(context: Context): Boolean {
        val nightValue = sharedPreferences.getInt(KEY_NIGHT_MODE, NightMode.SYSTEM)
        if (nightValue != NightMode.SYSTEM) {
            return nightValue == NightMode.DAY
        }

        return when (context.resources.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> true
            Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> false
            else -> false
        }
    }

    fun getNightMode(): Int {
        return sharedPreferences.getInt(KEY_NIGHT_MODE, NightMode.SYSTEM)
    }

    fun setNightMode(context: Context, mode: Int) {
        nightModeListeners.forEach {
            it.onChangedNightMode(context, when (mode.and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_YES -> true
                Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> false
                else -> false
            })
        }

        editor.putInt(KEY_NIGHT_MODE, mode).apply()

//        when (mode) {
//            NightMode.DAY -> uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_NO)
//            NightMode.NIGHT -> uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_YES)
//            NightMode.SYSTEM -> uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_AUTO)
//        }

        when (mode) {
            NightMode.DAY -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            NightMode.NIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            NightMode.SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    fun setLocale(context: Context, language: String) {
        Lingver.getInstance().setLocale(context, language)
        // 설정을 적용하기 위해 액티비티 재시작 (옵션)
//        val refresh = Intent(context, MainActivity::class.java)
//        refresh.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//        context.startActivity(refresh)
    }
}

interface OnNightModeChangeListener {
    fun onChangedNightMode(context: Context, isNightTheme: Boolean)
}