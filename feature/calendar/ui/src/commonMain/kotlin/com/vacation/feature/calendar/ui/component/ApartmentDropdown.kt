package com.vacation.feature.calendar.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.vacation.feature.calendar.domain.model.Apartment
import com.vacation.feature.calendar.domain.model.ApartmentId

/** A read-only dropdown for choosing an apartment. Shared by the booking form and availability screen. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApartmentDropdown(
    apartments: List<Apartment>,
    selectedId: ApartmentId?,
    onSelect: (ApartmentId) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Apartment",
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = apartments.firstOrNull { it.id == selectedId }?.name ?: ""

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
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
