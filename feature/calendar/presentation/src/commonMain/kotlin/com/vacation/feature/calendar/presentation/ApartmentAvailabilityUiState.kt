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

/** Immutable snapshot the per-apartment availability screen renders. */
data class ApartmentAvailabilityUiState(
    val hasApartments: Boolean,
    val apartmentId: ApartmentId?,
    val apartmentName: String,
    val monthLabel: String,
    val weekdayLabels: List<String>,
    val weeks: List<List<DayAvailability>>,
    val conflicts: List<AvailabilityConflict>,
    val isLoading: Boolean,
) {
    companion object {
        fun empty(monthLabel: String, hasApartments: Boolean, isLoading: Boolean): ApartmentAvailabilityUiState =
            ApartmentAvailabilityUiState(
                hasApartments = hasApartments,
                apartmentId = null,
                apartmentName = "",
                monthLabel = monthLabel,
                weekdayLabels = emptyList(),
                weeks = emptyList(),
                conflicts = emptyList(),
                isLoading = isLoading,
            )
    }
}
