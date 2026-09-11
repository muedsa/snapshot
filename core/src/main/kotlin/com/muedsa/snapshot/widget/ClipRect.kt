package com.muedsa.snapshot.widget

import com.muedsa.geometry.Size
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderClipRect
import org.jetbrains.skia.Rect

inline fun ChildSlot.ClipRect(
    noinline clipper: ((Size) -> Rect)? = null,
    clipBehavior: ClipBehavior = ClipBehavior.HARD_EDGE,
    content: ClipRect.() -> Unit = {},
) {
    attach(
        com.muedsa.snapshot.widget.ClipRect(
            clipper = clipper,
            clipBehavior = clipBehavior,
        ).apply(content)
    )
}

class ClipRect(
    var clipper: ((Size) -> Rect)? = null,
    var clipBehavior: ClipBehavior = ClipBehavior.HARD_EDGE,
) : SingleChildWidget() {

    override fun createRenderBox(child: Widget?): RenderBox = RenderClipRect(
        clipper = clipper,
        clipBehavior = clipBehavior,
    ).also { p ->
        child?.createRenderBox()?.let {
            p.appendChild(it)
        }
    }
}
