package com.example.swuni_up

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.swuni_up.DBHelper.Companion.COLUMN_CHEER_LOG_ID
import com.example.swuni_up.DBHelper.Companion.COLUMN_LOG_CHALLENGER_ID
import com.example.swuni_up.DBHelper.Companion.COLUMN_LOG_CHALLENGE_ID
import com.example.swuni_up.DBHelper.Companion.COLUMN_LOG_ID
import com.example.swuni_up.DBHelper.Companion.TABLE_CHEER
import com.example.swuni_up.DBHelper.Companion.TABLE_LOG

class ChallengeFinish : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var challengerAdapter: ChallengerAdapter
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_challenge_finish)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // 뒤로가기 버튼 동작 설정
        toolbar.setNavigationOnClickListener {
            finish() // 현재 액티비티 종료
        }

        val nickTextView1 = findViewById<TextView>(R.id.nickTextView1)
        val profile1 = findViewById<ImageView>(R.id.profile1)
        val upTextView1 = findViewById<TextView>(R.id.upTextView1)
        val nickTextView2 = findViewById<TextView>(R.id.nickTextView2)
        val profile2 = findViewById<ImageView>(R.id.profile2)
        val upTextView2 = findViewById<TextView>(R.id.upTextView2)

        dbHelper = DBHelper(this)

        recyclerView = findViewById(R.id.recyclerViewChallengers)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 챌린지 아이디 가져오기
        val challengeId = intent.getLongExtra("challenge_id", -1L)

        if (challengeId != -1L) {
            loadChallengers(challengeId) // 챌린지 아이디를 전달하여 챌린저들만 로드
            loadTopChallengers(challengeId) // 상위 챌린저들도 로드
        } else {
            Log.e("ChallengeFinish", "챌린지 ID가 유효하지 않습니다.")
        }

        val topChallenger = getTopChallengerForChallenge(challengeId)

        if (topChallenger != null) {
            val (challengerId, cheerCount) = topChallenger
            println("가장 많은 응원을 받은 챌린저 ID: $challengerId, 응원 개수: $cheerCount")

            upTextView1.text = "응원 수: $cheerCount"
        } else {
            println("챌린지에 참가한 챌린저가 없거나 응원을 받은 사람이 없음.")
        }

        val topCheerChallenger = dbHelper.getTopCheerChallengerForChallenge(challengeId)

        if (topCheerChallenger != null) {
            val (challengerId, cheerCount) = topCheerChallenger

            // 챌린저 ID로 유저 프로필 사진과 닉네임 가져오기
            val profileImage = dbHelper.getUserProfilePhotoByChallengerId(challengerId)
            val userName = dbHelper.getNicknameByChallengerId(challengerId)

            if (userName != null) {
                // UI 업데이트
                upTextView2.text = "$cheerCount"
                nickTextView2.text = "$userName"

                // 프로필 이미지 로드
                if (profileImage != null) {
                    profile2.setImageBitmap(profileImage)
                } else {
                    Log.d("UserDBHelper", "해당 유저의 프로필 사진이 없습니다.")
                }
            } else {
                Log.d("UserDBHelper", "유저의 닉네임을 가져올 수 없습니다.")
            }
        } else {
            println("챌린지에 참가한 챌린저가 없거나 응원을 받은 사람이 없음.")
        }

        val topCheerUserChallenger = dbHelper.getTopCheerUserIdForChallenge(challengeId)

        if (topCheerUserChallenger != null) {
            val (userId, cheerCount) = topCheerUserChallenger

            // 챌린저 ID로 유저 프로필 사진과 닉네임 가져오기
            val profileImage = dbHelper.getUserProfilePhotoById(userId)
            val userName = dbHelper.getNicknameById(userId)

            if (userName != null) {
                // UI 업데이트
                upTextView1.text = "$cheerCount"
                nickTextView1.text = "$userName"

                // 프로필 이미지 로드
                if (profileImage != null) {
                    profile1.setImageBitmap(profileImage)
                } else {
                    Log.d("UserDBHelper", "해당 유저의 프로필 사진이 없습니다.")
                }
            } else {
                Log.d("UserDBHelper", "유저의 닉네임을 가져올 수 없습니다.")
            }
        } else {
            println("챌린지에 참가한 챌린저가 없거나 응원을 받은 사람이 없음.")
        }

    }

    fun getTopChallengerForChallenge(challengeId: Long): Pair<Long, Int>? {
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            """
        SELECT l.$COLUMN_LOG_CHALLENGER_ID, COUNT(c.$COLUMN_CHEER_LOG_ID) AS cheer_count
        FROM $TABLE_LOG l
        LEFT JOIN $TABLE_CHEER c ON l.$COLUMN_LOG_ID = c.$COLUMN_CHEER_LOG_ID
        WHERE l.$COLUMN_LOG_CHALLENGE_ID = ?
        GROUP BY l.$COLUMN_LOG_CHALLENGER_ID
        ORDER BY cheer_count DESC
        LIMIT 1
        """,
            arrayOf(challengeId.toString())
        )

        var topChallenger: Pair<Long, Int>? = null
        if (cursor.moveToFirst()) {
            val challengerId = cursor.getLong(0) // 챌린저 ID
            val cheerCount = cursor.getInt(1) // 응원 개수
            topChallenger = Pair(challengerId, cheerCount)
        }

        cursor.close()
        db.close()
        return topChallenger
    }

    // 특정 챌린지에 참여한 챌린저들 로드
    private fun loadChallengers(challengeId: Long) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${DBHelper.TABLE_CHALLENGER} WHERE ${DBHelper.COLUMN_CHALLENGE_ID} = ? ORDER BY ${DBHelper.COLUMN_PERCENTAGE} DESC LIMIT -1 OFFSET 3",
            arrayOf(challengeId.toString()) // challengeId를 쿼리에 전달
        )

        val challengers = mutableListOf<DBHelper.Challenger>()

        while (cursor.moveToNext()) {
            val challengerId = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_CHALLENGER_ID))
            val userId = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_USER_ID))
            val challengeId = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_CHALLENGE_ID))
            val challengeRole = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_CHALLENGE_ROLE))
            val joinedAt = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_JOINED_AT))
            val percentage = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_PERCENTAGE))

            challengers.add(DBHelper.Challenger(challengerId, userId, challengeId, challengeRole, joinedAt, percentage))
        }
        cursor.close()

        challengerAdapter = ChallengerAdapter(this, challengers)
        recyclerView.adapter = challengerAdapter
    }

    // 특정 챌린지에서 상위 3명의 챌린저만 로드
    private fun loadTopChallengers(challengeId: Long) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${DBHelper.TABLE_CHALLENGER} WHERE ${DBHelper.COLUMN_CHALLENGE_ID} = ? ORDER BY ${DBHelper.COLUMN_PERCENTAGE} DESC LIMIT 3",
            arrayOf(challengeId.toString()) // challengeId를 쿼리에 전달
        )

        val topChallengers = mutableListOf<DBHelper.Challenger>()

        while (cursor.moveToNext()) {
            val challengerId = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_CHALLENGER_ID))
            val userId = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_USER_ID))
            val challengeId = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_CHALLENGE_ID))
            val challengeRole = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_CHALLENGE_ROLE))
            val joinedAt = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_JOINED_AT))
            val percentage = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_PERCENTAGE))

            topChallengers.add(DBHelper.Challenger(challengerId, userId, challengeId, challengeRole, joinedAt, percentage))
        }
        cursor.close()

        Log.d("TopChallengers", "Loaded challengers: ${topChallengers.size}")

        val topRankRecyclerView: RecyclerView = findViewById(R.id.recyclerViewTopChallengers)

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        layoutManager.reverseLayout = true // 아이템을 반대로 배치하여 bottom으로 정렬

        topRankRecyclerView.layoutManager = layoutManager
        val adapter = topRankAdapter(this, topChallengers)
        topRankRecyclerView.adapter = adapter
    }

}

