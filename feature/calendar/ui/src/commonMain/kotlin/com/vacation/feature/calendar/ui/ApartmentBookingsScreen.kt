package com.vacation.feature.calendar.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vacation.core.designsystem.theme.VacationDesign
import com.vacation.feature.calendar.domain.model.Apartment
import com.vacation.feature.calendar.domain.model.ApartmentId
import com.vacation.feature.calendar.domain.model.BookingId
import com.vacation.feature.calendar.domain.model.BookingSummary
import com.vacation.feature.calendar.presentation.ApartmentBookingRow
import com.vacation.feature.calendar.presentation.ApartmentBookingsUiState
import com.vacation.feature.calendar.presentation.ApartmentBookingsViewModel
import com.vacation.feature.calendar.presentation.ApartmentStats
import com.vacation.feature.calendar.presentation.BookingDraft
import com.vacation.feature.calendar.presentation.BookingStatus
import com.vacation.feature.calendar.ui.component.ApartmentDropdown
import org.koin.compose.viewmodel.koinViewModel

/** Reusable entry point: wires [ApartmentBookingsViewModel] via Koin. */
@Composable
fun ApartmentBookingsRoute(
    modifier: Modifier = Modifier,
    viewModel: ApartmentBookingsViewModel = koinViewModel(),
) {
    val apartments by viewModel.apartments.collectAsStateWithLifecycle()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ApartmentBookingsScreen(
        apartments = apartments,
        state = state,
        onSelectApartment = viewModel::selectApartment,
        onAddApartment = viewModel::addApartment,
        onRenameApartment = viewModel::renameApartment,
        onDeleteApartment = viewModel::deleteApartment,
        conflictsFor = viewModel::conflictsFor,
        onUpdateBooking = viewModel::updateBooking,
        onDeleteBooking = viewModel::deleteBooking,
        modifier = modifier,
    )
}

@Composable
fun ApartmentBookingsScreen(
    apartments: List<Apartment>,
    state: ApartmentBookingsUiState,
    onSelectApartment: (ApartmentId) -> Unit,
    onAddApartment: (String) -> Unit,
    onRenameApartment: (ApartmentId, String) -> Unit,
    onDeleteApartment: (ApartmentId) -> Unit,
    conflictsFor: (BookingDraft, BookingId?) -> List<BookingSummary>,
    onUpdateBooking: (BookingId, BookingDraft) -> Unit,
    onDeleteBooking: (BookingId) -> Unit,
    modifier: Modifier = Modifier,
) {
    var editTarget by remember { mutableStateOf<BookingSummary?>(null) }
    var deleteTarget by remember { mutableStateOf<BookingSummary?>(null) }
    var showAddApartment by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<Apartment?>(null) }
    var deleteApartmentTarget by remember { mutableStateOf<Apartment?>(null) }
    val expanded = remember { mutableStateListOf<String>() }

    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Header: eyebrow + apartment title + add-apartment action.
        Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    "APARTMENTS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    state.apartmentName.ifBlank { "No apartment" },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Surface(
                onClick = { showAddApartment = true },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text(
                    "＋ Apartment",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                )
            }
        }

        if (!state.hasApartments) {
            EmptyCard(
                title = "No apartments yet",
                subtitle = "Add one above, or paste bookings in the Import tab to create them automatically.",
            )
            return@Column
        }

        ApartmentDropdown(
            apartments = apartments,
            selectedId = state.apartmentId,
            onSelect = onSelectApartment,
            modifier = Modifier.fillMaxWidth(),
        )

        // Manage the selected apartment.
        state.apartmentId?.let { id ->
            val current = apartments.firstOrNull { it.id == id }
            if (current != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Rename",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { renameTarget = current }.padding(vertical = 2.dp),
                    )
                    Text(
                        "Delete apartment",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.clickable { deleteApartmentTarget = current }.padding(vertical = 2.dp),
                    )
                }
            }
        }

        StatsRow(state.stats)

        if (state.bookings.isEmpty()) {
            EmptyCard(
                title = "No bookings yet",
                subtitle = "Tap + on the Calendar or Availability tab to add one.",
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                items(state.bookings, key = { it.summary.bookingId.value }) { row ->
                    val id = row.summary.bookingId.value
                    BookingCard(
                        row = row,
                        expanded = id in expanded,
                        onToggle = { if (id in expanded) expanded.remove(id) else expanded.add(id) },
                        onEdit = { editTarget = row.summary },
                        onDelete = { deleteTarget = row.summary },
                    )
                }
            }
        }
    }

    // ---- edit reservation ----
    editTarget?.let { s ->
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
            onConfirm = { draft -> onUpdateBooking(s.bookingId, draft); editTarget = null },
            onDismiss = { editTarget = null },
        )
    }

    deleteTarget?.let { s ->
        ConfirmDialog(
            title = "Delete reservation?",
            message = "Remove ${s.guestName}'s stay in ${s.apartmentName}? This cannot be undone.",
            confirmLabel = "Delete",
            onConfirm = { onDeleteBooking(s.bookingId); deleteTarget = null },
            onDismiss = { deleteTarget = null },
        )
    }

    // ---- apartment management ----
    if (showAddApartment) {
        ApartmentNameDialog(
            title = "New apartment",
            initialValue = "",
            confirmLabel = "Add",
            onConfirm = { onAddApartment(it); showAddApartment = false },
            onDismiss = { showAddApartment = false },
        )
    }
    renameTarget?.let { target ->
        ApartmentNameDialog(
            title = "Rename apartment",
            initialValue = target.name,
            confirmLabel = "Save",
            onConfirm = { onRenameApartment(target.id, it); renameTarget = null },
            onDismiss = { renameTarget = null },
        )
    }
    deleteApartmentTarget?.let { target ->
        ConfirmDialog(
            title = "Delete \"${target.name}\"?",
            message = "This removes the apartment and all of its bookings. This cannot be undone.",
            confirmLabel = "Delete",
            onConfirm = { onDeleteApartment(target.id); deleteApartmentTarget = null },
            onDismiss = { deleteApartmentTarget = null },
        )
    }
}

@Composable
private fun StatsRow(stats: ApartmentStats) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatCard(stats.count.toString(), "Bookings", Modifier.weight(1f))
        StatCard(stats.nights.toString(), "Nights booked", Modifier.weight(1f), value = VacationDesign.calendarColors.arrival)
        StatCard(stats.revenue, "Expected", Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(number: String, label: String, modifier: Modifier = Modifier, value: Color = MaterialTheme.colorScheme.onSurface) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            Modifier.padding(vertical = 12.dp, horizontal = 8.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(number, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = value, maxLines = 1)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

@Composable
private fun BookingCard(
    row: ApartmentBookingRow,
    expanded: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(13.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(
                Modifier.fillMaxWidth().clickable(onClick = onToggle),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        row.summary.guestName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        row.dateLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                StatusChip(row.status)
                Text(
                    if (expanded) "▴" else "▾",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    HorizontalDivider(
                        Modifier.padding(vertical = 10.dp),
                        color = MaterialTheme.colorScheme.outline,
                    )
                    val meta = listOfNotNull(
                        row.summary.country.takeIf { it.isNotBlank() },
                        row.summary.contactInfo.takeIf { it.isNotBlank() },
                    ).joinToString(" · ")
                    if (meta.isNotBlank()) {
                        Text(meta, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Text(
                        row.paymentLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                    if (row.summary.notes.isNotBlank()) {
                        Text(
                            "“${row.summary.notes}”",
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                    Row(
                        Modifier.fillMaxWidth().padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f)) { Text("Edit") }
                        OutlinedButton(onClick = onDelete) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: BookingStatus) {
    val calendar = VacationDesign.calendarColors
    val (bg, fg, label) = when (status) {
        BookingStatus.Past -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, "Past")
        BookingStatus.Staying -> Triple(calendar.arrivalContainer, calendar.arrival, "Staying")
        BookingStatus.Upcoming -> Triple(calendar.departureContainer, calendar.departure, "Upcoming")
    }
    Surface(shape = RoundedCornerShape(20.dp), color = bg) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = fg,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
        )
    }
}

@Composable
private fun EmptyCard(title: String, subtitle: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            Modifier.padding(vertical = 40.dp, horizontal = 20.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
