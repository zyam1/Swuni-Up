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
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class AddLog : AppCompatActivity() {
    private lateinit var logImage: ImageView
    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null
    private lateinit var dbHelper: ADDLogDBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_log)

        dbHelper = ADDLogDBHelper(this)
        logImage = findViewById(R.id.logImage)

        logImage.setOnClickListener {
            openGallery()
        }

        val saveButton: Button = findViewById(R.id.saveBtn)
        saveButton.setOnClickListener {
            val userId = getCurrentLoggedInUserId()
            val challengerId = getChallengerIdByUserId(userId)

            if (challengerId != null) {
                saveLog(challengerId)
            } else {
                Toast.makeText(this, "챌린저 ID를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
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

    private fun getCurrentLoggedInUserId(): Long {
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getLong("user_id", -1L)
    }

    private fun getChallengerIdByUserId(userId: Long): Long? {
        val dbHelper = ChallengerDBHelper(this)
        val db = dbHelper.readableDatabase
        var challengerId: Long? = null

        val cursor = db.query(
            ChallengerDBHelper.TABLE_CHALLENGER,
            arrayOf(ChallengerDBHelper.COLUMN_ID),
            "${ChallengerDBHelper.COLUMN_USER_ID} = ?",
            arrayOf(userId.toString()),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndex(ChallengerDBHelper.COLUMN_ID)
            challengerId = cursor.getLong(columnIndex)
        }

        cursor.close()
        db.close()  // 🛠 개선점 1: db.close() 추가

        return challengerId
    }

    private fun saveLog(challengerId: Long) {
        if (selectedImageUri == null) {
            Toast.makeText(this, "이미지를 선택해야 합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val bitmap = uriToBitmap(selectedImageUri!!)
        val byteArray = bitmapToByteArray(bitmap)

        val certifiedAt = getCurrentDateTime()

        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("challenger_id", challengerId)
            put("log_date", certifiedAt)
            put("log_image", byteArray)
        }

        val newRowId = db.insert("Log", null, values)
        db.close()

        if (newRowId != -1L) {
            Toast.makeText(this, "로그가 저장되었습니다.", Toast.LENGTH_SHORT).show()
            selectedImageUri = null // 🛠 개선점 2: 저장 후 selectedImageUri 초기화
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
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)  // 🛠 원래대로 90 유지
        return stream.toByteArray()
    }

    private fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }
}
