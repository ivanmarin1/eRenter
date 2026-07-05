package com.vacation.feature.calendar.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vacation.feature.calendar.domain.model.ImportPreview
import com.vacation.feature.calendar.domain.model.ImportRow
import com.vacation.feature.calendar.presentation.ImportResult
import com.vacation.feature.calendar.presentation.ImportUiState
import com.vacation.feature.calendar.presentation.ImportViewModel
import org.koin.compose.viewmodel.koinViewModel

private const val FORMAT_HELP =
    "Paste one booking per line, with four columns separated by tabs or commas:\n" +
        "apartment, guest, check-in, check-out\n\n" +
        "Dates must be YYYY-MM-DD (e.g. 2026-07-05). Tip: copy straight from Excel or " +
        "Google Sheets. A same-day checkout/checkin (turnover) is fine — reuse the date."

@Composable
fun ImportRoute(
    modifier: Modifier = Modifier,
    viewModel: ImportViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ImportScreen(
        state = state,
        onTextChange = viewModel::onTextChange,
        onAutoCreateChange = viewModel::onAutoCreateChange,
        onPreview = viewModel::preview,
        onConfirm = viewModel::confirmImport,
        modifier = modifier,
    )
}

@Composable
fun ImportScreen(
    state: ImportUiState,
    onTextChange: (String) -> Unit,
    onAutoCreateChange: (Boolean) -> Unit,
    onPreview: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Import bookings", style = MaterialTheme.typography.titleLarge)
        Text(
            FORMAT_HELP,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        OutlinedTextField(
            value = state.rawText,
            onValueChange = onTextChange,
            label = { Text("Paste bookings here") },
            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp),
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = state.autoCreateApartments,
                onCheckedChange = onAutoCreateChange,
            )
            Text(
                "Create apartments that don't exist yet",
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        OutlinedButton(
            onClick = onPreview,
            enabled = state.rawText.isNotBlank() && !state.isImporting,
        ) { Text("Preview") }

        state.lastResult?.let { ResultCard(it) }

        state.preview?.let { preview ->
            PreviewSection(
                preview = preview,
                autoCreate = state.autoCreateApartments,
                isImporting = state.isImporting,
                onConfirm = onConfirm,
            )
        }
    }
}

@Composable
private fun PreviewSection(
    preview: ImportPreview,
    autoCreate: Boolean,
    isImporting: Boolean,
    onConfirm: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "${preview.validCount} valid, ${preview.errorCount} with errors" +
                if (preview.newApartmentNames.isNotEmpty()) {
                    " · ${preview.newApartmentNames.size} new apartment(s)"
                } else "",
            style = MaterialTheme.typography.titleMedium,
        )

        if (preview.newApartmentNames.isNotEmpty()) {
            val names = preview.newApartmentNames.joinToString(", ")
            Text(
                if (autoCreate) "Will create: $names"
                else "These apartments don't exist and won't be created (their rows are skipped): $names",
                style = MaterialTheme.typography.bodySmall,
                color = if (autoCreate) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.error,
            )
        }

        preview.rows.forEach { row -> PreviewRow(row) }

        Button(
            onClick = onConfirm,
            enabled = preview.validCount > 0 && !isImporting,
        ) {
            if (isImporting) {
                CircularProgressIndicator(Modifier.padding(end = 8.dp))
            }
            Text("Import ${preview.validCount} booking(s)")
        }
    }
}

@Composable
private fun PreviewRow(row: ImportRow) {
    if (row.isValid) {
        Text(
            "✓  ${row.apartmentName} — ${row.guestName}  (${row.checkIn} → ${row.checkOut})",
            style = MaterialTheme.typography.bodySmall,
        )
    } else {
        Text(
            "Line ${row.lineNumber}: ${row.error}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun ResultCard(result: ImportResult) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Import complete", style = MaterialTheme.typography.titleMedium)
            Text(
                "Imported ${result.importedBookings} booking(s)" +
                    ", created ${result.createdApartments} apartment(s)" +
                    if (result.skippedRows > 0) ", skipped ${result.skippedRows} row(s)" else "",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
