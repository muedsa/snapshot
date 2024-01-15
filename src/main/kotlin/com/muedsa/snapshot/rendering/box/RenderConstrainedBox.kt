package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import com.muedsa.snapshot.rendering.PaintingContext
import org.jetbrains.skia.Paint

class RenderConstrainedBox(
    var additionalConstraints: BoxConstraints,
    child: RenderBox? = null,
) : RenderSingleChildBox(child = child) {

    override fun performLayout() {
        if (child != null) {
            child.layout(additionalConstraints.enforce(definiteConstraints))
            size = child.definiteSize
        } else {
            size = additionalConstraints.enforce(definiteConstraints).constrain(Size.ZERO)
        }
    }

    override fun debugPaint(context: PaintingContext, offset: Offset) {
        super.debugPaint(context, offset)
        if (child == null || child.definiteSize.isEmpty) {
            context.canvas.drawRect(offset combine definiteSize, Paint().setARGB(144, 144, 144, 144))
        }
    }
}