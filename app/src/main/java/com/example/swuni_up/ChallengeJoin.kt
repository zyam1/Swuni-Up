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
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.os.Build
import android.view.Gravity
import androidx.core.content.res.ResourcesCompat
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

        val joinButton = findViewById<Button>(R.id.joinButton)
        joinButton.setOnClickListener {
            joinChallenge()
        }

        val challengeId = intent.getLongExtra("challenge_id", -1)
        Log.d("ChallengeJoin", "챌린지 ID: $challengeId")
        if (challengeId != -1L) {
            loadChallengeById(challengeId)

        } else {
            Toast.makeText(this, "챌린지 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
        }

        // 로그인 성공 후 SharedPreferences에 저장한 후, 값 확인
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getLong("user_id", -1L)
        Log.d("userInfo", "저장된 user_id: $userId")  // 저장된 user_id 로그 찍어보기

    }

    private fun joinChallenge() {
        val dbHelper = DBHelper(this)

        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getLong("user_id", -1L)

        val challengeId = currentChallengeId // 현재 불러온 챌린지 ID 사용
        if (challengeId == null) {
            Toast.makeText(this, "참여할 챌린지가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val challengeRole = "participant" // 역할: 참가자
        val joinedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()) // 현재 시간
        val percentage = 0 // 초기 참여율

        // Challenger 객체 생성
        val challenger = DBHelper.Challenger(
            userId = userId,
            challengeId = challengeId,
            challengeRole = challengeRole,
            joinedAt = joinedAt,
            percentage = percentage
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



    private fun loadChallengeById(challengeId: Long) {
        val dbHelper = DBHelper(this)
        val db = dbHelper.readableDatabase

        val query = "SELECT * FROM ${DBHelper.TABLE_CHALLENGE} WHERE ${DBHelper.COLUMN_CHALLENGE_ID} = ?"
        val cursor = db.rawQuery(query, arrayOf(challengeId.toString()))

        if (cursor.moveToFirst()) {
            val title = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_CHALLENGE_TITLE))
            val description = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_DESCRIPTION))
            val photoBlob = cursor.getBlob(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_PHOTO))
            val startDay = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_START_DAY))
            val endDay = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_END_DAY))
            val maxParticipants = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_MAX_PARTICIPANT))

            currentChallengeId = challengeId

            titleTextView.text = title
            descriptionTextView1.text = description

            val formattedStartDay = formatDate(startDay)
            val formattedEndDay = formatDate(endDay)

            findViewById<TextView>(R.id.dayTextView).text = "$formattedStartDay ~ $formattedEndDay"
            findViewById<TextView>(R.id.dateTextView).text = "${calculateDaysDifference(startDay, endDay) + 1}일 챌린지"

            val joinedParticipants = countParticipants(challengeId)
            findViewById<TextView>(R.id.countTextView).text = "$joinedParticipants / $maxParticipants 명"

            val daysRemaining = calculateDaysRemaining(startDay)
            findViewById<TextView>(R.id.dDayTextView).text = if (daysRemaining == 0) "오늘 마감!" else "마감 D - $daysRemaining"

            if (photoBlob != null) {
                val bitmap = BitmapFactory.decodeByteArray(photoBlob, 0, photoBlob.size)
                imageView.setImageBitmap(bitmap)
            } else {
                Toast.makeText(this, "이미지를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            }

            val joinButton = findViewById<Button>(R.id.joinButton)
            if (joinedParticipants >= maxParticipants) {
                joinButton.isEnabled = false
                joinButton.setOnClickListener {
                    Toast.makeText(this, "참여 가능 인원이 모두 모집되었습니다!", Toast.LENGTH_SHORT).show()
                }
            } else {
                joinButton.isEnabled = true
            }

            // 참가자들의 프로필을 추가하는 코드
            val participants = getParticipantsForChallenge(challengeId)
            Log.d("Participants", "Participants for challenge $challengeId: $participants")
            val circleContainer = findViewById<LinearLayout>(R.id.circleContainer)

            // 기존에 있는 동그라미들을 초기화하고 새로 추가
            circleContainer.removeAllViews()

            for (participant in participants) {
                val profileLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(32, 25, 32, 0) // 간격 설정
                    }
                }

                val userProfileBitmap = loadUserProfilePhoto(participant.userId)

                // 프로필 이미지뷰 생성
                val imageView = ImageView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(270, 270)
                    background = getDrawable(R.drawable.circle_background) // 배경을 원형으로 설정
                    clipToOutline = true
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setImageBitmap(userProfileBitmap ?: BitmapFactory.decodeResource(resources, R.drawable.ic_profile))
                }

                // 닉네임 텍스트뷰 생성
                val textView = TextView(this).apply {
                    text = participant.userNick
                    textSize = 12f
                    typeface = ResourcesCompat.getFont(this@ChallengeJoin, R.font.scdream5)
                    gravity = Gravity.CENTER
                    setPadding(0, 25, 0, 0)
                }

                // 레이아웃에 추가
                profileLayout.addView(imageView)
                profileLayout.addView(textView)

                // circleContainer에 추가
                circleContainer.addView(profileLayout)
            }
        } else {
            Toast.makeText(this, "챌린지를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
        }

        cursor.close()
        db.close()
    }

    private fun loadUserProfilePhoto(userId: Long): Bitmap? {
        val dbHelper = DBHelper(this) // 사용자 DB 헬퍼 클래스
        return dbHelper.getUserProfilePhotoById(userId) // userId를 기반으로 프로필 사진을 로드
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

    // 참가자 목록을 가져오는 함수
    private fun getParticipantsForChallenge(challengeId: Long): List<Participant> {
        val dbHelper = DBHelper(this)
        val db = dbHelper.readableDatabase
        val query = "SELECT * FROM ${DBHelper.TABLE_CHALLENGER} WHERE ${DBHelper.COLUMN_CHALLENGE_ID} = ?"
        val cursor = db.rawQuery(query, arrayOf(challengeId.toString()))

        val participants = mutableListOf<Participant>()
        while (cursor.moveToNext()) {
            val userId = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_USER_ID))

            // userId를 이용해 user 테이블에서 userNick을 가져옵니다.
            val userNick = getUserNick(userId, db)
            participants.add(Participant(userId, userNick))
        }

        cursor.close()
        db.close()
        return participants
    }

    private fun getUserNick(userId: Long, db: SQLiteDatabase): String {
        val query = "SELECT ${DBHelper.COLUMN_NICKNAME} FROM ${DBHelper.TABLE_USER} WHERE ${DBHelper.COLUMN_USER_ID} = ?"
        val cursor = db.rawQuery(query, arrayOf(userId.toString()))

        var userNick = ""
        if (cursor.moveToFirst()) {
            userNick = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_NICKNAME))
        }

        cursor.close()
        return userNick
    }

    // 참가자 데이터 클래스
    data class Participant(val userId: Long, val userNick: String)


}
