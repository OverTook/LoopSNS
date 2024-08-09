package com.hci.loopsns

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.hci.loopsns.storage.NightMode
import com.hci.loopsns.storage.SettingManager
import com.kakao.sdk.common.KakaoSdk
import org.litepal.LitePal

class MainApplication : Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(SettingManager.getInstance(base).getCurrentLocaleContext(base))
    }

    override fun onCreate() {
        super.onCreate()

        val settingManager = SettingManager.getInstance(this)
        LitePal.initialize(this)
        KakaoSdk.init(this, "df17ea99e1579611972ffbb1ff069e51")

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