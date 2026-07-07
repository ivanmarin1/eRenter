package com.vacation.feature.calendar.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vacation.feature.calendar.domain.model.Apartment
import com.vacation.feature.calendar.domain.model.ApartmentId
import com.vacation.feature.calendar.domain.model.Booking
import com.vacation.feature.calendar.domain.model.BookingId
import com.vacation.feature.calendar.domain.model.BookingSummary
import com.vacation.feature.calendar.domain.repository.BookingRepository
import com.vacation.feature.calendar.domain.usecase.DetectOverbookingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.todayIn

/**
 * Drives the Apartments tab: pick an apartment and see every reservation for it with quick stats
 * (bookings / nights / expected revenue), expandable detail, and edit / delete. Apartment
 * management (add, rename, delete) also lives here so the tab is self-contained.
 */
class ApartmentBookingsViewModel(
    private val repository: BookingRepository,
    private val detectOverbookings: DetectOverbookingsUseCase,
    private val clock: Clock = Clock.System,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) : ViewModel() {

    private val today: LocalDate = clock.todayIn(timeZone)
    private val selectedApartmentId = MutableStateFlow<ApartmentId?>(null)

    val apartments: StateFlow<List<Apartment>> =
        repository.observeApartments()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Kept eagerly current so the synchronous overbooking check sees the latest bookings.
    private val bookings: StateFlow<List<Booking>> =
        repository.observeBookings()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        // Default to the first apartment once the list arrives, and keep the selection valid.
        apartments.onEach { list ->
            val current = selectedApartmentId.value
            if (current == null || list.none { it.id == current }) {
                selectedApartmentId.value = list.firstOrNull()?.id
            }
        }.launchIn(viewModelScope)
    }

    val uiState: StateFlow<ApartmentBookingsUiState> =
        combine(selectedApartmentId, bookings, apartments) { apartmentId, bookings, apartments ->
            if (apartmentId == null) {
                ApartmentBookingsUiState.empty(today, hasApartments = apartments.isNotEmpty())
            } else {
                val apartment = apartments.firstOrNull { it.id == apartmentId }
                val rows = bookings
                    .filter { it.apartmentId == apartmentId }
                    .sortedBy { it.checkIn }
                    .map { it.toRow(apartment?.name ?: "") }
                ApartmentBookingsUiState(
                    hasApartments = true,
                    apartmentId = apartmentId,
                    apartmentName = apartment?.name ?: "",
                    bookings = rows,
                    stats = rows.toStats(),
                    today = today,
                    isLoading = false,
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ApartmentBookingsUiState.empty(today, hasApartments = false, isLoading = true),
        )

    fun selectApartment(id: ApartmentId) { selectedApartmentId.value = id }

    /** Existing reservations that would clash with [draft] (empty when safe to save). */
    fun conflictsFor(draft: BookingDraft, editingId: BookingId?): List<BookingSummary> =
        detectOverbookings.conflictSummaries(draft, editingId, bookings.value, apartments.value)

    fun updateBooking(bookingId: BookingId, draft: BookingDraft) {
        if (draft.guestName.isBlank() || draft.checkOut < draft.checkIn) return
        viewModelScope.launch { repository.upsertBooking(draft.toBooking(bookingId)) }
    }

    fun deleteBooking(bookingId: BookingId) {
        viewModelScope.launch { repository.deleteBooking(bookingId) }
    }

    fun addApartment(name: String) {
        val clean = name.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch {
            val id = ApartmentId(newId())
            repository.upsertApartment(Apartment(id, clean))
            selectedApartmentId.value = id
        }
    }

    fun renameApartment(id: ApartmentId, name: String) {
        val clean = name.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch { repository.upsertApartment(Apartment(id, clean)) }
    }

    fun deleteApartment(id: ApartmentId) {
        viewModelScope.launch { repository.deleteApartment(id) }
    }

    private fun Booking.toRow(apartmentName: String): ApartmentBookingRow {
        val nights = checkIn.daysUntil(checkOut)
        return ApartmentBookingRow(
            summary = BookingSummary(
                bookingId = id,
                apartmentId = apartmentId,
                apartmentName = apartmentName,
                guestName = guestName,
                checkIn = checkIn,
                checkOut = checkOut,
                upfrontPayment = upfrontPayment,
                restPayment = restPayment,
                notes = notes,
                contactInfo = contactInfo,
                country = country,
            ),
            status = when {
                checkOut <= today -> BookingStatus.Past
                checkIn <= today -> BookingStatus.Staying
                else -> BookingStatus.Upcoming
            },
            nights = nights,
            dateLabel = "${shortDate(checkIn)} → ${shortDate(checkOut)} · ${nights}n",
            paymentLabel = paymentLabel(upfrontPayment, restPayment),
        )
    }

    private fun List<ApartmentBookingRow>.toStats(): ApartmentStats {
        val nights = sumOf { it.nights }
        val revenue = bookings.value.asSequence()
            .filter { it.apartmentId == selectedApartmentId.value }
            .sumOf { (it.upfrontPayment ?: 0.0) + (it.restPayment ?: 0.0) }
        return ApartmentStats(count = size, nights = nights, revenue = euro(revenue))
    }
}
