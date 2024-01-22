package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.text.RenderSimpleText
import org.jetbrains.skia.Color
import org.jetbrains.skia.FontStyle

@ExperimentalStdlibApi
fun Widget.SimpleText(
    text: String,
    color: Int = Color.BLACK,
    fontSize: Float = 14f,
    fontFamilyName: Array<String>? = null,
    fontStyle: FontStyle = FontStyle.NORMAL,
) {
    buildChild(
        widget = SimpleText(
            text = text,
            color = color,
            fontSize = fontSize,
            fontFamilyName = fontFamilyName,
            fontStyle = fontStyle,
            parent = this
        ),
        content = { }
    )
}

@ExperimentalStdlibApi
class SimpleText(
    val text: String,
    val color: Int = Color.BLACK,
    val fontSize: Float = 14f,
    val fontFamilyName: Array<String>? = null,
    val fontStyle: FontStyle = FontStyle.NORMAL,
    parent: Widget? = null,
) : Widget(parent = parent) {

    override fun createRenderBox(): RenderBox = RenderSimpleText(
        text = text,
        color = color,
        fontSize = fontSize,
        fontFamilyName = fontFamilyName,
        fontStyle = fontStyle
    )
}