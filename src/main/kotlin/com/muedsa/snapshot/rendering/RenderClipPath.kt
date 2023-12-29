package com.muedsa.snapshot.rendering

import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import org.jetbrains.skia.Path


class RenderClipPath(
    clipper: ((Size) -> Path)? = null,
    clipBehavior: ClipBehavior = ClipBehavior.ANTI_ALIAS,
    child: RenderBox? = null,
) : RenderCustomClip<Path>(
    clipper = clipper,
    clipBehavior = clipBehavior,
    child = child
) {
    override val defaultClip: Path
        get() = Path().addRect(Offset.ZERO combine definiteSize)


    override fun paint(context: PaintingContext, offset: Offset) {
        if (child != null) {
            if (clipBehavior != ClipBehavior.NONE) {
                context.doClipPath(
                    offset = offset,
                    bounds = Offset.ZERO combine definiteSize,
                    clipPath = clip,
                    clipBehavior = clipBehavior
                ) { c, o ->
                    super.paint(c, o)
                }
            } else {
                context.paintChild(child, offset)
            }
        }
    }
}