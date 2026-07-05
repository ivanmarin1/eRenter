package com.vacation.feature.calendar.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vacation.feature.calendar.domain.model.Apartment
import com.vacation.feature.calendar.domain.model.ApartmentId
import com.vacation.feature.calendar.presentation.ApartmentsViewModel
import org.koin.compose.viewmodel.koinViewModel

/** Reusable entry point: wires [ApartmentsViewModel] via Koin and renders the stateless screen. */
@Composable
fun ManageApartmentsRoute(
    modifier: Modifier = Modifier,
    viewModel: ApartmentsViewModel = koinViewModel(),
) {
    val apartments by viewModel.apartments.collectAsStateWithLifecycle()
    ManageApartmentsScreen(
        apartments = apartments,
        onAdd = viewModel::add,
        onRename = viewModel::rename,
        onDelete = viewModel::delete,
        modifier = modifier,
    )
}

@Composable
fun ManageApartmentsScreen(
    apartments: List<Apartment>,
    onAdd: (String) -> Unit,
    onRename: (ApartmentId, String) -> Unit,
    onDelete: (ApartmentId) -> Unit,
    modifier: Modifier = Modifier,
) {
    var newName by remember { mutableStateOf("") }
    var renameTarget by remember { mutableStateOf<Apartment?>(null) }
    var deleteTarget by remember { mutableStateOf<Apartment?>(null) }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Apartments", style = MaterialTheme.typography.titleLarge)
        Text(
            "Add the units you rent out. You can rename or remove them at any time; " +
                "removing a unit also removes its bookings.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("New apartment name") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            Button(
                onClick = { onAdd(newName); newName = "" },
                enabled = newName.isNotBlank(),
            ) { Text("Add") }
        }

        if (apartments.isEmpty()) {
            Text(
                "No apartments yet — add your first one above.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(apartments, key = { it.id.value }) { apartment ->
                    ApartmentRow(
                        apartment = apartment,
                        onRename = { renameTarget = apartment },
                        onDelete = { deleteTarget = apartment },
                    )
                }
            }
        }
    }

    renameTarget?.let { target ->
        ApartmentNameDialog(
            title = "Rename apartment",
            initialValue = target.name,
            confirmLabel = "Save",
            onConfirm = { onRename(target.id, it); renameTarget = null },
            onDismiss = { renameTarget = null },
        )
    }

    deleteTarget?.let { target ->
        ConfirmDialog(
            title = "Delete \"${target.name}\"?",
            message = "This removes the apartment and all of its bookings. This cannot be undone.",
            confirmLabel = "Delete",
            onConfirm = { onDelete(target.id); deleteTarget = null },
            onDismiss = { deleteTarget = null },
        )
    }
}

@Composable
private fun ApartmentRow(
    apartment: Apartment,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                apartment.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onRename) { Text("Rename") }
            TextButton(onClick = onDelete) { Text("Delete") }
        }
    }
}
