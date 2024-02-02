package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.Size

class RenderSizedOverflowBox(
    val requestedSize: Size,
    alignment: BoxAlignment = BoxAlignment.CENTER,
) : RenderAligningBox(
    alignment = alignment,
) {

    override fun performLayout() {
        size = definiteConstraints.constrain(requestedSize)
        val currentChild: RenderBox? = this.child
        if (currentChild != null) {
            currentChild.layout(definiteConstraints)
            alignChild()
        }
    }
}