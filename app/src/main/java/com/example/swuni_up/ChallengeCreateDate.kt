package com.example.swuni_up

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.example.swuni_up.databinding.ActivityChallengeCreateDateBinding
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.format.ArrayWeekDayFormatter
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ChallengeCreateDate : AppCompatActivity() {
    private lateinit var binding: ActivityChallengeCreateDateBinding
    private var selectedStartSchedule: LocalDate? = null
    private var selectedEndSchedule: LocalDate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChallengeCreateDateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // makeButton 클릭 리스너 추가
        binding.makeButton.setOnClickListener {
            // 버튼 클릭 시에 실행할 로직

            // 입력된 데이터 추출
            val title = intent.getStringExtra("challengeTitle")
            val description = intent.getStringExtra("description")
            val maxParticipant = intent.getIntExtra("maxParticipant", 0)
            val category = intent.getIntExtra("category", 0)
            val challengePhoto = intent.getStringExtra("challengePhoto")  // 이미지 파일 처리 필요

            // 날짜 추출
            val createdAt = binding.recruitmentTextViewRight.tag as String
            val startDay = binding.startTextViewRight.tag as String
            val endDay = binding.endTextViewRight.tag as String


            // status 계산
            val today = LocalDate.now()
            val createdLocalDate = LocalDate.parse(createdAt, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val startLocalDate = LocalDate.parse(startDay, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val endLocalDate = LocalDate.parse(endDay, DateTimeFormatter.ofPattern("yyyy-MM-dd"))

            val status = when {
                today.isBefore(createdLocalDate) -> 0
                today.isBefore(startLocalDate) -> 1 // 모집 중
                today.isBefore(endLocalDate) ->  2// 시작됨
                today.isAfter(endLocalDate) -> 3 // 마감됨
                else -> 5 // 시작됨
            }

            val photoByteArray = uriToByteArray(this, challengePhoto)

            if (photoByteArray == null) {
                Toast.makeText(this, "이미지 변환에 실패했습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Challenge 객체 생성 (필요시, 서버에 보낼 데이터 포맷에 맞추어 구성)
            val challenge = ChallengeDBHelper.Challenge(
                title = title ?: "Untitled",
                description = description,
                photo = photoByteArray,
                createdAt = createdAt,
                startDay = startDay,
                endDay = endDay,
                status = status,
                maxParticipant = maxParticipant,
                category = category
            )

            // 데이터베이스 저장
            val dbHelper = ChallengeDBHelper(this)
            val challengeId = dbHelper.insertChallenge(challenge)

            // 삽입 결과에 따라 처리
            if (challengeId != -1L) {
                val dbHelper = ChallengerDBHelper(this)

                // 예시 데이터 (실제 사용자와 챌린지 ID는 적절히 설정해야 함)
                val userId: Long = 1 // 로그인된 사용자 ID

                val challengeRole = "admin" // 역할: 참가자
                val joinedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                    Date()
                ) // 현재 시간
                val percentage = 0 // 초기 참여율
                val isCompleted = false // 초기 완료 상태

                // Challenger 객체 생성
                val challenger = ChallengerDBHelper.Challenger(
                    userId = userId,
                    challengeId = challengeId,
                    challengeRole = challengeRole,
                    joinedAt = joinedAt,
                    percentage = percentage,
                    isCompleted = isCompleted
                )

                // 데이터베이스에 삽입
                val result = dbHelper.insertChallenger(challenger)
                if (result != -1L) {
                    Toast.makeText(this, "참여가 완료되었습니다!", Toast.LENGTH_SHORT).show()
                    Log.d("ChallengeJoin", "참여 완료: ID = $result")
                } else {
                    Toast.makeText(this, "참여 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    Log.e("ChallengeJoin", "참여 실패")
                }

                Toast.makeText(this, "챌린지가 성공적으로 생성되었습니다.", Toast.LENGTH_SHORT).show()
                finish()  // 예시: 현재 액티비티 종료
            } else {
                Toast.makeText(this, "챌린지 생성에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }



        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // 뒤로가기 버튼 동작 설정
        toolbar.setNavigationOnClickListener {
            finish() // 현재 액티비티 종료
        }

        val dateFormatter = DateTimeFormatter.ofPattern("M월 d일")

        val calendarView = findViewById<MaterialCalendarView>(R.id.calendar_view)
        calendarView.setHeaderTextAppearance(R.style.CalendarWidgetHeader)

        // 요일을 한글로 보이게 설정 월..일 순서로 배치해서 캘린더에는 일..월 순서로 보이도록 설정
        binding.calendarView.setWeekDayFormatter(ArrayWeekDayFormatter(resources.getTextArray(R.array.custom_weekdays)));

        // 좌우 화살표 사이 연, 월의 폰트 스타일 설정
        binding.calendarView.setHeaderTextAppearance(R.style.CalendarWidgetHeader)

        // 시작, 종료 범위가 설정되었을 때 리스너
        binding.calendarView.setOnDateChangedListener { _, date, selected ->
            if (selected) {
                selectedStartSchedule = date.date
                selectedEndSchedule = null
                updateDateViews(dateFormatter)
            }
        }
        // 날짜가 단일 선택되었을 때 리스너
        binding.calendarView.setOnRangeSelectedListener { _, dates ->
            if (dates.isNotEmpty()) {
                val startDate = dates.first().date
                val endDate = dates.last().date
                selectedStartSchedule = startDate
                selectedEndSchedule = endDate
                updateDateViews(dateFormatter)

                // 날짜 차이 계산 후 표시
                updateDayDifferenceTextView(startDate, endDate)
            }
        }


        val dayDecorator = DayDecorator(this)
        val todayDecorator = TodayDecorator(this)

        val selectedMonth = CalendarDay.today().month - 1
        var selectedMonthDecorator = SelectedMonthDecorator(selectedMonth, this)

        // 캘린더에 Decorator 추가
        binding.calendarView.addDecorators(dayDecorator, todayDecorator, selectedMonthDecorator)

        // 좌우 화살표 가운데의 연/월이 보이는 방식 지정
        binding.calendarView.setTitleFormatter { day ->
            val year = day.year
            val month = day.month

            val calendarHeaderBuilder = StringBuilder()
            calendarHeaderBuilder.append(year).append("년 ").append(month).append("월")
            calendarHeaderBuilder.toString()
        }

        // 캘린더에 보여지는 Month가 변경된 경우
        binding.calendarView.setOnMonthChangedListener { widget, date ->
            // 기존에 설정되어 있던 Decorators 초기화
            binding.calendarView.removeDecorators()
            binding.calendarView.invalidateDecorators()

            // CalendarDay에서 month 값을 추출 (월은 1부터 12까지, 0부터 시작하는 값이 아니므로 - 1을 해줘야 합니다)
            val selectedMonth = date.month - 1

            // Decorators 추가
            selectedMonthDecorator = SelectedMonthDecorator(selectedMonth, this)
            binding.calendarView.addDecorators(dayDecorator, todayDecorator, selectedMonthDecorator)
        }
    }

    fun uriToByteArray(context: Context, uri: String?): ByteArray? {
        return try {
            // URI에서 InputStream 가져오기
            val inputStream = context.contentResolver.openInputStream(android.net.Uri.parse(uri))
            val bitmap = BitmapFactory.decodeStream(inputStream) // Bitmap 변환
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream) // 압축 (JPEG 형식)
            outputStream.toByteArray() // ByteArray로 변환
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private inner class DayDecorator(context: Context) : DayViewDecorator {
        private val drawable = ContextCompat.getDrawable(context,R.drawable.calendar_selector)
        // true를 리턴 시 모든 요일에 내가 설정한 드로어블이 적용된다
        override fun shouldDecorate(day: CalendarDay): Boolean {
            return true
        }

        // 일자 선택 시 내가 정의한 드로어블이 적용되도록 한다
        override fun decorate(view: DayViewFacade) {
            view.setSelectionDrawable(drawable!!)
        }
    }

    /* 오늘 날짜의 background를 설정하는 클래스 */
    private class TodayDecorator(context: Context): DayViewDecorator {
        private val drawable = ContextCompat.getDrawable(context, R.drawable.calendar_circle_navy)
        private val today = CalendarDay.today()

        override fun shouldDecorate(day: CalendarDay?): Boolean {
            // today와 day를 year, month, day로 직접 비교하여 오늘 날짜인지 확인
            return day?.year == today.year && day.month == today.month && day.day == today.day
        }

        override fun decorate(view: DayViewFacade?) {
            view?.setBackgroundDrawable(drawable!!)
        }
    }

    private inner class SelectedMonthDecorator(val selectedMonth: Int, context: Context) : DayViewDecorator {
        private val context = context

        override fun shouldDecorate(day: CalendarDay): Boolean {
            // 선택된 월만 기본 색상(검은색)으로 설정하고, 나머지 달의 날짜는 회색으로 설정
            return day.month != selectedMonth + 1
        }

        override fun decorate(view: DayViewFacade) {
            // 회색으로 설정
            view.addSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.gray40)))
        }
    }

    private fun updateDateViews(dateFormatter: DateTimeFormatter) {
        val storageFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val displayFormatter = DateTimeFormatter.ofPattern("M월 d일")

        selectedStartSchedule?.let { startDate ->
            // 저장용 (yyyy-MM-dd)
            val formattedStartDate = startDate.format(storageFormatter)
            val formattedRecruitmentDate = startDate.minusDays(3).format(storageFormatter)

            // UI 표시용 (M월 d일)
            binding.startTextViewRight.text = startDate.format(displayFormatter)
            binding.recruitmentTextViewRight.text = startDate.minusDays(3).format(displayFormatter)

            // 데이터 저장용 텍스트 뷰에 저장 (추가 작업 시 사용 가능)
            binding.startTextViewRight.tag = formattedStartDate
            binding.recruitmentTextViewRight.tag = formattedRecruitmentDate
        }

        selectedEndSchedule?.let { endDate ->
            // UI 표시용 (M월 d일)
            binding.endTextViewRight.text = endDate.format(displayFormatter)

            // 저장용 텍스트 뷰 태그에 데이터 저장
            binding.endTextViewRight.tag = endDate.format(storageFormatter)
        } ?: run {
            binding.endTextViewRight.text = ""
            binding.endTextViewRight.tag = ""
        }
    }


    // 날짜 차이를 계산하고 TextView에 표시하는 함수
    private fun updateDayDifferenceTextView(startDate: LocalDate, endDate: LocalDate) {
        val dayDifference = org.threeten.bp.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1
        binding.dayTextViewLeft.text = dayDifference.toString()
    }



}