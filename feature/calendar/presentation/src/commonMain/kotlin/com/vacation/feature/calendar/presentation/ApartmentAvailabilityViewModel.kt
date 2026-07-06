package com.vacation.feature.calendar.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vacation.feature.calendar.domain.model.Apartment
import com.vacation.feature.calendar.domain.model.ApartmentId
import com.vacation.feature.calendar.domain.model.Booking
import com.vacation.feature.calendar.domain.model.YearMonth
import com.vacation.feature.calendar.domain.repository.BookingRepository
import com.vacation.feature.calendar.domain.usecase.ApartmentAvailabilityBuilder
import com.vacation.feature.calendar.domain.usecase.DetectOverbookingsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * Drives the per-apartment availability calendar: pick an apartment + month and see every day
 * coloured available / booked / overbooked, plus the list of overlapping reservations.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ApartmentAvailabilityViewModel(
    private val repository: BookingRepository,
    private val availabilityBuilder: ApartmentAvailabilityBuilder,
    private val detectOverbookings: DetectOverbookingsUseCase,
    private val clock: Clock = Clock.System,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) : ViewModel() {

    private val today: LocalDate = clock.todayIn(timeZone)
    private val visibleMonth = MutableStateFlow(YearMonth.of(today))
    private val selectedApartmentId = MutableStateFlow<ApartmentId?>(null)

    val apartments: StateFlow<List<Apartment>> =
        repository.observeApartments()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        // Default to the first apartment once the list arrives, and keep the selection valid.
        apartments.onEach { list ->
            val current = selectedApartmentId.value
            if (current == null || list.none { it.id == current }) {
                selectedApartmentId.value = list.firstOrNull()?.id
            }
        }.launchIn(viewModelScope)
    }

    val uiState: StateFlow<ApartmentAvailabilityUiState> =
        combine(
            selectedApartmentId,
            visibleMonth,
            repository.observeBookings(),
            apartments,
        ) { apartmentId, month, bookings, apartments ->
            val label = CalendarLabels.monthLabel(month)
            if (apartmentId == null) {
                ApartmentAvailabilityUiState.empty(label, hasApartments = apartments.isNotEmpty(), isLoading = false)
            } else {
                val availability = availabilityBuilder.build(apartmentId, month, bookings)
                ApartmentAvailabilityUiState(
                    hasApartments = true,
                    apartmentId = apartmentId,
                    apartmentName = apartments.firstOrNull { it.id == apartmentId }?.name ?: "",
                    monthLabel = label,
                    weekdayLabels = CalendarLabels.weekdayLabels(availability.weekStart),
                    weeks = availability.weeks,
                    conflicts = detectOverbookings.conflictingBookings(bookings, apartmentId).toConflicts(),
                    isLoading = false,
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ApartmentAvailabilityUiState.empty(
                CalendarLabels.monthLabel(visibleMonth.value),
                hasApartments = false,
                isLoading = true,
            ),
        )

    fun selectApartment(id: ApartmentId) { selectedApartmentId.value = id }
    fun previousMonth() { visibleMonth.update { it.previous() } }
    fun nextMonth() { visibleMonth.update { it.next() } }
    fun goToToday() { visibleMonth.value = YearMonth.of(today) }

    private fun List<Booking>.toConflicts(): List<AvailabilityConflict> =
        map { AvailabilityConflict(it.guestName, it.checkIn, it.checkOut) }
}
