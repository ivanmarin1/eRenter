package com.vacation.feature.calendar.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vacation.core.designsystem.theme.VacationDesign
import com.vacation.feature.calendar.domain.model.ImportPreview
import com.vacation.feature.calendar.domain.model.ImportRow
import com.vacation.feature.calendar.presentation.ImportResult
import com.vacation.feature.calendar.presentation.ImportUiState
import com.vacation.feature.calendar.presentation.ImportViewModel
import org.koin.compose.viewmodel.koinViewModel

private const val PLACEHOLDER =
    "A1, Anna Kovač, 2026-07-28, 2026-08-04\nA3, Dubois, 2026-08-01, 2026-08-09"

@Composable
fun ImportRoute(
    modifier: Modifier = Modifier,
    viewModel: ImportViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Live preview: re-parse whenever the pasted text changes, so rows validate as you type.
    LaunchedEffect(state.rawText) {
        if (state.rawText.isNotBlank()) viewModel.preview()
    }
    ImportScreen(
        state = state,
        onTextChange = viewModel::onTextChange,
        onAutoCreateChange = viewModel::onAutoCreateChange,
        onConfirm = viewModel::confirmImport,
        modifier = modifier,
    )
}

@Composable
fun ImportScreen(
    state: ImportUiState,
    onTextChange: (String) -> Unit,
    onAutoCreateChange: (Boolean) -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(Modifier.padding(top = 4.dp)) {
            Text("IMPORT", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "Paste bookings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        Text(
            buildString {
                append("Paste rows from your spreadsheet — one booking per line:\n")
                append("Apartment, Guest, Check-in, Check-out")
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        OutlinedTextField(
            value = state.rawText,
            onValueChange = onTextChange,
            placeholder = {
                Text(
                    PLACEHOLDER,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().heightIn(min = 140.dp),
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = state.autoCreateApartments, onCheckedChange = onAutoCreateChange)
            Text("Create apartments that don't exist yet", style = MaterialTheme.typography.bodySmall)
        }

        state.lastResult?.let { ResultCard(it) }

        val preview = state.preview
        when {
            preview != null && preview.hasContent -> PreviewSection(
                preview = preview,
                autoCreate = state.autoCreateApartments,
                isImporting = state.isImporting,
                onConfirm = onConfirm,
            )
            state.rawText.isBlank() -> EmptyImportCard()
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
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Preview", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Text(
                "${preview.validCount} ok · ${preview.errorCount} to fix",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (preview.newApartmentNames.isNotEmpty()) {
            val names = preview.newApartmentNames.joinToString(", ")
            Text(
                if (autoCreate) "Will create: $names"
                else "These apartments don't exist and their rows are skipped: $names",
                style = MaterialTheme.typography.bodySmall,
                color = if (autoCreate) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
            )
        }

        preview.rows.forEach { row -> PreviewRow(row) }

        val enabled = preview.validCount > 0 && !isImporting
        Surface(
            onClick = { if (enabled) onConfirm() },
            enabled = enabled,
            shape = RoundedCornerShape(15.dp),
            color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        ) {
            Row(
                Modifier.fillMaxWidth().padding(vertical = 15.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isImporting) CircularProgressIndicator(Modifier.size(18.dp).padding(end = 8.dp))
                Text(
                    "Import ${preview.validCount} bookings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = if (enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PreviewRow(row: ImportRow) {
    val calendar = VacationDesign.calendarColors
    val ok = row.isValid
    val accent = if (ok) calendar.arrival else MaterialTheme.colorScheme.error
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(13.dp),
        color = if (ok) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.errorContainer,
        border = BorderStroke(1.dp, if (ok) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.error),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                Modifier.size(22.dp).clip(CircleShape).background(accent),
                contentAlignment = Alignment.Center,
            ) {
                Text(if (ok) "✓" else "!", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, color = androidx.compose.ui.graphics.Color.White)
            }
            Column(Modifier.weight(1f)) {
                Text(
                    if (ok) "${row.apartmentName} · ${row.guestName} · ${row.checkIn} → ${row.checkOut}"
                    else "Line ${row.lineNumber}: ${row.guestName.ifBlank { row.apartmentName }}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!ok) {
                    Text(row.error ?: "Invalid row", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun ResultCard(result: ImportResult) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = VacationDesign.calendarColors.arrivalContainer,
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Import complete", style = MaterialTheme.typography.titleSmall, color = VacationDesign.calendarColors.arrival)
            Text(
                "Imported ${result.importedBookings} booking(s)" +
                    ", created ${result.createdApartments} apartment(s)" +
                    if (result.skippedRows > 0) ", skipped ${result.skippedRows} row(s)" else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun EmptyImportCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            Modifier.padding(vertical = 30.dp, horizontal = 20.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("Nothing pasted yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
            Text(
                "Rows you paste appear here, checked for errors before importing.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
