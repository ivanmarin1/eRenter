package com.vacation.feature.calendar.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.vacation.feature.calendar.domain.model.Apartment
import com.vacation.feature.calendar.domain.model.BookingSummary
import com.vacation.feature.calendar.presentation.BookingDraft
import com.vacation.feature.calendar.ui.component.ApartmentDropdown
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/**
 * Add/edit form for a single reservation. Collects apartment, guest, contact info, country,
 * dates (Material date picker) and optional payments/notes, and emits a [BookingDraft]. Used
 * for both create and edit — the caller supplies the [initial] draft and confirm-button label.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDialog(
    title: String,
    apartments: List<Apartment>,
    initial: BookingDraft,
    confirmLabel: String,
    conflictsFor: (BookingDraft) -> List<BookingSummary>,
    onConfirm: (BookingDraft) -> Unit,
    onDismiss: () -> Unit,
) {
    var apartmentId by remember {
        mutableStateOf(
            apartments.firstOrNull { it.id == initial.apartmentId }?.id
                ?: apartments.firstOrNull()?.id,
        )
    }
    var guest by remember { mutableStateOf(initial.guestName) }
    var contact by remember { mutableStateOf(initial.contactInfo) }
    var country by remember { mutableStateOf(initial.country) }
    var checkIn by remember { mutableStateOf(initial.checkIn) }
    var checkOut by remember { mutableStateOf(initial.checkOut) }
    var upfrontText by remember { mutableStateOf(initial.upfrontPayment.toAmountText()) }
    var restText by remember { mutableStateOf(initial.restPayment.toAmountText()) }
    var notes by remember { mutableStateOf(initial.notes) }

    val datesValid = checkOut > checkIn
    val upfrontValid = upfrontText.isBlankAmount() || upfrontText.toAmountOrNull() != null
    val restValid = restText.isBlankAmount() || restText.toAmountOrNull() != null

    val currentDraft = apartmentId?.let { id ->
        BookingDraft(
            apartmentId = id,
            guestName = guest,
            contactInfo = contact,
            country = country,
            checkIn = checkIn,
            checkOut = checkOut,
            upfrontPayment = upfrontText.toAmountOrNull(),
            restPayment = restText.toAmountOrNull(),
            notes = notes,
        )
    }
    // Hard block: an overlapping reservation for the same apartment cannot be saved.
    val conflicts = if (currentDraft != null && datesValid) conflictsFor(currentDraft) else emptyList()
    val canSave = currentDraft != null && guest.isNotBlank() &&
        datesValid && upfrontValid && restValid && conflicts.isEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
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
                OutlinedTextField(
                    value = contact,
                    onValueChange = { contact = it },
                    label = { Text("Contact info (phone / email)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = country,
                    onValueChange = { country = it },
                    label = { Text("Country") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                DateField(
                    label = "Check-in",
                    date = checkIn,
                    onPick = { picked ->
                        checkIn = picked
                        if (checkOut <= picked) checkOut = picked.plus(1, DateTimeUnit.DAY)
                    },
                )
                DateField(label = "Check-out", date = checkOut, onPick = { checkOut = it })
                if (!datesValid) {
                    ErrorText("Check-out must be after check-in.")
                }
                if (conflicts.isNotEmpty()) {
                    ErrorText(
                        "Overbooking — these dates overlap:\n" +
                            conflicts.joinToString("\n") { c ->
                                "• ${c.guestName} (${formatDate(c.checkIn)} → ${formatDate(c.checkOut)})"
                            },
                    )
                }
                AmountField(
                    label = "Upfront payment",
                    text = upfrontText,
                    onChange = { upfrontText = it },
                    isError = !upfrontValid,
                )
                AmountField(
                    label = "Rest of payment",
                    text = restText,
                    onChange = { restText = it },
                    isError = !restValid,
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { currentDraft?.let(onConfirm) },
                enabled = canSave,
            ) { Text(confirmLabel) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun AmountField(label: String, text: String, onChange: (String) -> Unit, isError: Boolean) {
    OutlinedTextField(
        value = text,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        isError = isError,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        supportingText = if (isError) ({ Text("Enter a number, e.g. 150.00") }) else null,
        modifier = Modifier.fillMaxWidth(),
    )
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

@Composable
private fun ErrorText(message: String) {
    Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
}

private fun formatDate(date: LocalDate): String =
    "${date.dayOfMonth}.${date.month.number}.${date.year}."

private fun String.isBlankAmount(): Boolean = trim().isEmpty()

/** Parses "" -> null, otherwise a decimal (accepting a comma as the decimal separator). */
private fun String.toAmountOrNull(): Double? =
    trim().takeIf { it.isNotEmpty() }?.replace(',', '.')?.toDoubleOrNull()

/** Formats a stored amount back into an editable string, dropping a trailing ".0". */
private fun Double?.toAmountText(): String = when {
    this == null -> ""
    this == toLong().toDouble() -> toLong().toString()
    else -> toString()
}

private fun LocalDate.toUtcMillis(): Long =
    atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

private fun localDateFromUtcMillis(millis: Long): LocalDate =
    Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date
