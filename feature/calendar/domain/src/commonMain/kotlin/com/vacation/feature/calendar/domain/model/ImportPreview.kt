package com.vacation.feature.calendar.domain.model

import kotlinx.datetime.LocalDate

/** One parsed line from the pasted import text, valid or flagged with an [error]. */
data class ImportRow(
    val lineNumber: Int,
    val apartmentName: String,
    val guestName: String,
    val checkIn: LocalDate?,
    val checkOut: LocalDate?,
    val error: String?,
) {
    val isValid: Boolean get() = error == null
}

/**
 * The result of parsing pasted import text: every row (so the UI can show errors inline) plus
 * the apartment names that don't yet exist and would be created on confirm.
 */
data class ImportPreview(
    val rows: List<ImportRow>,
    val newApartmentNames: List<String>,
) {
    val validRows: List<ImportRow> get() = rows.filter { it.isValid }
    val validCount: Int get() = validRows.size
    val errorCount: Int get() = rows.count { !it.isValid }
    val hasContent: Boolean get() = rows.isNotEmpty()
}
