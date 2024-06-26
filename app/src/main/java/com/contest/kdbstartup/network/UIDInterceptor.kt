package com.contest.kdbstartup.network

import android.media.MediaDrm
import android.media.UnsupportedSchemeException
import okhttp3.Interceptor
import okhttp3.Response
import java.util.UUID


class UIDInterceptor : Interceptor {

    private val WIDEVINE_SECURITY_LEVEL_1 = "L1"
    private val WIDEVINE_SECURITY_LEVEL_2 = "L2"
    private val WIDEVINE_SECURITY_LEVEL_3 = "L3"
    private val SECURITY_LEVEL_PROPERTY = "securityLevel"

    private val WIDEVINE_UUID = UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L)

    override fun intercept(chain: Interceptor.Chain): Response {
        var secLevel = 0
        var uid: ByteArray? = null
        var secId: String
        try {
            val mediaDrm = MediaDrm(WIDEVINE_UUID)
            val securityProperty: String = mediaDrm.getPropertyString(SECURITY_LEVEL_PROPERTY)

            uid = mediaDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID)

            secLevel = when(securityProperty) {
                WIDEVINE_SECURITY_LEVEL_1 -> 0
                WIDEVINE_SECURITY_LEVEL_2 -> 1
                WIDEVINE_SECURITY_LEVEL_3 -> 2
                else -> 3
            }
        } catch (e: UnsupportedSchemeException) {
            secLevel = 3
        }

        if (uid == null) {
            secId = UUID.randomUUID().toString()
        } else {
            secId = UUID.nameUUIDFromBytes(uid).toString()
        }

        val original = chain.request()
        val requestBuilder = original.newBuilder()
            .header("Security-ID", secId)
            .header("Security-Level", secLevel.toString())
        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}