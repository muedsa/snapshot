package com.muedsa.snapshot.parser.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.snapshot.paint.BoxFit
import com.muedsa.snapshot.paint.ImageRepeat
import com.muedsa.snapshot.paint.text.InlineSpan
import com.muedsa.snapshot.paint.text.TextSpan
import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.widget.Widget
import com.muedsa.snapshot.widget.text.ImageEmoji
import com.muedsa.snapshot.widget.text.RichText
import com.muedsa.snapshot.widget.text.TextStyle
import com.muedsa.snapshot.widget.text.WidgetSpan
import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.paragraph.BaselineMode
import org.jetbrains.skia.paragraph.PlaceholderAlignment
import org.jetbrains.skia.paragraph.TextStyle

open class TextParser : WidgetParser {

    override val id: String = "Text"

    override val containerMode: ContainerMode = ContainerMode.MULTIPLE

    override fun buildWidget(element: Element): Widget =
        RichText(
            text = element.parseInlineSpan()
        )

    companion object {
        val ATTR_IMAGE_ALIGNMENT = CommonAttrDefine.ALIGNMENT.copyWith("imageAlignment")
    }
}

private fun Element.parseTextSpan(raw: Boolean = false): TextSpan {
    var text: String? = WidgetParser.parseAttrValue(CommonAttrDefine.TEXT_N, attrs)
    if (!raw) {
        text = text?.trim { it.isWhitespace() }?.trim()
    }
    val color: Int? = WidgetParser.parseAttrValue(CommonAttrDefine.COLOR_N, attrs)
    val fontSize: Float? = WidgetParser.parseAttrValue(CommonAttrDefine.FONT_SIZE_N, attrs)
    val fontFamilyNames: Array<String>? = WidgetParser.parseAttrValue(CommonAttrDefine.FONT_FAMILY_N, attrs)?.split(",")
        ?.toTypedArray()
    val fontStyle: FontStyle? = WidgetParser.parseAttrValue(CommonAttrDefine.FONT_STYLE_N, attrs)
    val style: TextStyle? = if (color != null || fontSize != null || !fontFamilyNames.isNullOrEmpty() || fontStyle != null)
        TextStyle {
            color?.let {
                this.color = it
            }
            fontSize?.let {
                this.fontSize = it
            }
            fontFamilyNames?.let {
                this.fontFamilies = it
            }
            fontStyle?.let {
                this.fontStyle = it
            }
        } else null
    return TextSpan(
        text = text,
        style = style,
        initChildren = children.map { it.parseInlineSpan() }
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
        colorBlendMode = colorBlendMode
    )
    return WidgetSpan(
        alignment = alignment,
        baseline = baseline,
        child = imageEmojiWidget
    )
}

private fun Element.parseInlineSpan(): InlineSpan {
    return when (widgetParser) {
        is TextParser -> parseTextSpan(raw = false)
        is RawTextParser -> parseTextSpan(raw = true)
        is EmojiParser -> parseEmojiSpan()
        else -> throw IllegalArgumentException("Unknown inline span type: ${widgetParser.id}")
    }
}