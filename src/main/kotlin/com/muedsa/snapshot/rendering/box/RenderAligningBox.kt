package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.AlignmentGeometry
import com.muedsa.geometry.BoxAlignment
import org.jetbrains.skia.paragraph.Direction

abstract class RenderAligningBox(
    val alignment: AlignmentGeometry = BoxAlignment.CENTER,
    val textDirection: Direction? = null,
    child : RenderBox? = null
) : RenderSingleChildBox(child = child) {

    val resolveAlignment: BoxAlignment = alignment.resolve(textDirection)

    protected fun alignChild() {
        val childParentData = child!!.parentData!!
        childParentData.offset = resolveAlignment.alongOffset(definiteSize - child.definiteSize)
    }
}