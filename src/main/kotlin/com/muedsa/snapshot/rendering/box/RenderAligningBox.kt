package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.Alignment

abstract class RenderAligningBox(
    val alignment: Alignment = Alignment.CENTER,
    child : RenderBox? = null
) : RenderSingleChildBox(child = child) {

    protected fun alignChild() {
        val childParentData = child!!.parentData!!
        childParentData.offset = alignment.alongOffset(definiteSize - child.definiteSize)
    }
}