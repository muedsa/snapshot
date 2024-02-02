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
) : RenderPositionedBox(
    alignment = alignment,
    textDirection = textDirection,
) {

    private fun getInnerConstraints(constraints: BoxConstraints): BoxConstraints = BoxConstraints(
        minWidth = minWidth ?: constraints.minWidth,
        maxWidth = maxWidth ?: constraints.maxWidth,
        minHeight = minHeight ?: constraints.minHeight,
        maxHeight = maxHeight ?: constraints.maxHeight
    )

    override fun performLayout() {
        size = definiteConstraints.biggest
        val currentChild: RenderBox? = this.child
        if (currentChild != null) {
            currentChild.layout(getInnerConstraints(definiteConstraints))
            alignChild()
        }
    }
}