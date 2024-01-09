package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.text.RenderSimpleText
import org.jetbrains.skia.Color

@Deprecated(message = "only unit test use it")
internal class SimpleText(
    val content: String,
    val color: Int = Color.BLACK,
    val fontSize: Float = 15f
) : Widget() {

    override fun createRenderTree(): RenderBox = RenderSimpleText(
        content = content,
        color = color,
        fontSize = fontSize
    )
}