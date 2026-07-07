package com.vacation.feature.calendar.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vacation.feature.calendar.domain.model.Apartment
import com.vacation.feature.calendar.domain.model.ApartmentId
import com.vacation.feature.calendar.domain.model.Booking
import com.vacation.feature.calendar.domain.model.BookingId
import com.vacation.feature.calendar.domain.model.BookingSummary
import com.vacation.feature.calendar.domain.model.YearMonth
import com.vacation.feature.calendar.domain.repository.BookingRepository
import com.vacation.feature.calendar.domain.usecase.ApartmentAvailabilityBuilder
import com.vacation.feature.calendar.domain.usecase.DetectOverbookingsUseCase
import com.vacation.feature.calendar.domain.usecase.MiniMonthBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * Drives the availability screen. Two scopes: **per apartment** (one apartment across a month or a
 * whole year, coloured available / booked / overbooked, with its conflicts) and **per month** (one
 * month as a matrix of every apartment). Also hosts adding a reservation.
 */
class ApartmentAvailabilityViewModel(
    private val repository: BookingRepository,
    private val availabilityBuilder: ApartmentAvailabilityBuilder,
    private val detectOverbookings: DetectOverbookingsUseCase,
    private val miniMonthBuilder: MiniMonthBuilder,
    private val clock: Clock = Clock.System,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) : ViewModel() {

    private val today: LocalDate = clock.todayIn(timeZone)
    private val visibleMonth = MutableStateFlow(YearMonth.of(today))
    private val selectedApartmentId = MutableStateFlow<ApartmentId?>(null)
    private val scope = MutableStateFlow(AvailabilityScope.PerApartment)
    private val viewMode = MutableStateFlow(CalendarViewMode.Month)

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

    private data class Controls(
        val apartmentId: ApartmentId?,
        val scope: AvailabilityScope,
        val viewMode: CalendarViewMode,
    )

    val uiState: StateFlow<ApartmentAvailabilityUiState> =
        combine(
            combine(selectedApartmentId, scope, viewMode) { a, s, v -> Controls(a, s, v) },
            visibleMonth,
            repository.observeBookings(),
            apartments,
        ) { controls, month, bookings, apartments ->
            build(controls, month, bookings, apartments)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ApartmentAvailabilityUiState.empty(
                CalendarLabels.monthLabel(visibleMonth.value),
                today,
                hasApartments = false,
                isLoading = true,
            ),
        )

    private fun build(
        controls: Controls,
        month: YearMonth,
        bookings: List<Booking>,
        apartments: List<Apartment>,
    ): ApartmentAvailabilityUiState {
        val label = CalendarLabels.monthLabel(month)
        val yearLabel = month.year.toString()
        if (apartments.isEmpty()) {
            return ApartmentAvailabilityUiState.empty(label, today, hasApartments = false, isLoading = false)
        }

        // Per-month matrix: every apartment as a mini month, no apartment selection needed.
        if (controls.scope == AvailabilityScope.PerMonth) {
            val matrix = apartments.map { apt ->
                MiniMonthUi(
                    yearMonth = month,
                    label = apt.name,
                    cells = miniMonthBuilder.availabilityMonth(month, apt.id, bookings, today).cells,
                )
            }
            return ApartmentAvailabilityUiState(
                hasApartments = true,
                apartmentId = controls.apartmentId,
                apartmentName = "",
                monthLabel = label,
                weekdayLabels = emptyList(),
                weeks = emptyList(),
                conflicts = emptyList(),
                today = today,
                isLoading = false,
                scope = AvailabilityScope.PerMonth,
                viewMode = controls.viewMode,
                yearLabel = yearLabel,
                overview = matrix,
            )
        }

        val apartmentId = controls.apartmentId
            ?: return ApartmentAvailabilityUiState.empty(label, today, hasApartments = true, isLoading = false)
        val name = apartments.firstOrNull { it.id == apartmentId }?.name ?: ""
        val conflicts = detectOverbookings.conflictingBookings(bookings, apartmentId).toConflicts()

        // Per-apartment year overview.
        if (controls.viewMode == CalendarViewMode.Year) {
            val minis = miniMonthBuilder.availabilityYear(month.year, apartmentId, bookings, today).map {
                MiniMonthUi(it.yearMonth, CalendarLabels.shortMonthLabel(it.yearMonth), it.cells)
            }
            return ApartmentAvailabilityUiState(
                hasApartments = true,
                apartmentId = apartmentId,
                apartmentName = name,
                monthLabel = label,
                weekdayLabels = emptyList(),
                weeks = emptyList(),
                conflicts = conflicts,
                today = today,
                isLoading = false,
                scope = AvailabilityScope.PerApartment,
                viewMode = CalendarViewMode.Year,
                yearLabel = yearLabel,
                overview = minis,
            )
        }

        // Per-apartment month grid (the original view).
        val availability = availabilityBuilder.build(apartmentId, month, bookings)
        return ApartmentAvailabilityUiState(
            hasApartments = true,
            apartmentId = apartmentId,
            apartmentName = name,
            monthLabel = label,
            weekdayLabels = CalendarLabels.weekdayLabels(availability.weekStart),
            weeks = availability.weeks,
            conflicts = conflicts,
            today = today,
            isLoading = false,
            scope = AvailabilityScope.PerApartment,
            viewMode = CalendarViewMode.Month,
            yearLabel = yearLabel,
            overview = emptyList(),
        )
    }

    fun selectApartment(id: ApartmentId) { selectedApartmentId.value = id }
    fun setScope(newScope: AvailabilityScope) { scope.value = newScope }
    fun setViewMode(mode: CalendarViewMode) { viewMode.value = mode }

    fun previousMonth() = step(forward = false)
    fun nextMonth() = step(forward = true)

    private fun step(forward: Boolean) {
        val byYear = scope.value == AvailabilityScope.PerApartment && viewMode.value == CalendarViewMode.Year
        visibleMonth.update {
            when {
                byYear -> YearMonth(if (forward) it.year + 1 else it.year - 1, it.month)
                forward -> it.next()
                else -> it.previous()
            }
        }
    }

    fun goToToday() {
        visibleMonth.value = YearMonth.of(today)
        viewMode.value = CalendarViewMode.Month
    }

    /** Tap a month in the per-apartment year overview: open it in month view. */
    fun openMonth(yearMonth: YearMonth) {
        visibleMonth.value = yearMonth
        viewMode.value = CalendarViewMode.Month
    }

    /** Existing reservations that would clash with [draft] (empty when safe to save). */
    fun conflictsFor(draft: BookingDraft, editingId: BookingId?): List<BookingSummary> =
        detectOverbookings.conflictSummaries(draft, editingId, bookings.value, apartments.value)

    /** Create a new reservation. Silently ignores invalid input (blank guest / bad date range). */
    fun addBooking(draft: BookingDraft) {
        if (draft.guestName.isBlank() || draft.checkOut < draft.checkIn) return
        viewModelScope.launch { repository.upsertBooking(draft.toBooking(BookingId(newId()))) }
    }

    private fun List<Booking>.toConflicts(): List<AvailabilityConflict> =
        map { AvailabilityConflict(it.guestName, it.checkIn, it.checkOut) }
}
