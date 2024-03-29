package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.EdgeInsets
import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import com.muedsa.snapshot.rendering.PaintingContext
import org.jetbrains.skia.*


class RenderPadding(
    val padding: EdgeInsets,
) : RenderSingleChildBox() {

    override fun performLayout() {
        val currentChild: RenderBox? = this.child
        if (currentChild == null) {
            size = definiteConstraints.constrain(
                Size(
                    padding.left + padding.right,
                    padding.top + padding.bottom
                )
            )
        } else {
            val innerConstraints = definiteConstraints.deflate(padding)
            currentChild.layout(innerConstraints)
            currentChild.parentData!!.offset = Offset(x = padding.left, y = padding.top)
            size = definiteConstraints.constrain(
                Size(
                    width = padding.left + currentChild.definiteSize.width + padding.right,
                    height = padding.top + currentChild.definiteSize.height + padding.bottom
                )
            )
        }
    }

    override fun debugPaint(context: PaintingContext, offset: Offset) {
        super.debugPaint(context, offset)
        val outerRect: Rect = offset combine definiteSize
        if (child != null) {
            val innerRect: Rect = padding.deflateRect(outerRect)
            context.canvas.drawPath(
                Path().apply {
                    fillMode = PathFillMode.EVEN_ODD
                    addRect(outerRect)
                    addRect(innerRect)
                },
                Paint().apply { color = Color.makeARGB(144, 0, 144, 255) }
            )
            context.canvas.drawPath(
                Path().apply {
                    fillMode = PathFillMode.EVEN_ODD
                    addRect(innerRect.inflate(2f).intersect(outerRect)!!)
                    addRect(innerRect)
                },
                Paint().apply { color = Color.makeARGB(255, 0, 144, 255) })
        } else {
            context.canvas.drawRect(outerRect, Paint().apply { color = Color.makeARGB(144, 144, 144, 144) })
        }
    }
}