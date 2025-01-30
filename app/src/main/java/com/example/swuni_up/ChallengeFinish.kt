package com.example.swuni_up

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChallengeFinish : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var challengerAdapter: ChallengerAdapter
    private lateinit var dbHelper: ChallengerDBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_challenge_finish)

        dbHelper = ChallengerDBHelper(this)

        recyclerView = findViewById(R.id.recyclerViewChallengers)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadChallengers()
        loadTopChallengers()
    }

    private fun loadChallengers() {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${ChallengerDBHelper.TABLE_CHALLENGER} ORDER BY ${ChallengerDBHelper.COLUMN_PERCENTAGE} DESC LIMIT -1 OFFSET 3",
            null
        )

        val challengers = mutableListOf<ChallengerDBHelper.Challenger>()

        while (cursor.moveToNext()) {
            val userId = cursor.getLong(cursor.getColumnIndexOrThrow(ChallengerDBHelper.COLUMN_USER_ID))
            val challengeId = cursor.getLong(cursor.getColumnIndexOrThrow(ChallengerDBHelper.COLUMN_CHALLENGE_ID))
            val challengeRole = cursor.getString(cursor.getColumnIndexOrThrow(ChallengerDBHelper.COLUMN_CHALLENGE_ROLE))
            val joinedAt = cursor.getString(cursor.getColumnIndexOrThrow(ChallengerDBHelper.COLUMN_JOINED_AT))
            val percentage = cursor.getInt(cursor.getColumnIndexOrThrow(ChallengerDBHelper.COLUMN_PERCENTAGE))

            challengers.add(ChallengerDBHelper.Challenger(userId, challengeId, challengeRole, joinedAt, percentage))
        }
        cursor.close()

        challengerAdapter = ChallengerAdapter(this, challengers)
        recyclerView.adapter = challengerAdapter
    }

    private fun loadTopChallengers() {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${ChallengerDBHelper.TABLE_CHALLENGER} ORDER BY ${ChallengerDBHelper.COLUMN_PERCENTAGE} DESC LIMIT 3",
            null
        )

        val topChallengers = mutableListOf<ChallengerDBHelper.Challenger>()

        while (cursor.moveToNext()) {
            val userId = cursor.getLong(cursor.getColumnIndexOrThrow(ChallengerDBHelper.COLUMN_USER_ID))
            val challengeId = cursor.getLong(cursor.getColumnIndexOrThrow(ChallengerDBHelper.COLUMN_CHALLENGE_ID))
            val challengeRole = cursor.getString(cursor.getColumnIndexOrThrow(ChallengerDBHelper.COLUMN_CHALLENGE_ROLE))
            val joinedAt = cursor.getString(cursor.getColumnIndexOrThrow(ChallengerDBHelper.COLUMN_JOINED_AT))
            val percentage = cursor.getInt(cursor.getColumnIndexOrThrow(ChallengerDBHelper.COLUMN_PERCENTAGE))

            topChallengers.add(ChallengerDBHelper.Challenger(userId, challengeId, challengeRole, joinedAt, percentage))
        }
        cursor.close()

        Log.d("TopChallengers", "Loaded challengers: ${topChallengers.size}")

        val topRankRecyclerView: RecyclerView = findViewById(R.id.recyclerViewTopChallengers)

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        layoutManager.reverseLayout = true // 아이템을 반대로 배치하여 bottom으로 정렬

        topRankRecyclerView.layoutManager = layoutManager
        val adapter = TopRankAdapter(this, topChallengers)
        topRankRecyclerView.adapter = adapter

    }

}
