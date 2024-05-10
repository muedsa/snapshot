package com.muedsa.geometry

import org.jetbrains.skia.Rect
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

val Rect.size: Size
    get() = Size(width = width, height = height)

val Rect.isInfinite: Boolean
    get() = left >= Float.POSITIVE_INFINITY
            || top >= Float.POSITIVE_INFINITY
            || right >= Float.POSITIVE_INFINITY
            || bottom >= Float.POSITIVE_INFINITY

val Rect.isFinite: Boolean
    get() = left.isFinite() && top.isFinite() && right.isFinite() && bottom.isFinite()

fun Rect.shift(offset: Offset) =
    Rect.makeLTRB(left + offset.x, top + offset.y, right + offset.x, bottom + offset.y)

fun Rect.translate(translateX: Float, translateY: Float) =
    Rect.makeLTRB(left + translateX, top + translateY, right + translateX, bottom + translateY)

val Rect.shortestSide: Float
    get() = min(width.absoluteValue, height.absoluteValue)

val Rect.longestSide: Float
    get() = max(width.absoluteValue, height.absoluteValue)

val Rect.topLeft: Offset
    get() = Offset(x = left, y = top)

val Rect.getTopCenter: Offset
    get() = Offset(x = left + width / 2f, y = top)

val Rect.getTopRight: Offset
    get() = Offset(x = right, y = top)

val Rect.centerLeft: Offset
    get() = Offset(x = left, y = top + height / 2f)

val Rect.center: Offset
    get() = Offset(x = left + width / 2f, y = top + height / 2f)

val Rect.centerRight: Offset
    get() = Offset(x = right, y = top + height / 2f)

val Rect.bottomLeft: Offset
    get() = Offset(x = left, y = bottom)

val Rect.bottomRight: Offset
    get() = Offset(x = right, y = bottom)

fun Rect.contains(offset: Offset): Boolean =
    offset.x >= left && offset.x < right && offset.y >= top && offset.y < bottom

fun makeRectFromCircle(center: Offset, radius: Float) = makeRectFromCenter(
    center = center,
    width = radius * 2,
    height = radius * 2
)

fun makeRectFromCenter(center: Offset, width: Float, height: Float) = Rect.makeLTRB(
    center.x - width / 2f,
    center.y - height / 2f,
    center.x + width / 2f,
    center.y + height / 2f,
)