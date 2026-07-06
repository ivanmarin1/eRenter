package com.vacation.feature.calendar.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vacation.core.designsystem.theme.VacationDesign
import com.vacation.feature.calendar.domain.model.AvailabilityStatus
import com.vacation.feature.calendar.domain.model.DayAvailability

/** One day in the per-apartment availability grid: a solid green/red fill with the date. */
@Composable
fun AvailabilityDayCell(day: DayAvailability, modifier: Modifier = Modifier) {
    val colors = VacationDesign.calendarColors
    val (fill, content) = when (day.status) {
        AvailabilityStatus.Available -> colors.availableContainer to colors.available
        AvailabilityStatus.Booked -> colors.bookedContainer to colors.booked
        AvailabilityStatus.Overbooked -> colors.overbookedContainer to colors.overbooked
    }
    // Days outside the shown month are muted so the month itself stands out.
    val dimmed = !day.inVisibleMonth
    val fillColor = if (dimmed) fill.copy(alpha = 0.35f) else fill
    val textColor = if (dimmed) content.copy(alpha = 0.5f) else content

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(fillColor)
            .padding(6.dp),
        contentAlignment = Alignment.TopStart,
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = textColor,
        )
        if (day.status == AvailabilityStatus.Overbooked) {
            Text(
                text = "!",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = content,
                modifier = Modifier.align(Alignment.BottomEnd),
            )
        }
    }
}
