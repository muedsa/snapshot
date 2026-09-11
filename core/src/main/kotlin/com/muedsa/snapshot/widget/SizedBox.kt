package com.muedsa.snapshot.widget

import com.muedsa.geometry.Size
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderConstrainedBox

inline fun ChildSlot.SizedBox(
    width: Float? = null,
    height: Float? = null,
    content: SizedBox.() -> Unit = {},
) {
    attach(
        com.muedsa.snapshot.widget.SizedBox(
            width = width,
            height = height,
        ).apply(content)
    )
}


class SizedBox(
    var width: Float? = null,
    var height: Float? = null,
) : SingleChildWidget() {

    protected val additionalConstraints by lazy {
        BoxConstraints.tightFor(width = width, height = height)
    }

    override fun createRenderBox(child: Widget?): RenderBox =
        RenderConstrainedBox(additionalConstraints = additionalConstraints).also { p ->
            child?.createRenderBox()?.let {
                p.appendChild(it)
            }
        }

    companion object {
        @JvmStatic
        fun expand(): SizedBox = SizedBox(
            width = Float.POSITIVE_INFINITY,
            height = Float.POSITIVE_INFINITY,
        )

        @JvmStatic
        fun shrink(): SizedBox = SizedBox(
            width = 0f,
            height = 0f,
        )

        @JvmStatic
        fun fromSize(size: Size): SizedBox = SizedBox(
            width = size.width,
            height = size.height,
        )

        @JvmStatic
        fun square(dimension: Float): SizedBox = SizedBox(
            width = dimension,
            height = dimension,
        )
    }
}
