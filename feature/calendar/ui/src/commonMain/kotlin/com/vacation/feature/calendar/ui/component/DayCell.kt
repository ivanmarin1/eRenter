package com.vacation.feature.calendar.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vacation.core.designsystem.theme.CalendarColors
import com.vacation.core.designsystem.theme.VacationDesign
import com.vacation.feature.calendar.domain.model.BookingSummary
import com.vacation.feature.calendar.domain.model.DaySchedule

/**
 * A single day. The date sits in a square day box split along the top-right→bottom-left
 * diagonal:
 *   • upper-left triangle  = a guest CHECKS OUT (leaves in the morning)
 *   • lower-right triangle = a guest CHECKS IN  (arrives in the afternoon)
 * When both triangles are filled the day is a TURNOVER — instantly recognisable as a
 * cleaning day.
 *
 * When [expanded], that same square day box is kept unchanged and a compact list of which
 * apartment is checking out/in is shown directly below it, using the full cell width, so the
 * whole month's turnovers are readable without tapping into every day.
 */
@Composable
fun DayCell(
    day: DaySchedule,
    isToday: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
) {
    val colors = VacationDesign.calendarColors

    Box(
        modifier = modifier
            .padding(2.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(enabled = day.inVisibleMonth, onClick = onClick),
        contentAlignment = Alignment.TopStart,
    ) {
        if (expanded) {
            Column(Modifier.fillMaxWidth()) {
                DaySquare(day = day, isToday = isToday, isSelected = isSelected, colors = colors)
                day.departures.forEach { booking ->
                    BookingLine(prefix = "↑", booking = booking, color = colors.departure)
                }
                day.arrivals.forEach { booking ->
                    BookingLine(prefix = "↓", booking = booking, color = colors.arrival)
                }
            }
        } else {
            DaySquare(day = day, isToday = isToday, isSelected = isSelected, colors = colors)
        }
    }
}

/** The square date box: full cell width, diagonal check-in/out split, number, turnover dot. */
@Composable
private fun DaySquare(
    day: DaySchedule,
    isToday: Boolean,
    isSelected: Boolean,
    colors: CalendarColors,
) {
    val hasDeparture = day.departures.isNotEmpty()
    val hasArrival = day.arrivals.isNotEmpty()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .drawBehind { drawDiagonalSplit(hasDeparture, hasArrival, colors) }
            .padding(6.dp),
        contentAlignment = Alignment.TopStart,
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
            color = when {
                !day.inVisibleMonth -> colors.outsideMonth
                isToday -> colors.todayRing
                else -> MaterialTheme.colorScheme.onSurface
            },
        )

        // Turnover gets a small solid dot so it reads even at a glance / for colour-blind users.
        if (day.isTurnover) {
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .clip(RoundedCornerShape(50))
                    .drawBehind { drawCircle(colors.turnoverAccent) }
                    .padding(4.dp),
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDiagonalSplit(
    hasDeparture: Boolean,
    hasArrival: Boolean,
    colors: CalendarColors,
) {
    if (hasDeparture) {
        val topLeft = Path().apply {
            moveTo(0f, 0f); lineTo(size.width, 0f); lineTo(0f, size.height); close()
        }
        drawPath(topLeft, colors.departureContainer)
    }
    if (hasArrival) {
        val bottomRight = Path().apply {
            moveTo(size.width, 0f); lineTo(size.width, size.height); lineTo(0f, size.height); close()
        }
        drawPath(bottomRight, colors.arrivalContainer)
    }
}

/** "Apartment 3" -> "3". Falls back to the full name when it doesn't follow that convention. */
private fun shortApartmentLabel(name: String): String {
    val stripped = Regex("(?i)^apartment\\s*").replace(name, "").trim()
    return stripped.ifBlank { name }
}

@Composable
private fun BookingLine(prefix: String, booking: BookingSummary, color: Color) {
    Text(
        text = "$prefix ${shortApartmentLabel(booking.apartmentName)}",
        style = MaterialTheme.typography.bodyMedium,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(horizontal = 4.dp),
    )
}
