package com.muedsa.snapshot.widget

import com.muedsa.geometry.Size
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderClipOval
import org.jetbrains.skia.Rect

class ClipOval(
    val clipper: ((Size) -> Rect)? = null,
    val clipBehavior: ClipBehavior = ClipBehavior.ANTI_ALIAS,
    childBuilder: SingleWidgetBuilder? = null,
) : SingleChildWidget(childBuilder = childBuilder) {

    override fun createRenderTree(): RenderBox = RenderClipOval(
        clipper = clipper,
        clipBehavior = clipBehavior,
        child = child?.createRenderTree()
    )
}