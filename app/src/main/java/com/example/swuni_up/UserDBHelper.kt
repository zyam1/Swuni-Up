package com.example.swuni_up

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
    fun getUserByEmail(email: String): User? {
        val db = readableDatabase
        Log.d("UserDBHelper", "Searching for user with Email: $email")

        val cursor = db.query(
            TABLE_USER,
            arrayOf(COLUMN_ID, COLUMN_NAME, COLUMN_EMAIL, COLUMN_PASSWORD, COLUMN_KAKAO_ID, COLUMN_NICKNAME, COLUMN_MAJOR),
            "$COLUMN_EMAIL = ?",
            arrayOf(email),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            Log.d("UserDBHelper", "User found: Email = $email")
            val user = User(
                id = cursor.getLong(0),
                name = cursor.getString(1),
                email = cursor.getString(2),
                password = cursor.getString(3),
                kakaoId = cursor.getString(4),
                nickname = cursor.getString(5),
                major = cursor.getString(6),
                photo = null
            )
            cursor.close()
            return user
        }
        cursor.close()
        Log.d("UserDBHelper", "No user found with Email: $email")
        return null
    }

    fun getUserProfilePhotoByNick(nickname: String): Bitmap? {
        val db = this.readableDatabase
        val query = "SELECT ${COLUMN_PHOTO} FROM ${TABLE_USER} WHERE ${COLUMN_NICKNAME} = ?"
        val cursor = db.rawQuery(query, arrayOf(nickname))

        var bitmap: Bitmap? = null
        Log.d("UserDBHelper", "닉네임: $nickname"+"로 프로필 사진을 조회합니다.")
        if (cursor.moveToFirst()) {
            val photoBlob = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_PHOTO))
            Log.d("UserDBHelper", "프로필 사진 데이터를 성공적으로 가져왔습니다. 데이터 크기: ${photoBlob.size} bytes")
            if (photoBlob != null) {
                val options = BitmapFactory.Options()
                options.inSampleSize = 2  // 이미지 크기 축소 (필요에 따라 조정)
                bitmap = BitmapFactory.decodeByteArray(photoBlob, 0, photoBlob.size, options)
                Log.d("UserDBHelper", "Bitmap 변환 완료.")
            }
        } else {
            Log.d("UserDBHelper", "해당 닉네임에 대한 프로필 사진이 없습니다.")
        }
        cursor.close()
        db.close()

        return bitmap
    }

    fun getUserProfilePhotoById(userId: Long): Bitmap? {
        val db = this.readableDatabase
        val query = "SELECT ${COLUMN_PHOTO} FROM ${TABLE_USER} WHERE ${COLUMN_ID} = ?"
        val cursor = db.rawQuery(query, arrayOf(userId.toString()))

        var bitmap: Bitmap? = null
        Log.d("UserDBHelper", "아이디: $userId" +"로 프로필 사진을 조회합니다.")
        if (cursor.moveToFirst()) {
            val photoBlob = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_PHOTO))
            Log.d("UserDBHelper", "프로필 사진 데이터를 성공적으로 가져왔습니다. 데이터 크기: ${photoBlob.size} bytes")
            if (photoBlob != null) {
                val options = BitmapFactory.Options()
                options.inSampleSize = 2  // 이미지 크기 축소 (필요에 따라 조정)
                bitmap = BitmapFactory.decodeByteArray(photoBlob, 0, photoBlob.size, options)
                Log.d("UserDBHelper", "Bitmap 변환 완료.")
            }
        } else {
            Log.d("UserDBHelper", "해당 아이디에 대한 프로필 사진이 없습니다.")
        }
        cursor.close()
        db.close()

        return bitmap
    }

    fun getNicknameById(userId: Long): String? {
        val db = this.readableDatabase
        val query = "SELECT $COLUMN_NICKNAME FROM $TABLE_USER WHERE $COLUMN_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(userId.toString()))

        var nickname: String? = null
        if (cursor.moveToFirst()) {
            nickname = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NICKNAME))
        }
        cursor.close()
        db.close()

        return nickname
    }

    fun getMajorById(userId: Long): String? {
        val db = this.readableDatabase
        val query = "SELECT $COLUMN_MAJOR FROM $TABLE_USER WHERE $COLUMN_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(userId.toString()))

        var major: String? = null
        if (cursor.moveToFirst()) {
            major = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MAJOR))
        }
        cursor.close()
        db.close()

        return major
    }



}
