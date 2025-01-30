package com.example.swuni_up

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast

class MainActivity : AppCompatActivity() {


    private lateinit var notificationHelper: NotificationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        notificationHelper = NotificationHelper(this)

        // 알림 채널 설정
        notificationHelper.createNotificationChannel()

        // 알림 권한 요청 및 알림 설정
        notificationHelper.requestNotification(this)
    }

    // 권한 요청 결과
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        notificationHelper.handlePermissionsResult(requestCode, grantResults, this)
    }
}
