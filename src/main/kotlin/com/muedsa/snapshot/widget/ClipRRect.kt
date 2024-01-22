package com.muedsa.snapshot.widget

import com.muedsa.geometry.Size
import com.muedsa.snapshot.paint.decoration.BorderRadius
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderClipRRect
import org.jetbrains.skia.RRect

inline fun Widget.ClipRRect(
    borderRadius: BorderRadius = BorderRadius.ZERO,
    noinline clipper: ((Size) -> RRect)? = null,
    clipBehavior: ClipBehavior = ClipBehavior.ANTI_ALIAS,
    content: ClipRRect.() -> Unit = {},
) {
    buildChild(
        widget = ClipRRect(
            borderRadius = borderRadius,
            clipper = clipper,
            clipBehavior = clipBehavior,
            parent = this
        ),
        content = content
    )
}

class ClipRRect(
    val borderRadius: BorderRadius = BorderRadius.ZERO,
    val clipper: ((Size) -> RRect)? = null,
    val clipBehavior: ClipBehavior = ClipBehavior.ANTI_ALIAS,
    parent: Widget? = null,
) : SingleChildWidget(parent = parent) {

    override fun createRenderBox(child: Widget?): RenderBox = RenderClipRRect(
        borderRadius = borderRadius,
        clipper = clipper,
        clipBehavior = clipBehavior,
        child = child?.createRenderBox()
    )
}