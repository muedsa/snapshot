package com.muedsa.snapshot.paint

import com.muedsa.geometry.Matrix44CMO
import org.jetbrains.skia.Rect

abstract class GradientTransform {
    abstract fun transform(bounds: Rect): Matrix44CMO?
}