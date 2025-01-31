package com.example.swuni_up

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class MyChallenge : AppCompatActivity() {

    private var currentChallengeId: Long? = null
    private lateinit var imageView: ImageView
    private lateinit var titleTextView: TextView
    private lateinit var descriptionTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_my_challenge)

        val challengeComponent = findViewById<LinearLayout>(R.id.my_challenge_component)
        imageView = findViewById(R.id.my_challenge_photo)
        titleTextView = findViewById(R.id.my_title)
        descriptionTextView = findViewById(R.id.my_description)

        challengeComponent.setOnClickListener {
            val intent = Intent(this, ChallengeJoin::class.java).apply {
                putExtra("challenge_id", currentChallengeId) // challenge_id 전달
                Log.d("MyChallenge", "챌린지 ID: $currentChallengeId")
            }
            startActivity(intent)
        }

        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getLong("user_id", -1L)

        val userNick = sharedPreferences.getString("user_nick", "")
        Log.d("userInfo", "저장된 user_id: $userId, user_nick: $userNick")

        loadRandomChallenge()
    }

    private fun loadRandomChallenge() {
        val dbHelper = DBHelper(this)
        val db = dbHelper.readableDatabase

        val query = "SELECT * FROM ${DBHelper.TABLE_CHALLENGE} ORDER BY RANDOM() LIMIT 1"
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            val challengeId = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_CHALLENGE_ID)) // ID 가져오기
            val title = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_CHALLENGE_TITLE))
            var description = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_DESCRIPTION))
            val photoBlob = cursor.getBlob(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_PHOTO)) // BLOB 데이터 가져오기
            val startDay = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_START_DAY))
            val endDay = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_END_DAY))

            currentChallengeId = challengeId

            // 텍스트 설정
            titleTextView.text = title

            if (description.length > 25) {
                description = description.substring(0, 25) + "..."
            }
            descriptionTextView.text = description

            val daysDifference = calculateDaysDifference(startDay, endDay) + 1
            val dateTextView = findViewById<TextView>(R.id.my_date)
            dateTextView.text = "$daysDifference" + "일 챌린지"

            val countTextView = findViewById<TextView>(R.id.my_participants)
            val joinedParticipants = countParticipants(challengeId)
            countTextView.text = "참여인원 $joinedParticipants" + "명"

            // BLOB 데이터를 Bitmap으로 변환 후 ImageView에 설정
            if (photoBlob != null) {
                val bitmap = BitmapFactory.decodeByteArray(photoBlob, 0, photoBlob.size)
                imageView.setImageBitmap(bitmap) // ImageView에 Bitmap 설정
            } else {
                Toast.makeText(this, "이미지를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            }

        } else {
            Toast.makeText(this, "데이터가 없습니다!", Toast.LENGTH_SHORT).show()
        }

        cursor.close()
        db.close()
    }

    private fun countParticipants(challengeId: Long): Int {
        val dbHelper = DBHelper(this)
        val db = dbHelper.readableDatabase

        val query = "SELECT COUNT(*) FROM ${DBHelper.TABLE_CHALLENGER} WHERE ${DBHelper.COLUMN_CHALLENGE_ID} = ?"
        val cursor = db.rawQuery(query, arrayOf(challengeId.toString()))

        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0) // 첫 번째 컬럼 값이 참여자 수
        }

        cursor.close()
        db.close()
        return count
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
}