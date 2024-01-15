package com.muedsa.snapshot.widget

import com.muedsa.geometry.Size
import com.muedsa.snapshot.paint.decoration.BorderRadius
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderClipRRect
import org.jetbrains.skia.RRect

class ClipRRect(
    val borderRadius: BorderRadius = BorderRadius.ZERO,
    val clipper: ((Size) -> RRect)? = null,
    val clipBehavior: ClipBehavior = ClipBehavior.ANTI_ALIAS,
    childBuilder: SingleWidgetBuilder? = null,
) : SingleChildWidget(childBuilder = childBuilder) {

    override fun createRenderTree(): RenderBox = RenderClipRRect(
        borderRadius = borderRadius,
        clipper = clipper,
        clipBehavior = clipBehavior,
        child = child?.createRenderTree()
    )
}