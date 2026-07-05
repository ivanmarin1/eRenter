package com.vacation.feature.calendar.domain.model

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

/** A calendar month, independent of any specific day. Kept in the domain to avoid leaking date libs upward. */
data class YearMonth(val year: Int, val month: Month) {

    fun atDay(day: Int): LocalDate = LocalDate(year, month.number, day)

    fun firstDay(): LocalDate = atDay(1)

    fun next(): YearMonth = firstDay().plus(1, DateTimeUnit.MONTH).let { YearMonth(it.year, it.month) }

    fun previous(): YearMonth = firstDay().plus(-1, DateTimeUnit.MONTH).let { YearMonth(it.year, it.month) }

    companion object {
        fun of(date: LocalDate): YearMonth = YearMonth(date.year, date.month)

        fun current(
            clock: Clock = Clock.System,
            timeZone: TimeZone = TimeZone.currentSystemDefault(),
        ): YearMonth = of(clock.todayIn(timeZone))
    }
}
