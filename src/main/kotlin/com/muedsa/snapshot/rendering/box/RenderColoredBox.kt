package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import com.muedsa.snapshot.rendering.PaintingContext
import org.jetbrains.skia.Paint

class RenderColoredBox(
    var color: Int,
    child: RenderBox? = null,
) : RenderSingleChildBox(child = child) {

    override fun paint(context: PaintingContext, offset: Offset) {
        val size = definiteSize
        if (size > Size.ZERO) {
            val paint = Paint()
            paint.color = color
            context.canvas.drawRect(offset combine size, paint)
        }
        child?.let {
            context.paintChild(it, offset + it.parentData!!.offset)
        }
    }
}