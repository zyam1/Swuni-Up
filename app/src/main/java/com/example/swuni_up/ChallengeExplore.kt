package com.example.swuni_up

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.swuni_up.DBHelper.Challenge
import com.example.swuni_up.DBHelper.Companion.COLUMN_CATEGORY
import com.example.swuni_up.DBHelper.Companion.COLUMN_CHALLENGE_ID
import com.example.swuni_up.DBHelper.Companion.COLUMN_CHALLENGE_TITLE
import com.example.swuni_up.DBHelper.Companion.COLUMN_CREATED_AT
import com.example.swuni_up.DBHelper.Companion.COLUMN_DESCRIPTION
import com.example.swuni_up.DBHelper.Companion.COLUMN_END_DAY
import com.example.swuni_up.DBHelper.Companion.COLUMN_IS_OFFICIAL
import com.example.swuni_up.DBHelper.Companion.COLUMN_MAX_PARTICIPANT
import com.example.swuni_up.DBHelper.Companion.COLUMN_PHOTO
import com.example.swuni_up.DBHelper.Companion.COLUMN_START_DAY
import com.example.swuni_up.DBHelper.Companion.COLUMN_STATUS

class ChallengeExplore : AppCompatActivity() {

    // UI 요소들
    private lateinit var cateHealth: LinearLayout
    private lateinit var cateStudy: LinearLayout
    private lateinit var cateHabit: LinearLayout
    private lateinit var cateHobby: LinearLayout
    private lateinit var cateMoney: LinearLayout
    private lateinit var cateEtc: LinearLayout

    private lateinit var healthImageView: ImageView
    private lateinit var studyImageView: ImageView
    private lateinit var habitImageView: ImageView
    private lateinit var hobbyImageView: ImageView
    private lateinit var moneyImageView: ImageView
    private lateinit var etcImageView: ImageView

    private lateinit var healthTextView: TextView
    private lateinit var studyTextView: TextView
    private lateinit var habitTextView: TextView
    private lateinit var hobbyTextView: TextView
    private lateinit var moneyTextView: TextView
    private lateinit var etcTextView: TextView

    private lateinit var navMyChallenge: ImageView
    private lateinit var navChallengeHome: ImageView

    // 선택된 상태를 추적할 변수
    private var selectedCategory: LinearLayout? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewLong: RecyclerView
    private lateinit var adapter: BigChallengeAdapter
    private lateinit var adapterLong: LongChallengeAdapter
    private var challengeList = mutableListOf<DBHelper.Challenge>()  // 전체 챌린지 리스트
    private var filteredList = mutableListOf<DBHelper.Challenge>() // 필터된 리스트
    private var filteredOfficailList = mutableListOf<DBHelper.Challenge>()

    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_challenge_explore)

        dbHelper = DBHelper(this)

        dbHelper.updateChallengeStatus()

        // UI 요소들 초기화
        cateHealth = findViewById(R.id.cate_health)
        cateStudy = findViewById(R.id.cate_study)
        cateHabit = findViewById(R.id.cate_habit)
        cateHobby = findViewById(R.id.cate_hobby)
        cateMoney = findViewById(R.id.cate_money)
        cateEtc = findViewById(R.id.cate_etc)

        healthImageView = cateHealth.findViewById(R.id.cate_health_image)
        studyImageView = cateStudy.findViewById(R.id.cate_study_image)
        habitImageView = cateHabit.findViewById(R.id.cate_habit_image)
        hobbyImageView = cateHobby.findViewById(R.id.cate_hobby_image)
        moneyImageView = cateMoney.findViewById(R.id.cate_money_image)
        etcImageView = cateEtc.findViewById(R.id.cate_etc_image)

        healthTextView = cateHealth.findViewById(R.id.cate_health_text)
        studyTextView = cateStudy.findViewById(R.id.cate_study_text)
        habitTextView = cateHabit.findViewById(R.id.cate_habit_text)
        hobbyTextView = cateHobby.findViewById(R.id.cate_hobby_text)
        moneyTextView = cateMoney.findViewById(R.id.cate_money_text)
        etcTextView = cateEtc.findViewById(R.id.cate_etc_text)

        navMyChallenge = findViewById(R.id.nav_my_challenge)
        navChallengeHome = findViewById(R.id.nav_home)

        // 클릭 리스너 설정
        cateHealth.setOnClickListener { onCategoryClicked(cateHealth, healthImageView, healthTextView, 1) }
        cateStudy.setOnClickListener { onCategoryClicked(cateStudy, studyImageView, studyTextView, 2) }
        cateHabit.setOnClickListener { onCategoryClicked(cateHabit, habitImageView, habitTextView, 3) }
        cateHobby.setOnClickListener { onCategoryClicked(cateHobby, hobbyImageView, hobbyTextView, 4) }
        cateMoney.setOnClickListener { onCategoryClicked(cateMoney, moneyImageView, moneyTextView, 5) }
        cateEtc.setOnClickListener { onCategoryClicked(cateEtc, etcImageView, etcTextView, 6) }



        recyclerView = findViewById(R.id.recyclerViewBigChallenge)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // DB에서 모든 챌린지를 가져옴
        challengeList = getChallengesFromDB()

        // 어댑터 초기화 시 데이터 전달
        adapter = BigChallengeAdapter(this, challengeList)
        recyclerView.adapter = adapter

        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getLong("user_id", -1L)
        Log.d("userInfo", "저장된 user_id: $userId")

        val createButton = findViewById<ImageView>(R.id.create_button)

        createButton.setOnClickListener {
            // ChallengeCreate 액티비티로 이동
            val intent = Intent(this, ChallengeCreate::class.java)
            startActivity(intent)
        }

        recyclerViewLong = findViewById(R.id.recyclerViewLongChallenge)
        recyclerViewLong.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)


        // 어댑터 초기화 시 데이터 전달
        adapterLong = LongChallengeAdapter(this, challengeList)
        recyclerViewLong.adapter = adapterLong

        filterChallenges(1)
        filterOfficialChallenges(1)
        onCategoryClicked(cateHealth, healthImageView, healthTextView, 1)

        navMyChallenge.setOnClickListener {
            val intent = Intent(this, my_challenge::class.java)
            startActivity(intent)
        }

        navChallengeHome.setOnClickListener {
            val intent = Intent(this, ChallengeHome::class.java)
            startActivity(intent)
        }

    }

    // 카테고리 클릭 시 호출되는 함수
    private fun onCategoryClicked(category: LinearLayout, imageView: ImageView, textView: TextView, categoryId: Int) {
        // 이전에 선택된 카테고리가 있으면 원래대로 복원
        selectedCategory?.let {
            resetCategory(it)
        }

        // 선택된 카테고리 스타일 변경
        category.backgroundTintList = ContextCompat.getColorStateList(this, R.color.navy)

        // 해당 카테고리에 맞는 아이콘과 텍스트 색상 변경
        when (category) {
            cateHealth -> {
                imageView.setImageResource(R.drawable.ic_heart_white)
                healthTextView.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
            cateStudy -> {
                imageView.setImageResource(R.drawable.ic_pencil_white)
                studyTextView.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
            cateHabit -> {
                imageView.setImageResource(R.drawable.ic_habit_white)
                habitTextView.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
            cateHobby -> {
                imageView.setImageResource(R.drawable.ic_hobby_white)
                hobbyTextView.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
            cateMoney -> {
                imageView.setImageResource(R.drawable.ic_money_white)
                moneyTextView.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
            cateEtc -> {
                imageView.setImageResource(R.drawable.ic_etc_white)
                etcTextView.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
        }

        selectedCategory = category

        // 카테고리에 맞게 챌린지 목록 필터링
        filterChallenges(categoryId)

        filterOfficialChallenges(categoryId)
    }

    private fun resetCategory(category: LinearLayout) {
        category.backgroundTintList = ContextCompat.getColorStateList(this, R.color.gray25)

        // 해당 카테고리에 맞는 아이콘과 텍스트 색상 복원
        when (category) {
            cateHealth -> {
                healthImageView.setImageResource(R.drawable.ic_heart_gray)
                healthTextView.setTextColor(ContextCompat.getColor(this, R.color.gray70))
            }
            cateStudy -> {
                studyImageView.setImageResource(R.drawable.ic_pencil_gray)
                studyTextView.setTextColor(ContextCompat.getColor(this, R.color.gray70))
            }
            cateHabit -> {
                habitImageView.setImageResource(R.drawable.ic_habit_gray)
                habitTextView.setTextColor(ContextCompat.getColor(this, R.color.gray70))
            }
            cateHobby -> {
                hobbyImageView.setImageResource(R.drawable.ic_hobby_gray)
                hobbyTextView.setTextColor(ContextCompat.getColor(this, R.color.gray70))
            }
            cateMoney -> {
                moneyImageView.setImageResource(R.drawable.ic_money_gray)
                moneyTextView.setTextColor(ContextCompat.getColor(this, R.color.gray70))
            }
            cateEtc -> {
                etcImageView.setImageResource(R.drawable.ic_etc_gray)
                etcTextView.setTextColor(ContextCompat.getColor(this, R.color.gray70))
            }
        }
    }

    private fun filterChallenges(categoryId: Int) {
        filteredList = challengeList.filter { it.category == categoryId && it.status == 1 }.toMutableList()  // 상태가 1인 챌린지만 필터링
        adapter.updateChallenges(filteredList)
    }

    private fun filterOfficialChallenges(categoryId: Int) {
        // 해당 카테고리와 is_official이 1인 챌린지, 그리고 상태가 1인 챌린지만 필터링
        filteredOfficailList = challengeList.filter { it.category == categoryId && it.isOfficial == 1 && it.status == 1 }.toMutableList()
        adapterLong.updateChallenges(filteredOfficailList)
    }



    // DB에서 챌린지 목록 가져오기 (예제 코드)
    private fun getChallengesFromDB(): MutableList<DBHelper.Challenge> {
        val dbHelper = DBHelper(this)
        val db = dbHelper.readableDatabase
        val challengeList = mutableListOf<DBHelper.Challenge>()

        val cursor = db.rawQuery("SELECT * FROM ${DBHelper.TABLE_CHALLENGE}", null)
        while (cursor.moveToNext()) {
            val challenge = Challenge(
                challengeId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CHALLENGE_ID)),
                title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CHALLENGE_TITLE)),
                description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                photo = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_PHOTO)),
                createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)),
                startDay = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START_DAY)),
                endDay = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_END_DAY)),
                status = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STATUS)),
                maxParticipant = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MAX_PARTICIPANT)),
                category = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                isOfficial = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_OFFICIAL))
            )
            challengeList.add(challenge)
        }
        cursor.close()
        db.close()
        return challengeList
    }


}
