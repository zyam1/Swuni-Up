package com.example.swuni_up

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.widget.ImageView

class ChallengeHome : AppCompatActivity() {
    private lateinit var dbHelper: DBHelper
    private lateinit var myChallengeRecyclerView: RecyclerView
    private lateinit var myChallengeAdapter: MyChallengeAdapter
    private lateinit var tvOngoingTitle: TextView
    private lateinit var tvCompletedTitle: TextView
    private lateinit var navChallengeExplore: ImageView
    private lateinit var popularChallengeAdapter: PopularChallengeAdapter
    private lateinit var popularChallengeRecyclerView: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_challenge_home)

        dbHelper = DBHelper(this)

        // 진행 중인 챌린지 RecyclerView 설정
        myChallengeRecyclerView = findViewById(R.id.recyclerViewMyChallenge)
        myChallengeRecyclerView.layoutManager = LinearLayoutManager(this)
        myChallengeAdapter = MyChallengeAdapter(this, emptyList())
        myChallengeRecyclerView.adapter = myChallengeAdapter

        //인기 챌린지 관련
        popularChallengeRecyclerView = findViewById(R.id.popularChallengeRecyclerView)
        popularChallengeRecyclerView.layoutManager = LinearLayoutManager(this)
        popularChallengeAdapter = PopularChallengeAdapter(this, emptyList())
        popularChallengeRecyclerView.adapter = popularChallengeAdapter


        // "OO 슈니 ing 챌린지" TextView 연결
        tvOngoingTitle = findViewById(R.id.nick_title)

        // 사용자 닉네임 가져오기
        val nickname = getUserNickname()

        // 닉네임을 포함한 타이틀 설정
        tvOngoingTitle.text = "$nickname 슈니, 오늘도 응원해!"

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
        val popularList = dbHelper.getPopularChallenges()
        if (ongoingList.isNotEmpty()) {
            myChallengeAdapter.updateData(ongoingList)
            Log.d("MyChallengeActivity", "완료된 챌린지 ${ongoingList.size}개 로드 완료")
        } else {
            Log.d("MyChallengeActivity", "완료된 챌린지가 없습니다.")
        }


        if (popularList.isNotEmpty()) {
            popularChallengeAdapter.updateData(popularList)
        }
    }

    // ✅ 사용자 닉네임 가져오기
    private fun getUserNickname(): String {
        val userId = 1L // 현재 로그인한 사용자 ID (예제 값, 실제 ID를 가져오는 로직 필요)
        return dbHelper.getNicknameById(userId) ?: "OO"
    }
}
