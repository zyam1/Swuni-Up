package com.example.swuni_up

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.swuni_up.R

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "push_notification_channel"  // 알림 채널 ID

        // 알림 생성
        val notification: Notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("슈니업!") // 알림 제목
            .setContentText("챌린지 참여 잊지 않으셨나요?") // 알림 내용
            .setSmallIcon(R.drawable.up)  // 알림 아이콘(48dpx48dp로 준비)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(1, notification)
    }

    // 알림을 위한 Intent를 생성하는 메서드
    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, NotificationReceiver::class.java)
        }
    }
}
