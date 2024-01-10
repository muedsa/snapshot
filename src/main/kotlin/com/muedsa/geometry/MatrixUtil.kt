package com.muedsa.geometry

import kotlin.math.cos
import kotlin.math.sin

fun getAsTranslation(transform: Matrix44CMO): Offset? {
    // transform.mat are stored in column-major order.
    return if (transform.mat[0] == 1f && // col 1
        transform.mat[1] == 0f &&
        transform.mat[2] == 0f &&
        transform.mat[3] == 0f &&
        transform.mat[4] == 0f && // col 2
        transform.mat[5] == 1f &&
        transform.mat[6] == 0f &&
        transform.mat[7] == 0f &&
        transform.mat[8] == 0f && // col 3
        transform.mat[9] == 0f &&
        transform.mat[10] == 1f &&
        transform.mat[11] == 0f &&
        transform.mat[14] == 0f && // bottom of col 4 (transform.mat 12 and 13 are the x and y offsets)
        transform.mat[15] == 1f) {
        Offset(transform.mat[12], transform.mat[13])
    } else null
}


fun computeRotation(radians: Float): Matrix44CMO {
    assert(radians.isFinite()) {
        "Cannot compute the rotation matrix for a non-finite angle: $radians"
    }
    if (radians == 0f) {
        return Matrix44CMO.identity()
    }
    val rSin: Float = sin(radians)
    if (rSin == 1f) {
        return createZRotation(1f, 0f)
    }
    if (rSin == -1f) {
        return createZRotation(-1f, 0f)
    }
    val rCos: Float = cos(radians)
    if (rCos == -1f) {
        return createZRotation(0f, -1f)
    }
    return createZRotation(rSin, rCos)
}

fun createZRotation(rSin: Float, rCos: Float): Matrix44CMO {
    val m = Matrix44CMO.zero()
    m.mat[0] = rCos
    m.mat[1] = rSin
    m.mat[4] = -rSin
    m.mat[5] = rCos
    m.mat[10] = 1f
    m.mat[15] = 1f
    return m
}