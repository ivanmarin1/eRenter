package com.vacation.feature.calendar.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vacation.feature.calendar.domain.model.Apartment
import com.vacation.feature.calendar.domain.model.ApartmentId
import com.vacation.feature.calendar.domain.repository.BookingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Manages the dynamic set of apartments. Hosts can add, rename, and delete apartments; all
 * changes persist through the repository. Deleting an apartment also removes its bookings.
 */
class ApartmentsViewModel(
    private val repository: BookingRepository,
) : ViewModel() {

    val apartments: StateFlow<List<Apartment>> =
        repository.observeApartments()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun add(name: String) {
        val clean = name.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch {
            repository.upsertApartment(Apartment(ApartmentId(newId()), clean))
        }
    }

    fun rename(id: ApartmentId, name: String) {
        val clean = name.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch { repository.upsertApartment(Apartment(id, clean)) }
    }

    fun delete(id: ApartmentId) {
        viewModelScope.launch { repository.deleteApartment(id) }
    }
}
