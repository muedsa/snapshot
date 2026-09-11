package com.muedsa.snapshot.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.Matrix44CMO
import com.muedsa.geometry.Offset
import com.muedsa.geometry.computeRotation
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderTransform

inline fun ChildSlot.Transform(
    transform: Matrix44CMO,
    origin: Offset? = null,
    alignment: BoxAlignment?,
    content: Transform.() -> Unit = {},
) {
    attach(
        com.muedsa.snapshot.widget.Transform(
            transform = transform,
            origin = origin,
            alignment = alignment,
        ).apply(content)
    )
}


class Transform(
    var transform: Matrix44CMO,
    var origin: Offset? = null,
    var alignment: BoxAlignment?,
) : SingleChildWidget() {


    override fun createRenderBox(child: Widget?): RenderBox = RenderTransform(
        transform = transform,
        origin = origin,
        alignment = alignment,
    ).also { p ->
        child?.createRenderBox()?.let {
            p.appendChild(it)
        }
    }

    companion object {

        @JvmStatic
        fun rotate(
            angle: Float,
            origin: Offset? = null,
            alignment: BoxAlignment = BoxAlignment.CENTER,
        ): Transform = Transform(
            transform = computeRotation(angle),
            origin = origin,
            alignment = alignment,
        )

        @JvmStatic
        fun translate(
            offset: Offset,
        ): Transform = Transform(
            transform = Matrix44CMO.translationValues(x = offset.x, y = offset.y, z = 0f),
            origin = null,
            alignment = null,
        )
    }
}
