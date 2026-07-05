package com.vacation.feature.calendar.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vacation.feature.calendar.domain.model.Apartment
import com.vacation.feature.calendar.domain.model.ApartmentId
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/**
 * Add/edit form for a single reservation: pick the apartment, type the guest, and choose
 * check-in / check-out with the Material date picker. Used for both create and edit — the
 * caller supplies the initial values and the confirm-button label.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDialog(
    title: String,
    apartments: List<Apartment>,
    initialApartmentId: ApartmentId?,
    initialGuestName: String,
    initialCheckIn: LocalDate,
    initialCheckOut: LocalDate,
    confirmLabel: String,
    onConfirm: (ApartmentId, String, LocalDate, LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    var apartmentId by remember {
        mutableStateOf(initialApartmentId ?: apartments.firstOrNull()?.id)
    }
    var guest by remember { mutableStateOf(initialGuestName) }
    var checkIn by remember { mutableStateOf(initialCheckIn) }
    var checkOut by remember { mutableStateOf(initialCheckOut) }

    val datesValid = checkOut > checkIn
    val canSave = apartmentId != null && guest.isNotBlank() && datesValid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ApartmentDropdown(
                    apartments = apartments,
                    selectedId = apartmentId,
                    onSelect = { apartmentId = it },
                )
                OutlinedTextField(
                    value = guest,
                    onValueChange = { guest = it },
                    label = { Text("Guest name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                DateField(
                    label = "Check-in",
                    date = checkIn,
                    onPick = { picked ->
                        checkIn = picked
                        // Keep the range valid: nudge check-out to the day after if needed.
                        if (checkOut <= picked) checkOut = picked.plus(1, DateTimeUnit.DAY)
                    },
                )
                DateField(label = "Check-out", date = checkOut, onPick = { checkOut = it })
                if (!datesValid) {
                    Text(
                        "Check-out must be after check-in.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { apartmentId?.let { onConfirm(it, guest, checkIn, checkOut) } },
                enabled = canSave,
            ) { Text(confirmLabel) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApartmentDropdown(
    apartments: List<Apartment>,
    selectedId: ApartmentId?,
    onSelect: (ApartmentId) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = apartments.firstOrNull { it.id == selectedId }?.name ?: ""

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Apartment") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            apartments.forEach { apt ->
                DropdownMenuItem(
                    text = { Text(apt.name) },
                    onClick = { onSelect(apt.id); expanded = false },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(label: String, date: LocalDate, onPick: (LocalDate) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }

    OutlinedButton(onClick = { showPicker = true }, modifier = Modifier.fillMaxWidth()) {
        Text("$label:  ${formatDate(date)}")
    }

    if (showPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = date.toUtcMillis())
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { onPick(localDateFromUtcMillis(it)) }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Cancel") } },
        ) {
            DatePicker(state = state)
        }
    }
}

private fun formatDate(date: LocalDate): String =
    "${date.dayOfMonth}.${date.month.number}.${date.year}."

private fun LocalDate.toUtcMillis(): Long =
    atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

private fun localDateFromUtcMillis(millis: Long): LocalDate =
    Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date
