package com.example.swuni_up

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class KakaoSigninActivity : AppCompatActivity() {

    private lateinit var dbHelper: UserDBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kakao_signin)

        dbHelper = UserDBHelper(this)

        val etId = findViewById<EditText>(R.id.et_id)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)
        val tvRegister = findViewById<TextView>(R.id.tv_register)

        // 로그인 버튼 클릭
        btnLogin.setOnClickListener {
            val id = etId.text.toString()
            val password = etPassword.text.toString()

            if (id.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "아이디와 비밀번호를 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // DB에서 유저 정보 확인
            val user = dbHelper.getUserById(id)

            if (user != null && user.password == password) {
                //성공
                Toast.makeText(this, "로그인 성공, ${user.name}", Toast.LENGTH_SHORT).show()

                // 홈으로 이동
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                // 로그인 실패
                Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show()
            }
        }

        // 회원가입 버튼 클릭
        tvRegister.setOnClickListener {
            // 회원가입 화면으로 이동
            val intent = Intent(this, SiginUpActivity::class.java)
            startActivity(intent)
        }
    }
}
