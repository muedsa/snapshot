package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.text.RenderSimpleText
import org.jetbrains.skia.Color
import org.jetbrains.skia.FontStyle

@ExperimentalStdlibApi
class SimpleText(
    val content: String,
    val color: Int = Color.BLACK,
    val fontSize: Float = 14f,
    val fontFamilyName: Array<String>? = null,
    val fontStyle: FontStyle = FontStyle.NORMAL,
) : Widget() {

    override fun createRenderBox(): RenderBox = RenderSimpleText(
        content = content,
        color = color,
        fontSize = fontSize,
        fontFamilyName = fontFamilyName,
        fontStyle = fontStyle
    )
}