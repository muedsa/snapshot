package com.muedsa.snapshot.widget

import com.muedsa.geometry.AlignmentDirectional
import com.muedsa.geometry.AlignmentGeometry
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.stack.RenderStack
import com.muedsa.snapshot.rendering.stack.StackFit
import org.jetbrains.skia.paragraph.Direction

inline fun ChildSlot.Stack(
    alignment: AlignmentGeometry = AlignmentDirectional.TOP_START,
    textDirection: Direction = Direction.LTR,
    fit: StackFit = StackFit.LOOSE,
    clipBehavior: ClipBehavior = ClipBehavior.HARD_EDGE,
    content: Stack.() -> Unit = {},
) {
    attach(
        com.muedsa.snapshot.widget.Stack(
            alignment = alignment,
            textDirection = textDirection,
            fit = fit,
            clipBehavior = clipBehavior,
        ).apply(content)
    )
}

class Stack(
    var alignment: AlignmentGeometry = AlignmentDirectional.TOP_START,
    var textDirection: Direction = Direction.LTR,
    var fit: StackFit = StackFit.LOOSE,
    var clipBehavior: ClipBehavior = ClipBehavior.HARD_EDGE,
) : MultiChildWidget() {

    override fun createRenderBox(children: List<Widget>): RenderBox = RenderStack(
        alignment = alignment,
        textDirection = textDirection,
        fit = fit,
        clipBehavior = clipBehavior,
    ).also { p ->
        children.createRenderBox()?.let {
            p.appendChildren(it)
        }
    }
}
