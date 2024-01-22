package com.muedsa.snapshot.widget

import com.muedsa.snapshot.paint.Axis
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.flex.CrossAxisAlignment
import com.muedsa.snapshot.rendering.flex.MainAxisAlignment
import com.muedsa.snapshot.rendering.flex.MainAxisSize
import com.muedsa.snapshot.rendering.flex.VerticalDirection
import org.jetbrains.skia.paragraph.BaselineMode
import org.jetbrains.skia.paragraph.Direction

inline fun Widget.Column(
    mainAxisAlignment: MainAxisAlignment = MainAxisAlignment.START,
    mainAxisSize: MainAxisSize = MainAxisSize.MAX,
    crossAxisAlignment: CrossAxisAlignment = CrossAxisAlignment.CENTER,
    textDirection: Direction? = null,
    verticalDirection: VerticalDirection = VerticalDirection.DOWN,
    textBaseline: BaselineMode? = null,
    clipBehavior: ClipBehavior = ClipBehavior.NONE,
    content: Column.() -> Unit = {},
) {
    buildChild(
        widget = Column(
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

class Column(
    mainAxisAlignment: MainAxisAlignment = MainAxisAlignment.START,
    mainAxisSize: MainAxisSize = MainAxisSize.MAX,
    crossAxisAlignment: CrossAxisAlignment = CrossAxisAlignment.CENTER,
    textDirection: Direction? = null,
    verticalDirection: VerticalDirection = VerticalDirection.DOWN,
    textBaseline: BaselineMode? = null,
    clipBehavior: ClipBehavior = ClipBehavior.NONE,
    parent: Widget? = null,
) : Flex(
    direction = Axis.VERTICAL,
    mainAxisSize = mainAxisSize,
    mainAxisAlignment = mainAxisAlignment,
    crossAxisAlignment = crossAxisAlignment,
    textDirection = textDirection,
    verticalDirection = verticalDirection,
    textBaseline = textBaseline,
    clipBehavior = clipBehavior,
    parent = parent
)