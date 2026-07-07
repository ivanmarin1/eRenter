package com.vacation.feature.calendar.presentation

import com.vacation.feature.calendar.domain.model.ApartmentId
import com.vacation.feature.calendar.domain.model.BookingSummary
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import kotlin.math.roundToLong

/** Where a reservation sits relative to today, shown as a coloured chip. */
enum class BookingStatus { Past, Staying, Upcoming }

/** A reservation row on the Apartments tab, with everything pre-formatted for the view. */
data class ApartmentBookingRow(
    val summary: BookingSummary,
    val status: BookingStatus,
    val nights: Int,
    val dateLabel: String,
    val paymentLabel: String,
)

/** Headline numbers for the selected apartment. */
data class ApartmentStats(
    val count: Int,
    val nights: Int,
    val revenue: String,
)

/** Immutable snapshot the Apartments tab renders. */
data class ApartmentBookingsUiState(
    val hasApartments: Boolean,
    val apartmentId: ApartmentId?,
    val apartmentName: String,
    val bookings: List<ApartmentBookingRow>,
    val stats: ApartmentStats,
    val today: LocalDate,
    val isLoading: Boolean,
) {
    companion object {
        fun empty(today: LocalDate, hasApartments: Boolean, isLoading: Boolean = false): ApartmentBookingsUiState =
            ApartmentBookingsUiState(
                hasApartments = hasApartments,
                apartmentId = null,
                apartmentName = "",
                bookings = emptyList(),
                stats = ApartmentStats(0, 0, euro(0.0)),
                today = today,
                isLoading = isLoading,
            )
    }
}

private val monthShort = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
)

/** "28 Jun". */
internal fun shortDate(date: LocalDate): String = "${date.dayOfMonth} ${monthShort[date.month.number - 1]}"

/** "€400 · paid in full" when nothing is due, otherwise "€200 paid · €400 due". */
internal fun paymentLabel(upfront: Double?, rest: Double?): String {
    val paid = upfront ?: 0.0
    val due = rest ?: 0.0
    return if (due == 0.0) "${euro(paid)} · paid in full" else "${euro(paid)} paid · ${euro(due)} due"
}

/** "€1,234" — whole euros with thousands separators, no decimals. */
internal fun euro(amount: Double): String {
    val whole = amount.roundToLong()
    val digits = kotlin.math.abs(whole).toString()
    val grouped = buildString {
        digits.forEachIndexed { i, c ->
            if (i > 0 && (digits.length - i) % 3 == 0) append(',')
            append(c)
        }
    }
    return if (whole < 0) "-€$grouped" else "€$grouped"
}
