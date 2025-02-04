package com.example.swuni_up

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class longChallengeAdapter(private val context: Context, private var challenges: List<DBHelper.Challenge>) : RecyclerView.Adapter<longChallengeAdapter.ChallengeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.long_challenge_component, parent, false)
        return ChallengeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        val challenge = challenges[position]

        // 이미지 로딩 (Blob 데이터를 Bitmap으로 변환)
        val bitmap = BitmapFactory.decodeByteArray(challenge.photo, 0, challenge.photo.size)
        holder.imageView.setImageBitmap(bitmap)

        // 텍스트 설정
        holder.titleTextView.text = challenge.title
        holder.descriptionTextView.text = challenge.description

        // 날짜 계산
        holder.dDayTextView.text = getDDayText(challenge.startDay)

        // 참여 인원 계산
        val joinedParticipants = countParticipants(context, challenge.challengeId  ?: 0L)
        holder.countTextView.text = "$joinedParticipants / ${challenge.maxParticipant} 명"

        holder.challengeItem.setOnClickListener {
            val intent = Intent(context, ChallengeJoin::class.java).apply {
                putExtra("challenge_id", challenge.challengeId)  // challenge_id 전달
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = challenges.size

    class ChallengeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val challengeItem: LinearLayout = itemView.findViewById(R.id.my_challenge_component)
        val imageView: ImageView = itemView.findViewById(R.id.my_challenge_photo)
        val titleTextView: TextView = itemView.findViewById(R.id.my_title)
        val descriptionTextView: TextView = itemView.findViewById(R.id.my_description)
        val dDayTextView: TextView = itemView.findViewById(R.id.my_dDay)
        val countTextView: TextView = itemView.findViewById(R.id.my_participants)
    }

    private fun countParticipants(context: Context, challengeId: Long): Int {
        val dbHelper = DBHelper(context)
        val db = dbHelper.readableDatabase

        val query = "SELECT COUNT(*) FROM ${DBHelper.TABLE_CHALLENGER} WHERE ${DBHelper.COLUMN_CHALLENGE_ID} = ?"
        val cursor = db.rawQuery(query, arrayOf(challengeId.toString()))

        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }

        cursor.close()
        db.close()
        return count
    }

    private fun getDDayText(endDateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = Calendar.getInstance().time
            val endDate = inputFormat.parse(endDateString)

            if (endDate == null) {
                "날짜 오류"
            } else {
                val diffInMillis = endDate.time - today.time
                val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt()

                when {
                    diffInDays == 0 -> "오늘 마감!"
                    diffInDays < 0 -> "마감됨"
                    else -> "마감 D - $diffInDays"
                }
            }
        } catch (e: Exception) {
            "날짜 오류"
        }
    }

    fun updateChallenges(newChallenges: List<DBHelper.Challenge>) {
        challenges = newChallenges
        notifyDataSetChanged()
    }
}
