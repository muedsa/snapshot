package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.AlignmentGeometry
import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.Size
import org.jetbrains.skia.paragraph.Direction

open class RenderPositionedBox(
    val widthFactor: Float? = null,
    val heightFactor: Float? = null,
    alignment: AlignmentGeometry = BoxAlignment.CENTER,
    textDirection: Direction? = null,
    child: RenderBox? = null,
) : RenderAligningBox(
    alignment = alignment,
    textDirection = textDirection,
    child = child
) {
    init {
        assert(widthFactor == null || widthFactor >= 0f)
        assert(heightFactor == null || heightFactor >= 0f)
    }

    override fun performLayout() {
        val shrinkWrapWidth: Boolean = widthFactor != null || definiteConstraints.maxWidth.isInfinite()
        val shrinkWrapHeight: Boolean = heightFactor != null || definiteConstraints.maxHeight.isInfinite()
        if (child != null) {
            child.layout(definiteConstraints.loosen())
            val childSize = child.definiteSize
            size = definiteConstraints.constrain(
                Size(
                    width = if (shrinkWrapWidth) childSize.width * (widthFactor ?: 1f) else Float.POSITIVE_INFINITY,
                    height = if (shrinkWrapHeight) childSize.height * (heightFactor ?: 1f) else Float.POSITIVE_INFINITY
                )
            )
            alignChild()
        } else {
            size = definiteConstraints.constrain(
                Size(
                    width = if (shrinkWrapWidth) 0f else Float.POSITIVE_INFINITY,
                    height = if (shrinkWrapHeight) 0f else Float.POSITIVE_INFINITY,
                )
            )
        }
    }
}