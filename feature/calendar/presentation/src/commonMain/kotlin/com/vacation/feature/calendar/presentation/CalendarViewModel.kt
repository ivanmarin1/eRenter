package com.vacation.feature.calendar.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vacation.feature.calendar.domain.model.Apartment
import com.vacation.feature.calendar.domain.model.Booking
import com.vacation.feature.calendar.domain.model.BookingId
import com.vacation.feature.calendar.domain.model.BookingSummary
import com.vacation.feature.calendar.domain.model.MonthSchedule
import com.vacation.feature.calendar.domain.model.YearMonth
import com.vacation.feature.calendar.domain.repository.BookingRepository
import com.vacation.feature.calendar.domain.usecase.DetectOverbookingsUseCase
import com.vacation.feature.calendar.domain.usecase.GetMonthScheduleUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * Owns calendar UI state. Reads the month schedule through the domain use case and writes
 * single-reservation changes back through the repository. Because it is a multiplatform
 * ViewModel it survives configuration changes on Android and works unchanged on iOS/Desktop.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModel(
    private val getMonthSchedule: GetMonthScheduleUseCase,
    private val repository: BookingRepository,
    private val detectOverbookings: DetectOverbookingsUseCase,
    private val clock: Clock = Clock.System,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) : ViewModel() {

    private val today: LocalDate = clock.todayIn(timeZone)

    private val visibleMonth = MutableStateFlow(YearMonth.of(today))
    private val selectedDate = MutableStateFlow<LocalDate?>(null)

    /** Apartments to choose from when adding or editing a reservation. */
    val apartments: StateFlow<List<Apartment>> =
        repository.observeApartments()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Kept eagerly current so the synchronous overbooking check (conflictsFor) always sees the
    // latest bookings, even before anything collects the derived flows.
    private val bookings: StateFlow<List<Booking>> =
        repository.observeBookings()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Ids of bookings that overlap another; used to badge conflicting rows in the day detail. */
    val conflictedBookingIds: StateFlow<Set<BookingId>> =
        bookings.map { detectOverbookings.conflictedIds(it) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    val uiState: StateFlow<CalendarUiState> =
        combine(
            visibleMonth.flatMapLatest { month -> getMonthSchedule(month) },
            selectedDate,
        ) { schedule, selected ->
            schedule.toUiState(selected)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CalendarUiState.loading(
                visibleMonth.value,
                CalendarLabels.monthLabel(visibleMonth.value),
            ),
        )

    fun onEvent(event: CalendarEvent) {
        when (event) {
            CalendarEvent.PreviousMonth -> {
                visibleMonth.update { it.previous() }
                selectedDate.value = null
            }
            CalendarEvent.NextMonth -> {
                visibleMonth.update { it.next() }
                selectedDate.value = null
            }
            CalendarEvent.GoToToday -> {
                visibleMonth.value = YearMonth.of(today)
                selectedDate.value = today
            }
            is CalendarEvent.SelectDay -> {
                selectedDate.update { current -> if (current == event.date) null else event.date }
            }
            CalendarEvent.ClearSelection -> selectedDate.value = null
        }
    }

    /**
     * Existing reservations that would clash with [draft] for the same apartment. Empty when the
     * draft is safe to save. [editingId] excludes the booking being edited from clashing with itself.
     */
    fun conflictsFor(draft: BookingDraft, editingId: BookingId?): List<BookingSummary> {
        if (draft.checkOut <= draft.checkIn) return emptyList()
        val candidate = Booking(
            id = editingId ?: CANDIDATE_ID,
            apartmentId = draft.apartmentId,
            guestName = draft.guestName,
            checkIn = draft.checkIn,
            checkOut = draft.checkOut,
        )
        val names = apartments.value.associate { it.id to it.name }
        return detectOverbookings.conflictsFor(candidate, bookings.value).map { b ->
            BookingSummary(
                bookingId = b.id,
                apartmentId = b.apartmentId,
                apartmentName = names[b.apartmentId] ?: "",
                guestName = b.guestName,
                checkIn = b.checkIn,
                checkOut = b.checkOut,
            )
        }
    }

    /** Create a new reservation. Silently ignores invalid input (blank guest / bad date range). */
    fun addBooking(draft: BookingDraft) = saveBooking(BookingId(newId()), draft)

    /** Overwrite an existing reservation (matched by [bookingId]). */
    fun updateBooking(bookingId: BookingId, draft: BookingDraft) = saveBooking(bookingId, draft)

    fun deleteBooking(bookingId: BookingId) {
        viewModelScope.launch { repository.deleteBooking(bookingId) }
    }

    private fun saveBooking(bookingId: BookingId, draft: BookingDraft) {
        val name = draft.guestName.trim()
        if (name.isEmpty() || draft.checkOut < draft.checkIn) return
        viewModelScope.launch {
            repository.upsertBooking(
                Booking(
                    id = bookingId,
                    apartmentId = draft.apartmentId,
                    guestName = name,
                    checkIn = draft.checkIn,
                    checkOut = draft.checkOut,
                    upfrontPayment = draft.upfrontPayment,
                    restPayment = draft.restPayment,
                    notes = draft.notes.trim(),
                    contactInfo = draft.contactInfo.trim(),
                    country = draft.country.trim(),
                ),
            )
        }
    }

    private companion object {
        // Placeholder id for the draft when adding (not yet persisted) so it never matches a real row.
        val CANDIDATE_ID = BookingId("__candidate__")
    }

    private fun MonthSchedule.toUiState(selected: LocalDate?): CalendarUiState =
        CalendarUiState(
            yearMonth = yearMonth,
            monthLabel = CalendarLabels.monthLabel(yearMonth),
            weekdayLabels = CalendarLabels.weekdayLabels(weekStart),
            weeks = weeks,
            today = today,
            selectedDay = selected?.let { day(it) },
            isLoading = false,
        )
}
