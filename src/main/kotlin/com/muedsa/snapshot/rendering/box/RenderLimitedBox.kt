package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.Size

class RenderLimitedBox(
    val maxWidth: Float = Float.POSITIVE_INFINITY,
    val maxHeight: Float = Float.POSITIVE_INFINITY,
) : RenderSingleChildBox() {

    private fun limitConstraints(constraints: BoxConstraints): BoxConstraints = BoxConstraints(
        minWidth = constraints.minWidth,
        maxWidth = if (constraints.hasBoundedWidth) constraints.maxWidth else constraints.constrainWidth(maxWidth),
        minHeight = constraints.minHeight,
        maxHeight = if (constraints.hasBoundedHeight) constraints.maxHeight else constraints.constrainHeight(maxHeight)
    )

    override fun performLayout() {
        val currentChild: RenderBox? = this.child
        size = if (currentChild != null) {
            currentChild.layout(limitConstraints(definiteConstraints))
            definiteConstraints.constrain(currentChild.definiteSize)
        } else {
            limitConstraints(definiteConstraints).constrain(Size.ZERO)
        }
    }
}