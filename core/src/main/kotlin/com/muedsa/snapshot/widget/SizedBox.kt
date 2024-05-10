package com.muedsa.snapshot.widget

import com.muedsa.geometry.Size
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderConstrainedBox

inline fun Widget.SizedBox(
    width: Float? = null,
    height: Float? = null,
    content: SizedBox.() -> Unit = {},
) {
    buildChild(
        widget = SizedBox(
            width = width,
            height = height,
            parent = this
        ),
        content = content
    )
}


class SizedBox(
    var width: Float? = null,
    var height: Float? = null,
    parent: Widget? = null,
) : SingleChildWidget(parent = parent) {

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
        fun expand(parent: Widget? = null): SizedBox = SizedBox(
            width = Float.POSITIVE_INFINITY,
            height = Float.POSITIVE_INFINITY,
            parent = parent
        )

        @JvmStatic
        fun shrink(parent: Widget? = null): SizedBox = SizedBox(
            width = 0f,
            height = 0f,
            parent = parent
        )

        @JvmStatic
        fun fromSize(size: Size, parent: Widget? = null): SizedBox = SizedBox(
            width = size.width,
            height = size.height,
            parent = parent
        )

        @JvmStatic
        fun square(dimension: Float, parent: Widget? = null): SizedBox = SizedBox(
            width = dimension,
            height = dimension,
            parent = parent
        )
    }
}