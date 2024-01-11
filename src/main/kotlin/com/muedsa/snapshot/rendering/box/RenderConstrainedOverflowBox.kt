package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.AlignmentGeometry
import com.muedsa.geometry.BoxAlignment
import org.jetbrains.skia.paragraph.Direction

class RenderConstrainedOverflowBox(
    val minWidth: Float? = null,
    val maxWidth: Float? = null,
    val minHeight: Float? = null,
    val maxHeight: Float? = null,
    alignment: AlignmentGeometry = BoxAlignment.CENTER,
    textDirection: Direction? = null,
    child: RenderBox? = null,
) : RenderPositionedBox(
    alignment = alignment,
    textDirection = textDirection,
    child = child
) {

    private fun getInnerConstraints(constraints: BoxConstraints): BoxConstraints = BoxConstraints(
        minWidth = minWidth ?: constraints.minWidth,
        maxWidth = maxWidth ?: constraints.maxWidth,
        minHeight = minHeight ?: constraints.minHeight,
        maxHeight = maxHeight ?: constraints.maxHeight
    )

    override fun performLayout() {
        size = definiteConstraints.biggest
        if (child != null) {
            child.layout(getInnerConstraints(definiteConstraints))
            alignChild()
        }
    }
}