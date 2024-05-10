package com.muedsa.snapshot.widget

import com.muedsa.geometry.Size
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderClipPath
import org.jetbrains.skia.Path

inline fun Widget.ClipPath(
    noinline clipper: ((Size) -> Path)? = null,
    clipBehavior: ClipBehavior = ClipBehavior.ANTI_ALIAS,
    content: ClipPath.() -> Unit = {},
) {
    buildChild(
        widget = ClipPath(
            clipper = clipper,
            clipBehavior = clipBehavior,
            parent = this
        ),
        content = content
    )
}

class ClipPath(
    var clipper: ((Size) -> Path)? = null,
    var clipBehavior: ClipBehavior = ClipBehavior.ANTI_ALIAS,
    parent: Widget? = null,
) : SingleChildWidget(parent = parent) {

    override fun createRenderBox(child: Widget?): RenderBox = RenderClipPath(
        clipper = clipper,
        clipBehavior = clipBehavior,
    ).also { p ->
        child?.createRenderBox()?.let {
            p.appendChild(it)
        }
    }
}