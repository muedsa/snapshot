package com.muedsa.snapshot.widget

import com.muedsa.geometry.Size
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderClipPath
import org.jetbrains.skia.Path

inline fun ChildSlot.ClipPath(
    noinline clipper: ((Size) -> Path)? = null,
    clipBehavior: ClipBehavior = ClipBehavior.ANTI_ALIAS,
    content: ClipPath.() -> Unit = {},
) {
    attach(
        com.muedsa.snapshot.widget.ClipPath(
            clipper = clipper,
            clipBehavior = clipBehavior,
        ).apply(content)
    )
}

class ClipPath(
    var clipper: ((Size) -> Path)? = null,
    var clipBehavior: ClipBehavior = ClipBehavior.ANTI_ALIAS,
) : SingleChildWidget() {

    override fun createRenderBox(child: Widget?): RenderBox = RenderClipPath(
        clipper = clipper,
        clipBehavior = clipBehavior,
    ).also { p ->
        child?.createRenderBox()?.let {
            p.appendChild(it)
        }
    }
}
