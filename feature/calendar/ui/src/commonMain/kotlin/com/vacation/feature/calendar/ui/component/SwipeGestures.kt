package com.vacation.feature.calendar.ui.component

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * Horizontal swipe over a calendar, mirroring the ‹ / › period buttons: swipe **left** goes to the
 * next period (like ›), swipe **right** to the previous (like ‹). A per-gesture distance threshold
 * keeps small horizontal wobble during vertical scrolling from flipping the period.
 *
 * Coexists with a `verticalScroll` parent: the horizontal drag detector only claims the gesture once
 * horizontal movement passes touch slop, so vertical scrolling is unaffected.
 */
fun Modifier.swipeToChangePeriod(
    onPrevious: () -> Unit,
    onNext: () -> Unit,
): Modifier = this.pointerInput(onPrevious, onNext) {
    val threshold = 48.dp.toPx()
    var totalDrag = 0f
    detectHorizontalDragGestures(
        onDragStart = { totalDrag = 0f },
        onDragEnd = {
            when {
                totalDrag <= -threshold -> onNext()
                totalDrag >= threshold -> onPrevious()
            }
        },
        onDragCancel = { totalDrag = 0f },
        onHorizontalDrag = { change, dragAmount ->
            totalDrag += dragAmount
            if (abs(dragAmount) > 0f) change.consume()
        },
    )
}
