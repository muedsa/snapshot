package com.muedsa.snapshot.widget

import com.muedsa.geometry.Size
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderClipOval
import org.jetbrains.skia.Rect

inline fun Widget.ClipOval(
    noinline clipper: ((Size) -> Rect)? = null,
    clipBehavior: ClipBehavior = ClipBehavior.ANTI_ALIAS,
    content: ClipOval.() -> Unit = {},
) {
    buildChild(
        widget = ClipOval(
            clipper = clipper,
            clipBehavior = clipBehavior,
            parent = this
        ),
        content = content
    )
}

class ClipOval(
    val clipper: ((Size) -> Rect)? = null,
    val clipBehavior: ClipBehavior = ClipBehavior.ANTI_ALIAS,
    parent: Widget? = null,
) : SingleChildWidget(parent = parent) {

    override fun createRenderBox(child: Widget?): RenderBox = RenderClipOval(
        clipper = clipper,
        clipBehavior = clipBehavior,
    ).also { p ->
        child?.createRenderBox()?.let {
            p.appendChild(it)
        }
    }
}