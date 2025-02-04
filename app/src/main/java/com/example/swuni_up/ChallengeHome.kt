package com.example.swuni_up

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
    private lateinit var myChallengeAdapter: myChallengeAdapter
    private lateinit var tvOngoingTitle: TextView
    private lateinit var navChallengeExplore: ImageView
    private lateinit var navMyChallenge: ImageView
    private lateinit var popularChallengeAdapter: PopularChallengeAdapter
    private lateinit var popularChallengeRecyclerView: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_challenge_home)

        dbHelper = DBHelper(this)

        dbHelper.updateChallengeStatus()

        // 진행 중인 챌린지 RecyclerView 설정
        myChallengeRecyclerView = findViewById(R.id.recyclerViewMyChallenge)
        myChallengeRecyclerView.layoutManager = LinearLayoutManager(this)
        myChallengeAdapter = myChallengeAdapter(this, emptyList())
        myChallengeRecyclerView.adapter = myChallengeAdapter

        //인기 챌린지 관련
        popularChallengeRecyclerView = findViewById(R.id.popularChallengeRecyclerView)
        popularChallengeRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
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
        navMyChallenge = findViewById(R.id.nav_my_challenge)

        navChallengeExplore.setOnClickListener {
            val intent = Intent(this, ChallengeExplore::class.java)
            startActivity(intent)
        }

        navMyChallenge.setOnClickListener {
            val intent = Intent(this, my_challenge::class.java)
            startActivity(intent)
        }

    }

    private fun loadChallenges() {
        val sharedPreferences: SharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getLong("user_id", -1L)

        val ongoingList = dbHelper.getOngoingChallenges(userId)
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
        val sharedPreferences: SharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getLong("user_id", -1L)
        return dbHelper.getNicknameById(userId) ?: "OO"
    }
}
