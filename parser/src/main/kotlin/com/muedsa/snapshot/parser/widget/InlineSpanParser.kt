package com.muedsa.snapshot.parser.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.snapshot.paint.BoxFit
import com.muedsa.snapshot.paint.ImageRepeat
import com.muedsa.snapshot.paint.text.InlineSpan
import com.muedsa.snapshot.paint.text.TextSpan
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.widget.text.ImageEmoji
import com.muedsa.snapshot.widget.text.WidgetSpan
import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.paragraph.BaselineMode
import org.jetbrains.skia.paragraph.PlaceholderAlignment

private fun Element.parseTextSpan(raw: Boolean = false): TextSpan {
    var text = WidgetParser.parseAttrValue(CommonAttrDefine.TEXT_N, attrs)
    if (!raw) {
        text = text?.trim { it.isWhitespace() }?.trim()
    }
    return TextSpan(
        text = text,
        style = parseTextStyle(),
        initChildren = children.map { it.parseInlineSpan() },
    )
}

private fun Element.parseEmojiSpan(): WidgetSpan {
    val baseline: BaselineMode? = WidgetParser.parseAttrValue(CommonAttrDefine.BASELINE_N, attrs)
    val alignment: PlaceholderAlignment = WidgetParser.parseAttrValue(CommonAttrDefine.PLACEHOLDER_ALIGNMENT, attrs)
    val url: String = WidgetParser.parseAttrValue(CommonAttrDefine.URL, attrs)
    val width: Float? = WidgetParser.parseAttrValue(CommonAttrDefine.WIDTH_N, attrs)
    val height: Float? = WidgetParser.parseAttrValue(CommonAttrDefine.HEIGHT_N, attrs)
    val fit: BoxFit? = WidgetParser.parseAttrValue(CommonAttrDefine.FIT_N, attrs)
    val imageAlignment: BoxAlignment = WidgetParser.parseAttrValue(TextParser.ATTR_IMAGE_ALIGNMENT, attrs)
    val repeat: ImageRepeat = WidgetParser.parseAttrValue(CommonAttrDefine.REPEAT, attrs)
    val scale: Float = WidgetParser.parseAttrValue(CommonAttrDefine.SCALE, attrs)
    val opacity: Float = WidgetParser.parseAttrValue(CommonAttrDefine.OPACITY, attrs)
    val color: Int? = WidgetParser.parseAttrValue(CommonAttrDefine.COLOR_N, attrs)
    val colorBlendMode: BlendMode? = WidgetParser.parseAttrValue(ImageParser.ATTR_COLOR_BLEND_MODE, attrs)
    val imageEmojiWidget = ImageEmoji(
        provider = { owner!!.getNetworkImageCache().getImage(url) },
        width = width,
        height = height,
        fit = fit,
        alignment = imageAlignment,
        repeat = repeat,
        scale = scale,
        opacity = opacity,
        color = color,
        colorBlendMode = colorBlendMode,
    )
    return WidgetSpan(
        alignment = alignment,
        baseline = baseline,
        child = imageEmojiWidget,
    )
}

private fun Element.parseWidgetSpan(): WidgetSpan {
    require(children.size == 1) {
        "Tag WidgetSpan must have exactly one child element, but got ${children.size}"
    }
    return WidgetSpan(
        alignment = WidgetParser.parseAttrValue(WidgetSpanParser.ATTR_ALIGNMENT, attrs),
        baseline = WidgetParser.parseAttrValue(CommonAttrDefine.BASELINE_N, attrs),
        style = parseTextStyle(),
        child = children.single().createWidget(),
    )
}

internal fun Element.parseInlineSpan(): InlineSpan = when (widgetParser) {
    is TextParser -> parseTextSpan(raw = false)
    is RawTextParser -> parseTextSpan(raw = true)
    is EmojiParser -> parseEmojiSpan()
    is WidgetSpanParser -> parseWidgetSpan()
    else -> throw IllegalArgumentException("Unknown inline span type: ${widgetParser.id}")
}
