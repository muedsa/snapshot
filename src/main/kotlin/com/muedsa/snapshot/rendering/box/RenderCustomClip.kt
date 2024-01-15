package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.Size
import com.muedsa.snapshot.rendering.ClipBehavior

abstract class RenderCustomClip<T>(
    val clipper: ((Size) -> T)? = null,
    val clipBehavior: ClipBehavior = ClipBehavior.ANTI_ALIAS,
    child: RenderBox? = null,
) : RenderSingleChildBox(child = child) {

    abstract val defaultClip: T

    val clip: T
        get() = clipper?.invoke(definiteSize) ?: defaultClip
}