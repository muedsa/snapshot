package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.rendering.PaintingContext
import org.jetbrains.skia.ImageFilter

internal class ImageFilterRenderObject(
    val imageFilter: ImageFilter,
    child: RenderBox? = null,
) : RenderSingleChildBox(
    child = child
) {

    override fun paint(context: PaintingContext, offset: Offset) {
        if (child != null) {
            context.pushImageFilter(
                offset = offset,
                imageFilter = imageFilter
            ) { c, o -> super.paint(c, o) }
        }
    }
}