package com.hci.loopsns.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hci.loopsns.MainActivity
import com.hci.loopsns.R
import com.hci.loopsns.network.AddFcmTokenRequest
import com.hci.loopsns.network.FcmTokenResponse
import com.hci.loopsns.network.NetworkManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoopFirebaseMessagingService : FirebaseMessagingService() {
    companion object {
        private const val TAG = "FirebaseMessagingService"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "new Token ${token}")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "From : ${message.from}")

        if (message.data.isNotEmpty()) {
            scheduleJob()
        } else {
            handleNow()
        }

        //알람 내용이 비어 있지 않은 경우
        if (message.notification!= null) {
            setNotification(message)
        }
    }

    // 메시지에 데이터 페이로드가 포함 되어 있을 때 실행되는 메서드
    // 장시간 실행 (10초 이상) 작업의 경우 WorkManager를 사용하여 비동기 작업 예약
    private fun scheduleJob() {
        val work = OneTimeWorkRequest.Builder(MyWorker::class.java)
            .build()
        WorkManager.getInstance(this)
            .beginWith(work)
            .enqueue()
    }

    // 메시지에 데이터 페이로드가 포함 되어 있을 때 실행되는 메서드
    // 10초 이내로 걸릴 때 메시지를 처리한다.
    private fun handleNow() {
        Log.d(TAG, "Short lived task is done.")
    }

    private fun setNotification(message : RemoteMessage) {
        val channelId = "fcm_set_notification_channel" // 알림 채널 이름
        val channelName = "fcm_set_notification"
        val channelDescription = "fcm_send_notify"

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 오레오 버전 이후에는 채널이 필요
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH// 중요도 (HIGH: 상단바 표시 가능)
            val channel = NotificationChannel(channelId, channelName, importance).apply{
                description= channelDescription
            }
            notificationManager.createNotificationChannel(channel)
        }

        // RequestCode, Id를 고유값으로 지정하여 알림이 개별 표시
        val uniId: Int = (System.currentTimeMillis() / 7).toInt()

        // 일회용 PendingIntent : Intent 의 실행 권한을 외부의 어플리케이션에게 위임
        val intent = Intent(this, MainActivity::class.java)

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // Activity Stack 을 경로만 남김(A-B-C-D-B => A-B)

        //PendingIntent.FLAG_MUTABLE은 PendingIntent의 내용을 변경할 수 있도록 허용, PendingIntent.FLAG_IMMUTABLE은 PendingIntent의 내용을 변경할 수 없음
        val pendingIntent = if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(this, uniId, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getActivity(this, uniId, intent, PendingIntent.FLAG_IMMUTABLE)
        }

        val title: String? = message.notification?.title
        val body: String? = message.notification?.body

        // 알림에 대한 UI 정보, 작업
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // 중요도 (HIGH: 상단바 표시 가능)
            .setSmallIcon(R.mipmap.ic_launcher) // 아이콘 설정
            .setContentTitle(title) // 제목
            .setContentText(body) // 메시지 내용
            .setAutoCancel(true) // 알람클릭시 삭제여부
            .setContentIntent(pendingIntent) // 알림 실행 시 Intent

        notificationManager.notify(uniId, notificationBuilder.build())
    }

    internal class MyWorker(appContext: Context, workerParams: WorkerParameters) :
        Worker(appContext, workerParams) {
        override fun doWork(): Result {
            return Result.success()
        }
    }
}