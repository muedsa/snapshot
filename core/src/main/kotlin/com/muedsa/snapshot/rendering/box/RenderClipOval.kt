package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import com.muedsa.snapshot.kDefaultFontSize
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.PaintingContext
import org.jetbrains.skia.Path
import org.jetbrains.skia.Rect

class RenderClipOval(
    clipper: ((Size) -> Rect)? = null,
    clipBehavior: ClipBehavior = ClipBehavior.ANTI_ALIAS,
) : RenderCustomClip<Rect>(
    clipper = clipper,
    clipBehavior = clipBehavior,
) {
    override val defaultClip: Rect
        get() = Offset.ZERO combine definiteSize

    private var cachedRect: Rect? = null
    private lateinit var cachedPath: Path

    private fun getClipPath(rect: Rect): Path {
        if (rect != cachedRect) {
            cachedRect = rect
            cachedPath = Path().addOval(cachedRect!!)
        }
        return cachedPath
    }

    override fun paint(context: PaintingContext, offset: Offset) {
        if (child != null) {
            if (clipBehavior != ClipBehavior.NONE) {
                context.pushClipPath(
                    offset = offset,
                    bounds = Offset.ZERO combine definiteSize,
                    clipPath = getClipPath(rect = getClip()),
                    clipBehavior = clipBehavior
                ) { c, o ->
                    super.paint(c, o)
                }
            } else {
                super.paint(context, offset)
            }
        }
    }

    override fun debugPaint(context: PaintingContext, offset: Offset) {
        if (child != null) {
            super.debugPaint(context, offset)
            if (clipBehavior != ClipBehavior.NONE) {
                context.canvas.drawPath(
                    Path().also { getClipPath(getClip()).offset(offset.x, offset.y, it) },
                    debugPaint!!
                )
                debugText!!.paint(
                    canvas = context.canvas,
                    offset = offset + Offset(
                        x = (getClip().width - debugText!!.width) / 2f,
                        y = -(debugText!!.text.style?.fontSize ?: kDefaultFontSize) * 1.1f
                    )
                )
            }
        }
    }
}