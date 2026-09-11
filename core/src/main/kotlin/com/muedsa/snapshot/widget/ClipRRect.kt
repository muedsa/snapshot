package com.muedsa.snapshot.widget

import com.muedsa.geometry.Size
import com.muedsa.snapshot.paint.decoration.BorderRadius
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderClipRRect
import org.jetbrains.skia.RRect

inline fun ChildSlot.ClipRRect(
    borderRadius: BorderRadius = BorderRadius.ZERO,
    noinline clipper: ((Size) -> RRect)? = null,
    clipBehavior: ClipBehavior = ClipBehavior.ANTI_ALIAS,
    content: ClipRRect.() -> Unit = {},
) {
    attach(
        com.muedsa.snapshot.widget.ClipRRect(
            borderRadius = borderRadius,
            clipper = clipper,
            clipBehavior = clipBehavior,
        ).apply(content)
    )
}

class ClipRRect(
    var borderRadius: BorderRadius = BorderRadius.ZERO,
    var clipper: ((Size) -> RRect)? = null,
    var clipBehavior: ClipBehavior = ClipBehavior.ANTI_ALIAS,
) : SingleChildWidget() {

    override fun createRenderBox(child: Widget?): RenderBox = RenderClipRRect(
        borderRadius = borderRadius,
        clipper = clipper,
        clipBehavior = clipBehavior,
    ).also { p ->
        child?.createRenderBox()?.let {
            p.appendChild(it)
        }
    }
}
