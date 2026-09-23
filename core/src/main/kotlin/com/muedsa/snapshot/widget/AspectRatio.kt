package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderAspectRatio
import com.muedsa.snapshot.rendering.box.RenderBox

inline fun ChildSlot.AspectRatio(
    aspectRatio: Float,
    content: AspectRatio.() -> Unit = {},
) {
    attach(com.muedsa.snapshot.widget.AspectRatio(aspectRatio).apply(content))
}

/** 将宽高比设为 [aspectRatio]，即宽度除以高度。 */
class AspectRatio(
    var aspectRatio: Float,
) : SingleChildWidget() {

    init {
        require(aspectRatio.isFinite() && aspectRatio > 0f) {
            "aspectRatio must be finite and positive"
        }
    }

    override fun createRenderBox(child: Widget?): RenderBox = RenderAspectRatio(aspectRatio).also { renderBox ->
        child?.createRenderBox()?.let(renderBox::appendChild)
    }
}
