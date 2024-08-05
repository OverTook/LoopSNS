package com.hci.loopsns

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.yariksoffice.lingver.Lingver
import org.litepal.LitePal

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        LitePal.initialize(this)
        KakaoSdk.init(this, "df17ea99e1579611972ffbb1ff069e51")
        Lingver.init(this)
    }
}