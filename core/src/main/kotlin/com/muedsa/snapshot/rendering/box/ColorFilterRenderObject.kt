package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.Offset
import com.muedsa.geometry.shift
import com.muedsa.snapshot.rendering.PaintingContext
import org.jetbrains.skia.ColorFilter

internal class ColorFilterRenderObject(
    val colorFilter: ColorFilter,
) : RenderSingleChildBox() {

    override fun paint(context: PaintingContext, offset: Offset) {
        if (child != null) {
            val currentChild = child!!
            val bounds = currentChild.getFilterPaintBounds()
                ?.shift(offset + currentChild.parentData!!.offset)
            context.pushColorFilter(
                offset = offset,
                colorFilter = colorFilter,
                bounds = bounds,
            ) { c, o -> super.paint(c, o) }
        }
    }
}
