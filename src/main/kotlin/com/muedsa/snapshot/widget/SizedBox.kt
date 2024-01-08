package com.muedsa.snapshot.widget

import com.muedsa.geometry.Size
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderConstrainedBox

class SizedBox(
    val width: Float?,
    val height: Float?,
    childBuilder: SingleWidgetBuilder?= null
) : SingleChildWidget(childBuilder = childBuilder) {

    protected val additionalConstraints by lazy {
        BoxConstraints.tightFor(width = width, height = height)
    }

    override fun createRenderTree(): RenderBox {
        return RenderConstrainedBox(additionalConstraints = additionalConstraints, child = child?.createRenderTree())
    }

    companion object {
        @JvmStatic
        fun expand(childBuilder: SingleWidgetBuilder? = null): SizedBox = SizedBox(
            width = Float.POSITIVE_INFINITY,
            height = Float.POSITIVE_INFINITY,
            childBuilder = childBuilder
        )

        @JvmStatic
        fun shrink(childBuilder: SingleWidgetBuilder? = null): SizedBox = SizedBox(
            width = 0f,
            height = 0f,
            childBuilder = childBuilder
        )

        @JvmStatic
        fun fromSize(size: Size, childBuilder: SingleWidgetBuilder? = null): SizedBox = SizedBox(
            width = size.width,
            height = size.height,
            childBuilder = childBuilder
        )

        @JvmStatic
        fun square(dimension: Float, childBuilder: SingleWidgetBuilder? = null): SizedBox = SizedBox(
            width = dimension,
            height = dimension,
            childBuilder = childBuilder
        )
    }
}