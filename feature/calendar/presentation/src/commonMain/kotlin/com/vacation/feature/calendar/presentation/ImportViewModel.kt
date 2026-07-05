package com.vacation.feature.calendar.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vacation.feature.calendar.domain.model.Apartment
import com.vacation.feature.calendar.domain.model.ApartmentId
import com.vacation.feature.calendar.domain.model.Booking
import com.vacation.feature.calendar.domain.model.BookingId
import com.vacation.feature.calendar.domain.model.ImportPreview
import com.vacation.feature.calendar.domain.repository.BookingRepository
import com.vacation.feature.calendar.domain.usecase.ParseBookingImportUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Outcome of a confirmed import, surfaced to the user as a summary. */
data class ImportResult(
    val importedBookings: Int,
    val createdApartments: Int,
    val skippedRows: Int,
)

data class ImportUiState(
    val rawText: String = "",
    val autoCreateApartments: Boolean = true,
    val preview: ImportPreview? = null,
    val isImporting: Boolean = false,
    val lastResult: ImportResult? = null,
)

/**
 * Drives the paste-text import: parse → preview (with per-row errors) → confirm. On confirm it
 * optionally creates any unknown apartments, then inserts every valid booking. Nothing is
 * baked in — this is how each host loads their own initial data at runtime.
 */
class ImportViewModel(
    private val repository: BookingRepository,
    private val parse: ParseBookingImportUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ImportUiState())
    val state: StateFlow<ImportUiState> = _state.asStateFlow()

    fun onTextChange(text: String) {
        _state.update { it.copy(rawText = text, preview = null, lastResult = null) }
    }

    fun onAutoCreateChange(enabled: Boolean) {
        _state.update { it.copy(autoCreateApartments = enabled) }
    }

    fun preview() {
        viewModelScope.launch {
            val existing = repository.observeApartments().first()
            val preview = parse.parse(_state.value.rawText, existing)
            _state.update { it.copy(preview = preview, lastResult = null) }
        }
    }

    fun confirmImport() {
        val snapshot = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isImporting = true) }

            val existing = repository.observeApartments().first()
            val preview = snapshot.preview ?: parse.parse(snapshot.rawText, existing)
            val nameToId = existing.associateTo(HashMap()) { it.name.trim().lowercase() to it.id }

            var createdApartments = 0
            if (snapshot.autoCreateApartments) {
                for (name in preview.newApartmentNames) {
                    val id = ApartmentId(newId())
                    repository.upsertApartment(Apartment(id, name))
                    nameToId[name.trim().lowercase()] = id
                    createdApartments++
                }
            }

            var imported = 0
            var skipped = 0
            for (row in preview.validRows) {
                val apartmentId = nameToId[row.apartmentName.trim().lowercase()]
                if (apartmentId == null) {
                    // Unknown apartment and auto-create is off — cannot place this booking.
                    skipped++
                    continue
                }
                repository.upsertBooking(
                    Booking(
                        id = BookingId(newId()),
                        apartmentId = apartmentId,
                        guestName = row.guestName,
                        checkIn = row.checkIn!!,
                        checkOut = row.checkOut!!,
                    ),
                )
                imported++
            }

            _state.update {
                it.copy(
                    isImporting = false,
                    preview = null,
                    rawText = if (imported > 0) "" else it.rawText,
                    lastResult = ImportResult(imported, createdApartments, skipped),
                )
            }
        }
    }
}
