package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.rendering.PaintingContext
import org.jetbrains.skia.ColorFilter

internal class ColorFilterRenderObject(
    val colorFilter: ColorFilter,
    child: RenderBox? = null,
) : RenderSingleChildBox(
    child = child
) {

    override fun paint(context: PaintingContext, offset: Offset) {
        if (child != null) {
            context.pushColorFilter(
                offset = offset,
                colorFilter = colorFilter
            ) { c, o -> super.paint(c, o) }
        }
    }
}