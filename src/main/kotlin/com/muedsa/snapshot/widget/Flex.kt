package com.muedsa.snapshot.widget

import com.muedsa.snapshot.paint.Axis
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.flex.*
import org.jetbrains.skia.paragraph.BaselineMode
import org.jetbrains.skia.paragraph.Direction

inline fun Widget.Flex(
    direction: Axis,
    mainAxisAlignment: MainAxisAlignment = MainAxisAlignment.START,
    mainAxisSize: MainAxisSize = MainAxisSize.MAX,
    crossAxisAlignment: CrossAxisAlignment = CrossAxisAlignment.CENTER,
    textDirection: Direction? = null,
    verticalDirection: VerticalDirection = VerticalDirection.DOWN,
    textBaseline: BaselineMode? = null,
    clipBehavior: ClipBehavior = ClipBehavior.NONE,
    content: Flex.() -> Unit = {},
) {
    buildChild(
        widget = Flex(
            direction = direction,
            mainAxisSize = mainAxisSize,
            mainAxisAlignment = mainAxisAlignment,
            crossAxisAlignment = crossAxisAlignment,
            textDirection = textDirection,
            verticalDirection = verticalDirection,
            textBaseline = textBaseline,
            clipBehavior = clipBehavior,
            parent = this
        ),
        content = content
    )
}

open class Flex(
    val direction: Axis,
    val mainAxisAlignment: MainAxisAlignment = MainAxisAlignment.START,
    val mainAxisSize: MainAxisSize = MainAxisSize.MAX,
    val crossAxisAlignment: CrossAxisAlignment = CrossAxisAlignment.CENTER,
    val textDirection: Direction? = null,
    val verticalDirection: VerticalDirection = VerticalDirection.DOWN,
    val textBaseline: BaselineMode? = null,
    val clipBehavior: ClipBehavior = ClipBehavior.NONE,
    parent: Widget? = null,
) : MultiChildWidget(parent = parent) {
    override fun createRenderBox(children: List<Widget>): RenderBox = RenderFlex(
        direction = direction,
        mainAxisSize = mainAxisSize,
        mainAxisAlignment = mainAxisAlignment,
        crossAxisAlignment = crossAxisAlignment,
        textDirection = textDirection,
        verticalDirection = verticalDirection,
        textBaseline = textBaseline,
        clipBehavior = clipBehavior,
    ).also { p ->
        children.createRenderBox()?.let {
            p.appendChildren(it)
        }
    }
}