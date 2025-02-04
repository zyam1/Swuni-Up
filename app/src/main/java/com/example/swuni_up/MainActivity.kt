package com.example.swuni_up

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var notificationHelper: NotificationHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kakao_signin)

        dbHelper = DBHelper(this)

        notificationHelper = NotificationHelper(this)
        // 알림 채널 설정
        notificationHelper.createNotificationChannel()
        // 알림 권한 요청 및 알림 설정
        notificationHelper.requestNotification(this)

        val etEmail = findViewById<EditText>(R.id.et_email)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)
        val tvRegister = findViewById<TextView>(R.id.tv_register)

        val eye = findViewById<ImageView>(R.id.btn_password_toggle)

        var isPasswordVisible = false

        eye.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                eye.setImageResource(R.drawable.ic_eye) // 눈 모양 아이콘 변경
            } else {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                eye.setImageResource(R.drawable.ic_eye_close) // 눈 감은 아이콘 변경
            }
            etPassword.setSelection(etPassword.text.length) // 커서를 유지하기 위해 추가
        }

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
                    Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()

                    val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("user_nick", user.nickname)
                    editor.putLong("user_id", user.id ?: -1L)
                    editor.apply()

                    // 챌린저 DB에서 user_id가 있는지 확인
                    val isChallenger = dbHelper.isUserInChallenger(user.id ?: -1L)

                    val intent = if (isChallenger) {
                        Log.d("Login", "User ${user.id} is a challenger. Redirecting to ChallengeHome")
                        Intent(this, ChallengeHome::class.java)  // 챌린저 DB에 있음 → ChallengeHome
                    } else {
                        Log.d("Login", "User ${user.id} is NOT a challenger. Redirecting to HomeActivity")
                        Intent(this, HomeActivity::class.java)  // 챌린저 DB에 없음 → HomeActivity
                    }

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


        val btnKakaoLogin = findViewById<Button>(R.id.btn_kakao_login)

        btnKakaoLogin.setOnClickListener {
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
                // ✅ 카카오톡 앱 로그인 (설치된 경우)
                UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                    if (error != null) {
                        Log.e("KakaoLogin", "카카오톡 로그인 실패", error)
                        // 카카오톡 로그인 실패하면 이메일 로그인 시도
                        loginWithKakaoAccount()
                    } else if (token != null) {
                        Log.d("KakaoLogin", "카카오톡 로그인 성공: ${token.accessToken}")
                        getUserInfo()
                    }
                }
            } else {
                // ✅ 이메일(카카오계정) 로그인
                loginWithKakaoAccount()
            }
        }

        val keyHash = Utility.getKeyHash(this)
        Log.d("Hash", keyHash)


        // 회원가입 버튼 클릭
        tvRegister.setOnClickListener {
            // 회원가입 화면으로 이동
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginWithKakaoAccount() {
        UserApiClient.instance.loginWithKakaoAccount(this) { token, error ->
            if (error != null) {
                Log.e("KakaoLogin", "카카오 계정 로그인 실패", error)
            } else if (token != null) {
                Log.d("KakaoLogin", "카카오 계정 로그인 성공: ${token.accessToken}")
                getUserInfo()
            }
        }
    }

    private fun getUserInfo() {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e("KakaoUser", "사용자 정보 가져오기 실패", error)
            } else if (user != null) {
                val name = user.kakaoAccount?.profile?.nickname ?: "이름 없음"
                val email = user.kakaoAccount?.email ?: "이메일 없음"

                Log.d("KakaoUser", "사용자 이름: $name, 이메일: $email")

                // DB에서 해당 이메일이 있는지 확인
                val existingUser = dbHelper.getUserByEmail(email)

                if (existingUser != null) {
                    Log.d("KakaoLogin", "기존 회원 확인됨: ${existingUser.name}")
                    val userId = existingUser.id ?: -1L

                    // 로그인 성공 처리
                    val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("user_nick", existingUser.nickname)  // 닉네임 저장
                    editor.putLong("user_id", userId)
                    editor.apply()

                    // ✅ challenger 테이블에서 user_id 확인
                    val isChallenger = dbHelper.isUserInChallenger(userId)

                    // 🔀 이동할 화면 결정
                    val intent = if (isChallenger) {
                        Intent(this, ChallengeHome::class.java) // 챌린지 참여 O
                    } else {
                        Intent(this, HomeActivity::class.java) // 챌린지 참여 X
                    }

                    Log.d("Navigation", "이동할 화면: ${if (isChallenger) "ChallengeHomeActivity" else "HomeActivity"}")
                    startActivity(intent)
                    finish()
                } else {
                    // 신규 회원이면 회원가입 화면으로 이동
                    val intent = Intent(this, SignUpActivity::class.java)
                    intent.putExtra("name", name)
                    intent.putExtra("email", email)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }



}