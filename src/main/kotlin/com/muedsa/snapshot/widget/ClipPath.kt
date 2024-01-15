package com.muedsa.snapshot.widget

import com.muedsa.geometry.Size
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderClipPath
import org.jetbrains.skia.Path

class ClipPath(
    val clipper: ((Size) -> Path)?,
    val clipBehavior: ClipBehavior = ClipBehavior.ANTI_ALIAS,
    childBuilder: SingleWidgetBuilder? = null,
) : SingleChildWidget(childBuilder = childBuilder) {

    override fun createRenderTree(): RenderBox = RenderClipPath(
        clipper = clipper,
        clipBehavior = clipBehavior,
        child = child?.createRenderTree()
    )
}