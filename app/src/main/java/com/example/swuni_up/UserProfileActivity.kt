package com.example.swuni_up

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class UserProfileActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var logAdapter: LogAdapter
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        dbHelper = DBHelper(this)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2) // 한 줄에 2개씩

        val profileImage = findViewById<ImageView>(R.id.profile_image)
        val nickText = findViewById<TextView>(R.id.nick)
        val majorText = findViewById<TextView>(R.id.major)
        val progressText = findViewById<TextView>(R.id.percent)
        val cheerText = findViewById<TextView>(R.id.cheer) // 응원 수

        // 툴바 설정
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        // 챌린저 ID, 챌린지 ID, 진행률 가져오기
        val challengerId = intent.getLongExtra("challenger_id", -1)
        val challengeId = intent.getLongExtra("challenge_id", -1)
        val progress = intent.getIntExtra("percentage", 0) // 기본값 0%

        if (challengerId == -1L || challengeId == -1L) {
            finish()
            return
        }

        // 인증 로그 불러와서 어댑터에 설정
        val logList = dbHelper.getLogsByChallenger(challengerId, challengeId)
        logAdapter = LogAdapter(logList) // 어댑터 생성
        recyclerView.adapter = logAdapter // RecyclerView에 적용

        // 유저 정보 가져오기
        val nickname = dbHelper.getNicknameById(challengerId) ?: "닉네임 없음"
        val major = dbHelper.getMajorById(challengerId) ?: "학과 없음"
        val cheerCount = dbHelper.getCheerCountForChallenger(challengerId, challengeId) // 응원 수

        // UI 업데이트
        nickText.text = nickname
        majorText.text = major
        progressText.text = "$progress%"
        cheerText.text = "$cheerCount"

        // 프로필 사진 가져오기
        val photoBitmap: Bitmap? = dbHelper.getUserProfilePhotoById(challengerId)
        profileImage.setImageBitmap(photoBitmap ?: BitmapFactory.decodeResource(resources, R.color.gray25))
    }

    // 뒤로 가기
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
