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
        val channelId = "push_notification_channel"

        val notification: Notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("슈니업!") // 알림 제목
            .setContentText("챌린지 참여 잊지 않으셨나요?") //알림 내용
            .setSmallIcon(R.drawable.up_logo)  // 알림 아이콘(현재 이미지 깨져서 수정 필요)
            .build()

        notificationManager.notify(1, notification)
    }
}
