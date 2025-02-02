package com.example.swuni_up

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class NotificationHelper(private val context: Context) {

    private val channelId = "push_notification_channel"
    private val channelName = "Push Notification Channel"

    // 알림 채널 생성
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    // 알림 권한 요청 및 알림 설정
    fun requestNotification(activity: MainActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // 권한이 없으면 권한 요청
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
            } else {
                // 권한이 있으면 알림 설정
                setDailyNotification()
            }
        } else {
            setDailyNotification()
        }
    }

    // 알림 설정
    private fun setDailyNotification() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = NotificationReceiver.createIntent(context)  // 알림을 받을 리시버 설정

        // PendingIntent 생성
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // 시간 설정(PM 12:00)
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 12)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)

        // 반복 설정
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,  // 하루 한 번 반복
            pendingIntent
        )
    }

    // 권한 요청 결과 처리
    fun handlePermissionsResult(requestCode: Int, grantResults: IntArray, activity: MainActivity) {
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한 승인 > 알림 설정
                setDailyNotification()
            } else {
                // 권한 거부 > 토스트 메시지
                Toast.makeText(activity, "알림을 받지 않습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
