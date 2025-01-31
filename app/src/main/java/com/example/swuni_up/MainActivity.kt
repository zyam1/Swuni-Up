package com.example.swuni_up

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.app.NotificationManager
import android.app.NotificationChannel

class MainActivity : AppCompatActivity() {

    private val channelId = "push_notification_channel"
    private val channelName = "Push Notification Channel"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 알림 채널 설정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        // Android 13(API 33) 이상에서 알림 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                //권한이 없으면 권한 요청
                ActivityCompat.requestPermissions(
                    this,
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

    // 권한 요청 결과
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // 1일 경우 처리
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한 승인 > 알림 설정
                setDailyNotification()
            } else {
                // 권한 거부 > 토스트 메시지
                Toast.makeText(this, "알림을 받지 않습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 알람 설정
    private fun setDailyNotification() {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationReceiver::class.java)  // 알림을 받을 리시버 설정

        // PendingIntent 생성
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // 시간 설정(PM 12:00)
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 14)
        calendar.set(java.util.Calendar.MINUTE, 46)
        calendar.set(java.util.Calendar.SECOND, 0)

        // 반복 설정
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,  // 하루 한 번 반복
            pendingIntent
        )
    }
}
