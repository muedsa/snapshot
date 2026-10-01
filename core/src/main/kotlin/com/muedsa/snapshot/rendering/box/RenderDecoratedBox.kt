package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.Offset
import com.muedsa.geometry.shift
import com.muedsa.snapshot.paint.decoration.BoxPainter
import com.muedsa.snapshot.paint.decoration.BoxDecoration
import com.muedsa.snapshot.paint.decoration.Decoration
import com.muedsa.snapshot.rendering.PaintingContext
import org.jetbrains.skia.Rect
import kotlin.math.max

class RenderDecoratedBox(
    val decoration: Decoration,
    val position: DecorationPosition = DecorationPosition.BACKGROUND,
) : RenderSingleChildBox() {

    val painter: BoxPainter = decoration.createBoxPainter()

    internal override fun getFilterPaintBounds(): Rect? {
        val boxDecoration = decoration as? BoxDecoration ?: return null
        val rect = getPaintBounds()
        var bounds: Rect? = super.getFilterPaintBounds()
        if (boxDecoration.color != null || boxDecoration.gradient != null ||
            boxDecoration.image != null || boxDecoration.border != null
        ) {
            bounds = bounds.unionFilterPaintBounds(rect)
        }
        boxDecoration.boxShadow?.forEach { shadow ->
            val blurExtent = 3f * shadow.blurSigma
            val shadowBounds = rect.shift(shadow.offset)
                .inflate(max(0f, shadow.spreadRadius) + blurExtent)
            bounds = bounds.unionFilterPaintBounds(shadowBounds)
        }
        return bounds
    }

    override fun paint(context: PaintingContext, offset: Offset) {
        if (position == DecorationPosition.BACKGROUND) {
            painter.paint(context.canvas, offset, definiteSize)
        }
        super.paint(context, offset)
        if (position == DecorationPosition.FOREGROUND) {
            painter.paint(context.canvas, offset, definiteSize)
        }
    }
}
