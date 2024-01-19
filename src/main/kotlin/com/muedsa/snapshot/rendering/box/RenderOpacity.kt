package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.rendering.PaintingContext

class RenderOpacity(
    val opacity: Float = 1f,
    child: RenderBox? = null,
) : RenderSingleChildBox(child = child) {

    init {
        assert(opacity in 0f..1f)
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