package com.muedsa.snapshot.widget

import com.muedsa.geometry.Size
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderClipOval
import org.jetbrains.skia.Rect

inline fun ChildSlot.ClipOval(
    noinline clipper: ((Size) -> Rect)? = null,
    clipBehavior: ClipBehavior = ClipBehavior.ANTI_ALIAS,
    content: ClipOval.() -> Unit = {},
) {
    attach(
        com.muedsa.snapshot.widget.ClipOval(
            clipper = clipper,
            clipBehavior = clipBehavior,
        ).apply(content)
    )
}

class ClipOval(
    var clipper: ((Size) -> Rect)? = null,
    var clipBehavior: ClipBehavior = ClipBehavior.ANTI_ALIAS,
) : SingleChildWidget() {

    override fun createRenderBox(child: Widget?): RenderBox = RenderClipOval(
        clipper = clipper,
        clipBehavior = clipBehavior,
    ).also { p ->
        child?.createRenderBox()?.let {
            p.appendChild(it)
        }
    }
}
