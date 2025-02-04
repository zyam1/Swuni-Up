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
import java.text.SimpleDateFormat
import java.util.Locale

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

        holder.challengeTitle.text = challenge.title?.let {
            if (it.length > 12) it.substring(0, 12) + "..." else it
        } ?: ""
        holder.challengeDescription.text = challenge.description?.let {
            if (it.length > 18) it.substring(0, 18) + "..." else it
        } ?: ""

        holder.challengeDday.text = "마감 D - ${calculateDDay(challenge.endDay)}"
        holder.challengeCount.text = "${challenge.maxParticipant}명 참가"

        holder.challengeDuration.text = "${formatDate(challenge.startDay)} ~ ${formatDate(challenge.endDay)}"

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

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("M/d", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: "")
        } catch (e: Exception) {
            ""
        }
    }
}
