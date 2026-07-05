package com.vacation.feature.calendar.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vacation.feature.calendar.domain.model.DaySchedule
import com.vacation.feature.calendar.presentation.CalendarEvent
import com.vacation.feature.calendar.presentation.CalendarUiState
import com.vacation.feature.calendar.ui.component.DayCell
import com.vacation.feature.calendar.ui.component.DayDetails
import com.vacation.feature.calendar.ui.component.Legend
import com.vacation.feature.calendar.ui.component.MonthHeader

/**
 * Stateless screen: it takes a [CalendarUiState] and emits [CalendarEvent]s. It holds no
 * business logic, so it can be previewed, snapshot-tested, or swapped for another UI entirely.
 */
@Composable
fun CalendarScreen(
    state: CalendarUiState,
    onEvent: (CalendarEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MonthHeader(
            monthLabel = state.monthLabel,
            onPrevious = { onEvent(CalendarEvent.PreviousMonth) },
            onNext = { onEvent(CalendarEvent.NextMonth) },
            onToday = { onEvent(CalendarEvent.GoToToday) },
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
                    DayCell(
                        day = day,
                        isToday = state.today == day.date,
                        isSelected = state.selectedDay?.date == day.date,
                        onClick = { onEvent(CalendarEvent.SelectDay(day.date)) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        Legend(Modifier.padding(top = 4.dp))

        state.selectedDay?.let { day: DaySchedule ->
            DayDetails(day, Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun WeekdayRow(labels: List<String>) {
    Row(Modifier.fillMaxWidth()) {
        labels.forEach { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
