package com.muedsa.snapshot.widget

import com.muedsa.geometry.*
import com.muedsa.snapshot.rendering.RenderBox
import com.muedsa.snapshot.rendering.RenderTransform

class Transform(
    val transform: Matrix44CMO,
    val origin: Offset? = null,
    val alignment: Alignment?,
    childBuilder: SingleWidgetBuilder?= null
) : SingleChildWidget(childBuilder = childBuilder) {


    override fun createRenderTree(): RenderBox = RenderTransform(
        transform = transform,
        origin = origin,
        alignment = alignment,
        child = child?.createRenderTree()
    )

    companion object {

        @JvmStatic
        fun rotate(
            angle :Float,
            origin: Offset? = null,
            alignment: Alignment = Alignment.CENTER,
            childBuilder: SingleWidgetBuilder? = null
        ): Transform = Transform(
            transform = computeRotation(angle),
            origin = origin,
            alignment = alignment,
            childBuilder = childBuilder
        )

        @JvmStatic
        fun translate(
            offset: Offset,
            childBuilder: SingleWidgetBuilder? = null
        ): Transform = Transform(
            transform = Matrix44CMO.translationValues(x = offset.x, y = offset.y, z = 0f),
            origin = null,
            alignment = null,
            childBuilder = childBuilder
        )
    }
}