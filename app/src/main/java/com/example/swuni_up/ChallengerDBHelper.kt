package com.example.swuni_up

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class ChallengerDBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "challenger.db"
        const val DATABASE_VERSION = 1
        const val TABLE_CHALLENGER = "Challenger"

        // Challenger 테이블의 컬럼 정의
        const val COLUMN_ID = "challenger_id"
        const val COLUMN_USER_ID = "user_id"
        const val COLUMN_CHALLENGE_ID = "challenge_id"
        const val COLUMN_CHALLENGE_ROLE = "challenge_role"
        const val COLUMN_JOINED_AT = "joined_at"
        const val COLUMN_PERCENTAGE = "percentage"
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.d("ChallengerDBHelper", "onCreate called")
        // Challenger 테이블 생성 쿼리
        val createTable = "CREATE TABLE $TABLE_CHALLENGER (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_USER_ID INTEGER, " +
                "$COLUMN_CHALLENGE_ID INTEGER NOT NULL, " +
                "$COLUMN_CHALLENGE_ROLE TEXT NOT NULL CHECK($COLUMN_CHALLENGE_ROLE IN ('admin', 'participant')), " + // ENUM 역할 설정
                "$COLUMN_JOINED_AT DATETIME NOT NULL, " +
                "$COLUMN_PERCENTAGE INTEGER NOT NULL DEFAULT 0 " +
                ")"
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CHALLENGER")
        onCreate(db)
    }

    // Challenger 객체를 인자로 받아서 삽입하는 함수
    fun insertChallenger(challenger: Challenger): Long {
        val db = writableDatabase

        val contentValues = ContentValues()

        contentValues.put(COLUMN_USER_ID, challenger.userId)
        contentValues.put(COLUMN_CHALLENGE_ID, challenger.challengeId)
        contentValues.put(COLUMN_CHALLENGE_ROLE, challenger.challengeRole)
        contentValues.put(COLUMN_JOINED_AT, challenger.joinedAt)
        contentValues.put(COLUMN_PERCENTAGE, challenger.percentage)

        Log.d("ChallengerDBHelper", "Inserting Challenger: $contentValues")

        // Challenger 테이블에 데이터 삽입
        return db.insert(TABLE_CHALLENGER, null, contentValues)
    }

    // Challenger 데이터 클래스
    data class Challenger(
        val userId: Long,
        val challengeId: Long,
        val challengeRole: String, // 'admin' 또는 'participant'
        val joinedAt: String, // "yyyy-MM-dd HH:mm:ss"
        val percentage: Int,  // 참가율
    )
}
