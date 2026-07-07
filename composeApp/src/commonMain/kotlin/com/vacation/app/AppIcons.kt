package com.vacation.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The bottom-nav glyphs, drawn as line art so we don't pull in the Material icons dependency for a
 * handful of shapes (the same reasoning as [com.vacation.feature.calendar.ui.component.AddReservationFab]).
 * Each mirrors the matching SVG in the eRenter design.
 */
private fun DrawScope.strokeStyle(width: Float) =
    Stroke(width = width, cap = StrokeCap.Round, join = StrokeJoin.Round)

@Composable
fun CalendarIcon(tint: Color, size: Dp = 23.dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) {
        val s = strokeStyle(2.dp.toPx())
        val w = this.size.width
        val h = this.size.height
        val inset = w * 0.13f
        drawRoundRect(
            color = tint,
            topLeft = Offset(inset, inset * 1.4f),
            size = Size(w - inset * 2, h - inset * 2.2f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.14f),
            style = s,
        )
        // header rule + two hanger ticks
        drawLine(tint, Offset(inset, h * 0.37f), Offset(w - inset, h * 0.37f), s.width, s.cap)
        drawLine(tint, Offset(w * 0.34f, inset * 0.5f), Offset(w * 0.34f, h * 0.25f), s.width, s.cap)
        drawLine(tint, Offset(w * 0.66f, inset * 0.5f), Offset(w * 0.66f, h * 0.25f), s.width, s.cap)
    }
}

@Composable
fun GridIcon(tint: Color, size: Dp = 23.dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) {
        val s = strokeStyle(2.dp.toPx())
        val w = this.size.width
        val cell = w * 0.34f
        val gap = w * 0.1f
        val r = androidx.compose.ui.geometry.CornerRadius(w * 0.08f)
        val positions = listOf(
            Offset(w * 0.12f, w * 0.12f),
            Offset(w * 0.12f + cell + gap, w * 0.12f),
            Offset(w * 0.12f, w * 0.12f + cell + gap),
            Offset(w * 0.12f + cell + gap, w * 0.12f + cell + gap),
        )
        positions.forEach { p ->
            drawRoundRect(tint, topLeft = p, size = Size(cell, cell), cornerRadius = r, style = s)
        }
    }
}

@Composable
fun BuildingIcon(tint: Color, size: Dp = 23.dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) {
        val s = strokeStyle(2.dp.toPx())
        val w = this.size.width
        val h = this.size.height
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.18f, h * 0.12f),
            size = Size(w * 0.64f, h * 0.76f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.09f),
            style = s,
        )
        // two rows of windows
        val dot = w * 0.06f
        listOf(0.36f, 0.62f).forEach { col ->
            listOf(0.32f, 0.5f).forEach { row ->
                drawCircle(tint, radius = dot, center = Offset(w * col, h * row))
            }
        }
        // door
        drawLine(tint, Offset(w * 0.42f, h * 0.88f), Offset(w * 0.42f, h * 0.72f), s.width, s.cap)
        drawLine(tint, Offset(w * 0.58f, h * 0.88f), Offset(w * 0.58f, h * 0.72f), s.width, s.cap)
        drawLine(tint, Offset(w * 0.42f, h * 0.72f), Offset(w * 0.58f, h * 0.72f), s.width, s.cap)
    }
}

@Composable
fun ImportIcon(tint: Color, size: Dp = 23.dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) {
        val s = strokeStyle(2.dp.toPx())
        val w = this.size.width
        val h = this.size.height
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.22f, h * 0.16f),
            size = Size(w * 0.56f, h * 0.72f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.08f),
            style = s,
        )
        // clip at the top
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.38f, h * 0.1f),
            size = Size(w * 0.24f, h * 0.12f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.03f),
            style = s,
        )
        drawLine(tint, Offset(w * 0.34f, h * 0.5f), Offset(w * 0.66f, h * 0.5f), s.width, s.cap)
        drawLine(tint, Offset(w * 0.34f, h * 0.66f), Offset(w * 0.56f, h * 0.66f), s.width, s.cap)
    }
}
