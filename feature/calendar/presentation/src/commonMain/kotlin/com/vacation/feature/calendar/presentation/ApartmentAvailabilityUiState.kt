package com.vacation.feature.calendar.presentation

import com.vacation.feature.calendar.domain.model.ApartmentId
import com.vacation.feature.calendar.domain.model.DayAvailability
import kotlinx.datetime.LocalDate

/** One overlapping reservation, shown in the conflicts list. */
data class AvailabilityConflict(
    val guestName: String,
    val checkIn: LocalDate,
    val checkOut: LocalDate,
)

/** Look at one apartment across time, or one month across all apartments. */
enum class AvailabilityScope { PerApartment, PerMonth }

/** Immutable snapshot the availability screen renders. */
data class ApartmentAvailabilityUiState(
    val hasApartments: Boolean,
    val apartmentId: ApartmentId?,
    val apartmentName: String,
    val monthLabel: String,
    val weekdayLabels: List<String>,
    val weeks: List<List<DayAvailability>>,
    val conflicts: List<AvailabilityConflict>,
    val today: LocalDate,
    val isLoading: Boolean,
    val scope: AvailabilityScope = AvailabilityScope.PerApartment,
    val viewMode: CalendarViewMode = CalendarViewMode.Month,
    val yearLabel: String = "",
    /** Year overview (per-apartment year view) or matrix (per-month) — labelled accordingly. */
    val overview: List<MiniMonthUi> = emptyList(),
) {
    companion object {
        fun empty(monthLabel: String, today: LocalDate, hasApartments: Boolean, isLoading: Boolean): ApartmentAvailabilityUiState =
            ApartmentAvailabilityUiState(
                hasApartments = hasApartments,
                apartmentId = null,
                apartmentName = "",
                monthLabel = monthLabel,
                weekdayLabels = emptyList(),
                weeks = emptyList(),
                conflicts = emptyList(),
                today = today,
                isLoading = isLoading,
            )
    }
}
