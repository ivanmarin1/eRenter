package com.vacation.feature.calendar.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vacation.core.designsystem.theme.CalendarColors
import com.vacation.core.designsystem.theme.VacationDesign
import com.vacation.feature.calendar.domain.model.MiniMonthCell
import com.vacation.feature.calendar.domain.model.ScheduleDayKind

/** Which colour language a mini month speaks. */
enum class MiniMonthPalette { Schedule, Availability }

/**
 * A compact month for the year overview / matrix: a short label over a 7-column grid of tiny day
 * squares coloured by [MiniMonthCell.kind]. Tapping the card (if [onClick] is set) usually opens
 * the full month.
 */
@Composable
fun MiniMonthGrid(
    label: String,
    cells: List<MiniMonthCell>,
    palette: MiniMonthPalette,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val colors = VacationDesign.calendarColors
    // Pad to whole weeks so every row has 7 columns.
    val padded = cells + List((7 - cells.size % 7) % 7) { MiniMonthCell(null, ScheduleDayKind.None, false) }

    Surface(
        modifier = modifier.then(if (onClick != null) Modifier.clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(Modifier.padding(horizontal = 7.dp, vertical = 8.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp),
            )
            padded.chunked(7).forEach { week ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    week.forEach { cell -> MiniCell(cell, palette, colors, Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun MiniCell(cell: MiniMonthCell, palette: MiniMonthPalette, colors: CalendarColors, modifier: Modifier) {
    if (cell.dayOfMonth == null) {
        Box(modifier.aspectRatio(1f))
        return
    }
    val (fill, brush, onColor) = cellColors(cell.kind, palette, colors)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(top = 2.dp)
            .clip(RoundedCornerShape(3.dp))
            .then(if (brush != null) Modifier.background(brush) else Modifier.background(fill))
            .then(if (cell.isToday) Modifier.border(1.5.dp, colors.todayRing, RoundedCornerShape(3.dp)) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            cell.dayOfMonth.toString(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = onColor,
            textAlign = TextAlign.Center,
        )
    }
}

/** Returns (solidFill, optionalBrush, textColor). Brush wins over fill when non-null. */
@Composable
private fun cellColors(kind: ScheduleDayKind, palette: MiniMonthPalette, colors: CalendarColors): Triple<Color, Brush?, Color> {
    val muted = MaterialTheme.colorScheme.surfaceVariant
    val dim = MaterialTheme.colorScheme.onSurfaceVariant
    return when (palette) {
        MiniMonthPalette.Schedule -> when (kind) {
            ScheduleDayKind.None -> Triple(muted, null, dim)
            ScheduleDayKind.Occupied -> Triple(colors.arrivalContainer, null, dim)
            ScheduleDayKind.Departure -> Triple(colors.departure, null, Color.White)
            ScheduleDayKind.Arrival -> Triple(colors.arrival, null, Color.White)
            ScheduleDayKind.Turnover -> Triple(colors.arrival, Brush.linearGradient(listOf(colors.arrival, colors.departure)), Color.White)
            ScheduleDayKind.Conflict -> Triple(colors.overbookedContainer, null, Color.White)
        }
        MiniMonthPalette.Availability -> when (kind) {
            ScheduleDayKind.None -> Triple(colors.availableContainer, null, colors.available)
            ScheduleDayKind.Conflict -> Triple(colors.overbookedContainer, null, Color.White)
            else -> Triple(colors.bookedContainer, null, colors.booked) // Occupied == booked
        }
    }
}
