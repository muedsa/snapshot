package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.rendering.PaintingContext
import org.jetbrains.skia.ColorFilter

internal class ColorFilterRenderObject(
    val colorFilter: ColorFilter,
) : RenderSingleChildBox() {

    override fun paint(context: PaintingContext, offset: Offset) {
        if (child != null) {
            context.pushColorFilter(
                offset = offset,
                colorFilter = colorFilter
            ) { c, o -> super.paint(c, o) }
        }
    }
}