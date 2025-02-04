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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
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
        val logButton = findViewById<Button>(R.id.addlog_btn)

        // ✅ Intent에서 챌린지 ID 가져오기 (중복 제거)
        val challengeId = intent.getLongExtra("challenge_id", -1)

        // ✅ 챌린지 ID가 유효한지 검사
        if (challengeId == -1L) {
            Toast.makeText(this, "챌린지 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // ✅ 챌린지 정보 가져오기 (중복 제거)
        challenge = getChallengeById(challengeId)

        if (challenge == null) {
            Toast.makeText(this, "해당 챌린지를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // ✅ RecyclerView 설정 (한 줄에 3개씩 배치)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 3)  // 한 줄에 3개의 아이템

        // ✅ 참여자 리스트를 가져오기
        val participants = getParticipants()

        // ✅ RecyclerView 어댑터 설정
        adapter = InfoChallengerAdapter(this, participants) { selectedChallengerId ->
            val intent = Intent(this, UserProfileActivity::class.java)
            intent.putExtra("challenger_id", selectedChallengerId)
            intent.putExtra("challenge_id", challengeId)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        recyclerView.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                val child = rv.findChildViewUnder(e.x, e.y)
                if (child != null && e.action == MotionEvent.ACTION_UP) {
                    val position = rv.getChildAdapterPosition(child)
                    if (position != RecyclerView.NO_POSITION) {
                        val selectedChallenger = participants[position]
                        val selectedChallengerId = selectedChallenger.challengerId
                        val selectedProgress = selectedChallenger.percentage

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

        // ✅ 챌린지 정보 UI에 표시
        challenge?.let {
            Title.text = it.title
            Description.text = it.description
            date.text = "${formatDate(it.startDay)} ~ ${formatDate(it.endDay)}"

            val bitmap = BitmapFactory.decodeByteArray(it.photo, 0, it.photo.size)
            ImageView.setImageBitmap(bitmap)

            day.text = "${calculateDaysDifference(it.startDay, it.endDay)}일 챌린지"
            challengeNum.text = "참여인원 ${countParticipants(this, it.challengeId)}"
        }

        // ✅ 피드 버튼 클릭 리스너 설정
        feedButton.setOnClickListener {
            val intent = Intent(this, ChallengeInfoFeed::class.java)
            intent.putExtra("challenge_id", challengeId)
            startActivity(intent)
        }

        // ✅ 로그 버튼 클릭 리스너 설정
        val logLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                refreshChallengers()
            }
        }

        logButton.setOnClickListener {
            val challengeStartDay = challenge?.startDay ?: ""
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            // 챌린지 시작일과 현재 날짜 비교
            if (challengeStartDay.isNotEmpty() && challengeStartDay > currentDate) {
                Toast.makeText(this, "챌린지가 아직 시작되지 않았습니다.", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, AddLog::class.java)
                intent.putExtra("challenge_id", challenge?.challengeId ?: -1L)
                intent.putExtra("challenge_name", Title.text.toString())
                intent.putExtra("challenge_start_day", challengeStartDay)
                logLauncher.launch(intent)
            }
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    // ✅ DB에서 챌린지 정보 가져오기
    private fun getChallengeById(challengeId: Long): DBHelper.Challenge? {
        val dbHelper = DBHelper(this)
        return dbHelper.getChallengeById(challengeId)
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

    private fun calculateDaysDifference(startDateString: String, endDateString: String): Long {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val startDate = inputFormat.parse(startDateString)
            val endDate = inputFormat.parse(endDateString)

            val diffInMillis = endDate?.time?.minus(startDate?.time ?: 0) ?: 0
            TimeUnit.MILLISECONDS.toDays(diffInMillis) + 1
        } catch (e: Exception) {
            Log.e("DateDiffError", "Error calculating date difference", e)
            0L
        }
    }

    private fun countParticipants(context: Context, challengeId: Long?): Int {
        val dbHelper = DBHelper(context)
        return dbHelper.countParticipants(challengeId)
    }

    private fun getParticipants(): List<DBHelper.Challenger> {
        val dbHelper = DBHelper(this)
        return dbHelper.getParticipantsByChallengeId(intent.getLongExtra("challenge_id", -1))
    }

    private fun refreshChallengers() {
        val updatedParticipants = getParticipants()
        runOnUiThread {
            adapter.updateData(updatedParticipants)
        }
    }
}
