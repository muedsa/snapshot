package com.muedsa.snapshot.widget

import com.muedsa.geometry.AlignmentDirectional
import com.muedsa.geometry.AlignmentGeometry
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.stack.RenderIndexedStack
import com.muedsa.snapshot.rendering.stack.StackFit
import org.jetbrains.skia.paragraph.Direction

inline fun ChildSlot.IndexedStack(
    index: Int? = 0,
    alignment: AlignmentGeometry = AlignmentDirectional.TOP_START,
    textDirection: Direction = Direction.LTR,
    fit: StackFit = StackFit.LOOSE,
    clipBehavior: ClipBehavior = ClipBehavior.HARD_EDGE,
    content: IndexedStack.() -> Unit = {},
) {
    attach(
        com.muedsa.snapshot.widget.IndexedStack(
            index = index,
            alignment = alignment,
            textDirection = textDirection,
            fit = fit,
            clipBehavior = clipBehavior,
        ).apply(content)
    )
}

/** 布局全部子节点，只绘制 [index] 指向的子节点。 */
class IndexedStack(
    var index: Int? = 0,
    var alignment: AlignmentGeometry = AlignmentDirectional.TOP_START,
    var textDirection: Direction = Direction.LTR,
    var fit: StackFit = StackFit.LOOSE,
    var clipBehavior: ClipBehavior = ClipBehavior.HARD_EDGE,
) : MultiChildWidget() {

    override fun createRenderBox(children: List<Widget>): RenderBox = RenderIndexedStack(
        index = index,
        alignment = alignment,
        textDirection = textDirection,
        fit = fit,
        clipBehavior = clipBehavior,
    ).also { renderBox ->
        children.createRenderBox()?.let(renderBox::appendChildren)
    }
}
