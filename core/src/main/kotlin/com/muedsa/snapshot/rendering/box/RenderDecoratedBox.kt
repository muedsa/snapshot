package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.paint.decoration.BoxPainter
import com.muedsa.snapshot.paint.decoration.Decoration
import com.muedsa.snapshot.rendering.PaintingContext

class RenderDecoratedBox(
    val decoration: Decoration,
    val position: DecorationPosition = DecorationPosition.BACKGROUND,
) : RenderSingleChildBox() {

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