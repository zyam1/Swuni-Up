import android.graphics.Color
import android.text.style.ForegroundColorSpan
import com.prolificinteractive.materialcalendarview.*
import org.threeten.bp.LocalDate

// 오늘 이전 날짜를 비활성화하는 데코레이터
class PastDayDisableDecorator : DayViewDecorator {
    private val today: LocalDate = LocalDate.now()

    override fun shouldDecorate(day: CalendarDay?): Boolean {
        return day != null && day.date.isBefore(today) // 오늘 이전 날짜만 적용
    }

    override fun decorate(view: DayViewFacade) {
        view.setDaysDisabled(true) // 선택 비활성화
        view.addSpan(ForegroundColorSpan(Color.GRAY)) // 글자 색상 변경
    }
}
