package com.example.swuni_up

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.util.Log

class InfoChallengerAdapter(
    private val context: Context,
    private var participants: List<DBHelper.Challenger>  // DB에서 가져온 참여자 리스트
) : RecyclerView.Adapter<InfoChallengerAdapter.ParticipantViewHolder>() {

    // ViewHolder 정의
    class ParticipantViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView = view.findViewById(R.id.profile_image)
        val nickname: TextView = view.findViewById(R.id.user_nick)
        val progress: TextView = view.findViewById(R.id.info_percent)
    }

    // onCreateViewHolder로 아이템 레이아웃을 연결
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.percent, parent, false)
        return ParticipantViewHolder(view)
    }

    // onBindViewHolder에서 데이터를 바인딩
    override fun onBindViewHolder(holder: ParticipantViewHolder, position: Int) {
        val participant = participants[position]

        // DBHelper를 사용하여 사용자의 세부 정보를 가져옵니다.
        val dbHelper = DBHelper(holder.itemView.context)
        val nickname = dbHelper.getNicknameById(participant.userId)
        val bitmap = dbHelper.getUserProfilePhotoById(participant.userId)

        Log.d("InfoChallengerAdapter", "🔍 참가자 정보 - userId: ${participant.userId}, nickname: $nickname")

        // 프로필 이미지 설정
        bitmap?.let {
            holder.profileImage.setImageBitmap(it)
        } ?: run {
            // 이미지가 없으면 기본값으로 처리
            holder.profileImage.setImageResource(R.drawable.rightarrow) // 기본 이미지로 설정
        }

        // 유저 정보와 진행률 세팅
        holder.nickname.text = nickname ?: "유저 ${participant.userId}"
        holder.progress.text = "${participant.percentage}%"  // 진행률 표시
    }

    // 아이템 개수 반환
    override fun getItemCount(): Int = participants.size

    fun updateData(newParticipants: List<DBHelper.Challenger>) {
        participants = newParticipants.toMutableList() // 새로운 데이터로 리스트 갱신
        notifyDataSetChanged() // 변경 사항 UI에 반영
    }
}
