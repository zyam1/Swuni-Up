package com.example.swuni_up

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log


class ChallengeDBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "swuniup.db"
        const val DATABASE_VERSION = 1
        const val TABLE_CHALLENGE = "Challenge"

        const val COLUMN_ID = "challenge_id"
        const val COLUMN_TITLE = "challenge_title"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_PHOTO = "challenge_photo"
        const val COLUMN_CREATED_AT = "created_at"
        const val COLUMN_START_DAY = "start_day"
        const val COLUMN_END_DAY = "end_day"
        const val COLUMN_STATUS = "status"
        const val COLUMN_MAX_PARTICIPANT = "max_participant"
        const val COLUMN_CATEGORY = "category"
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.d("ChallengeDBHelper", "onCreate called")
        val createTable = "CREATE TABLE $TABLE_CHALLENGE (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_TITLE TEXT NOT NULL, " +
                "$COLUMN_DESCRIPTION TEXT, " +
                "$COLUMN_PHOTO BLOB NOT NULL, " +
                "$COLUMN_CREATED_AT DATE NOT NULL, " +
                "$COLUMN_START_DAY DATE NOT NULL, " +
                "$COLUMN_END_DAY DATE NOT NULL, " +
                "$COLUMN_STATUS INTEGER NOT NULL, " +
                "$COLUMN_MAX_PARTICIPANT INTEGER NOT NULL, " +
                "$COLUMN_CATEGORY INTEGER NOT NULL" +
                ")"
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CHALLENGE")
        onCreate(db)
    }

    // Challenge 객체를 인자로 받도록 수정
    fun insertChallenge(challenge: Challenge): Long {
        val db = writableDatabase

        val contentValues = ContentValues()

        contentValues.put(COLUMN_TITLE, challenge.title)
        contentValues.put(COLUMN_DESCRIPTION, challenge.description)
        contentValues.put(COLUMN_PHOTO, challenge.photo)
        contentValues.put(COLUMN_CREATED_AT, challenge.createdAt)
        contentValues.put(COLUMN_START_DAY, challenge.startDay)
        contentValues.put(COLUMN_END_DAY, challenge.endDay)
        contentValues.put(COLUMN_STATUS, challenge.status)
        contentValues.put(COLUMN_MAX_PARTICIPANT, challenge.maxParticipant)
        contentValues.put(COLUMN_CATEGORY, challenge.category)

        Log.d("ChallengeDBHelper", "Inserting Challenge: $contentValues")

        // 데이터베이스에 삽입
        return db.insert(TABLE_CHALLENGE, null, contentValues)
    }

    // Challenge 데이터 클래스
    data class Challenge(
        val title: String,
        val description: String?,
        val photo: ByteArray, // 썸네일은 ByteArray로 처리
        val createdAt: String, // "yyyy-MM-dd HH:mm:ss"
        val startDay: String, // "yyyy-MM-dd"
        val endDay: String, // "yyyy-MM-dd"
        val status: Int, // 상태
        val maxParticipant: Int,
        val category: Int
    )
}
