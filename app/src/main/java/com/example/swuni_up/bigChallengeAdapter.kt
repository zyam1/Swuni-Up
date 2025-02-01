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

class BigChallengeAdapter(private val context: Context, private var challenges: List<DBHelper.Challenge>) : RecyclerView.Adapter<BigChallengeAdapter.ChallengeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.big_challenge_component, parent, false)
        return ChallengeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        val challenge = challenges[position]

        // 마지막 아이템인 경우 marginEnd 추가
        val layoutParams = holder.challengeItem.layoutParams as ViewGroup.MarginLayoutParams
        if (position == challenges.size - 1) {
            layoutParams.marginEnd = 50  // 원하는 margin 값 (px 단위)
        } else {
            layoutParams.marginEnd = 0  // 나머지 항목에는 marginEnd를 0으로 설정
        }
        holder.challengeItem.layoutParams = layoutParams

        // 이미지 로딩 (Blob 데이터를 Bitmap으로 변환)
        val bitmap = BitmapFactory.decodeByteArray(challenge.photo, 0, challenge.photo.size)
        holder.imageView.setImageBitmap(bitmap)

        // 텍스트 설정
        holder.titleTextView.text = challenge.title
        holder.descriptionTextView.text = challenge.description

        // 날짜 계산
        holder.dDayTextView.text = getDDayText(challenge.endDay)
        holder.dayTextView.text = "${formatDate(challenge.startDay)} ~ ${formatDate(challenge.endDay)}"

        holder.dateTextView.text = "${calculateDaysDifference(challenge.startDay, challenge.endDay) + 1}일 챌린지"

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
        val challengeItem: LinearLayout = itemView.findViewById(R.id.big_challenge)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val dDayTextView: TextView = itemView.findViewById(R.id.dDayTextView)
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        val dayTextView: TextView = itemView.findViewById(R.id.dayTextView)
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val countTextView: TextView = itemView.findViewById(R.id.countTextView)
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

    private fun calculateDaysDifference(startDateString: String, endDateString: String): Long {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // 데이터베이스의 날짜 형식
            val startDate = inputFormat.parse(startDateString)
            val endDate = inputFormat.parse(endDateString)

            // 시작일과 종료일의 날짜 차이 계산
            val diffInMillis = endDate?.time?.minus(startDate?.time ?: 0) ?: 0
            TimeUnit.MILLISECONDS.toDays(diffInMillis) // 밀리초를 일수로 변환
        } catch (e: Exception) {
            Log.e("DateDiffError", "Error calculating date difference", e)
            0L // 오류 발생 시 0일로 반환
        }
    }

    fun updateChallenges(newChallenges: List<DBHelper.Challenge>) {
        Log.d("BigChallengeAdapter", "Received challenges count: ${newChallenges.size}")
        challenges = newChallenges
        notifyDataSetChanged()
    }
}
