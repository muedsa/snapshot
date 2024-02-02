package com.muedsa.snapshot.widget

import com.muedsa.geometry.AlignmentDirectional
import com.muedsa.geometry.AlignmentGeometry
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.stack.RenderStack
import com.muedsa.snapshot.rendering.stack.StackFit
import org.jetbrains.skia.paragraph.Direction

inline fun Widget.Stack(
    alignment: AlignmentGeometry = AlignmentDirectional.TOP_START,
    textDirection: Direction = Direction.LTR,
    fit: StackFit = StackFit.LOOSE,
    clipBehavior: ClipBehavior = ClipBehavior.HARD_EDGE,
    content: Stack.() -> Unit = {},
) {
    buildChild(
        widget = Stack(
            alignment = alignment,
            textDirection = textDirection,
            fit = fit,
            clipBehavior = clipBehavior,
            parent = this
        ),
        content = content
    )
}

class Stack(
    val alignment: AlignmentGeometry = AlignmentDirectional.TOP_START,
    val textDirection: Direction = Direction.LTR,
    val fit: StackFit = StackFit.LOOSE,
    val clipBehavior: ClipBehavior = ClipBehavior.HARD_EDGE,
    parent: Widget? = null,
) : MultiChildWidget(parent = parent) {

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