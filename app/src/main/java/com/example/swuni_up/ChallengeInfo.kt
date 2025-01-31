package com.example.swuni_up

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChallengeInfo : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var adapter: PercentAdapter
    private lateinit var recyclerView: RecyclerView
    private var challengeId: Long = -1  // ✅ 기본값 -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.challenge_info)

        // ✅ Intent에서 챌린지 ID 가져오기
        challengeId = intent.getLongExtra("challenge_id", -1)

        // 툴바 설정
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.arrow_left)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // ✅ 피드 버튼 클릭 시 ChallengeInfoFeed로 이동
        val feedButton: ImageView = findViewById(R.id.feedButton)
        feedButton.setOnClickListener {
            val intent = Intent(this, ChallengeInfoFeed::class.java)
            intent.putExtra("challenge_id", challengeId)
            startActivity(intent)
        }

        val title = intent.getStringExtra("title") ?: "제목 없음"
        val description = intent.getStringExtra("description") ?: "설명 없음"
        val photo = intent.getStringExtra("photo")
        val maxParticipant = intent.getStringExtra("max_participant") ?: "없음"
        val date = intent.getStringExtra("date") ?: "날짜 없음"

        findViewById<TextView>(R.id.challenge_title).text = title
        findViewById<TextView>(R.id.description).text = description
        findViewById<ImageView>(R.id.infoImage).setImageURI(photo?.let { Uri.parse(it) })

        findViewById<TextView>(R.id.max_participant).text = maxParticipant
        findViewById<TextView>(R.id.date).text = date

        // ✅ RecyclerView 설정
        dbHelper = DBHelper(this)
        recyclerView = findViewById(R.id.recyclerView)
        adapter = PercentAdapter(mutableListOf())

        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = adapter
    }



}
