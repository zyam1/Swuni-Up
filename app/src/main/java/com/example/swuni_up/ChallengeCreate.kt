package com.example.swuni_up

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class ChallengeCreate : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var nextButton: Button
    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_challenge_create)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // 뒤로가기 버튼 동작 설정
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, ChallengeJoin::class.java) // ChallengeCreateActivity로 이동
            startActivity(intent)
            finish() // 현재 액티비티 종료
        }

        imageView = findViewById(R.id.imageView)

        imageView.setOnClickListener {
            openGallery()
        }

        val checkBoxes = listOf<CheckBox>(
            findViewById(R.id.checkBox1),
            findViewById(R.id.checkBox2),
            findViewById(R.id.checkBox3),
            findViewById(R.id.checkBox4),
            findViewById(R.id.checkBox5),
            findViewById(R.id.checkBox6)
        )

        checkBoxes.forEach { checkBox ->
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    checkBoxes.forEach { other ->
                        if (other != checkBox) {
                            other.isChecked = false
                        }
                    }
                }
            }
        }

        nextButton = findViewById(R.id.button)

        nextButton.setOnClickListener {
            // 데이터베이스에 데이터 삽입
            val challengeTitle = findViewById<EditText>(R.id.titleEditText).text.toString()
            val description = findViewById<EditText>(R.id.descriptionEditText).text.toString()
            val maxParticipants = findViewById<EditText>(R.id.maxEditText).text.toString().toIntOrNull()
            val category = getSelectedCategory()
            val challengePhoto = selectedImageUri?.toString() ?: ""

            Log.d("ChallengeCreate", "challenge_title: $challengeTitle, description: $description, max_participants: $maxParticipants, category: $category, challenge_photo: $challengePhoto")

            // 데이터를 다음 액티비티로 넘기기
            val intent = Intent(this, ChallengeCreateDate::class.java).apply {
                putExtra("challengeTitle", challengeTitle)
                putExtra("description", description)
                putExtra("maxParticipant", maxParticipants)
                putExtra("category", category)
                putExtra("challengePhoto", challengePhoto)
            }
            startActivity(intent)
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
                imageView.setImageURI(selectedImageUri)
            }
        }
    }

    private fun getSelectedCategory(): Int {
        val checkBox1 = findViewById<CheckBox>(R.id.checkBox1)
        val checkBox2 = findViewById<CheckBox>(R.id.checkBox2)
        val checkBox3 = findViewById<CheckBox>(R.id.checkBox3)
        val checkBox4 = findViewById<CheckBox>(R.id.checkBox4)
        val checkBox5 = findViewById<CheckBox>(R.id.checkBox5)
        val checkBox6 = findViewById<CheckBox>(R.id.checkBox6)

        return when {
            checkBox1.isChecked -> 1
            checkBox2.isChecked -> 2
            checkBox3.isChecked -> 3
            checkBox4.isChecked -> 4
            checkBox5.isChecked -> 5
            checkBox6.isChecked -> 6
            else -> 0
        }
    }

}
