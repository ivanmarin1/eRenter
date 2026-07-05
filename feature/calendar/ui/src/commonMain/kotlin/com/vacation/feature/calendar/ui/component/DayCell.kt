package com.vacation.feature.calendar.ui.component

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vacation.core.designsystem.theme.VacationDesign
import com.vacation.feature.calendar.domain.model.DaySchedule

/**
 * A single day. The cell is split along the top-right→bottom-left diagonal:
 *   • upper-left triangle  = a guest CHECKS OUT (leaves in the morning)
 *   • lower-right triangle = a guest CHECKS IN  (arrives in the afternoon)
 * When both triangles are filled the day is a TURNOVER — instantly recognisable as a
 * cleaning day. No text clutter; the guest/apartment detail lives in the detail panel.
 */
@Composable
fun DayCell(
    day: DaySchedule,
    isToday: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VacationDesign.calendarColors
    val hasDeparture = day.departures.isNotEmpty()
    val hasArrival = day.arrivals.isNotEmpty()

    val baseModifier = modifier
        .aspectRatio(1f)
        .padding(2.dp)
        .clip(RoundedCornerShape(10.dp))
        .drawBehind {
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

    Box(
        modifier = baseModifier
            .clickable(enabled = day.inVisibleMonth, onClick = onClick)
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
