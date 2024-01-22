package com.muedsa.snapshot.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.Matrix44CMO
import com.muedsa.geometry.Offset
import com.muedsa.geometry.computeRotation
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderTransform

inline fun Widget.Transform(
    transform: Matrix44CMO,
    origin: Offset? = null,
    alignment: BoxAlignment?,
    content: Transform.() -> Unit = {},
) {
    buildChild(
        widget = Transform(
            transform = transform,
            origin = origin,
            alignment = alignment,
            parent = this
        ),
        content = content
    )
}


class Transform(
    val transform: Matrix44CMO,
    val origin: Offset? = null,
    val alignment: BoxAlignment?,
    parent: Widget? = null,
) : SingleChildWidget(parent = parent) {


    override fun createRenderBox(child: Widget?): RenderBox = RenderTransform(
        transform = transform,
        origin = origin,
        alignment = alignment,
        child = child?.createRenderBox()
    )

    companion object {

        @JvmStatic
        fun rotate(
            angle: Float,
            origin: Offset? = null,
            alignment: BoxAlignment = BoxAlignment.CENTER,
            parent: Widget? = null,
        ): Transform = Transform(
            transform = computeRotation(angle),
            origin = origin,
            alignment = alignment,
            parent = parent
        )

        @JvmStatic
        fun translate(
            offset: Offset,
            parent: Widget? = null,
        ): Transform = Transform(
            transform = Matrix44CMO.translationValues(x = offset.x, y = offset.y, z = 0f),
            origin = null,
            alignment = null,
            parent = parent
        )
    }
}