package com.vacation.feature.calendar.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vacation.core.designsystem.theme.VacationDesign
import com.vacation.feature.calendar.domain.model.BookingSummary
import com.vacation.feature.calendar.domain.model.DaySchedule
import kotlinx.datetime.number

/**
 * On-demand detail for the tapped day. Keeps the grid uncluttered — names live here, not in
 * cells — and hosts the entry points for adding, editing and deleting single reservations.
 */
@Composable
fun DayDetails(
    day: DaySchedule,
    canAdd: Boolean,
    onAddReservation: () -> Unit,
    onEditBooking: (BookingSummary) -> Unit,
    onDeleteBooking: (BookingSummary) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VacationDesign.calendarColors
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${day.date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }}, ${day.date.dayOfMonth}.${day.date.month.number}.${day.date.year}.",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                FilledTonalButton(onClick = onAddReservation, enabled = canAdd) { Text("Add") }
            }

            if (!canAdd) {
                Text(
                    "Add an apartment first (Apartments tab) to create reservations.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (day.isTurnover) {
                Surface(
                    color = colors.turnoverAccent.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        "Turnover day — clean between guests before the new check-in.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.turnoverAccent,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
            }

            if (!day.hasActivity) {
                Text("No arrivals or departures.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (day.departures.isNotEmpty()) {
                Section("Checking out", day.departures, colors.departure, onEditBooking, onDeleteBooking)
            }
            if (day.arrivals.isNotEmpty()) {
                Section("Checking in", day.arrivals, colors.arrival, onEditBooking, onDeleteBooking)
            }
        }
    }
}

@Composable
private fun Section(
    title: String,
    items: List<BookingSummary>,
    accent: Color,
    onEdit: (BookingSummary) -> Unit,
    onDelete: (BookingSummary) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = accent, fontWeight = FontWeight.Bold)
        items.forEach { s ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(s.apartmentName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Text(s.guestName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = { onEdit(s) }) { Text("Edit") }
                TextButton(onClick = { onDelete(s) }) { Text("Delete") }
            }
        }
    }
}
