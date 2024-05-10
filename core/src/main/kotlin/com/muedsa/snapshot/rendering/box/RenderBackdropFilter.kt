package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.rendering.PaintingContext
import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.ImageFilter

class RenderBackdropFilter(
    val imageFilter: ImageFilter,
    val blendMode: BlendMode = BlendMode.SRC_OVER,
) : RenderSingleChildBox() {

    override fun paint(context: PaintingContext, offset: Offset) {
        if (child != null) {
            context.pushBackDropFilter(
                offset = offset,
                imageFilter = imageFilter,
                blendMode = blendMode
            ) { c, o ->
                super.paint(c, o)
            }
        }
    }
}