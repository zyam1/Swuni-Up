package com.example.swuni_up

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.time.LocalDate

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
        const val COLUMN_NICKNAME = "user_nick"
        const val COLUMN_MAJOR = "user_major"
        const val COLUMN_PHOTO_USER = "user_photo"

        // Log 테이블
        const val TABLE_LOG = "Log"
        const val COLUMN_LOG_ID = "log_id"
        const val COLUMN_LOG_CHALLENGE_ID = "challenge_id"
        const val COLUMN_LOG_CHALLENGER_ID = "challenger_id"
        const val COLUMN_LOG_DATE = "log_date"
        const val COLUMN_LOG_PHOTO = "log_image"

        // 응원 테이블
        const val TABLE_CHEER = "Cheer"
        const val COLUMN_CHEER_ID = "cheer_id"
        const val COLUMN_CHEER_LOG_ID = "log_id"
        const val COLUMN_CHEER_USER_ID = "user_id"
        const val COLUMN_CHEER_AT = "cheer_at"
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
                $COLUMN_NICKNAME TEXT NOT NULL,
                $COLUMN_MAJOR TEXT,
                $COLUMN_PHOTO_USER BLOB
            )
        """.trimIndent()

        // Log 테이블 생성
        val createLogTable = """
            CREATE TABLE $TABLE_LOG (
                $COLUMN_LOG_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_LOG_CHALLENGE_ID INTEGER NOT NULL,
                $COLUMN_LOG_CHALLENGER_ID INTEGER NOT NULL,
                $COLUMN_LOG_DATE DATETIME NOT NULL,
                $COLUMN_LOG_PHOTO BLOB NOT NULL,
                FOREIGN KEY ($COLUMN_LOG_CHALLENGER_ID) REFERENCES $TABLE_CHALLENGER($COLUMN_CHALLENGER_ID),
                FOREIGN KEY ($COLUMN_LOG_CHALLENGE_ID) REFERENCES $TABLE_CHALLENGE($COLUMN_CHALLENGE_ID)
            )
        """.trimIndent()

        // Cheer 테이블 생성
        val createCheerTable = """
            CREATE TABLE $TABLE_CHEER (
                $COLUMN_CHEER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CHEER_LOG_ID INTEGER NOT NULL,
                $COLUMN_CHEER_AT DATETIME NOT NULL,
                $COLUMN_CHEER_USER_ID INTEGER NOT NULL,
                FOREIGN KEY ($COLUMN_CHEER_LOG_ID) REFERENCES $TABLE_LOG($COLUMN_LOG_ID),
                FOREIGN KEY ($COLUMN_CHEER_USER_ID) REFERENCES $TABLE_USER($COLUMN_USER_ID)
            )
        """.trimIndent()

        db.execSQL(createChallengeTable)
        db.execSQL(createChallengerTable)
        db.execSQL(createUserTable)
        db.execSQL(createLogTable)
        db.execSQL(createCheerTable)
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
            put(COLUMN_NICKNAME, user.nickname)
            put(COLUMN_MAJOR, user.major)
            put(COLUMN_PHOTO_USER, user.photo)
        }
        return db.insert(TABLE_USER, null, contentValues)
    }

    // Log 추가
    fun insertLog(logEntry: LogEntry): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_LOG_CHALLENGE_ID, logEntry.challengeId)
            put(COLUMN_LOG_CHALLENGER_ID, logEntry.challengerId)
            put(COLUMN_LOG_DATE, logEntry.logDate)
            put(COLUMN_LOG_PHOTO, logEntry.logImage)
        }
        return db.insert(TABLE_LOG, null, values)
    }

    // Cheer 추가
    fun insertCheer(cheer: Cheer): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CHEER_LOG_ID, cheer.LogId)
            put(COLUMN_CHEER_USER_ID, cheer.UserId)
            put(COLUMN_CHEER_AT, cheer.CheerAt)
        }
        return db.insert(TABLE_CHEER, null, values)
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
        val challengerId: Long? = null,
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
        val nickname: String,
        val major: String?,
        val photo: ByteArray?
    )

    // Log 데이터 클래스
    data class LogEntry(
        val challengeId: Long,
        val challengerId: Long,
        val logDate: String,
        val logImage: ByteArray
    )

    // Cheer 데이터 클래스
    data class Cheer(
        val LogId: Long,
        val UserId: Long,
        val CheerAt: String
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
                COLUMN_NICKNAME, COLUMN_MAJOR  // DBHelper에서 정의된 COLUMN_들 사용
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

        if (nickname == null) {
            Log.e("DBHelper", "❌ 닉네임이 존재하지 않습니다. userId: $userId")
        } else {
            Log.d("DBHelper", "✅ 닉네임 가져옴: $nickname (userId: $userId)")
        }

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

    fun getParticipantsByChallengeId(challengeId: Long): List<Challenger> {
        val participants = mutableListOf<Challenger>()
        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_CHALLENGER WHERE $COLUMN_CHALLENGE_ID_FK = ?",
            arrayOf(challengeId.toString())
        )

        while (cursor.moveToNext()) {
            val challengerId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CHALLENGER_ID))
            val userId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID))
            val challengeId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CHALLENGE_ID_FK))
            val challengeRole = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CHALLENGE_ROLE))
            val joinedAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_JOINED_AT))
            val percentage = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PERCENTAGE))

            // 각 참가자 정보 로그 출력
            Log.d("DBHelper", "Fetched participant: Challenger ID: $challengerId, User ID: $userId, Challenge Role: $challengeRole, Joined At: $joinedAt, Percentage: $percentage")

            val participant = Challenger(
                challengerId = challengerId,
                userId = userId,
                challengeId = challengeId,
                challengeRole = challengeRole,
                joinedAt = joinedAt,
                percentage = percentage
            )
            participants.add(participant)
        }

        cursor.close()
        db.close()
        return participants
    }

    fun getNicknameByChallengerId(challengerId: Long): String? {
        val db = readableDatabase
        var nickname: String? = null

        // 챌린저 ID에 해당하는 유저의 닉네임을 가져오는 쿼리
        val cursor = db.rawQuery(
            "SELECT $COLUMN_NICKNAME FROM $TABLE_USER WHERE $COLUMN_USER_ID = (SELECT $COLUMN_USER_ID FROM $TABLE_CHALLENGER WHERE $COLUMN_CHALLENGER_ID = ?)",
            arrayOf(challengerId.toString())
        )

        if (cursor.moveToFirst()) {
            nickname = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NICKNAME))
        }

        cursor.close()
        db.close()
        return nickname
    }

    fun getMajorByChallengerId(challengerId: Long): String? {
        val db = readableDatabase
        var major: String? = null

        // 챌린저 ID에 해당하는 유저의 학과를 가져오는 쿼리
        val cursor = db.rawQuery(
            "SELECT $COLUMN_MAJOR FROM $TABLE_USER WHERE $COLUMN_USER_ID = (SELECT $COLUMN_USER_ID FROM $TABLE_CHALLENGER WHERE $COLUMN_CHALLENGER_ID = ?)",
            arrayOf(challengerId.toString())
        )

        if (cursor.moveToFirst()) {
            major = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MAJOR))
        }

        cursor.close()
        db.close()
        return major
    }

    fun getUserProfilePhotoByChallengerId(challengerId: Long): Bitmap? {
        val db = this.readableDatabase
        val query = """
        SELECT ${COLUMN_PHOTO_USER} 
        FROM ${TABLE_USER} 
        WHERE ${COLUMN_USER_ID} = (
            SELECT ${COLUMN_USER_ID} 
            FROM ${TABLE_CHALLENGER} 
            WHERE ${COLUMN_CHALLENGER_ID} = ?
        )
    """
        val cursor = db.rawQuery(query, arrayOf(challengerId.toString()))

        var bitmap: Bitmap? = null
        Log.d("UserDBHelper", "챌린저 아이디: $challengerId" + "로 프로필 사진을 조회합니다.")

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
            Log.d("UserDBHelper", "해당 챌린저 아이디에 대한 프로필 사진이 없습니다.")
        }

        cursor.close()
        db.close()

        return bitmap
    }

    fun getLogEntriesByChallenge(challengeId: Long): List<LogEntry> {
        val db = readableDatabase
        val logList = mutableListOf<LogEntry>()

        val query = """
        SELECT log.${COLUMN_LOG_ID}, log.${COLUMN_LOG_CHALLENGE_ID}, log.${COLUMN_LOG_CHALLENGER_ID}, 
               log.${COLUMN_LOG_DATE}, log.${COLUMN_LOG_PHOTO}
        FROM $TABLE_LOG log
        WHERE log.${COLUMN_LOG_CHALLENGE_ID} = ?
        ORDER BY log.${COLUMN_LOG_DATE} DESC
    """

        val cursor = db.rawQuery(query, arrayOf(challengeId.toString()))

        if (cursor.moveToFirst()) {
            do {
                val logId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_LOG_ID))
                val challengeId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_LOG_CHALLENGE_ID))
                val challengerId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_LOG_CHALLENGER_ID))
                val logDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOG_DATE))
                val logImage = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_LOG_PHOTO))

                val userInfo = getUserInfoByChallengerId(challengerId)
                val userNick = userInfo?.first ?: "알 수 없음"
                val userMajor = userInfo?.second ?: "알 수 없음"

                Log.d("DBHelper", "로그 ID: $logId, 닉네임: $userNick, 학과: $userMajor")

                logList.add(LogEntry(challengeId, challengerId, logDate, logImage))

            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return logList
    }

    fun getUserInfoByChallengerId(challengerId: Long): Triple<String, String, Long>? {
        val db = readableDatabase
        val query = """
        SELECT user.${COLUMN_NICKNAME}, user.${COLUMN_MAJOR}, user.${COLUMN_USER_ID}
        FROM $TABLE_USER user
        INNER JOIN $TABLE_CHALLENGER ch ON user.${COLUMN_USER_ID} = ch.${COLUMN_USER_ID}
        WHERE ch.${COLUMN_CHALLENGER_ID} = ?
    """

        val cursor = db.rawQuery(query, arrayOf(challengerId.toString()))
        var result: Triple<String, String, Long>? = null

        if (cursor.moveToFirst()) {
            val userNick = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NICKNAME))
            val userMajor = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MAJOR))
            val userId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID))
            result = Triple(userNick, userMajor, userId) // Triple로 변경
        }

        cursor.close()
        db.close()
        return result
    }

    // 특정 유저가 특정 로그를 이미 응원했는지 확인
    fun hasUserCheered(logId: Long, userId: Long): Boolean {
        val db = readableDatabase
        val query = "SELECT COUNT(*) FROM $TABLE_CHEER WHERE $COLUMN_CHEER_LOG_ID = ? AND $COLUMN_CHEER_USER_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(logId.toString(), userId.toString()))

        var hasCheered = false
        if (cursor.moveToFirst()) {
            hasCheered = cursor.getInt(0) > 0
        }

        cursor.close()
        db.close()
        return hasCheered
    }

    // 응원 데이터 추가
    fun addCheer(logId: Long, userId: Long): Boolean {
        if (hasUserCheered(logId, userId)) {
            return false // 이미 응원한 경우 추가하지 않음
        }

        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CHEER_LOG_ID, logId)  // ✔ logId 사용
            put(COLUMN_CHEER_USER_ID, userId)
            put(COLUMN_CHEER_AT, System.currentTimeMillis()) // 현재 시간 저장
        }

        val result = db.insert(TABLE_CHEER, null, values)
        db.close()
        return result != -1L
    }

    fun getLogIdByChallenger(challengerId: Long, challengeId: Long): Long? {
        val db = readableDatabase
        val query = """
        SELECT $COLUMN_LOG_ID FROM $TABLE_LOG 
        WHERE $COLUMN_LOG_CHALLENGER_ID = ? AND $COLUMN_LOG_CHALLENGE_ID = ?
        ORDER BY $COLUMN_LOG_DATE DESC LIMIT 1
    """
        val cursor = db.rawQuery(query, arrayOf(challengerId.toString(), challengeId.toString()))

        var logId: Long? = null
        if (cursor.moveToFirst()) {
            logId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_LOG_ID))
        }

        cursor.close()
        db.close()
        return logId
    }

    fun getCheerCountForChallenger(challengerId: Long, challengeId: Long): Int {
        val db = this.readableDatabase
        var cheerCount = 0

        val query = """
        SELECT COUNT(*)
        FROM $TABLE_CHEER c
        JOIN $TABLE_LOG l ON c.$COLUMN_CHEER_LOG_ID = l.$COLUMN_LOG_ID
        WHERE l.$COLUMN_LOG_CHALLENGER_ID = ? AND l.$COLUMN_LOG_CHALLENGE_ID = ?;
    """

        val cursor = db.rawQuery(query, arrayOf(challengerId.toString(), challengeId.toString()))

        if (cursor != null) {
            Log.d("DB_CHEER", "쿼리 실행 성공 - Cursor Count: ${cursor.count}")
            if (cursor.moveToFirst()) {
                cheerCount = cursor.getInt(0)
            }
            cursor.close()
        } else {
            Log.e("DB_CHEER", "쿼리 실행 실패 - Cursor가 null임")
        }

        db.close()
        return cheerCount
    }

    fun getTopCheerChallengerForChallenge(challengeId: Long): Pair<Long, Int>? {
        val db = this.readableDatabase
        var topChallengerId: Long? = null
        var maxCheerCount = 0

        val query = """
        SELECT l.$COLUMN_LOG_CHALLENGER_ID, COUNT(*) AS cheerCount
        FROM $TABLE_CHEER c
        JOIN $TABLE_LOG l ON c.$COLUMN_CHEER_LOG_ID = l.$COLUMN_LOG_ID
        WHERE l.$COLUMN_LOG_CHALLENGE_ID = ?
        GROUP BY l.$COLUMN_LOG_CHALLENGER_ID
        ORDER BY cheerCount DESC
        LIMIT 1;
    """
        val cursor = db.rawQuery(query, arrayOf(challengeId.toString()))

        if (cursor != null) {
            Log.d("DB_CHEER", "쿼리 실행 성공 - Cursor Count: ${cursor.count}")
            if (cursor.moveToFirst()) {
                // COLUMN_LOG_CHALLENGER_ID의 인덱스를 확인
                val challengerColumnIndex = cursor.getColumnIndex(COLUMN_LOG_CHALLENGER_ID)
                if (challengerColumnIndex != -1) {
                    topChallengerId = cursor.getLong(challengerColumnIndex)
                    maxCheerCount = cursor.getInt(1)
                } else {
                    Log.e("DB_CHEER", "컬럼 $COLUMN_LOG_CHALLENGER_ID" + "를 찾을 수 없습니다.")
                }
            }
            cursor.close()
        } else {
            Log.e("DB_CHEER", "쿼리 실행 실패 - Cursor가 null임")
        }

        db.close()
        return if (topChallengerId != null) Pair(topChallengerId, maxCheerCount) else null
    }

    fun getTopCheerUserIdForChallenge(challengeId: Long): Pair<Long, Int>? {
        val db = this.readableDatabase
        var topCheerUserId: Long? = null
        var cheerCount = 0

        val query = """
        SELECT $COLUMN_USER_ID, COUNT(*)
        FROM $TABLE_CHEER c
        JOIN $TABLE_LOG l ON c.$COLUMN_CHEER_LOG_ID = l.$COLUMN_LOG_ID
        WHERE l.$COLUMN_LOG_CHALLENGE_ID = ?
        GROUP BY $COLUMN_USER_ID
        ORDER BY COUNT(*) DESC
        LIMIT 1;
    """

        val cursor = db.rawQuery(query, arrayOf(challengeId.toString()))

        if (cursor != null) {
            Log.d("DB_CHEER", "쿼리 실행 성공 - Cursor Count: ${cursor.count}")
            if (cursor.moveToFirst()) {
                // 직접 인덱스로 데이터 추출
                topCheerUserId = cursor.getLong(0)  // COLUMN_USER_ID는 첫 번째 컬럼
                cheerCount = cursor.getInt(1)       // COUNT(*) 결과는 두 번째 컬럼
            }
            cursor.close()
        } else {
            Log.e("DB_CHEER", "쿼리 실행 실패 - Cursor가 null임")
        }

        db.close()

        // 유저 ID와 응원 횟수 반환
        return if (topCheerUserId != null) {
            Pair(topCheerUserId, cheerCount)
        } else {
            null
        }
    }

    fun getChallengeById(challengeId: Long): Challenge? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_CHALLENGE WHERE $COLUMN_CHALLENGE_ID = ?",
            arrayOf(challengeId.toString())
        )

        var challenge: Challenge? = null
        if (cursor.moveToFirst()) {
            challenge = Challenge(
                challengeId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CHALLENGE_ID)),
                title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CHALLENGE_TITLE)),
                description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                photo = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_PHOTO)),
                createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)),
                startDay = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START_DAY)),
                endDay = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_END_DAY)),
                status = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STATUS)),
                maxParticipant = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MAX_PARTICIPANT)),
                category = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                isOfficial = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_OFFICIAL))
            )
        }

        cursor.close()
        db.close()
        return challenge
    }

    fun getChallengerProgress(challengerId: Long, challengeId: Long): Int {
        val db = readableDatabase
        val query = """
        SELECT $COLUMN_PERCENTAGE FROM $TABLE_CHALLENGER 
        WHERE ${COLUMN_CHALLENGER_ID} = ? AND ${COLUMN_CHALLENGE_ID} = ?
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(challengerId.toString(), challengeId.toString()))

        var progress = 0
        if (cursor.moveToFirst()) {
            progress = cursor.getInt(0)
        }

        cursor.close()
        db.close()
        return progress
    }

    fun countParticipants(challengeId: Long?): Int {
        if (challengeId == null || challengeId == -1L) return 0
        val db = this.readableDatabase
        val query = "SELECT COUNT(*) FROM challenger WHERE challenge_id = ?"
        val cursor = db.rawQuery(query, arrayOf(challengeId.toString()))

        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        db.close()

        return count
    }

    fun getChallengerIdByUserAndChallenge(userId: Long, challengeId: Long): Long? {
        val db = readableDatabase
        val query = """
        SELECT $COLUMN_CHALLENGER_ID FROM $TABLE_CHALLENGER
        WHERE $COLUMN_USER_ID = ? AND $COLUMN_CHALLENGE_ID = ?
        ORDER BY $COLUMN_JOINED_AT DESC -- 가장 최근에 참가한 순서대로 정렬
    """
        val cursor = db.rawQuery(query, arrayOf(userId.toString(), challengeId.toString()))

        var challengerId: Long? = null
        if (cursor.moveToFirst()) {
            challengerId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CHALLENGER_ID))
        }

        cursor.close()
        db.close()

        return challengerId
    }

    fun getLogsByChallenger(challengerId: Long, challengeId: Long): List<LogEntry> {
        val db = readableDatabase
        val logList = mutableListOf<LogEntry>()

        val query = """
        SELECT * FROM $TABLE_LOG
        WHERE $COLUMN_LOG_CHALLENGER_ID = ? AND $COLUMN_LOG_CHALLENGE_ID = ?
        ORDER BY $COLUMN_LOG_DATE DESC
    """

        Log.d("DB_QUERY", "쿼리 실행: $query")
        Log.d("DB_PARAMS", "challengerId: $challengerId, challengeId: $challengeId")

        val cursor = db.rawQuery(query, arrayOf(challengerId.toString(), challengeId.toString()))

        if (cursor.moveToFirst()) {
            Log.d("DB_LOG", "로그 데이터 찾음 - 총 ${cursor.count} 개의 로그")
            do {
                val logId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_LOG_ID))
                val logDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOG_DATE))
                val logImage = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_LOG_PHOTO))

                Log.d("DB_LOG_ENTRY", "logId: $logId, logDate: $logDate, logImage size: ${logImage.size}")

                logList.add(LogEntry(challengeId, challengerId, logDate, logImage))
            } while (cursor.moveToNext())
        } else {
            Log.d("DB_LOG", "로그 데이터 없음")
        }

        cursor.close()
        db.close()
        Log.d("DB_LOG", "총 ${logList.size} 개의 로그 반환됨")
        return logList
    }

    fun getChallengeProgress(challengeId: Long, userId: Long): Int {
        val db = this.readableDatabase
        var progress = 0

        val cursor = db.rawQuery(
            "SELECT $COLUMN_PERCENTAGE FROM $TABLE_CHALLENGER WHERE $COLUMN_CHALLENGE_ID_FK = ? AND $COLUMN_USER_ID = ?",
            arrayOf(challengeId.toString(), userId.toString()) // challengeId와 userId 조건 추가
        )

        if (cursor.moveToFirst()) {
            progress = if (cursor.isNull(0)) 0 else cursor.getInt(0) // NULL 체크
        }

        cursor.close()
        db.close()

        return progress
    }

    fun getOngoingChallenges(userId: Long): List<Challenge> {
        val db = this.readableDatabase
        val challengeList = mutableListOf<Challenge>()

        val cursor = db.rawQuery(
            """
        SELECT c.*, 
            (julianday(c.$COLUMN_END_DAY) - julianday(c.$COLUMN_START_DAY)) AS challenge_days, 
            (SELECT COUNT(*) FROM $TABLE_CHALLENGER ch WHERE ch.$COLUMN_CHALLENGE_ID_FK = c.$COLUMN_CHALLENGE_ID) AS participants
        FROM $TABLE_CHALLENGE c
        JOIN $TABLE_CHALLENGER ch ON ch.$COLUMN_CHALLENGE_ID_FK = c.$COLUMN_CHALLENGE_ID
        WHERE c.$COLUMN_STATUS IN (1, 2) AND ch.$COLUMN_USER_ID = ?
        ORDER BY c.$COLUMN_END_DAY ASC
        """, arrayOf(userId.toString()) // userId를 쿼리 파라미터로 전달
        )

        if (cursor.moveToFirst()) {
            do {
                val challenge = Challenge(
                    challengeId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CHALLENGE_ID)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CHALLENGE_TITLE)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                    photo = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_PHOTO)),
                    createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)),
                    startDay = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START_DAY)),
                    endDay = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_END_DAY)),
                    status = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STATUS)),
                    maxParticipant = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MAX_PARTICIPANT)),
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

    fun getCompleteChallenges(userId: Long): List<Challenge> {
        val db = this.readableDatabase
        val challengeList = mutableListOf<Challenge>()

        val cursor = db.rawQuery(
            """
        SELECT c.*, 
            (julianday(c.$COLUMN_END_DAY) - julianday(c.$COLUMN_START_DAY)) AS challenge_days, 
            (SELECT COUNT(*) FROM $TABLE_CHALLENGER ch WHERE ch.$COLUMN_CHALLENGE_ID_FK = c.$COLUMN_CHALLENGE_ID) AS participants
        FROM $TABLE_CHALLENGE c
        JOIN $TABLE_CHALLENGER ch ON ch.$COLUMN_CHALLENGE_ID_FK = c.$COLUMN_CHALLENGE_ID
        WHERE c.$COLUMN_STATUS = 3 AND ch.$COLUMN_USER_ID = ?
        ORDER BY c.$COLUMN_END_DAY ASC
        """, arrayOf(userId.toString()) // userId를 쿼리 파라미터로 전달
        )

        if (cursor.moveToFirst()) {
            do {
                val challenge = Challenge(
                    challengeId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CHALLENGE_ID)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CHALLENGE_TITLE)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                    photo = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_PHOTO)),
                    createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)),
                    startDay = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START_DAY)),
                    endDay = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_END_DAY)),
                    status = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STATUS)),
                    maxParticipant = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MAX_PARTICIPANT)),
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

    fun updateChallengeStatus() {
        val today = LocalDate.now()

        val db = this.writableDatabase

        // 현재 날짜에 따라 상태 업데이트
        val query = "UPDATE Challenge SET status = CASE " +
                "WHEN ? < created_at THEN 0 " + // 생성 전
                "WHEN ? < start_day THEN 1 " +   // 모집 중
                "WHEN ? < end_day THEN 2 " +     // 진행 중
                "WHEN ? > end_day THEN 3 " +     // 마감됨
                "ELSE 5 END"

        val statement = db.compileStatement(query)
        statement.bindString(1, today.toString())
        statement.bindString(2, today.toString())
        statement.bindString(3, today.toString())
        statement.bindString(4, today.toString())
        statement.executeUpdateDelete()

        db.close()
    }

    //인기 챌린지 관련 함수 두개 표시
    fun getPopularChallenges(): List<Challenge> {
        val db = this.readableDatabase
        val challengeList = mutableListOf<Challenge>()

        val cursor = db.rawQuery(
            """
        SELECT c.*, 
               (SELECT COUNT(*) FROM $TABLE_CHALLENGER ch WHERE ch.$COLUMN_CHALLENGE_ID_FK = c.$COLUMN_CHALLENGE_ID) AS participants
        FROM $TABLE_CHALLENGE c
        WHERE c.$COLUMN_STATUS = 1  -- 🔹 상태(status)가 1인 챌린지만 선택
        ORDER BY participants DESC
        LIMIT 2
        """, null
        )

        if (cursor.moveToFirst()) {
            do {
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

    fun isUserInChallenger(userId: Long): Boolean {
        val db = this.readableDatabase
        val query = "SELECT COUNT(*) FROM challenger WHERE user_id = ?"
        val cursor = db.rawQuery(query, arrayOf(userId.toString()))

        var exists = false
        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0
        }

        cursor.close()
        db.close()
        return exists
    }

}