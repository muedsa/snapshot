package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import com.muedsa.geometry.shift
import com.muedsa.geometry.tlRadiusX
import com.muedsa.snapshot.paint.decoration.BorderRadius
import com.muedsa.snapshot.paint.decoration.BorderRadiusGeometry
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.PaintingContext
import org.jetbrains.skia.RRect
import org.jetbrains.skia.paragraph.Direction

class RenderClipRRect(
    val borderRadius: BorderRadiusGeometry = BorderRadius.ZERO,
    clipper: ((Size) -> RRect)? = null,
    clipBehavior: ClipBehavior = ClipBehavior.ANTI_ALIAS,
    val textDirection: Direction? = null,
) : RenderCustomClip<RRect>(
    clipper = clipper,
    clipBehavior = clipBehavior
) {

    override val defaultClip: RRect
        get() = borderRadius.resolve(textDirection).toRRect(rect = Offset.ZERO combine definiteSize)

    override fun paint(context: PaintingContext, offset: Offset) {
        if (child != null) {
            if (clipBehavior != ClipBehavior.NONE) {
                context.pushClipRRect(
                    offset = offset,
                    bounds = getClip(),
                    clipRRect = getClip(),
                    clipBehavior = clipBehavior
                ) { c, o ->
                    super.paint(c, o)
                }
            } else {
                super.paint(context, offset)
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun debugPaint(context: PaintingContext, offset: Offset) {
        if (child != null) {
            super.debugPaint(context, offset)
            if (clipBehavior != ClipBehavior.NONE) {
                context.canvas.drawRRect(getClip().shift(offset), debugPaint!!)
                debugText!!.paint(context.canvas, offset + Offset(getClip().tlRadiusX, -debugText!!.fontSize * 1.1f))
            }
        }
    }
}