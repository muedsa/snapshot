package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.rendering.PaintingContext
import org.jetbrains.skia.Rect

class RenderOpacity(
    val opacity: Float = 1f,
) : RenderSingleChildBox() {

    internal override fun getFilterPaintBounds(): Rect? =
        if (opacity == 0f) EMPTY_FILTER_PAINT_BOUNDS else super.getFilterPaintBounds()

    init {
        require(opacity in 0f..1f) { "opacity must be between 0 and 1" }
    }

    override fun paint(context: PaintingContext, offset: Offset) {
        if (child == null || opacity == 0f) {
            return
        }
        context.pushOpacity(
            offset = offset,
            opacity = opacity
        ) { c, o ->
            super.paint(c, o)
        }
    }
}
