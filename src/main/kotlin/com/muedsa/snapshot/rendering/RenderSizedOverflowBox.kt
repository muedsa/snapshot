package com.muedsa.snapshot.rendering

import com.muedsa.geometry.Alignment
import com.muedsa.geometry.Size

class RenderSizedOverflowBox(
    val requestedSize: Size,
    alignment: Alignment = Alignment.CENTER,
    child: RenderBox? = null
) : RenderAligningBox(
    alignment = alignment,
    child = child
) {

    override fun computeMinIntrinsicWidth(height: Float): Float = requestedSize.width

    override fun computeMaxIntrinsicWidth(height: Float): Float = requestedSize.width

    override fun computeMinIntrinsicHeight(width: Float): Float = requestedSize.height

    override fun computeMaxIntrinsicHeight(width: Float): Float = requestedSize.height

    override fun performLayout() {
        size = definiteConstraints.constrain(requestedSize)
        if (child != null) {
            child.layout(definiteConstraints)
            alignChild()
        }
    }
}