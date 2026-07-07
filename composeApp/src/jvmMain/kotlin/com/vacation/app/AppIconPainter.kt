package com.vacation.app

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.painter.Painter

/**
 * The eRenter desktop window icon, drawn as a [Painter] so it needs no bundled PNG. It reproduces
 * the app icon: a teal tile holding one enlarged "split turnover day" (amber departure / sage
 * arrival halves, split by the diagonal, with the plum turnover dot).
 */
class AppIconPainter : Painter() {

    override val intrinsicSize: Size = Size(512f, 512f)

    override fun DrawScope.onDraw() {
        val s = size.minDimension

        // Teal tile.
        drawRoundRect(
            color = Color(0xFF3D6E8C),
            size = Size(s, s),
            cornerRadius = CornerRadius(s * 0.22f),
        )

        // Centred day cell.
        val cell = s * 0.46f
        val left = (s - cell) / 2f
        val top = (s - cell) / 2f
        val cellPath = Path().apply {
            addRoundRect(RoundRect(Rect(left, top, left + cell, top + cell), CornerRadius(cell * 0.2f)))
        }

        clipPath(cellPath) {
            drawRect(Color.White, topLeft = Offset(left, top), size = Size(cell, cell))
            // departure (upper-left)
            drawPath(
                Path().apply {
                    moveTo(left, top); lineTo(left + cell, top); lineTo(left, top + cell); close()
                },
                Color(0xFFCB8A2C),
            )
            // arrival (lower-right)
            drawPath(
                Path().apply {
                    moveTo(left + cell, top); lineTo(left + cell, top + cell); lineTo(left, top + cell); close()
                },
                Color(0xFF5E7D42),
            )
            // diagonal split
            drawLine(
                color = Color.White,
                start = Offset(left + cell, top),
                end = Offset(left, top + cell),
                strokeWidth = s * 0.03f,
                cap = StrokeCap.Round,
            )
        }

        // Turnover dot.
        val dot = Offset(left + cell * 0.72f, top + cell * 0.72f)
        drawCircle(Color.White, radius = cell * 0.11f, center = dot)
        drawCircle(Color(0xFF8A5A7A), radius = cell * 0.078f, center = dot)
    }
}
