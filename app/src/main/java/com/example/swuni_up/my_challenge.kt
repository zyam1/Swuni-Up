package com.example.swuni_up

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.widget.ImageView

class my_challenge : AppCompatActivity() {
    private lateinit var dbHelper: DBHelper
    private lateinit var ongoingRecyclerView: RecyclerView
    private lateinit var completedRecyclerView: RecyclerView
    private lateinit var ongoingAdapter: OngoingChallengeAdapter
    private lateinit var completedAdapter: CompletedChallengeAdapter
    private lateinit var tvOngoingTitle: TextView
    private lateinit var tvCompletedTitle: TextView
    private lateinit var navChallengeExplore: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_challenge2)

        dbHelper = DBHelper(this)

        // 진행 중인 챌린지 RecyclerView 설정
        ongoingRecyclerView = findViewById(R.id.challengeContainer)
        ongoingRecyclerView.layoutManager = LinearLayoutManager(this)
        ongoingAdapter = OngoingChallengeAdapter(this, emptyList())
        ongoingRecyclerView.adapter = ongoingAdapter

        // 완료된 챌린지 RecyclerView 설정
        completedRecyclerView = findViewById(R.id.completeChallengeContainer)
        completedRecyclerView.layoutManager = LinearLayoutManager(this)
        completedAdapter = CompletedChallengeAdapter(this, emptyList())
        completedRecyclerView.adapter = completedAdapter

        // "OO 슈니 ing 챌린지" TextView 연결
        tvOngoingTitle = findViewById(R.id.tvOngoingChallengeTitle)
        tvCompletedTitle = findViewById(R.id.tvCompletedChallengeTitle)

        // 사용자 닉네임 가져오기
        val nickname = getUserNickname()

        // 닉네임을 포함한 타이틀 설정
        tvOngoingTitle.text = "$nickname 슈니 ing 챌린지"
        tvCompletedTitle.text = "$nickname 슈니 완료 챌린지"

        // 데이터 불러오기
        loadChallenges()

        navChallengeExplore = findViewById(R.id.nav_challenge_explore)

        navChallengeExplore.setOnClickListener {
            val intent = Intent(this, ChallengeExplore::class.java)
            startActivity(intent)
        }

    }

    private fun loadChallenges() {
        val ongoingList = dbHelper.getOngoingChallenges()
        val completedList = dbHelper.getCompleteChallenges()

        if (ongoingList.isNotEmpty()) {
            ongoingAdapter.updateData(ongoingList)
            Log.d("MyChallengeActivity", "진행 중인 챌린지 ${ongoingList.size}개 로드 완료")
        } else {
            Log.d("MyChallengeActivity", "진행 중인 챌린지가 없습니다.")
        }

        if (completedList.isNotEmpty()) {
            completedAdapter.updateData(completedList)
            Log.d("MyChallengeActivity", "완료된 챌린지 ${completedList.size}개 로드 완료")
        } else {
            Log.d("MyChallengeActivity", "완료된 챌린지가 없습니다.")
        }
    }

    // ✅ 사용자 닉네임 가져오기
    private fun getUserNickname(): String {
        val userId = 1L // 현재 로그인한 사용자 ID (예제 값, 실제 ID를 가져오는 로직 필요)
        return dbHelper.getNicknameById(userId) ?: "OO"
    }
}
