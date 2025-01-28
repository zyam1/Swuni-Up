package com.example.swuni_up

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class UserDBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "user.db"
        const val DATABASE_VERSION = 2  // 버전 업 (사진 컬럼 추가)

        const val TABLE_USER = "User"

        const val COLUMN_ID = "user_id"
        const val COLUMN_NAME = "user_name"
        const val COLUMN_EMAIL = "user_email"
        const val COLUMN_PASSWORD = "user_pwd"
        const val COLUMN_KAKAO_ID = "user_kakaoID"
        const val COLUMN_NICKNAME = "user_nick"
        const val COLUMN_MAJOR = "user_major"
        const val COLUMN_PHOTO = "user_photo"  // 사진 추가 (BLOB 타입)
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.d("UserDBHelper", "onCreate called")

        val createTable = "CREATE TABLE $TABLE_USER (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_NAME TEXT NOT NULL, " +
                "$COLUMN_EMAIL TEXT NOT NULL UNIQUE, " +
                "$COLUMN_PASSWORD TEXT NOT NULL, " +
                "$COLUMN_KAKAO_ID TEXT NOT NULL, " +
                "$COLUMN_NICKNAME TEXT NOT NULL, " +
                "$COLUMN_MAJOR TEXT, " +
                "$COLUMN_PHOTO BLOB" +  // 사진을 BLOB으로 저장 <-나중에 임시이미지 설정 넣어야함
                ")"

        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {  // 버전이 낮다면 user_photo 컬럼 추가
            db.execSQL("ALTER TABLE $TABLE_USER ADD COLUMN $COLUMN_PHOTO BLOB")
        }
    }

    // 회원가입 기능 (사진 포함)
    fun insertUser(user: User): Long {
        val db = writableDatabase
        val contentValues = ContentValues()

        contentValues.put(COLUMN_NAME, user.name)
        contentValues.put(COLUMN_EMAIL, user.email)
        contentValues.put(COLUMN_PASSWORD, user.password)
        contentValues.put(COLUMN_KAKAO_ID, user.kakaoId)
        contentValues.put(COLUMN_NICKNAME, user.nickname)
        contentValues.put(COLUMN_MAJOR, user.major)
        contentValues.put(COLUMN_PHOTO, user.photo)  // 사진 저장

        Log.d("UserDBHelper", "Inserting User: $contentValues")

        return db.insert(TABLE_USER, null, contentValues)
    }

    // 사용자 데이터 클래스
    data class User(
        val id: Long? = null,
        val name: String,
        val email: String,
        val password: String,
        val kakaoId: String,
        val nickname: String,
        val major: String?,
        val photo: ByteArray?  // 사진을 ByteArray로 저장
    )

    // 로그인
    fun getUserById(userId: String): User? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USER,
            arrayOf(COLUMN_ID, COLUMN_NAME, COLUMN_EMAIL, COLUMN_PASSWORD, COLUMN_KAKAO_ID, COLUMN_NICKNAME, COLUMN_MAJOR, COLUMN_PHOTO),
            "$COLUMN_NAME = ?",  // user_name 기준으로 검색
            arrayOf(userId),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            val user = User(
                id = cursor.getLong(0),
                name = cursor.getString(1),
                email = cursor.getString(2),
                password = cursor.getString(3),
                kakaoId = cursor.getString(4),
                nickname = cursor.getString(5),
                major = cursor.getString(6),
                photo = cursor.getBlob(7)
            )
            cursor.close()
            return user
        }
        cursor.close()
        return null
    }

}
