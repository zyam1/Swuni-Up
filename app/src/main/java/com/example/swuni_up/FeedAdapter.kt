package com.example.swuni_up

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import android.content.SharedPreferences

class FeedAdapter(
    private val context: Context,
    private val logList: List<DBHelper.LogEntry>
) : RecyclerView.Adapter<FeedAdapter.FeedViewHolder>() {

    class FeedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val logImage: ImageView = view.findViewById(R.id.logImage)
        val userNick: TextView = view.findViewById(R.id.user_nick)
        val userMajor: TextView = view.findViewById(R.id.user_major)
        val cheerButton: ImageView = view.findViewById(R.id.cheer_button) // 응원 버튼
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.feed, parent, false)
        return FeedViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        val logEntry = logList[position]
        val dbHelper = DBHelper(context)
        val userInfo = dbHelper.getUserInfoByChallengerId(logEntry.challengerId)
        val userNick = userInfo?.first ?: "알 수 없음"
        val userMajor = userInfo?.second ?: "알 수 없음"

        // 닉네임 & 전공 설정
        holder.userNick.text = userNick
        holder.userMajor.text = userMajor

        // 이미지 설정
        val bitmap = BitmapFactory.decodeByteArray(logEntry.logImage, 0, logEntry.logImage.size)
        holder.logImage.setImageBitmap(bitmap)

        // 현재 로그인한 유저 ID 가져오기
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val currentUserId = sharedPreferences.getLong("user_id", -1L) // 기본값 -1L

        if (currentUserId == -1L) {
            Toast.makeText(context, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 로그의 logId 가져오기
        val logId = dbHelper.getLogIdByChallenger(logEntry.challengerId, logEntry.challengeId)

        // logId가 없거나, 이미 응원한 경우 버튼 숨기기
        if (logId == null || dbHelper.hasUserCheered(logId, currentUserId)) {
            holder.cheerButton.visibility = View.GONE
        } else {
            holder.cheerButton.visibility = View.VISIBLE
        }

        // 응원 버튼 클릭 이벤트
        holder.cheerButton.setOnClickListener {
            if (logEntry.challengerId == currentUserId) {
                Toast.makeText(context, "자기 자신의 피드는 추천할 수 없어요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (logId != null && dbHelper.addCheer(logId, currentUserId)) {
                Toast.makeText(context, "${userNick}님을 응원했어요! ✊", Toast.LENGTH_SHORT).show()

                // 버튼 숨기기
                holder.cheerButton.visibility = View.GONE
            } else {
                Toast.makeText(context, "이미 응원한 피드입니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun getItemCount(): Int = logList.size
}
