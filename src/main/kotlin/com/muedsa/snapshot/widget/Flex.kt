package com.muedsa.snapshot.widget

import com.muedsa.snapshot.VerticalDirection
import com.muedsa.snapshot.paint.Axis
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.flex.CrossAxisAlignment
import com.muedsa.snapshot.rendering.flex.MainAxisAlignment
import com.muedsa.snapshot.rendering.flex.MainAxisSize
import com.muedsa.snapshot.rendering.flex.RenderFlex
import org.jetbrains.skia.paragraph.BaselineMode
import org.jetbrains.skia.paragraph.Direction

open class Flex(
    val direction: Axis,
    val mainAxisAlignment: MainAxisAlignment = MainAxisAlignment.START,
    val mainAxisSize: MainAxisSize = MainAxisSize.MAX,
    val crossAxisAlignment: CrossAxisAlignment = CrossAxisAlignment.CENTER,
    val textDirection: Direction? = null,
    val verticalDirection: VerticalDirection = VerticalDirection.DOWN,
    val textBaseline: BaselineMode? = null,
    val clipBehavior: ClipBehavior = ClipBehavior.NONE,
    childrenBuilder: MultiWidgetBuilder? = null,
) : MultiChildWidget(
    childrenBuilder = childrenBuilder
) {


    override fun createRenderTree(): RenderBox = RenderFlex(
        direction = direction,
        mainAxisSize = mainAxisSize,
        mainAxisAlignment = mainAxisAlignment,
        crossAxisAlignment = crossAxisAlignment,
        textDirection = textDirection,
        verticalDirection = verticalDirection,
        textBaseline = textBaseline,
        clipBehavior = clipBehavior,
        children = children?.createRenderTree()
    )
}