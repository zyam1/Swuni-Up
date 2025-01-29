package com.example.swuni_up

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChallengeInfo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.challenge_info)

        // 툴바 설정
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.arrow_left)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
            //이전 페이지로 이동
        }

        // 챌린지 정보
        val title = intent.getStringExtra("title")
        val description = intent.getStringExtra("description")
        val photo = intent.getStringExtra("photo")
        val max_participant = intent.getStringExtra("max_participant")
        val date = intent.getStringExtra("date")

        findViewById<TextView>(R.id.challenge_title).text = title
        findViewById<TextView>(R.id.description).text = description
        findViewById<ImageView>(R.id.infoImage).setImageURI(Uri.parse(photo))
        findViewById<TextView>(R.id.max_participant).text = title
        findViewById<TextView>(R.id.date).text = title


        // 임시 데이터 리스트(유저 정보 받으면 변경)
        val userList = listOf(
            UserData(R.drawable.arrow_left, "80%", "밥만잘먹더라"),
            UserData(R.drawable.arrow_right, "90%", "식사왕"),
            UserData(R.drawable.up, "70%", "야근러"),
        )

        // RecyclerView 설정
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val percentAdapter = PercentAdapter(userList)

        // GridLayoutManager, 1줄에 3개의 아이템
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = percentAdapter
    }
}
