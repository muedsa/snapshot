package com.muedsa.snapshot.rendering.flex

import com.muedsa.snapshot.VerticalDirection
import com.muedsa.snapshot.paint.Axis
import org.jetbrains.skia.paragraph.Direction

internal fun startIsTopLeft(
    direction: Axis,
    textDirection: Direction? = null,
    verticalDirection: VerticalDirection? = null,
): Boolean? =
    // If the relevant value of textDirection or verticalDirection is null, this returns null too.
    when (direction) {
        Axis.HORIZONTAL -> when (textDirection) {
            Direction.LTR -> true
            Direction.RTL -> false
            null -> null
        }

        Axis.VERTICAL -> when (verticalDirection) {
            VerticalDirection.DOWN -> true
            VerticalDirection.UP -> false
            null -> null
        }
    }