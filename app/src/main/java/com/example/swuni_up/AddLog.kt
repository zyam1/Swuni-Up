package com.example.swuni_up

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddLog : AppCompatActivity() {
    private lateinit var logImage: ImageView
    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_log)

        dbHelper = DBHelper(this)
        logImage = findViewById(R.id.logImage)

        val challengeId = intent.getLongExtra("challenge_id", -1L)
        if (challengeId == -1L) {
            Toast.makeText(this, "챌린지 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val challengeName = intent.getStringExtra("challenge_name") ?: "챌린지명 없음"
        val challengeStartDay = intent.getStringExtra("challenge_start_day") ?: ""

        val challengeDay: String = if (challengeStartDay.isNotEmpty()) {
            val dayCount = calculateChallengeDay(challengeStartDay).toString()
            "${dayCount}일째 도전"
        } else {
            "시작일 없음"
        }


        findViewById<TextView>(R.id.challenge_title).text = challengeName
        findViewById<TextView>(R.id.day).text = challengeDay

        logImage.setOnClickListener {
            openGallery()
        }

        // 툴바 설정
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        val saveButton: Button = findViewById(R.id.saveBtn)
        saveButton.setOnClickListener {
            val userId = getCurrentLoggedInUserId()
            val challengerId = getChallengerIdByUserId(userId, challengeId)

            if (challengerId == null) {
                Toast.makeText(this, "해당 유저는 챌린지에 참여하고 있지 않습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            saveLog(challengerId)
        }

    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            if (selectedImageUri != null) {
                logImage.setImageURI(selectedImageUri)
            }
        }
    }

    // 유저 아이디 가져오기
    private fun getCurrentLoggedInUserId(): Long {
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getLong("user_id", -1L)
    }

    private fun getChallengerIdByUserId(userId: Long, challengeId: Long): Long? {
        val db = dbHelper.readableDatabase
        var challengerId: Long? = null

        val cursor = db.query(
            DBHelper.TABLE_CHALLENGER,
            arrayOf(DBHelper.COLUMN_CHALLENGER_ID),
            "${DBHelper.COLUMN_USER_ID} = ? AND ${DBHelper.COLUMN_CHALLENGE_ID_FK} = ?",
            arrayOf(userId.toString(), challengeId.toString()),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            challengerId = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_CHALLENGER_ID))
        }

        cursor.close()
        db.close()

        Log.d("AddLog", "getChallengerIdByUserId() - userId: $userId, challengeId: $challengeId -> challengerId: $challengerId")
        return challengerId
    }



    private fun getChallengeIdByChallengerId(challengerId: Long): Long? {
        val db = dbHelper.readableDatabase
        var challengeId: Long? = null

        val cursor = db.query(
            DBHelper.TABLE_CHALLENGER,
            arrayOf(DBHelper.COLUMN_CHALLENGE_ID_FK),
            "${DBHelper.COLUMN_CHALLENGER_ID} = ?",
            arrayOf(challengerId.toString()),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            challengeId = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_CHALLENGE_ID_FK))
        }

        cursor.close()
        db.close()
        return challengeId
    }

    private fun saveLog(challengerId: Long) {
        if (selectedImageUri == null) {
            Toast.makeText(this, "이미지를 선택해야 합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val db = dbHelper.readableDatabase

        // ✅ 챌린지당 하루에 하나의 로그만 등록 가능하도록 검사
        if (!canUserLogToday(db, challengerId)) {
            Toast.makeText(this, "오늘은 이미 이 챌린지에서 인증을 완료했습니다!", Toast.LENGTH_SHORT).show()
            return
        }

        val challengeId = getChallengeIdByChallengerId(challengerId)
        if (challengeId == null) {
            Toast.makeText(this, "챌린지 ID를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val bitmap = uriToBitmap(selectedImageUri!!)
        val byteArray = bitmapToByteArray(bitmap)

        val certifiedAt = getCurrentDateTime() // 현재 날짜+시간

        val logEntry = DBHelper.LogEntry(
            challengeId = challengeId,
            challengerId = challengerId,
            logDate = certifiedAt,
            logImage = byteArray
        )

        val newRowId = dbHelper.insertLog(logEntry)

        if (newRowId != -1L) {
            updateProgress(challengerId)  // 진행률 업데이트
            setResult(Activity.RESULT_OK)

            Toast.makeText(this, "로그가 저장되었습니다!", Toast.LENGTH_SHORT).show()
            selectedImageUri = null
            finish()
        } else {
            Toast.makeText(this, "로그 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
    }




    private fun uriToBitmap(uri: Uri): Bitmap {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        return BitmapFactory.decodeStream(inputStream)!!
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        return stream.toByteArray()
    }

    private fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }

    // 뒤로가기
    override fun onSupportNavigateUp(): Boolean {
        finish() // 이전 화면으로 이동
        return true
    }

    fun canUserLogToday(db: SQLiteDatabase, challengerId: Long): Boolean {
        val query = """
        SELECT COUNT(*) FROM ${DBHelper.TABLE_LOG} 
        WHERE ${DBHelper.COLUMN_LOG_CHALLENGER_ID} = ? 
        AND DATE(${DBHelper.COLUMN_LOG_DATE}) = DATE('now');
    """
        val cursor = db.rawQuery(query, arrayOf(challengerId.toString()))

        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)  // COUNT 값 가져오기
        }
        cursor.close()

        Log.d("AddLog", "오늘 해당 챌린지에서 인증한 로그 개수: $count")
        return count == 0  // ✅ 0이면 인증 가능, 1 이상이면 이미 인증한 상태
    }




    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    // 진행률 반영
    private fun updateProgress(challengerId: Long) {
        val challengeId = getChallengeIdByChallengerId(challengerId) ?: return
        val totalDays = getTotalDaysOfChallenge(challengeId) ?: return
        val certifiedDays = getCertifiedDays(challengerId)

        // (현재 인증 횟수 / 총 챌린지 진행일) * 100
        val progress = ((certifiedDays.toFloat() / totalDays) * 100).toInt()

        // DB 업데이트
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DBHelper.COLUMN_PERCENTAGE, progress)
        }
        db.update(
            DBHelper.TABLE_CHALLENGER,
            values,
            "${DBHelper.COLUMN_CHALLENGER_ID} = ?",
            arrayOf(challengerId.toString())
        )
        db.close()
    }

    private fun getTotalDaysOfChallenge(challengeId: Long): Int? {
        val db = dbHelper.readableDatabase
        var totalDays: Int? = null

        val cursor = db.rawQuery(
            "SELECT ${DBHelper.COLUMN_START_DAY}, ${DBHelper.COLUMN_END_DAY} FROM ${DBHelper.TABLE_CHALLENGE} WHERE ${DBHelper.COLUMN_CHALLENGE_ID} = ?",
            arrayOf(challengeId.toString())
        )

        if (cursor.moveToFirst()) {
            val startDate = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_START_DAY))
            val endDate = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_END_DAY))

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val start = dateFormat.parse(startDate)?.time ?: return null
            val end = dateFormat.parse(endDate)?.time ?: return null

            totalDays = ((end - start) / (1000 * 60 * 60 * 24)).toInt() + 1
        }

        cursor.close()
        db.close()
        return totalDays
    }

    private fun getCertifiedDays(challengerId: Long): Int {
        val db = dbHelper.readableDatabase
        var certifiedDays = 0

        val cursor = db.rawQuery(
            "SELECT COUNT(DISTINCT DATE(${DBHelper.COLUMN_LOG_DATE})) FROM ${DBHelper.TABLE_LOG} WHERE ${DBHelper.COLUMN_LOG_CHALLENGER_ID} = ?",
            arrayOf(challengerId.toString())
        )

        if (cursor.moveToFirst()) {
            certifiedDays = cursor.getInt(0)
        }

        cursor.close()
        db.close()
        return certifiedDays
    }

    private fun calculateChallengeDay(startDate: String): Int {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            // 챌린지 시작일과 현재 날짜
            val start = dateFormat.parse(startDate)
            val today = Date()

            // 날짜 차이 계산
            val diff = (today.time - start.time) / (1000 * 60 * 60 * 24)

            diff.toInt() + 1
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }
}
