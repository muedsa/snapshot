package com.muedsa.snapshot.paint

import org.jetbrains.skia.Color
import kotlin.math.roundToInt

fun Color.scaleAlpha(a: Int, factor: Float): Int = withA(a, (getA(a) * factor).roundToInt().coerceIn(0, 255))