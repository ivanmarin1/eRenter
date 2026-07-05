package com.vacation.feature.calendar.domain.usecase

import com.vacation.feature.calendar.domain.model.Apartment
import com.vacation.feature.calendar.domain.model.ImportPreview
import com.vacation.feature.calendar.domain.model.ImportRow
import kotlinx.datetime.LocalDate

/**
 * Turns pasted text into a validated [ImportPreview]. Pure and framework-free, so it is
 * trivially unit-testable and identical on every platform.
 *
 * Accepted input: one booking per line, four columns in order
 * `apartment, guest, check-in, check-out`. Columns may be separated by tabs (as produced by
 * copying from a spreadsheet) or commas. Dates must be ISO `yyyy-MM-dd`. Blank lines and lines
 * starting with `#` are ignored, as is an optional header row beginning with "apartment".
 */
class ParseBookingImportUseCase {

    fun parse(rawText: String, existingApartments: List<Apartment>): ImportPreview {
        val existingNames = existingApartments.mapTo(HashSet()) { it.name.trim().lowercase() }
        val rows = mutableListOf<ImportRow>()
        // Preserve first-seen casing and order of apartment names to be created.
        val newNames = LinkedHashMap<String, String>()

        rawText.lineSequence().forEachIndexed { index, rawLine ->
            val trimmed = rawLine.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#")) return@forEachIndexed

            val cells = splitColumns(rawLine)
            if (cells.firstOrNull()?.trim()?.lowercase() == "apartment") return@forEachIndexed // header

            val lineNumber = index + 1
            val apartment = cells.getOrNull(0)?.trim().orEmpty()
            val guest = cells.getOrNull(1)?.trim().orEmpty()
            val checkInRaw = cells.getOrNull(2)?.trim().orEmpty()
            val checkOutRaw = cells.getOrNull(3)?.trim().orEmpty()

            val checkIn = parseDate(checkInRaw)
            val checkOut = parseDate(checkOutRaw)

            val error = when {
                cells.size < 4 ->
                    "Expected 4 columns (apartment, guest, check-in, check-out)"
                apartment.isEmpty() -> "Missing apartment name"
                guest.isEmpty() -> "Missing guest name"
                checkIn == null -> "Invalid check-in date \"$checkInRaw\" (use YYYY-MM-DD)"
                checkOut == null -> "Invalid check-out date \"$checkOutRaw\" (use YYYY-MM-DD)"
                checkOut < checkIn -> "Check-out ($checkOutRaw) is before check-in ($checkInRaw)"
                else -> null
            }

            if (error == null) {
                val key = apartment.lowercase()
                if (key !in existingNames && key !in newNames) newNames[key] = apartment
            }

            rows += ImportRow(lineNumber, apartment, guest, checkIn, checkOut, error)
        }

        return ImportPreview(rows = rows, newApartmentNames = newNames.values.toList())
    }

    private fun splitColumns(line: String): List<String> =
        if (line.contains('\t')) line.split('\t') else line.split(',')

    private fun parseDate(value: String): LocalDate? =
        try {
            if (value.isEmpty()) null else LocalDate.parse(value)
        } catch (_: IllegalArgumentException) {
            null
        }
}
