package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.rendering.BackdropFilterLayer
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
            val layer = BackdropFilterLayer(
                bounds = context.estimatedBounds,
                filter = imageFilter,
                blendMode = blendMode,
            )
            context.pushLayer(
                childLayer = layer,
                painter = { c, o -> super.paint(c, o) },
                offset = offset
            )
        }
    }
}