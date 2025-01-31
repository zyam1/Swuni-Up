package com.example.swuni_up

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChallengeInfoFeed : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var dbHelper: DBHelper
    private var challengeId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.challenge_info_feed)

        challengeId = intent.getLongExtra("challenge_id", -1) // 챌린지 ID 받기

        dbHelper = DBHelper(this)
        recyclerView = findViewById(R.id.recyclerView)

        // DB에서 데이터를 가져오고 RecyclerView에 연결
        val feedItems = getFeedItems()
        val adapter = FeedAdapter(feedItems)
        recyclerView.adapter = adapter

        // RecyclerView의 레이아웃 매니저 설정 (2열 그리드)
        recyclerView.layoutManager = GridLayoutManager(this, 2) // 2열로 설정
    }

    private fun getFeedItems(): List<FeedItem> {
        val feedItems = mutableListOf<FeedItem>()
        val db = dbHelper.readableDatabase

        val query = """
            SELECT log.log_photo, challenger.nickname, challenger.department
            FROM Log AS log
            JOIN Challenger AS challenger ON log.challenger_id = challenger.challenger_id
            WHERE log.challenge_id = ?
            ORDER BY log.log_date DESC
        """.trimIndent()

        db.rawQuery(query, arrayOf(challengeId.toString())).use { cursor ->
            while (cursor.moveToNext()) {
                val image = cursor.getBlob(cursor.getColumnIndexOrThrow("log_photo"))
                val nickname = cursor.getString(cursor.getColumnIndexOrThrow("nickname"))
                val department = cursor.getString(cursor.getColumnIndexOrThrow("department"))
                feedItems.add(FeedItem(image, nickname, department))
            }
        }

        return feedItems
    }
}
