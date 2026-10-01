package com.muedsa.snapshot.rendering.box

import org.jetbrains.skia.Rect
import kotlin.math.max
import kotlin.math.min

/** null 表示无法安全估算绘制边界；空矩形表示没有可绘制内容。 */
internal val EMPTY_FILTER_PAINT_BOUNDS: Rect = Rect.makeLTRB(0f, 0f, 0f, 0f)

internal fun Rect?.unionFilterPaintBounds(other: Rect?): Rect? {
    if (this == null || other == null) return null
    if (!isFiniteFilterBounds() || !other.isFiniteFilterBounds()) return null
    if (width <= 0f || height <= 0f) return other
    if (other.width <= 0f || other.height <= 0f) return this
    return Rect.makeLTRB(
        min(left, other.left),
        min(top, other.top),
        max(right, other.right),
        max(bottom, other.bottom),
    )
}

internal fun Rect.isFiniteFilterBounds(): Boolean =
    left.isFinite() && top.isFinite() && right.isFinite() && bottom.isFinite()

internal fun Rect?.intersectFilterPaintBounds(clip: Rect): Rect? =
    if (this == null) clip else intersect(clip) ?: EMPTY_FILTER_PAINT_BOUNDS
