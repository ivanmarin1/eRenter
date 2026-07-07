package com.vacation.feature.calendar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vacation.core.designsystem.theme.VacationDesign
import com.vacation.feature.calendar.domain.model.Apartment
import com.vacation.feature.calendar.domain.model.ApartmentId
import com.vacation.feature.calendar.domain.model.BookingId
import com.vacation.feature.calendar.domain.model.BookingSummary
import com.vacation.feature.calendar.domain.model.YearMonth
import com.vacation.feature.calendar.presentation.ApartmentAvailabilityUiState
import com.vacation.feature.calendar.presentation.ApartmentAvailabilityViewModel
import com.vacation.feature.calendar.presentation.AvailabilityConflict
import com.vacation.feature.calendar.presentation.AvailabilityScope
import com.vacation.feature.calendar.presentation.BookingDraft
import com.vacation.feature.calendar.presentation.CalendarViewMode
import com.vacation.feature.calendar.ui.component.AddReservationFab
import com.vacation.feature.calendar.ui.component.ApartmentDropdown
import com.vacation.feature.calendar.ui.component.AvailabilityDayCell
import com.vacation.feature.calendar.ui.component.MiniMonthGrid
import com.vacation.feature.calendar.ui.component.MiniMonthPalette
import com.vacation.feature.calendar.ui.component.MonthHeader
import com.vacation.feature.calendar.ui.component.SegmentedControl
import com.vacation.feature.calendar.ui.component.WeekdayRow
import com.vacation.feature.calendar.ui.component.swipeToChangePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import kotlinx.datetime.plus
import org.koin.compose.viewmodel.koinViewModel

/** Reusable entry point: wires [ApartmentAvailabilityViewModel] via Koin. */
@Composable
fun ApartmentCalendarRoute(
    modifier: Modifier = Modifier,
    viewModel: ApartmentAvailabilityViewModel = koinViewModel(),
) {
    val apartments by viewModel.apartments.collectAsStateWithLifecycle()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ApartmentCalendarScreen(
        apartments = apartments,
        state = state,
        onSelectApartment = viewModel::selectApartment,
        onSetScope = viewModel::setScope,
        onSetViewMode = viewModel::setViewMode,
        onOpenMonth = viewModel::openMonth,
        onPrevious = viewModel::previousMonth,
        onNext = viewModel::nextMonth,
        onToday = viewModel::goToToday,
        conflictsFor = viewModel::conflictsFor,
        onAddBooking = viewModel::addBooking,
        modifier = modifier,
    )
}

@Composable
fun ApartmentCalendarScreen(
    apartments: List<Apartment>,
    state: ApartmentAvailabilityUiState,
    onSelectApartment: (ApartmentId) -> Unit,
    onSetScope: (AvailabilityScope) -> Unit,
    onSetViewMode: (CalendarViewMode) -> Unit,
    onOpenMonth: (YearMonth) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
    conflictsFor: (BookingDraft, BookingId?) -> List<BookingSummary>,
    onAddBooking: (BookingDraft) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAdd by remember { mutableStateOf(false) }
    val perApartment = state.scope == AvailabilityScope.PerApartment
    val isYear = state.viewMode == CalendarViewMode.Year

    Box(modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .swipeToChangePeriod(onPrevious = onPrevious, onNext = onNext)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MonthHeader(
                monthLabel = when {
                    !perApartment -> state.monthLabel
                    isYear -> state.apartmentName.ifBlank { state.yearLabel }
                    else -> state.monthLabel
                },
                onPrevious = onPrevious,
                onNext = onNext,
                onToday = onToday,
                eyebrow = "Availability",
            )

            SegmentedControl(
                options = listOf("Per apartment", "Per month"),
                selectedIndex = if (perApartment) 0 else 1,
                onSelect = { index ->
                    onSetScope(if (index == 0) AvailabilityScope.PerApartment else AvailabilityScope.PerMonth)
                },
            )

            if (!state.hasApartments) {
                Text(
                    "No apartments yet — add one in the Apartments tab to see its availability.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                return@Column
            }

            if (perApartment) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ApartmentDropdown(
                        apartments = apartments,
                        selectedId = state.apartmentId,
                        onSelect = onSelectApartment,
                        modifier = Modifier.weight(1f),
                    )
                    SegmentedControl(
                        options = listOf("Month", "Year"),
                        selectedIndex = if (isYear) 1 else 0,
                        onSelect = { index ->
                            onSetViewMode(if (index == 1) CalendarViewMode.Year else CalendarViewMode.Month)
                        },
                    )
                }
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Column
            }

            when {
                // Per-month matrix or per-apartment year overview: a 2-column grid of mini months.
                !perApartment || isYear -> {
                    state.overview.chunked(2).forEach { rowMonths ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            rowMonths.forEach { mini ->
                                MiniMonthGrid(
                                    label = mini.label,
                                    cells = mini.cells,
                                    palette = MiniMonthPalette.Availability,
                                    onClick = if (perApartment) ({ onOpenMonth(mini.yearMonth) }) else null,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (rowMonths.size == 1) Box(Modifier.weight(1f))
                        }
                    }
                    AvailabilityLegend()
                }
                // Per-apartment month grid.
                else -> {
                    WeekdayRow(labels = state.weekdayLabels)
                    state.weeks.forEach { week ->
                        Row(Modifier.fillMaxWidth()) {
                            week.forEach { day ->
                                AvailabilityDayCell(day = day, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                    AvailabilityLegend()
                    if (state.conflicts.isNotEmpty()) {
                        ConflictsCard(state.conflicts)
                    }
                }
            }
        }

        val addApartmentId = state.apartmentId
        if (!state.isLoading && perApartment && addApartmentId != null) {
            AddReservationFab(
                onClick = { showAdd = true },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            )
        }
    }

    val addApartmentId = state.apartmentId
    if (showAdd && addApartmentId != null) {
        BookingDialog(
            title = "Add reservation",
            apartments = apartments,
            initial = BookingDraft(
                apartmentId = addApartmentId,
                guestName = "",
                contactInfo = "",
                country = "",
                checkIn = state.today,
                checkOut = state.today.plus(1, DateTimeUnit.DAY),
                upfrontPayment = null,
                restPayment = null,
                notes = "",
            ),
            confirmLabel = "Add",
            conflictsFor = { draft -> conflictsFor(draft, null) },
            onConfirm = { draft -> onAddBooking(draft); showAdd = false },
            onDismiss = { showAdd = false },
        )
    }
}

@Composable
private fun AvailabilityLegend() {
    val colors = VacationDesign.calendarColors
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        LegendSwatch(colors.availableContainer, "Available")
        LegendSwatch(colors.bookedContainer, "Booked")
        LegendSwatch(colors.overbookedContainer, "Overbooked")
    }
}

@Composable
private fun LegendSwatch(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(14.dp).clip(RoundedCornerShape(4.dp)).background(color))
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ConflictsCard(conflicts: List<AvailabilityConflict>) {
    val colors = VacationDesign.calendarColors
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.overbookedContainer.copy(alpha = 0.14f)),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "Overbooking conflicts",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colors.overbookedContainer,
            )
            conflicts.forEach { c ->
                Text(
                    "${c.guestName}:  ${formatRange(c.checkIn, c.checkOut)}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

private fun formatRange(from: LocalDate, to: LocalDate): String =
    "${from.dayOfMonth}.${from.month.number}. → ${to.dayOfMonth}.${to.month.number}."
