package com.example.swuni_up

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class KakaoSigninActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kakao_signin)

        dbHelper = DBHelper(this)

        val etEmail = findViewById<EditText>(R.id.et_email)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)
        val tvRegister = findViewById<TextView>(R.id.tv_register)

        // 로그인 버튼 클릭
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "아이디와 비밀번호를 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d("Login", "Attempting to login with Email: $email and Password: $password")

            // DB에서 유저 정보 확인
            val user = dbHelper.getUserByEmail(email)

            if (user != null) {
                Log.d("Login", "User found: ${user.name}, Email: ${user.email}")
                if (user.password == password) {
                    // 로그인 성공
                    Toast.makeText(this, "로그인 성공, ${user.name}", Toast.LENGTH_SHORT).show()

                    val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()

                    editor.putString("user_nick", user.nickname)  // 로그인된 사용자의 닉네임 저장
                    editor.putLong("user_id", user.id ?: -1L)
                    editor.apply()

                    // 홈으로 이동
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // 비밀번호 불일치
                    Log.d("Login", "Password mismatch for user: $email")
                    Toast.makeText(this, "로그인 실패: 비밀번호 불일치", Toast.LENGTH_SHORT).show()
                }
            } else {
                // 이메일에 해당하는 유저가 없음
                Log.d("Login", "No user found with Email: $email")
                Toast.makeText(this, "로그인 실패: 유저가 존재하지 않음", Toast.LENGTH_SHORT).show()
            }
        }


        // 회원가입 버튼 클릭
        tvRegister.setOnClickListener {
            // 회원가입 화면으로 이동
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
}