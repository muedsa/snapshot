package com.muedsa.snapshot.widget

import com.muedsa.geometry.AlignmentDirectional
import com.muedsa.geometry.AlignmentGeometry
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.stack.RenderStack
import com.muedsa.snapshot.rendering.stack.StackFit
import org.jetbrains.skia.paragraph.Direction

class Stack(
    val alignment: AlignmentGeometry = AlignmentDirectional.TOP_START,
    val textDirection: Direction = Direction.LTR,
    val fit: StackFit = StackFit.LOOSE,
    val clipBehavior: ClipBehavior = ClipBehavior.HARD_EDGE,
    childrenBuilder: MultiWidgetBuilder? = null,
) : MultiChildWidget(
    childrenBuilder = childrenBuilder
) {
    override fun createRenderTree(): RenderBox = RenderStack(
        alignment = alignment,
        textDirection = textDirection,
        fit = fit,
        clipBehavior = clipBehavior,
        children = children?.createRenderTree()
    )
}