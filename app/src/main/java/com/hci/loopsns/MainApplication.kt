package com.hci.loopsns

import android.app.Application
import android.content.res.Resources
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import com.hci.loopsns.network.NetworkManager
import com.hci.loopsns.storage.NightMode
import com.hci.loopsns.storage.SettingManager
import com.kakao.sdk.common.KakaoSdk
import com.yariksoffice.lingver.Lingver
import org.litepal.LitePal

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val settingManager = SettingManager.getInstance(this)
        LitePal.initialize(this)
        KakaoSdk.init(this, "df17ea99e1579611972ffbb1ff069e51")

        if (settingManager.getLocaleIndex() == SettingManager.SupportedLanguage.size) {
            val systemLocale = Resources.getSystem().configuration.locales[0].language
            Lingver.init(this, systemLocale)
        } else {
            Lingver.init(this, SettingManager.SupportedLanguage[settingManager.getLocaleIndex()])
        }

        when (settingManager.getNightMode()) {
            NightMode.DAY -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            NightMode.NIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            NightMode.SYSTEM -> {
                when(settingManager.isSystemNightMode(this)) {
                    true -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    false -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }
        }


    }

}