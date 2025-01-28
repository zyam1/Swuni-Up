package com.example.swuni_up

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayOutputStream

class SiginUpActivity : AppCompatActivity() {

    private lateinit var dbHelper: UserDBHelper
    private lateinit var imgProfile: ImageView
    private var profileImage: ByteArray? = null  // 프로필 이미지 데이터

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sigin_up)

        dbHelper = UserDBHelper(this)

        val etId = findViewById<EditText>(R.id.et_id)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val etNickname = findViewById<EditText>(R.id.et_nickname)
        val spinnerMajor = findViewById<Spinner>(R.id.spinner_department)
        val btnRegister = findViewById<Button>(R.id.btn_register)
        imgProfile = findViewById(R.id.img_profile)
        val btnUploadImage = findViewById<ImageView>(R.id.btn_upload_image)
        val btnBack = findViewById<ImageView>(R.id.btn_back)

        btnBack.setOnClickListener {
            finish()  // 이전 화면으로 돌아감
        }

        // 이미지 업로드 버튼 클릭 시 갤러리에서 이미지 선택
        btnUploadImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1001)
        }

        // 가입하기 버튼 클릭 시 DB 저장
        btnRegister.setOnClickListener {
            val id = etId.text.toString()
            val password = etPassword.text.toString()
            val nickname = etNickname.text.toString()
            val major = spinnerMajor.selectedItem.toString()

            if (id.isEmpty() || password.isEmpty() || nickname.isEmpty()) {
                Toast.makeText(this, "모든 필드를 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = UserDBHelper.User(
                name = id,
                email = "$id@example.com",  // 이메일 형식 (db에만 있어서 임시)
                password = password,
                kakaoId = "kakao_$id",
                nickname = nickname,
                major = major,
                photo = profileImage
            )

            val result = dbHelper.insertUser(user)

            if (result > 0) {
                Toast.makeText(this, "회원가입 성공!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "회원가입 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 갤러리에서 선택한 이미지 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data ?: return
            val inputStream = contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            imgProfile.setImageBitmap(bitmap)  // UI에 표시

            // Bitmap을 ByteArray로 변환 (SQLite 저장용)
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            profileImage = byteArrayOutputStream.toByteArray()
        }
    }
}