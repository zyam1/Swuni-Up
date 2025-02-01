package com.example.swuni_up

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "swuniup.db"
        const val DATABASE_VERSION = 2

        // Challenge 테이블
        const val TABLE_CHALLENGE = "Challenge"
        const val COLUMN_CHALLENGE_ID = "challenge_id"
        const val COLUMN_CHALLENGE_TITLE = "challenge_title"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_PHOTO = "challenge_photo"
        const val COLUMN_CREATED_AT = "created_at"
        const val COLUMN_START_DAY = "start_day"
        const val COLUMN_END_DAY = "end_day"
        const val COLUMN_STATUS = "status"
        const val COLUMN_MAX_PARTICIPANT = "max_participant"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_IS_OFFICIAL = "is_official"

        // Challenger 테이블
        const val TABLE_CHALLENGER = "Challenger"
        const val COLUMN_CHALLENGER_ID = "challenger_id"
        const val COLUMN_USER_ID = "user_id"
        const val COLUMN_CHALLENGE_ID_FK = "challenge_id"
        const val COLUMN_CHALLENGE_ROLE = "challenge_role"
        const val COLUMN_JOINED_AT = "joined_at"
        const val COLUMN_PERCENTAGE = "percentage"

        // User 테이블
        const val TABLE_USER = "User"
        const val COLUMN_USER_NAME = "user_name"
        const val COLUMN_EMAIL = "user_email"
        const val COLUMN_PASSWORD = "user_pwd"
        const val COLUMN_KAKAO_ID = "user_kakaoID"
        const val COLUMN_NICKNAME = "user_nick"
        const val COLUMN_MAJOR = "user_major"
        const val COLUMN_PHOTO_USER = "user_photo"
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.d("MainDBHelper", "onCreate called")

        val createChallengeTable = """
            CREATE TABLE $TABLE_CHALLENGE (
                $COLUMN_CHALLENGE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CHALLENGE_TITLE TEXT NOT NULL,
                $COLUMN_DESCRIPTION TEXT,
                $COLUMN_PHOTO BLOB NOT NULL,
                $COLUMN_CREATED_AT DATE NOT NULL,
                $COLUMN_START_DAY DATE NOT NULL,
                $COLUMN_END_DAY DATE NOT NULL,
                $COLUMN_STATUS INTEGER NOT NULL,
                $COLUMN_MAX_PARTICIPANT INTEGER NOT NULL,
                $COLUMN_CATEGORY INTEGER NOT NULL,
                $COLUMN_IS_OFFICIAL INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent()

        val createChallengerTable = """
            CREATE TABLE $TABLE_CHALLENGER (
                $COLUMN_CHALLENGER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_ID INTEGER,
                $COLUMN_CHALLENGE_ID_FK INTEGER NOT NULL,
                $COLUMN_CHALLENGE_ROLE TEXT NOT NULL CHECK($COLUMN_CHALLENGE_ROLE IN ('admin', 'participant')),
                $COLUMN_JOINED_AT DATETIME NOT NULL,
                $COLUMN_PERCENTAGE INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY ($COLUMN_CHALLENGE_ID_FK) REFERENCES $TABLE_CHALLENGE($COLUMN_CHALLENGE_ID),
                FOREIGN KEY ($COLUMN_USER_ID) REFERENCES $TABLE_USER($COLUMN_USER_ID)
            )
        """.trimIndent()

        val createUserTable = """
            CREATE TABLE $TABLE_USER (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_NAME TEXT NOT NULL,
                $COLUMN_EMAIL TEXT NOT NULL UNIQUE,
                $COLUMN_PASSWORD TEXT NOT NULL,
                $COLUMN_KAKAO_ID TEXT NOT NULL,
                $COLUMN_NICKNAME TEXT NOT NULL,
                $COLUMN_MAJOR TEXT,
                $COLUMN_PHOTO_USER BLOB
            )
        """.trimIndent()

        db.execSQL(createChallengeTable)
        db.execSQL(createChallengerTable)
        db.execSQL(createUserTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_USER ADD COLUMN $COLUMN_PHOTO_USER BLOB")
        }
    }

    // Challenge 추가
    fun insertChallenge(challenge: Challenge): Long {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_CHALLENGE_TITLE, challenge.title)
            put(COLUMN_DESCRIPTION, challenge.description)
            put(COLUMN_PHOTO, challenge.photo)
            put(COLUMN_CREATED_AT, challenge.createdAt)
            put(COLUMN_START_DAY, challenge.startDay)
            put(COLUMN_END_DAY, challenge.endDay)
            put(COLUMN_STATUS, challenge.status)
            put(COLUMN_MAX_PARTICIPANT, challenge.maxParticipant)
            put(COLUMN_CATEGORY, challenge.category)
            put(COLUMN_IS_OFFICIAL, challenge.isOfficial)
        }
        return db.insert(TABLE_CHALLENGE, null, contentValues)
    }

    // Challenger 추가
    fun insertChallenger(challenger: Challenger): Long {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_USER_ID, challenger.userId)
            put(COLUMN_CHALLENGE_ID_FK, challenger.challengeId)
            put(COLUMN_CHALLENGE_ROLE, challenger.challengeRole)
            put(COLUMN_JOINED_AT, challenger.joinedAt)
            put(COLUMN_PERCENTAGE, challenger.percentage)
        }
        return db.insert(TABLE_CHALLENGER, null, contentValues)
    }

    // User 추가
    fun insertUser(user: User): Long {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_USER_NAME, user.name)
            put(COLUMN_EMAIL, user.email)
            put(COLUMN_PASSWORD, user.password)
            put(COLUMN_KAKAO_ID, user.kakaoId)
            put(COLUMN_NICKNAME, user.nickname)
            put(COLUMN_MAJOR, user.major)
            put(COLUMN_PHOTO_USER, user.photo)
        }
        return db.insert(TABLE_USER, null, contentValues)
    }

    // Challenge 데이터 클래스
    data class Challenge(
        val challengeId: Long? = null,
        val title: String,
        val description: String?,
        val photo: ByteArray,
        val createdAt: String,
        val startDay: String,
        val endDay: String,
        val status: Int,
        val maxParticipant: Int,
        val category: Int,
        val isOfficial: Int
    )

    // Challenger 데이터 클래스
    data class Challenger(
        val userId: Long,
        val challengeId: Long,
        val challengeRole: String,
        val joinedAt: String,
        val percentage: Int
    )

    // User 데이터 클래스
    data class User(
        val id: Long? = null,
        val name: String,
        val email: String,
        val password: String,
        val kakaoId: String,
        val nickname: String,
        val major: String?,
        val photo: ByteArray?
    )

    // 로그인
    // DBHelper.kt

    // User 정보를 가져오는 함수 (이메일을 기준으로)
    fun getUserByEmail(email: String): User? {
        val db = readableDatabase
        Log.d("UserDBHelper", "Searching for user with Email: $email")

        val cursor = db.query(
            TABLE_USER,  // DBHelper에서 정의된 TABLE_USER 사용
            arrayOf(
                COLUMN_USER_ID, COLUMN_USER_NAME,
                COLUMN_EMAIL, COLUMN_PASSWORD,
                COLUMN_KAKAO_ID, COLUMN_NICKNAME,
                COLUMN_MAJOR  // DBHelper에서 정의된 COLUMN_들 사용
            ),
            "$COLUMN_EMAIL = ?",  // 이메일로 검색
            arrayOf(email),  // 이메일 값 넘김
            null, null, null
        )

        var user: User? = null
        if (cursor.moveToFirst()) {
            // 커서에서 유저 정보를 추출
            user = User(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)),
                kakaoId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_KAKAO_ID)),
                nickname = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NICKNAME)),
                major = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MAJOR)),
                photo = null // 프로필 사진이 없으면 null 처리 (필요시 수정)
            )
        }
        cursor.close()
        db.close()

        return user
    }


    fun getUserProfilePhotoByNick(nickname: String): Bitmap? {
        val db = this.readableDatabase
        val query = "SELECT ${COLUMN_PHOTO_USER} FROM ${TABLE_USER} WHERE ${COLUMN_NICKNAME} = ?"
        val cursor = db.rawQuery(query, arrayOf(nickname))

        var bitmap: Bitmap? = null
        Log.d("UserDBHelper", "닉네임: $nickname"+"로 프로필 사진을 조회합니다.")
        if (cursor.moveToFirst()) {
            val photoBlob = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_PHOTO_USER))
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
        val query = "SELECT ${COLUMN_PHOTO_USER} FROM ${TABLE_USER} WHERE ${COLUMN_USER_ID} = ?"
        val cursor = db.rawQuery(query, arrayOf(userId.toString()))

        var bitmap: Bitmap? = null
        Log.d("UserDBHelper", "아이디: $userId" +"로 프로필 사진을 조회합니다.")
        if (cursor.moveToFirst()) {
            val photoBlob = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_PHOTO_USER))
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
        val query = "SELECT ${COLUMN_NICKNAME} FROM ${TABLE_USER} WHERE ${COLUMN_USER_ID} = ?"
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
        val query = "SELECT ${COLUMN_MAJOR} FROM ${TABLE_USER} WHERE ${COLUMN_USER_ID} = ?"
        val cursor = db.rawQuery(query, arrayOf(userId.toString()))

        var major: String? = null
        if (cursor.moveToFirst()) {
            major = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MAJOR))
        }
        cursor.close()
        db.close()

        return major
    }

    fun getChallengeProgress(challengeId: Long): Int {
        val db = this.readableDatabase
        var progress = 0

        val cursor = db.rawQuery(
            "SELECT AVG($COLUMN_PERCENTAGE) FROM $TABLE_CHALLENGER WHERE $COLUMN_CHALLENGE_ID_FK = ?",
            arrayOf(challengeId.toString())
        )

        if (cursor.moveToFirst()) {
            progress = if (cursor.isNull(0)) 0 else cursor.getInt(0) // NULL 체크
        }

        cursor.close()
        db.close()

        return progress
    }



    fun getOngoingChallenges(): List<Challenge> {
        val db = this.readableDatabase
        val challengeList = mutableListOf<Challenge>()

        val cursor = db.rawQuery(
            """
        SELECT c.*, 
            (julianday(c.end_day) - julianday(c.start_day)) AS challenge_days, 
            (SELECT COUNT(*) FROM $TABLE_CHALLENGER ch WHERE ch.$COLUMN_CHALLENGE_ID_FK = c.$COLUMN_CHALLENGE_ID) AS participants
        FROM $TABLE_CHALLENGE c
        WHERE c.$COLUMN_STATUS = 1
        ORDER BY c.$COLUMN_END_DAY ASC
        """, null
        )

        if (cursor.moveToFirst()) {
            do {
                val challengeDays = cursor.getInt(cursor.getColumnIndexOrThrow("challenge_days"))
                val participants = cursor.getInt(cursor.getColumnIndexOrThrow("participants"))

                val challenge = Challenge(
                    challengeId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CHALLENGE_ID)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CHALLENGE_TITLE)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                    photo = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_PHOTO)),
                    createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)),
                    startDay = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START_DAY)),
                    endDay = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_END_DAY)),
                    status = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STATUS)),
                    maxParticipant = participants,
                    category = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                    isOfficial = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_OFFICIAL))
                )
                challengeList.add(challenge)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        return challengeList
    }

    fun getCompleteChallenges(): List<Challenge> {
        val db = this.readableDatabase
        val challengeList = mutableListOf<Challenge>()

        val cursor = db.rawQuery(
            """
        SELECT c.*, 
            (julianday(c.end_day) - julianday(c.start_day)) AS challenge_days, 
            (SELECT COUNT(*) FROM $TABLE_CHALLENGER ch WHERE ch.$COLUMN_CHALLENGE_ID_FK = c.$COLUMN_CHALLENGE_ID) AS participants
        FROM $TABLE_CHALLENGE c
        WHERE c.$COLUMN_STATUS = 2
        ORDER BY c.$COLUMN_END_DAY DESC
        """, null
        )

        if (cursor.moveToFirst()) {
            do {
                val challengeDays = cursor.getInt(cursor.getColumnIndexOrThrow("challenge_days"))
                val participants = cursor.getInt(cursor.getColumnIndexOrThrow("participants"))

                val challenge = Challenge(
                    challengeId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CHALLENGE_ID)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CHALLENGE_TITLE)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                    photo = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_PHOTO)),
                    createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)),
                    startDay = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START_DAY)),
                    endDay = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_END_DAY)),
                    status = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STATUS)),
                    maxParticipant = participants,
                    category = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                    isOfficial = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_OFFICIAL))
                )
                challengeList.add(challenge)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        return challengeList
    }



}
