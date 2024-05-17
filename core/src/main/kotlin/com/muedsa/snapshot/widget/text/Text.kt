package com.muedsa.snapshot.widget.text

import com.muedsa.snapshot.paint.text.TextOverflow
import com.muedsa.snapshot.paint.text.TextSpan
import com.muedsa.snapshot.paint.text.TextStyle
import com.muedsa.snapshot.paint.text.TextWidthBasis
import com.muedsa.snapshot.widget.Widget
import org.jetbrains.skia.paragraph.*

fun Widget.Text(
    text: String,
    style: TextStyle? = null,
    textAlign: Alignment = Alignment.START,
    textDirection: Direction = Direction.LTR,
    softWrap: Boolean = true,
    overflow: TextOverflow = TextOverflow.CLIP,
    maxLines: Int? = null,
    strutStyle: StrutStyle? = null,
    textWidthBasis: TextWidthBasis = TextWidthBasis.PARENT,
    textHeightMode: HeightMode? = null,
) {
    RichText(
        text = TextSpan(
            text = text,
            style = style
        ),
        textAlign = textAlign,
        textDirection = textDirection,
        softWrap = softWrap,
        overflow = overflow,
        maxLines = maxLines,
        strutStyle = strutStyle,
        textWidthBasis = textWidthBasis,
        textHeightMode = textHeightMode
    )
}

fun Widget.RichText(
    textAlign: Alignment = Alignment.START,
    textDirection: Direction = Direction.LTR,
    softWrap: Boolean = true,
    overflow: TextOverflow = TextOverflow.CLIP,
    maxLines: Int? = null,
    strutStyle: StrutStyle? = null,
    textWidthBasis: TextWidthBasis = TextWidthBasis.PARENT,
    textHeightMode: HeightMode? = null,
    content: TextSpan.() -> Unit
) {
    RichText(
        text = TextSpan().apply(content),
        textAlign = textAlign,
        textDirection = textDirection,
        softWrap = softWrap,
        overflow = overflow,
        maxLines = maxLines,
        strutStyle = strutStyle,
        textWidthBasis = textWidthBasis,
        textHeightMode = textHeightMode
    )
}