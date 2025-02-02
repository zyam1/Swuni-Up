package com.example.swuni_up

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CompletedChallengeAdapter(
    private val context: Context,
    private var challengeList: List<DBHelper.Challenge>
) : RecyclerView.Adapter<CompletedChallengeAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val challengePhoto: ImageView = view.findViewById(R.id.my_challenge_photo)
        val challengeTitle: TextView = view.findViewById(R.id.my_title)
        val challengeDescription: TextView = view.findViewById(R.id.my_description)
        val challengeDuration: TextView = view.findViewById(R.id.my_dDay)
        val challengeParticipants: TextView = view.findViewById(R.id.my_participants)
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

        // 챌린지 기간 표시
        holder.challengeDuration.text = "${challenge.startDay} ~ ${challenge.endDay}"

        // 참여 인원 표시
        holder.challengeParticipants.text = "참여 인원 ${challenge.maxParticipant}"

        // 이미지 변환
        holder.challengePhoto.setImageBitmap(byteArrayToBitmap(challenge.photo))
    }

    override fun getItemCount(): Int = challengeList.size

    // ByteArray를 Bitmap으로 변환
    private fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    fun updateData(newList: List<DBHelper.Challenge>) {
        challengeList = newList
        notifyDataSetChanged()
    }
}
