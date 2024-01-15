package com.muedsa.snapshot.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.Matrix44CMO
import com.muedsa.geometry.Offset
import com.muedsa.geometry.computeRotation
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderTransform

class Transform(
    val transform: Matrix44CMO,
    val origin: Offset? = null,
    val alignment: BoxAlignment?,
    childBuilder: SingleWidgetBuilder? = null,
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
            angle: Float,
            origin: Offset? = null,
            alignment: BoxAlignment = BoxAlignment.CENTER,
            childBuilder: SingleWidgetBuilder? = null,
        ): Transform = Transform(
            transform = computeRotation(angle),
            origin = origin,
            alignment = alignment,
            childBuilder = childBuilder
        )

        @JvmStatic
        fun translate(
            offset: Offset,
            childBuilder: SingleWidgetBuilder? = null,
        ): Transform = Transform(
            transform = Matrix44CMO.translationValues(x = offset.x, y = offset.y, z = 0f),
            origin = null,
            alignment = null,
            childBuilder = childBuilder
        )
    }
}