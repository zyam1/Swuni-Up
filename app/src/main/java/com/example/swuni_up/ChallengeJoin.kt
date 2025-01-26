package com.example.swuni_up

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.os.Build
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


class ChallengeJoin : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var titleTextView: TextView
    private lateinit var descriptionTextView1: TextView

    private lateinit var horizontalScrollView: HorizontalScrollView
    private lateinit var circleContainer: LinearLayout
    private lateinit var scrollButton: Button
    private val scrollStep = 100 // 한 번에 이동하는 거리 (픽셀 단위)

    private var currentChallengeId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_challenge_join)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), 1)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
            }
        }


        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // 뒤로가기 버튼 동작 설정
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, ChallengeCreate::class.java) // ChallengeCreateActivity로 이동
            startActivity(intent)
            finish() // 현재 액티비티 종료
        }

        imageView = findViewById(R.id.imageView)
        titleTextView = findViewById(R.id.titleTextView)
        descriptionTextView1 = findViewById(R.id.descriptionTextView1)

        // View 초기화
        horizontalScrollView = findViewById(R.id.horizontalScrollView)
        circleContainer = findViewById(R.id.circleContainer)
        scrollButton = findViewById(R.id.scrollRightButton)

        // 버튼 클릭 시 동그라미가 하나씩 옆으로 밀리도록
        scrollButton.setOnClickListener {
            // 현재 스크롤 위치에서 오른쪽으로 이동
            horizontalScrollView.smoothScrollBy(scrollStep, 0)

            // Toast로 확인
            Toast.makeText(this, "밀기 클릭됨!", Toast.LENGTH_SHORT).show()
        }

        loadRandomChallenge()

        val joinButton = findViewById<Button>(R.id.joinButton)
        joinButton.setOnClickListener {
            joinChallenge()
        }
    }

    private fun joinChallenge() {
        val dbHelper = ChallengerDBHelper(this)

        // 예시 데이터 (실제 사용자와 챌린지 ID는 적절히 설정해야 함)
        val userId: Long = 1 // 로그인된 사용자 ID

        val challengeId = currentChallengeId // 현재 불러온 챌린지 ID 사용
        if (challengeId == null) {
            Toast.makeText(this, "참여할 챌린지가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val challengeRole = "participant" // 역할: 참가자
        val joinedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()) // 현재 시간
        val percentage = 0 // 초기 참여율
        val isCompleted = false // 초기 완료 상태

        // Challenger 객체 생성
        val challenger = ChallengerDBHelper.Challenger(
            userId = userId,
            challengeId = challengeId,
            challengeRole = challengeRole,
            joinedAt = joinedAt,
            percentage = percentage,
            isCompleted = isCompleted
        )

        // 데이터베이스에 삽입
        val result = dbHelper.insertChallenger(challenger)
        if (result != -1L) {
            Toast.makeText(this, "참여가 완료되었습니다!", Toast.LENGTH_SHORT).show()
            Log.d("ChallengeJoin", "참여 완료: ID = $result")
        } else {
            Toast.makeText(this, "참여 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            Log.e("ChallengeJoin", "참여 실패")
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "권한이 허용되었습니다!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "권한이 필요합니다!", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun loadRandomChallenge() {
        val dbHelper = ChallengeDBHelper(this)
        val db = dbHelper.readableDatabase

        val query = "SELECT * FROM ${ChallengeDBHelper.TABLE_CHALLENGE} ORDER BY RANDOM() LIMIT 1"
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            val challengeId = cursor.getLong(cursor.getColumnIndexOrThrow(ChallengeDBHelper.COLUMN_ID)) // ID 가져오기
            val title = cursor.getString(cursor.getColumnIndexOrThrow(ChallengeDBHelper.COLUMN_TITLE))
            val description = cursor.getString(cursor.getColumnIndexOrThrow(ChallengeDBHelper.COLUMN_DESCRIPTION))
            val photoBlob = cursor.getBlob(cursor.getColumnIndexOrThrow(ChallengeDBHelper.COLUMN_PHOTO)) // BLOB 데이터 가져오기
            val startDay = cursor.getString(cursor.getColumnIndexOrThrow(ChallengeDBHelper.COLUMN_START_DAY))
            val endDay = cursor.getString(cursor.getColumnIndexOrThrow(ChallengeDBHelper.COLUMN_END_DAY))
            val maxParticipants = cursor.getInt(cursor.getColumnIndexOrThrow(ChallengeDBHelper.COLUMN_MAX_PARTICIPANT))

            currentChallengeId = challengeId

            // 텍스트 설정
            titleTextView.text = title
            descriptionTextView1.text = description

            // 날짜 포맷 변경 및 설정
            val formattedStartDay = formatDate(startDay)
            val formattedEndDay = formatDate(endDay)

            val dayTextView = findViewById<TextView>(R.id.dayTextView)
            dayTextView.text = "$formattedStartDay ~ $formattedEndDay"

            val daysDifference = calculateDaysDifference(startDay, endDay) + 1
            val dateTextView = findViewById<TextView>(R.id.dateTextView)
            dateTextView.text = "$daysDifference" + "일 챌린지"

            val countTextView = findViewById<TextView>(R.id.countTextView)
            countTextView.text = "5 / $maxParticipants 명"

            val dDayTextView = findViewById<TextView>(R.id.dDayTextView)
            val daysRemaining = calculateDaysRemaining(startDay)

            if (daysRemaining == 0) {
                dDayTextView.text = "오늘 마감!"
            } else {
                dDayTextView.text = "마감 D - $daysRemaining"
            }

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

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // 데이터베이스의 날짜 형식
            val outputFormat = SimpleDateFormat("M/d", Locale.getDefault()) // 원하는 출력 형식
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: "") // 날짜를 출력 형식으로 변환
        } catch (e: Exception) {
            Log.e("DateFormatError", "Invalid date format: $dateString", e)
            ""
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

    private fun calculateDaysRemaining(startDay: String): Int {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = Date() // 오늘 날짜
        val startDate = sdf.parse(startDay)

        // startDate가 null인 경우에 대비
        if (startDate == null) {
            return 0
        }

        val diffInMillis = startDate.time - today.time
        val diffInDays = (diffInMillis / (1000 * 60 * 60 * 24)).toInt() // 밀리초를 일수로 변환

        return diffInDays
    }

}
