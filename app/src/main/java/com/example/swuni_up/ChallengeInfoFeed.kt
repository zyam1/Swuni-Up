package com.example.swuni_up

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChallengeInfoFeed : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FeedAdapter
    private lateinit var dbHelper: DBHelper
    private var challengeId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.challenge_info_feed)

        // 툴바 설정
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 챌린지 ID 받기 (ChallengeInfo에서 전달)
        challengeId = intent.getLongExtra("challenge_id", -1L)

        // DB 연결
        dbHelper = DBHelper(this)

        // RecyclerView 설정
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2) // 2개씩 배치

        // 특정 챌린지의 로그만 가져와서 어댑터 연결
        val logList = dbHelper.getLogEntriesByChallenge(challengeId)

        adapter = FeedAdapter(this, logList)
        recyclerView.adapter = adapter
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

