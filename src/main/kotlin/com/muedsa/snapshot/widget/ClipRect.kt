package com.muedsa.snapshot.widget

import com.muedsa.geometry.Size
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderClipRect
import org.jetbrains.skia.Rect

class ClipRect(
    val clipper: ((Size) -> Rect)? = null,
    val clipBehavior: ClipBehavior = ClipBehavior.HARD_EDGE,
    childBuilder: SingleWidgetBuilder? = null,
) : SingleChildWidget(childBuilder = childBuilder) {

    override fun createRenderTree(): RenderBox = RenderClipRect(
        clipper = clipper,
        clipBehavior = clipBehavior,
        child = child?.createRenderBox()
    )
}