package com.example.swuni_up

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class HomeActivity : AppCompatActivity() {
    private lateinit var btnChallenge : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        btnChallenge = findViewById(R.id.btn_challenge)

        btnChallenge.setOnClickListener {
            val intent = Intent(this, ChallengeExplore::class.java)
            startActivity(intent)
        }
    }
}