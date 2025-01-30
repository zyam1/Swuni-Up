package com.example.swuni_up

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayOutputStream
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

        // 인증하기 버튼
        val saveButton: Button = findViewById(R.id.saveBtn)
        saveButton.setOnClickListener {
            saveLog()
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
            val selectedImageUri: Uri? = data.data
            if (selectedImageUri != null) {
                logImage.setImageURI(selectedImageUri)
            }
        }
    }

    // 로그 저장
    private fun saveLog() {
        if (selectedImageUri == null) {
            Toast.makeText(this, "이미지를 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
        val byteArray = bitmapToByteArray(bitmap)

        var logId = 1L  // 초기값
        logId++  // 로그가 생성될 때마다 증가

        val challengerId = 123L // 예시용 챌린저 ID
        val certifiedAt = getCurrentDateTime() // 인증 날짜

        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("log_id", logId)
            put("challenger_id", challengerId)
            put("certified_at", certifiedAt)
            put("log_image", byteArray)
        }

        val newRowId = db.insert("Log", null, values)
        if (newRowId != -1L) {
            Toast.makeText(this, "로그가 저장되었습니다.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "로그 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    private fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }
}