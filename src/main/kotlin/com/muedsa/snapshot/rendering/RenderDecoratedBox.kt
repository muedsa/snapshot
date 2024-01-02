package com.muedsa.snapshot.rendering

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.paint.BoxPainter
import com.muedsa.snapshot.paint.Decoration

class RenderDecoratedBox(
    val decoration: Decoration,
    val position: DecorationPosition = DecorationPosition.BACKGROUND,
    child: RenderBox? = null,
) : RenderSingleChildBox(child = child) {

    val painter: BoxPainter = decoration.createBoxPainter()

    override fun paint(context: PaintingContext, offset: Offset) {
        if (position == DecorationPosition.BACKGROUND) {
            painter.paint(context.canvas, offset, definiteSize)
        }
        super.paint(context, offset)
        if (position == DecorationPosition.FOREGROUND) {
            painter.paint(context.canvas, offset, definiteSize)
        }
        super.paint(context, offset)
    }
}