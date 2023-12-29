package com.muedsa.geometry

import org.jetbrains.skia.RRect
import org.jetbrains.skia.Rect

fun makeRRectFromRectAndCorners(
    rect: Rect,
    topLeft: Radius = Radius.ZERO,
    topRight: Radius = Radius.ZERO,
    bottomRight: Radius = Radius.ZERO,
    bottomLeft: Radius = Radius.ZERO
) = RRect.makeComplexLTRB(
    rect.left, rect.top, rect.right, rect.bottom,
    floatArrayOf(
        topLeft.x, topLeft.y,
        topRight.x, topRight.y,
        bottomRight.x, bottomRight.y,
        bottomLeft.x, bottomLeft.y
    )
)