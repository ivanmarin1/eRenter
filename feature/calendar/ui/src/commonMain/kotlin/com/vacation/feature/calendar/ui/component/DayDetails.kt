package com.vacation.feature.calendar.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vacation.core.designsystem.theme.VacationDesign
import com.vacation.feature.calendar.domain.model.BookingSummary
import com.vacation.feature.calendar.domain.model.DaySchedule
import kotlinx.datetime.number

/** On-demand detail for the tapped day. Keeps the grid uncluttered — names live here, not in cells. */
@Composable
fun DayDetails(day: DaySchedule, modifier: Modifier = Modifier) {
    val colors = VacationDesign.calendarColors
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "${day.date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }}, ${day.date.dayOfMonth}.${day.date.month.number}.${day.date.year}.",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

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
                Section("Checking out", day.departures, colors.departure)
            }
            if (day.arrivals.isNotEmpty()) {
                Section("Checking in", day.arrivals, colors.arrival)
            }
        }
    }
}

@Composable
private fun Section(title: String, items: List<BookingSummary>, accent: androidx.compose.ui.graphics.Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = accent, fontWeight = FontWeight.Bold)
        items.forEach { s ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(s.apartmentName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                Text(s.guestName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
