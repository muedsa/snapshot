package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.AlignmentGeometry
import com.muedsa.geometry.BoxAlignment
import org.jetbrains.skia.paragraph.Direction

abstract class RenderAligningBox(
    val alignment: AlignmentGeometry = BoxAlignment.CENTER,
    val textDirection: Direction? = null,
) : RenderSingleChildBox() {

    val resolveAlignment: BoxAlignment = alignment.resolve(textDirection)

    protected fun alignChild() {
        val currentChild: RenderBox = this.child!!
        val childParentData = currentChild.parentData!!
        childParentData.offset = resolveAlignment.alongOffset(definiteSize - currentChild.definiteSize)
    }
}