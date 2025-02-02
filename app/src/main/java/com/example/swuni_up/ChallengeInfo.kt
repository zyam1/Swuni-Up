package com.example.swuni_up

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class ChallengeInfo : AppCompatActivity() {

    private lateinit var Title: TextView
    private lateinit var Description: TextView
    private lateinit var ImageView: ImageView
    private lateinit var date: TextView
    private lateinit var challengeNum: TextView
    private lateinit var day: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: InfoChallengerAdapter
    private var challenge: DBHelper.Challenge? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.challenge_info)

        // 툴바 설정
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 뒤로가기 버튼

        // UI 요소 초기화
        Title = findViewById(R.id.challenge_title)
        Description = findViewById(R.id.description)
        ImageView = findViewById(R.id.infoImage)
        date = findViewById(R.id.date)
        day = findViewById(R.id.day)
        challengeNum = findViewById(R.id.challeng_num)

        val feedButton = findViewById<ImageView>(R.id.feedButton)

        // RecyclerView 설정 (한 줄에 3개씩 배치)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 3)  // 한 줄에 3개의 아이템

        // 참여자 리스트를 가져오기
        val participants = getParticipants()

        // RecyclerView 어댑터 설정
        adapter = InfoChallengerAdapter(this, participants) { selectedChallengerId ->
            val intent = Intent(this, UserProfileActivity::class.java)
            intent.putExtra("challenger_id", selectedChallengerId)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        recyclerView.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                val child = rv.findChildViewUnder(e.x, e.y)  // 클릭한 아이템 찾기
                if (child != null && e.action == MotionEvent.ACTION_UP) {
                    val position = rv.getChildAdapterPosition(child)
                    if (position != RecyclerView.NO_POSITION) {
                        val selectedChallenger = participants[position] // 클릭한 챌린저 정보 가져오기
                        val selectedChallengerId = selectedChallenger.userId
                        val selectedProgress = selectedChallenger.percentage // 진행률 가져오기

                        // 챌린저 ID와 진행률을 함께 전달
                        val intent = Intent(this@ChallengeInfo, UserProfileActivity::class.java).apply {
                            putExtra("challenger_id", selectedChallengerId)
                            putExtra("percentage", selectedProgress)
                            putExtra("challenge_id", challenge?.challengeId ?: -1L)
                        }
                        startActivity(intent)
                    }
                }
                return false
            }
        })



        // 챌린지 ID 받기
        val challengeId = intent.getLongExtra("challenge_id", -1)

        if (challengeId != -1L) {
            // DB에서 챌린지 정보 가져오기
            challenge = getChallengeById(challengeId)

            // 챌린지 정보 UI에 표시
            challenge?.let {
                Title.text = it.title
                Description.text = it.description
                date.text = "${formatDate(it.startDay)} ~ ${formatDate(it.endDay)}"

                // 이미지 처리 (사진이 있을 경우)
                val bitmap = BitmapFactory.decodeByteArray(it.photo, 0, it.photo.size)
                ImageView.setImageBitmap(bitmap)

                // 진행일 표시
                day.text = "${calculateDaysDifference(it.startDay, it.endDay)}일"

                // 참여 중인 인원수 표시
                val joinedParticipants = countParticipants(this, it.challengeId)
                challengeNum.text = "참여인원 ${joinedParticipants}"
            }

            feedButton.setOnClickListener {
                val intent = Intent(this, ChallengeInfoFeed::class.java)
                intent.putExtra("challenge_id", challengeId) // 현재 챌린지 ID 전달
                startActivity(intent)
            }
        }

        // 로그 작성
        val logButton = findViewById<Button>(R.id.addlog_btn)

        val logLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                refreshChallengers() // 진행률 업데이트
            }
        }

        logButton.setOnClickListener {
            val intent = Intent(this, AddLog::class.java)
            intent.putExtra("challenge_id", challenge?.challengeId ?: -1L)
            intent.putExtra("challenge_name", Title.text.toString())
            intent.putExtra("challenge_start_day", challenge?.startDay ?: "")
            logLauncher.launch(intent)
        }

    }

    // 뒤로가기
    override fun onSupportNavigateUp(): Boolean {
        finish() // 이전 화면으로 이동
        return true
    }

    // DB 챌린지 정보, challenge_id로 조회
    private fun getChallengeById(challengeId: Long): DBHelper.Challenge? {
        val dbHelper = DBHelper(this)
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM ${DBHelper.TABLE_CHALLENGE} WHERE ${DBHelper.COLUMN_CHALLENGE_ID} = ?",
            arrayOf(challengeId.toString())
        )

        var challenge: DBHelper.Challenge? = null
        if (cursor.moveToFirst()) {
            challenge = DBHelper.Challenge(
                challengeId = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_CHALLENGE_ID)),
                title = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_CHALLENGE_TITLE)),
                description = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_DESCRIPTION)),
                photo = cursor.getBlob(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_PHOTO)),
                createdAt = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_CREATED_AT)),
                startDay = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_START_DAY)),
                endDay = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_END_DAY)),
                status = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_STATUS)),
                maxParticipant = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_MAX_PARTICIPANT)),
                category = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_CATEGORY)),
                isOfficial = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_IS_OFFICIAL))
            )
        }

        cursor.close()
        db.close()

        return challenge
    }

    // 날짜 형식 변환
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

    // 시작일과 마감일 차이 계산
    private fun calculateDaysDifference(startDateString: String, endDateString: String): Long {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // 데이터베이스의 날짜 형식
            val startDate = inputFormat.parse(startDateString)
            val endDate = inputFormat.parse(endDateString)

            // 시작일과 종료일의 날짜 차이 계산
            val diffInMillis = endDate?.time?.minus(startDate?.time ?: 0) ?: 0
            TimeUnit.MILLISECONDS.toDays(diffInMillis) + 1
        } catch (e: Exception) {
            Log.e("DateDiffError", "Error calculating date difference", e)
            0L // 오류 발생 시 0일로 반환
        }
    }

    // 현재 참여 중인 인원수
    private fun countParticipants(context: Context, challengeId: Long?): Int {
        val dbHelper = DBHelper(context)
        val db = dbHelper.readableDatabase

        val query = "SELECT COUNT(*) FROM ${DBHelper.TABLE_CHALLENGER} WHERE ${DBHelper.COLUMN_CHALLENGE_ID} = ?"
        val cursor = db.rawQuery(query, arrayOf(challengeId?.toString()))

        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }

        cursor.close()
        db.close()
        return count
    }

    // 참여 중인 유저 목록을 가져오기
    private fun getParticipants(): List<DBHelper.Challenger> {
        val dbHelper = DBHelper(this)
        return dbHelper.getParticipantsByChallengeId(intent.getLongExtra("challenge_id", -1))
    }

    // 진행률 갱신
    private fun refreshChallengers() {
        val challengeId = intent.getLongExtra("challenge_id", -1)
        if (challengeId == -1L) return

        val updatedParticipants = getParticipants() // 최신 데이터 가져오기

        runOnUiThread { // UI 업데이트 보장
            adapter.updateData(updatedParticipants) // 어댑터 데이터 갱신
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            refreshChallengers()
        }
    }
}
