package com.muedsa.geometry

import org.jetbrains.skia.RRect
import org.jetbrains.skia.Rect

fun RRect.deflate(delta: Float): Rect = inflate(-delta)


/**
 *      radii.size == 8
 *      0            2
 *     TLX          TRX
 *       -----------
 * 1 TLY |          | TRY 3
 *       |          |
 * 7 BLY |          | BRY 4
 *       -----------
 *     BLX          BRX
 *      6            5
 *
 * makeNinePatchXYWH
 *     0     1    2     3     4     5     6     7
 *  (lRad, tRad, rRad, tRad, rRad, bRad, lRad, bRad)
 *  0 left-top-x
 *  1 left-top-y
 *  2 right-top-x
 *  3 right-top-y
 *  4 right-bottom-x
 *  5 right-bottom-y
 *  6 left-bottom-x
 *  7 left-bottom-y
 */
val RRect.tlRadiusX: Float
    get() = when (radii.size) {
        0 -> 0f
        1 -> radii[0] // All are the same
        2 -> radii[0] // Corresponding values are same for each corner
        4 -> radii[0] // Horizontal and vertical are same for each corner
        8 -> radii[0]
        else -> throw Error("illegal radii array")
    }
val RRect.tlRadiusY: Float
    get() = when (radii.size) {
        0 -> 0f
        1 -> radii[0] // All are the same
        2 -> radii[1] // Corresponding values are same for each corner
        4 -> radii[0] // Horizontal and vertical are same for each corner
        8 -> radii[1]
        else -> throw Error("illegal radii array")
    }
val RRect.tlRadius: Radius
    get() = Radius.elliptical(x = tlRadiusX, y = tlRadiusY)
val RRect.trRadiusX: Float
    get() = when (radii.size) {
        0 -> 0f
        1 -> radii[0] // All are the same
        2 -> radii[0] // Corresponding values are same for each corner
        4 -> radii[1] // Horizontal and vertical are same for each corner
        8 -> radii[2]
        else -> throw Error("illegal radii array")
    }
val RRect.trRadiusY: Float
    get() = when (radii.size) {
        0 -> 0f
        1 -> radii[0] // All are the same
        2 -> radii[1] // Corresponding values are same for each corner
        4 -> radii[1] // Horizontal and vertical are same for each corner
        8 -> radii[3]
        else -> throw Error("illegal radii array")
    }
val RRect.trRadius: Radius
    get() = Radius.elliptical(x = trRadiusX, y = trRadiusY)
val RRect.brRadiusX: Float
    get() = when (radii.size) {
        0 -> 0f
        1 -> radii[0] // All are the same
        2 -> radii[0] // Corresponding values are same for each corner
        4 -> radii[2] // Horizontal and vertical are same for each corner
        8 -> radii[4]
        else -> throw Error("illegal radii array")
    }
val RRect.brRadiusY: Float
    get() = when (radii.size) {
        0 -> 0f
        1 -> radii[0] // All are the same
        2 -> radii[1] // Corresponding values are same for each corner
        4 -> radii[2] // Horizontal and vertical are same for each corner
        8 -> radii[5]
        else -> throw Error("illegal radii array")
    }
val RRect.brRadius: Radius
    get() = Radius.elliptical(x = brRadiusX, y = brRadiusY)
val RRect.blRadiusX: Float
    get() = when (radii.size) {
        0 -> 0f
        1 -> radii[0] // All are the same
        2 -> radii[0] // Corresponding values are same for each corner
        4 -> radii[3] // Horizontal and vertical are same for each corner
        8 -> radii[6]
        else -> throw Error("illegal radii array")
    }
val RRect.blRadiusY: Float
    get() = when (radii.size) {
        0 -> 0f
        1 -> radii[0] // All are the same
        2 -> radii[1] // Corresponding values are same for each corner
        4 -> radii[3] // Horizontal and vertical are same for each corner
        8 -> radii[7]
        else -> throw Error("illegal radii array")
    }
val RRect.blRadius: Radius
    get() = Radius.elliptical(x = blRadiusX, y = blRadiusY)

fun makeRRectFromRectAndCorners(
    rect: Rect,
    topLeft: Radius = Radius.ZERO,
    topRight: Radius = Radius.ZERO,
    bottomRight: Radius = Radius.ZERO,
    bottomLeft: Radius = Radius.ZERO,
) = RRect.makeComplexLTRB(
    l = rect.left, t = rect.top, r = rect.right, b = rect.bottom,
    radii = floatArrayOf(
        topLeft.x, topLeft.y,
        topRight.x, topRight.y,
        bottomRight.x, bottomRight.y,
        bottomLeft.x, bottomLeft.y
    )
)

fun makeRRectFromLTRBAndCorners(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    topLeft: Radius = Radius.ZERO,
    topRight: Radius = Radius.ZERO,
    bottomRight: Radius = Radius.ZERO,
    bottomLeft: Radius = Radius.ZERO,
) = RRect.makeComplexLTRB(
    l = left, t = top, r = right, b = bottom,
    radii = floatArrayOf(
        topLeft.x, topLeft.y,
        topRight.x, topRight.y,
        bottomRight.x, bottomRight.y,
        bottomLeft.x, bottomLeft.y
    )
)

fun makeRRectFromRectAndRadius(rect: Rect, radius: Radius) = RRect.makeLTRB(
    l = rect.left,
    t = rect.top,
    r = rect.right,
    b = rect.bottom,
    xRad = radius.x,
    yRad = radius.y
)

fun RRect.shift(offset: Offset): RRect =
    RRect.makeComplexLTRB(
        l = left + offset.x,
        t = top + offset.y,
        r = right + offset.x,
        b = bottom + offset.y,
        radii = radii.clone()
    )