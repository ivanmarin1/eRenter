package com.vacation.feature.calendar.domain.usecase

import com.vacation.feature.calendar.domain.model.YearMonth
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus

/** One cell of a month grid: its date and whether it belongs to the month being shown. */
data class GridDay(val date: LocalDate, val inVisibleMonth: Boolean)

/**
 * The dates of a month grid, padded with leading/trailing days from adjacent months so every
 * row is a full week starting on [weekStart]. Shared by the all-apartments schedule and the
 * per-apartment availability views so they always line up.
 */
fun monthGridDates(yearMonth: YearMonth, weekStart: DayOfWeek): List<GridDay> {
    val firstOfMonth = yearMonth.firstDay()
    val firstOfNext = firstOfMonth.plus(1, DateTimeUnit.MONTH)
    val daysInMonth = firstOfMonth.daysUntil(firstOfNext)

    // How many trailing days of the previous month we render before day 1.
    val leadOffset = (firstOfMonth.dayOfWeek.isoDayNumber - weekStart.isoDayNumber + 7) % 7
    val gridStart = firstOfMonth.plus(-leadOffset, DateTimeUnit.DAY)
    val totalCells = ((leadOffset + daysInMonth + 6) / 7) * 7 // round up to full weeks

    return (0 until totalCells).map { offset ->
        val date = gridStart.plus(offset, DateTimeUnit.DAY)
        GridDay(
            date = date,
            inVisibleMonth = date.month == yearMonth.month && date.year == yearMonth.year,
        )
    }
}
