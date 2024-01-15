package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.Size

class RenderSizedOverflowBox(
    val requestedSize: Size,
    alignment: BoxAlignment = BoxAlignment.CENTER,
    child: RenderBox? = null,
) : RenderAligningBox(
    alignment = alignment,
    child = child
) {

    override fun performLayout() {
        size = definiteConstraints.constrain(requestedSize)
        if (child != null) {
            child.layout(definiteConstraints)
            alignChild()
        }
    }
}