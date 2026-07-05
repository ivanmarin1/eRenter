package com.vacation.feature.calendar.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vacation.feature.calendar.domain.model.Apartment
import com.vacation.feature.calendar.domain.model.ApartmentId
import com.vacation.feature.calendar.domain.model.Booking
import com.vacation.feature.calendar.domain.model.BookingId
import com.vacation.feature.calendar.domain.model.MonthSchedule
import com.vacation.feature.calendar.domain.model.YearMonth
import com.vacation.feature.calendar.domain.repository.BookingRepository
import com.vacation.feature.calendar.domain.usecase.GetMonthScheduleUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
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

    /** Create a new reservation. Silently ignores invalid input (blank guest / bad date range). */
    fun addBooking(apartmentId: ApartmentId, guestName: String, checkIn: LocalDate, checkOut: LocalDate) {
        saveBooking(BookingId(newId()), apartmentId, guestName, checkIn, checkOut)
    }

    /** Overwrite an existing reservation (matched by [bookingId]). */
    fun updateBooking(
        bookingId: BookingId,
        apartmentId: ApartmentId,
        guestName: String,
        checkIn: LocalDate,
        checkOut: LocalDate,
    ) {
        saveBooking(bookingId, apartmentId, guestName, checkIn, checkOut)
    }

    fun deleteBooking(bookingId: BookingId) {
        viewModelScope.launch { repository.deleteBooking(bookingId) }
    }

    private fun saveBooking(
        bookingId: BookingId,
        apartmentId: ApartmentId,
        guestName: String,
        checkIn: LocalDate,
        checkOut: LocalDate,
    ) {
        val name = guestName.trim()
        if (name.isEmpty() || checkOut < checkIn) return
        viewModelScope.launch {
            repository.upsertBooking(Booking(bookingId, apartmentId, name, checkIn, checkOut))
        }
    }

    private fun MonthSchedule.toUiState(selected: LocalDate?): CalendarUiState =
        CalendarUiState(
            yearMonth = yearMonth,
            monthLabel = CalendarLabels.monthLabel(yearMonth),
            weekdayLabels = CalendarLabels.weekdayLabels(this),
            weeks = weeks,
            today = today,
            selectedDay = selected?.let { day(it) },
            isLoading = false,
        )
}
