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
    var text: String,
    var color: Int = Color.BLACK,
    var fontSize: Float = 14f,
    var fontFamilyName: Array<String>? = null,
    var fontStyle: FontStyle = FontStyle.NORMAL,
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