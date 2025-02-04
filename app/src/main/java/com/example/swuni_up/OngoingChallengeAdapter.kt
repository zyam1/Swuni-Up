package com.example.swuni_up

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class OngoingChallengeAdapter(
    private val context: Context,
    private var challengeList: List<DBHelper.Challenge>
) : RecyclerView.Adapter<OngoingChallengeAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val challengePhoto: ImageView = view.findViewById(R.id.my_challenge_photo)
        val challengeTitle: TextView = view.findViewById(R.id.my_title)
        val challengeDescription: TextView = view.findViewById(R.id.my_description)
        val challengeDuration: TextView = view.findViewById(R.id.my_dDay)
        val challengeParticipants: TextView = view.findViewById(R.id.my_participants)
        val progressText: TextView = view.findViewById(R.id.tv_progress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.overlay_long_challenge_component, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val challenge = challengeList[position]

        holder.challengeTitle.text = challenge.title
        holder.challengeDescription.text = challenge.description ?: "챌린지 설명 없음"

        val startDate = challenge.startDay
        val endDate = challenge.endDay

        val challengeDurationText = calculateDuration(startDate, endDate)
        holder.challengeDuration.text = "${challengeDurationText + 1}일 챌린지"

        // 참여 인원 표시
        holder.challengeParticipants.text = "참여인원 ${challenge.maxParticipant}"

        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getLong("user_id", -1L) // 기본값 -1L

        // 진행 퍼센트 가져오기
        if (challenge.challengeId != null && userId != -1L) {
            holder.progressText.text = "${calculateProgress(challenge.challengeId, userId)}%"
        } else {
            holder.progressText.text = "0%"  // challengeId가 없거나 userId가 없으면 0%라고 표현
        }

        // 이미지 변환
        holder.challengePhoto.setImageBitmap(byteArrayToBitmap(challenge.photo))

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ChallengeInfo::class.java).apply {
                putExtra("challenge_id", challenge.challengeId) // 챌린지 ID 전달
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = challengeList.size

    // ByteArray를 Bitmap으로 변환
    private fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    private fun calculateDuration(startDate: String, endDate: String): Long {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val start: Date = dateFormat.parse(startDate)
        val end: Date = dateFormat.parse(endDate)

        val diffInMillis = end.time - start.time
        return diffInMillis / (1000 * 60 * 60 * 24) // 밀리초 -> 일수로 변환
    }

    // 실제 진행 퍼센트 계산 (DB에서 가져오기)
    private fun calculateProgress(challengeId: Long, userId: Long): Int {
        val dbHelper = DBHelper(context)
        return dbHelper.getChallengeProgress(challengeId, userId) // userId 추가
    }


    fun updateData(newList: List<DBHelper.Challenge>) {
        challengeList = newList
        notifyDataSetChanged()
    }
}
