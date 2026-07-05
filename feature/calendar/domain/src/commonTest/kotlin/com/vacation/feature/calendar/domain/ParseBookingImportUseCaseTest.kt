package com.vacation.feature.calendar.domain

import com.vacation.feature.calendar.domain.model.Apartment
import com.vacation.feature.calendar.domain.model.ApartmentId
import com.vacation.feature.calendar.domain.usecase.ParseBookingImportUseCase
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ParseBookingImportUseCaseTest {

    private val parse = ParseBookingImportUseCase()

    @Test
    fun parsesCommaSeparatedRows() {
        val preview = parse.parse(
            "Apartment 1,Familija Ivić,2026-07-01,2026-07-08",
            existingApartments = emptyList(),
        )
        val row = preview.rows.single()
        assertTrue(row.isValid)
        assertEquals("Apartment 1", row.apartmentName)
        assertEquals("Familija Ivić", row.guestName)
        assertEquals(LocalDate(2026, 7, 1), row.checkIn)
        assertEquals(LocalDate(2026, 7, 8), row.checkOut)
    }

    @Test
    fun parsesTabSeparatedRowsFromSpreadsheets() {
        val preview = parse.parse("Apartment 2\tAna\t2026-07-03\t2026-07-06", emptyList())
        assertEquals(1, preview.validCount)
        assertEquals("Apartment 2", preview.rows.single().apartmentName)
    }

    @Test
    fun skipsHeaderCommentAndBlankLines() {
        val text = """
            # my paper bookings
            apartment,guest,check-in,check-out

            Apartment 1,Ivić,2026-07-01,2026-07-08
        """.trimIndent()
        val preview = parse.parse(text, emptyList())
        assertEquals(1, preview.rows.size)
        assertTrue(preview.rows.single().isValid)
    }

    @Test
    fun allowsSameDayTurnover() {
        val text = """
            Apartment 1,Leaving,2026-07-01,2026-07-08
            Apartment 1,Arriving,2026-07-08,2026-07-13
        """.trimIndent()
        val preview = parse.parse(text, emptyList())
        assertEquals(2, preview.validCount)
        assertEquals(0, preview.errorCount)
    }

    @Test
    fun flagsInvalidDateAndReversedRange() {
        val text = """
            Apartment 1,BadDate,2026-13-40,2026-07-08
            Apartment 1,Reversed,2026-07-10,2026-07-05
        """.trimIndent()
        val preview = parse.parse(text, emptyList())
        assertEquals(0, preview.validCount)
        assertEquals(2, preview.errorCount)
        assertFalse(preview.rows[0].isValid)
        assertNotNull(preview.rows[1].error)
    }

    @Test
    fun flagsMissingColumns() {
        val preview = parse.parse("Apartment 1,OnlyThree,2026-07-01", emptyList())
        assertFalse(preview.rows.single().isValid)
    }

    @Test
    fun reportsNewApartmentsNotYetExisting() {
        val existing = listOf(Apartment(ApartmentId("a1"), "Apartment 1"))
        val text = """
            Apartment 1,Known,2026-07-01,2026-07-08
            Apartment 2,New,2026-07-01,2026-07-08
            apartment 2,DuplicateCaseInsensitive,2026-07-10,2026-07-12
        """.trimIndent()
        val preview = parse.parse(text, existing)
        // "Apartment 1" already exists; "Apartment 2" is new and de-duplicated case-insensitively.
        assertEquals(listOf("Apartment 2"), preview.newApartmentNames)
    }

    @Test
    fun invalidRowsDoNotContributeNewApartments() {
        val text = "Ghost Apartment,Guest,not-a-date,2026-07-08"
        val preview = parse.parse(text, emptyList())
        assertTrue(preview.newApartmentNames.isEmpty())
        assertNull(preview.validRows.firstOrNull())
    }
}
