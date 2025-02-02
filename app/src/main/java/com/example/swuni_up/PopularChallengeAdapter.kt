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

class PopularChallengeAdapter(
    private val context: Context,
    private var challengeList: List<DBHelper.Challenge>
) : RecyclerView.Adapter<PopularChallengeAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val challengeImage: ImageView = view.findViewById(R.id.imageView)
        val challengeTitle: TextView = view.findViewById(R.id.titleTextView)
        val challengeDescription: TextView = view.findViewById(R.id.descriptionTextView)
        val challengeDday: TextView = view.findViewById(R.id.dDayTextView)
        val challengeCount: TextView = view.findViewById(R.id.countTextView)
        val challengeDuration: TextView = view.findViewById(R.id.dayTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.big_challenge_component, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val challenge = challengeList[position]

        holder.challengeTitle.text = challenge.title
        holder.challengeDescription.text = challenge.description ?: "챌린지 설명 없음"
        holder.challengeDday.text = "마감 D - ${calculateDDay(challenge.endDay)}"
        holder.challengeCount.text = "${challenge.maxParticipant}명 참가"
        holder.challengeDuration.text = "${challenge.startDay} ~ ${challenge.endDay}"

        // 이미지 변환
        holder.challengeImage.setImageBitmap(byteArrayToBitmap(challenge.photo))
    }

    override fun getItemCount(): Int = challengeList.size

    private fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    private fun calculateDDay(endDate: String): Int {
        return 3 // (여기에 날짜 비교 로직 추가하면 자동으로 D-Day 계산됨)
    }

    fun updateData(newList: List<DBHelper.Challenge>) {
        challengeList = newList
        notifyDataSetChanged()
    }
}
