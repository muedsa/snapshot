package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.Size

class RenderLimitedBox(
    val maxWidth: Float = Float.POSITIVE_INFINITY,
    val maxHeight: Float = Float.POSITIVE_INFINITY,
    child: RenderBox? = null,
) : RenderSingleChildBox(
    child = child
) {

    private fun limitConstraints(constraints: BoxConstraints): BoxConstraints = BoxConstraints(
        minWidth = constraints.minWidth,
        maxWidth = if (constraints.hasBoundedWidth) constraints.maxWidth else constraints.constrainWidth(maxWidth),
        minHeight = constraints.minHeight,
        maxHeight = if (constraints.hasBoundedHeight) constraints.maxHeight else constraints.constrainHeight(maxHeight)
    )

    override fun performLayout() {
        size = if (child != null) {
            child.layout(limitConstraints(definiteConstraints))
            definiteConstraints.constrain(child.definiteSize)
        } else {
            limitConstraints(definiteConstraints).constrain(Size.ZERO)
        }
    }
}