package com.vacation.feature.calendar.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

/**
 * "Add reservation" floating action button. The plus is drawn with a [Canvas] so we don't pull
 * in the Material icons dependency for a single glyph.
 */
@Composable
fun AddReservationFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(onClick = onClick, modifier = modifier) {
        val tint = LocalContentColor.current
        Canvas(Modifier.size(24.dp)) {
            val stroke = 2.5.dp.toPx()
            val cx = size.width / 2f
            val cy = size.height / 2f
            val half = size.minDimension * 0.3f
            drawLine(tint, Offset(cx - half, cy), Offset(cx + half, cy), strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(tint, Offset(cx, cy - half), Offset(cx, cy + half), strokeWidth = stroke, cap = StrokeCap.Round)
        }
    }
}
