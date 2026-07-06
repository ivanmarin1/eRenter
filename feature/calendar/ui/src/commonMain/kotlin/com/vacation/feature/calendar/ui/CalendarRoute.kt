package com.vacation.feature.calendar.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vacation.feature.calendar.presentation.CalendarViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * The one composable the host app needs to place the calendar. It wires the ViewModel via
 * Koin and hands the stateless screen its state + intent callback. Reuse in any KMP app by
 * including the calendar modules and dropping this into a screen slot.
 */
@Composable
fun CalendarRoute(
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val apartments by viewModel.apartments.collectAsStateWithLifecycle()
    val conflictedBookingIds by viewModel.conflictedBookingIds.collectAsStateWithLifecycle()
    CalendarScreen(
        state = state,
        apartments = apartments,
        conflictedBookingIds = conflictedBookingIds,
        conflictsFor = viewModel::conflictsFor,
        onEvent = viewModel::onEvent,
        onAddBooking = viewModel::addBooking,
        onUpdateBooking = viewModel::updateBooking,
        onDeleteBooking = viewModel::deleteBooking,
        modifier = modifier,
    )
}
