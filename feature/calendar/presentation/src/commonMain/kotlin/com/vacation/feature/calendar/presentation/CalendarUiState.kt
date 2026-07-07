package com.vacation.feature.calendar.presentation

import com.vacation.feature.calendar.domain.model.DaySchedule
import com.vacation.feature.calendar.domain.model.MiniMonthCell
import com.vacation.feature.calendar.domain.model.YearMonth
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

/** Month grid vs. 12-month year overview. */
enum class CalendarViewMode { Month, Year }

/** A compact month in the year overview, with a pre-formatted short label (e.g. "Jul"). */
data class MiniMonthUi(
    val yearMonth: YearMonth,
    val label: String,
    val cells: List<MiniMonthCell>,
)

/**
 * Immutable snapshot the UI renders. It is pure data — any UI toolkit (Compose today,
 * something else tomorrow) can bind to it. All labels are pre-formatted so the view
 * contains no business or date logic.
 */
data class CalendarUiState(
    val yearMonth: YearMonth,
    val monthLabel: String,
    val weekdayLabels: List<String>,
    val weeks: List<List<DaySchedule>>,
    val today: LocalDate?,
    val selectedDay: DaySchedule?,
    val isLoading: Boolean,
    val viewMode: CalendarViewMode = CalendarViewMode.Month,
    val yearLabel: String = yearMonth.year.toString(),
    val miniMonths: List<MiniMonthUi> = emptyList(),
) {
    companion object {
        fun loading(yearMonth: YearMonth, monthLabel: String): CalendarUiState =
            CalendarUiState(
                yearMonth = yearMonth,
                monthLabel = monthLabel,
                weekdayLabels = emptyList(),
                weeks = emptyList(),
                today = null,
                selectedDay = null,
                isLoading = true,
            )
    }
}

/** Small formatting helpers kept out of the UI so the view is dumb and portable. */
internal object CalendarLabels {
    private val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December",
    )
    private val weekdayShort = mapOf(
        kotlinx.datetime.DayOfWeek.MONDAY to "Mon",
        kotlinx.datetime.DayOfWeek.TUESDAY to "Tue",
        kotlinx.datetime.DayOfWeek.WEDNESDAY to "Wed",
        kotlinx.datetime.DayOfWeek.THURSDAY to "Thu",
        kotlinx.datetime.DayOfWeek.FRIDAY to "Fri",
        kotlinx.datetime.DayOfWeek.SATURDAY to "Sat",
        kotlinx.datetime.DayOfWeek.SUNDAY to "Sun",
    )

    private val monthsShort = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
    )

    fun monthLabel(ym: YearMonth): String = "${months[ym.month.number - 1]} ${ym.year}"

    fun shortMonthLabel(ym: YearMonth): String = monthsShort[ym.month.number - 1]

    fun weekdayLabels(weekStart: kotlinx.datetime.DayOfWeek): List<String> =
        (0 until 7).map { offset ->
            val dow = kotlinx.datetime.DayOfWeek(((weekStart.isoDayNumber - 1 + offset) % 7) + 1)
            weekdayShort.getValue(dow)
        }
}
