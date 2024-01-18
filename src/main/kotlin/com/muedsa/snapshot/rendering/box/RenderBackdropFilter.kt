package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.rendering.PaintingContext
import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.ImageFilter

class RenderBackdropFilter(
    val imageFilter: ImageFilter,
    val blendMode: BlendMode = BlendMode.SRC_OVER,
    child: RenderBox? = null,
) : RenderSingleChildBox(
    child = child
) {

    override fun paint(context: PaintingContext, offset: Offset) {
        if (child != null) {
            context.pushBackdrop(
                offset = offset,
                clipRect = Offset.ZERO combine definiteSize,
                imageFilter = imageFilter,
                blendMode = blendMode
            )
        }
        super.paint(context, offset)
    }
}