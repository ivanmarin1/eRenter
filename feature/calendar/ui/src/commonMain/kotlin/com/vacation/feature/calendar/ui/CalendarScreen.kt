package com.vacation.feature.calendar.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vacation.feature.calendar.domain.model.Apartment
import com.vacation.feature.calendar.domain.model.BookingId
import com.vacation.feature.calendar.domain.model.BookingSummary
import com.vacation.feature.calendar.domain.model.DaySchedule
import com.vacation.feature.calendar.presentation.BookingDraft
import com.vacation.feature.calendar.presentation.CalendarEvent
import com.vacation.feature.calendar.presentation.CalendarUiState
import com.vacation.feature.calendar.presentation.CalendarViewMode
import com.vacation.feature.calendar.ui.component.AddReservationFab
import com.vacation.feature.calendar.ui.component.DayCell
import com.vacation.feature.calendar.ui.component.DayDetails
import com.vacation.feature.calendar.ui.component.Legend
import com.vacation.feature.calendar.ui.component.MiniMonthGrid
import com.vacation.feature.calendar.ui.component.MiniMonthPalette
import com.vacation.feature.calendar.ui.component.MonthHeader
import com.vacation.feature.calendar.ui.component.SegmentedControl
import com.vacation.feature.calendar.ui.component.WeekdayRow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/** Which reservation editor is open, if any. */
private sealed interface BookingEditor {
    data class Add(val date: LocalDate) : BookingEditor
    data class Edit(val summary: BookingSummary) : BookingEditor
}

/**
 * Stateless as far as calendar data goes — it takes a [CalendarUiState] and emits
 * [CalendarEvent]s — but it holds the local UI state for the add/edit/delete dialogs, the
 * same way [ManageApartmentsScreen] does.
 */
@Composable
fun CalendarScreen(
    state: CalendarUiState,
    apartments: List<Apartment>,
    conflictedBookingIds: Set<BookingId>,
    conflictsFor: (BookingDraft, BookingId?) -> List<BookingSummary>,
    onEvent: (CalendarEvent) -> Unit,
    onAddBooking: (BookingDraft) -> Unit,
    onUpdateBooking: (BookingId, BookingDraft) -> Unit,
    onDeleteBooking: (BookingId) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var editor by remember { mutableStateOf<BookingEditor?>(null) }
    var deleteTarget by remember { mutableStateOf<BookingSummary?>(null) }

    Box(modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val isYear = state.viewMode == CalendarViewMode.Year
        MonthHeader(
            monthLabel = if (isYear) state.yearLabel else state.monthLabel,
            onPrevious = { onEvent(CalendarEvent.PreviousMonth) },
            onNext = { onEvent(CalendarEvent.NextMonth) },
            onToday = { onEvent(CalendarEvent.GoToToday) },
            eyebrow = "Calendar",
        )

        if (state.isLoading) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SegmentedControl(
                options = listOf("Month", "Year"),
                selectedIndex = if (isYear) 1 else 0,
                onSelect = { index ->
                    onEvent(CalendarEvent.SetViewMode(if (index == 1) CalendarViewMode.Year else CalendarViewMode.Month))
                },
            )
            if (!isYear) {
                val active = isExpanded
                Surface(
                    onClick = { isExpanded = !isExpanded },
                    shape = RoundedCornerShape(11.dp),
                    color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        text = "Detail",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    )
                }
            }
        }

        if (isYear) {
            state.miniMonths.chunked(2).forEach { rowMonths ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    rowMonths.forEach { mini ->
                        MiniMonthGrid(
                            label = mini.label,
                            cells = mini.cells,
                            palette = MiniMonthPalette.Schedule,
                            onClick = { onEvent(CalendarEvent.OpenMonth(mini.yearMonth)) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (rowMonths.size == 1) Box(Modifier.weight(1f))
                }
            }
            return@Column
        }

        WeekdayRow(labels = state.weekdayLabels)

        state.weeks.forEach { week ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                week.forEach { day ->
                    DayCell(
                        day = day,
                        isToday = state.today == day.date,
                        isSelected = state.selectedDay?.date == day.date,
                        onClick = { onEvent(CalendarEvent.SelectDay(day.date)) },
                        modifier = Modifier.weight(1f),
                        expanded = isExpanded,
                    )
                }
            }
        }

        Legend(Modifier.padding(top = 4.dp))
    }

        if (!state.isLoading && apartments.isNotEmpty()) {
            AddReservationFab(
                onClick = { state.today?.let { editor = BookingEditor.Add(state.selectedDay?.date ?: it) } },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            )
        }
    }

    // Tapping a day slides up its detail as a bottom sheet.
    state.selectedDay?.let { day: DaySchedule ->
        DayDetailSheet(
            day = day,
            canAdd = apartments.isNotEmpty(),
            conflictedBookingIds = conflictedBookingIds,
            onAddReservation = { editor = BookingEditor.Add(day.date); onEvent(CalendarEvent.ClearSelection) },
            onEditBooking = { editor = BookingEditor.Edit(it); onEvent(CalendarEvent.ClearSelection) },
            onDeleteBooking = { deleteTarget = it; onEvent(CalendarEvent.ClearSelection) },
            onDismiss = { onEvent(CalendarEvent.ClearSelection) },
        )
    }

    when (val current = editor) {
        is BookingEditor.Add -> {
            val firstApartment = apartments.first()
            BookingDialog(
                title = "Add reservation",
                apartments = apartments,
                initial = BookingDraft(
                    apartmentId = firstApartment.id,
                    guestName = "",
                    contactInfo = "",
                    country = "",
                    checkIn = current.date,
                    checkOut = current.date.plus(1, DateTimeUnit.DAY),
                    upfrontPayment = null,
                    restPayment = null,
                    notes = "",
                ),
                confirmLabel = "Add",
                conflictsFor = { draft -> conflictsFor(draft, null) },
                onConfirm = { draft -> onAddBooking(draft); editor = null },
                onDismiss = { editor = null },
            )
        }
        is BookingEditor.Edit -> {
            val s = current.summary
            BookingDialog(
                title = "Edit reservation",
                apartments = apartments,
                initial = BookingDraft(
                    apartmentId = s.apartmentId,
                    guestName = s.guestName,
                    contactInfo = s.contactInfo,
                    country = s.country,
                    checkIn = s.checkIn,
                    checkOut = s.checkOut,
                    upfrontPayment = s.upfrontPayment,
                    restPayment = s.restPayment,
                    notes = s.notes,
                ),
                confirmLabel = "Save",
                conflictsFor = { draft -> conflictsFor(draft, s.bookingId) },
                onConfirm = { draft -> onUpdateBooking(s.bookingId, draft); editor = null },
                onDismiss = { editor = null },
            )
        }
        null -> Unit
    }

    deleteTarget?.let { target ->
        ConfirmDialog(
            title = "Delete reservation?",
            message = "Remove ${target.guestName}'s stay in ${target.apartmentName}? This cannot be undone.",
            confirmLabel = "Delete",
            onConfirm = { onDeleteBooking(target.bookingId); deleteTarget = null },
            onDismiss = { deleteTarget = null },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayDetailSheet(
    day: DaySchedule,
    canAdd: Boolean,
    conflictedBookingIds: Set<BookingId>,
    onAddReservation: () -> Unit,
    onEditBooking: (BookingSummary) -> Unit,
    onDeleteBooking: (BookingSummary) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp).navigationBarsPadding().padding(bottom = 12.dp)) {
            DayDetails(
                day = day,
                canAdd = canAdd,
                conflictedBookingIds = conflictedBookingIds,
                onAddReservation = onAddReservation,
                onEditBooking = onEditBooking,
                onDeleteBooking = onDeleteBooking,
            )
        }
    }
}
