package com.hci.loopsns.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.hci.loopsns.LoginActivity
import com.hci.loopsns.MainActivity
import com.hci.loopsns.R
import com.hci.loopsns.storage.SharedPreferenceManager
import com.hci.loopsns.storage.models.NotificationComment
import com.hci.loopsns.utils.factory.NotificationFactory
import org.litepal.LitePal
import java.lang.reflect.Type
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


class LoopFirebaseMessagingService : FirebaseMessagingService() {
    companion object {
        private const val TAG = "FirebaseMessagingService"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "new Token ${token}")
        SharedPreferenceManager(baseContext).saveFcmToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "From : ${message.from}")

        //알람 내용이 비어 있지 않은 경우
        if (message.data.isNotEmpty()) {
            setNotification(message)
        }
        //알람 내용이 비어 있지 않은 경우
//        if (message.notification!= null) {
//        }
    }


    private fun setNotification(message : RemoteMessage) {
        val channelId = "fcm_set_notification_channel" // 알림 채널 이름
        val channelName = "fcm_set_notification"
        val channelDescription = "fcm_send_notify"

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 오레오 버전 이후에는 채널이 필요
        val importance = NotificationManager.IMPORTANCE_HIGH// 중요도 (HIGH: 상단바 표시 가능)
        val channel = NotificationChannel(channelId, channelName, importance).apply{
            description = channelDescription
        }
        notificationManager.createNotificationChannel(channel)

        // RequestCode, Id를 고유값으로 지정하여 알림이 개별 표시
        val uniId: Int = (System.currentTimeMillis() / 7).toInt()

        // 일회용 PendingIntent : Intent 의 실행 권한을 외부의 어플리케이션에게 위임
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // Activity Stack 을 경로만 남김(A-B-C-D-B => A-B)
        intent.putExtra("notification", true)

        val data = message.data

        val type = data["type"]!!
        val body = data["body"]!!

        // 알림에 대한 UI 정보, 작업
        when(type) {
            "comment" -> {
                val comment = GsonBuilder()
                    .registerTypeAdapter(Date::class.java, GsonUTCDateAdapter())
                    .create()
                    .fromJson(body, NotificationComment::class.java)

                intent.putExtra("type", "comment")
                intent.putExtra("highlight", comment)

                if(comment.save()) {
                    NotificationFactory.addNotification(comment)
                } else {
                    //애플리케이션이 실행중이지 않음
                    LitePal.initialize(baseContext)
                    comment.saveThrows()
                }

                val pendingIntent = buildPendingIntent(uniId, intent)

                val notificationBuilder = NotificationCompat.Builder(this, channelId)
                    .setPriority(NotificationCompat.PRIORITY_HIGH) // 중요도 (HIGH: 상단바 표시 가능)
                    .setSmallIcon(R.mipmap.ic_launcher) // 아이콘 설정
                    .setContentTitle(comment.writer) // 제목
                    .setContentText(comment.contents) // 메시지 내용
                    .setAutoCancel(true) // 알람클릭시 삭제여부
                    .setContentIntent(pendingIntent) // 알림 실행 시 Intent

                notificationManager.notify(uniId, notificationBuilder.build())
            }
            else -> {

            }
        }
    }

    fun buildPendingIntent(uniId: Int, intent: Intent): PendingIntent {
        //PendingIntent.FLAG_MUTABLE은 PendingIntent의 내용을 변경할 수 있도록 허용, PendingIntent.FLAG_IMMUTABLE은 PendingIntent의 내용을 변경할 수 없음
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(this, uniId, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getActivity(this, uniId, intent, PendingIntent.FLAG_IMMUTABLE)
        }
    }
}

class GsonUTCDateAdapter : JsonSerializer<Date?>,
    JsonDeserializer<Date?> {
    private val dateFormat: DateFormat =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)

    init {
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
    }

    @Synchronized
    override fun serialize(
        date: Date?,
        type: Type?,
        jsonSerializationContext: JsonSerializationContext?
    ): JsonElement {
        return JsonPrimitive(dateFormat.format(date!!))
    }

    @Synchronized
    override fun deserialize(
        jsonElement: JsonElement,
        type: Type?,
        jsonDeserializationContext: JsonDeserializationContext?
    ): Date {
        try {
            return dateFormat.parse(jsonElement.asString)!!
        } catch (e: ParseException) {
            throw JsonParseException(e)
        }
    }
}