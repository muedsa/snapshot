package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import com.muedsa.snapshot.rendering.PaintingContext
import org.jetbrains.skia.Paint

class RenderConstrainedBox(
    var additionalConstraints: BoxConstraints,
) : RenderSingleChildBox() {

    override fun performLayout() {
        val currentChild: RenderBox? = this.child
        if (currentChild != null) {
            currentChild.layout(additionalConstraints.enforce(definiteConstraints))
            size = currentChild.definiteSize
        } else {
            size = additionalConstraints.enforce(definiteConstraints).constrain(Size.ZERO)
        }
    }

    override fun debugPaint(context: PaintingContext, offset: Offset) {
        val currentChild: RenderBox? = this.child
        super.debugPaint(context, offset)
        if (currentChild == null || currentChild.definiteSize.isEmpty) {
            context.canvas.drawRect(offset combine definiteSize, Paint().setARGB(144, 144, 144, 144))
        }
    }
}