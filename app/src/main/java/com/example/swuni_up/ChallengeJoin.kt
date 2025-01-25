package com.example.swuni_up

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class ChallengeJoin : AppCompatActivity() {

    private lateinit var horizontalScrollView: HorizontalScrollView
    private lateinit var circleContainer: LinearLayout
    private lateinit var scrollButton: Button
    private val scrollStep = 100 // 한 번에 이동하는 거리 (픽셀 단위)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_challenge_join)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // 뒤로가기 버튼 동작 설정
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, ChallengeCreate::class.java) // ChallengeCreateActivity로 이동
            startActivity(intent)
            finish() // 현재 액티비티 종료
        }

        // View 초기화
        horizontalScrollView = findViewById(R.id.horizontalScrollView)
        circleContainer = findViewById(R.id.circleContainer)
        scrollButton = findViewById(R.id.scrollRightButton)

        // 버튼 클릭 시 동그라미가 하나씩 옆으로 밀리도록
        scrollButton.setOnClickListener {
            // 현재 스크롤 위치에서 오른쪽으로 이동
            horizontalScrollView.smoothScrollBy(scrollStep, 0)

            // Toast로 확인
            Toast.makeText(this, "밀기 클릭됨!", Toast.LENGTH_SHORT).show()
        }
    }
}
