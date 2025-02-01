package com.example.swuni_up

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayOutputStream

class SignUpActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var imgProfile: ImageView
    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sigin_up)

        dbHelper = DBHelper(this)

        val etName = findViewById<EditText>(R.id.et_name)
        val etEmail = findViewById<EditText>(R.id.et_email)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val etNickname = findViewById<EditText>(R.id.et_nickname)
        val spinnerMajor = findViewById<Spinner>(R.id.spinner_department)
        val btnRegister = findViewById<Button>(R.id.btn_register)
        imgProfile = findViewById(R.id.img_profile)
        val btnUploadImage = findViewById<ImageView>(R.id.btn_upload_image)
        val btnBack = findViewById<ImageView>(R.id.btn_back)

        val name = intent.getStringExtra("name")
        val email = intent.getStringExtra("email")

        if (name != null) {
            etName.setText(name)
        }

        if (email != null) {
            etEmail.setText(email)
        }

        btnBack.setOnClickListener {
            finish()  // 이전 화면으로 돌아감
        }

        // 이미지 업로드 버튼 클릭 시 갤러리에서 이미지 선택
        btnUploadImage.setOnClickListener {
            openGallery()
        }

        // 가입하기 버튼 클릭 시 DB 저장
        btnRegister.setOnClickListener {
            // 프로필 이미지를 바이트 배열로 변환
            val photoByteArray = uriToByteArray(this, selectedImageUri)

            // 프로필 이미지가 선택되지 않으면 오류 메시지 출력
            if (photoByteArray == null) {
                Toast.makeText(this, "프로필 사진을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 입력된 값 가져오기
            val name = etName.text.toString()
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            val nickname = etNickname.text.toString()
            val major = spinnerMajor.selectedItem.toString()

            // 필수 항목이 비어 있으면 오류 메시지 출력
            if (email.isEmpty() || password.isEmpty() || nickname.isEmpty()) {
                Toast.makeText(this, "모든 필드를 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 사용자의 정보를 User 객체로 생성
            val user = DBHelper.User(
                name = name,
                email = email,
                password = password,
                kakaoId = "kakao_$name",  // 카카오 ID는 임시로 이름으로 생성
                nickname = nickname,
                major = major,
                photo = photoByteArray  // 선택된 프로필 이미지
            )

            // DB에 사용자 데이터 삽입
            val result = dbHelper.insertUser(user)

            // 결과에 따른 메시지 출력
            if (result > 0) {
                Toast.makeText(this, "회원가입 성공!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, KakaoSigninActivity::class.java)
                startActivity(intent)
                finish() // 회원가입 성공 후 화면 종료
            } else {
                Toast.makeText(this, "회원가입 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 갤러리에서 선택한 이미지 처리
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
                imgProfile.setImageURI(selectedImageUri)
            }
        }
    }

    // URI를 ByteArray로 변환
    fun uriToByteArray(context: Context, uri: Uri?): ByteArray? {
        return try {
            if (uri == null) return null

            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream) // Bitmap 변환
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream) // 압축 (JPEG 형식)
            outputStream.toByteArray() // ByteArray로 변환
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

