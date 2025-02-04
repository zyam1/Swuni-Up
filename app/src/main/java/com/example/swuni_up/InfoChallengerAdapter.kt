package com.example.swuni_up

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class InfoChallengerAdapter(
    private val context: Context,
    private var participants: List<DBHelper.Challenger>,
    private val onItemClick: (Long) -> Unit
) : RecyclerView.Adapter<InfoChallengerAdapter.ParticipantViewHolder>() {

    // ViewHolder
    class ParticipantViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView = view.findViewById(R.id.profile_image)
        val nickname: TextView = view.findViewById(R.id.user_nick)
        val progress: TextView = view.findViewById(R.id.info_percent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.percent, parent, false)
        return ParticipantViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParticipantViewHolder, position: Int) {
        val participant = participants[position]

        val dbHelper = DBHelper(holder.itemView.context)
        val nickname = dbHelper.getNicknameById(participant.userId)
        val bitmap = dbHelper.getUserProfilePhotoById(participant.userId)

        // 프로필 이미지 설정
        bitmap?.let {
            holder.profileImage.setImageBitmap(it)
        } ?: run {
            holder.profileImage.setImageResource(R.drawable.rightarrow) // 기본 이미지로 설정
        }

        // 유저 정보, 진행률
        holder.nickname.text = nickname ?: "유저 ${participant.userId}"
        holder.progress.text = "${participant.percentage}%"  // 진행률 표시

        // 클릭 이벤트 처리
        holder.itemView.setOnClickListener {
            val challengerId = participant.challengerId ?: -1L
            Log.d("InfoChallengerAdapter", "Challenger ID: $challengerId")  // 로그 출력
            onItemClick(challengerId)
        }
    }

    // 아이템 개수 반환
    override fun getItemCount(): Int = participants.size

    fun updateData(newParticipants: List<DBHelper.Challenger>) {
        participants = newParticipants.toMutableList() // 새로운 데이터로 리스트 갱신
        notifyDataSetChanged() // 변경 사항 UI에 반영
    }
}
