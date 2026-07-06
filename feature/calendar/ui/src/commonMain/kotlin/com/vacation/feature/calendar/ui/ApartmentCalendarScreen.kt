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
import com.vacation.feature.calendar.presentation.ApartmentAvailabilityUiState
import com.vacation.feature.calendar.presentation.ApartmentAvailabilityViewModel
import com.vacation.feature.calendar.presentation.AvailabilityConflict
import com.vacation.feature.calendar.ui.component.ApartmentDropdown
import com.vacation.feature.calendar.ui.component.AvailabilityDayCell
import com.vacation.feature.calendar.ui.component.MonthHeader
import com.vacation.feature.calendar.ui.component.WeekdayRow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
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
        onPrevious = viewModel::previousMonth,
        onNext = viewModel::nextMonth,
        onToday = viewModel::goToToday,
        modifier = modifier,
    )
}

@Composable
fun ApartmentCalendarScreen(
    apartments: List<Apartment>,
    state: ApartmentAvailabilityUiState,
    onSelectApartment: (ApartmentId) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Availability by apartment", style = MaterialTheme.typography.titleLarge)

        if (!state.hasApartments) {
            Text(
                "No apartments yet — add one in the Apartments tab to see its availability.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@Column
        }

        ApartmentDropdown(
            apartments = apartments,
            selectedId = state.apartmentId,
            onSelect = onSelectApartment,
            modifier = Modifier.fillMaxWidth(),
        )

        MonthHeader(
            monthLabel = state.monthLabel,
            onPrevious = onPrevious,
            onNext = onNext,
            onToday = onToday,
        )

        if (state.isLoading) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

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
                color = colors.booked,
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
