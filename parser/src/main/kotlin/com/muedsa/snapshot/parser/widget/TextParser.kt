package com.muedsa.snapshot.parser.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.snapshot.paint.BoxFit
import com.muedsa.snapshot.paint.ImageRepeat
import com.muedsa.snapshot.paint.text.InlineSpan
import com.muedsa.snapshot.paint.text.TextSpan
import com.muedsa.snapshot.paint.text.TextStyle
import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.attr.BooleanAttrDefine
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.parser.attr.ParagraphAlignmentAttrDefine
import com.muedsa.snapshot.parser.attr.TextOverflowAttrDefine
import com.muedsa.snapshot.parser.attr.TextWidthBasisAttrDefine
import com.muedsa.snapshot.parser.attr.nullable.NullableBooleanAttrDefine
import com.muedsa.snapshot.parser.attr.nullable.NullableFloatAttrDefine
import com.muedsa.snapshot.parser.attr.nullable.NullableFontEdgingAttrDefine
import com.muedsa.snapshot.parser.attr.nullable.NullableFontHintingAttrDefine
import com.muedsa.snapshot.parser.attr.nullable.NullableHeightModeAttrDefine
import com.muedsa.snapshot.parser.attr.nullable.NullableIntAttrDefine
import com.muedsa.snapshot.parser.attr.nullable.NullableStringAttrDefine
import com.muedsa.snapshot.widget.Widget
import com.muedsa.snapshot.widget.text.ImageEmoji
import com.muedsa.snapshot.widget.text.RichText
import com.muedsa.snapshot.widget.text.WidgetSpan
import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.paragraph.BaselineMode
import org.jetbrains.skia.paragraph.PlaceholderAlignment

open class TextParser : WidgetParser {

    override val id: String = "Text"

    override val containerMode: ContainerMode = ContainerMode.MULTIPLE

    override fun buildWidget(element: Element): Widget =
        RichText(
            text = element.parseInlineSpan(),
            textAlign = WidgetParser.parseAttrValue(ATTR_TEXT_ALIGN, element.attrs),
            textDirection = WidgetParser.parseAttrValue(ATTR_TEXT_DIRECTION, element.attrs),
            softWrap = WidgetParser.parseAttrValue(ATTR_SOFT_WRAP, element.attrs),
            overflow = WidgetParser.parseAttrValue(ATTR_OVERFLOW, element.attrs),
            maxLines = WidgetParser.parseAttrValue(ATTR_MAX_LINES, element.attrs),
            textWidthBasis = WidgetParser.parseAttrValue(ATTR_TEXT_WIDTH_BASIS, element.attrs),
            textHeightMode = WidgetParser.parseAttrValue(ATTR_TEXT_HEIGHT_MODE, element.attrs),
        )

    companion object {
        val ATTR_IMAGE_ALIGNMENT = CommonAttrDefine.ALIGNMENT.copyWith("imageAlignment")
        val ATTR_TEXT_ALIGN = ParagraphAlignmentAttrDefine("textAlign")
        val ATTR_TEXT_DIRECTION = CommonAttrDefine.DIRECTION.copyWith("textDirection")
        val ATTR_SOFT_WRAP = BooleanAttrDefine("softWrap", defaultValue = true)
        val ATTR_OVERFLOW = TextOverflowAttrDefine("overflow")
        val ATTR_MAX_LINES = NullableIntAttrDefine("maxLines")
        val ATTR_TEXT_WIDTH_BASIS = TextWidthBasisAttrDefine("textWidthBasis")
        val ATTR_TEXT_HEIGHT_MODE = NullableHeightModeAttrDefine("textHeightMode")
        val ATTR_HEIGHT = NullableFloatAttrDefine("height")
        val ATTR_TOP_RATIO = NullableFloatAttrDefine("topRatio")
        val ATTR_LETTER_SPACING = NullableFloatAttrDefine("letterSpacing")
        val ATTR_WORD_SPACING = NullableFloatAttrDefine("wordSpacing")
        val ATTR_LOCALE = NullableStringAttrDefine("locale")
        val ATTR_BASELINE_MODE = CommonAttrDefine.BASELINE_N.copyWith("baselineMode")
        val ATTR_FONT_EDGING = NullableFontEdgingAttrDefine("fontEdging")
        val ATTR_FONT_HINTING = NullableFontHintingAttrDefine("fontHinting")
        val ATTR_SUBPIXEL = NullableBooleanAttrDefine("subpixel")
    }
}

private fun Element.parseTextSpan(raw: Boolean = false): TextSpan {
    var text: String? = WidgetParser.parseAttrValue(CommonAttrDefine.TEXT_N, attrs)
    if (!raw) {
        text = text?.trim { it.isWhitespace() }?.trim()
    }
    val fontFamilyNames: List<String>? = WidgetParser.parseAttrValue(CommonAttrDefine.FONT_FAMILY_N, attrs)?.split(",")
    val style = TextStyle(
        color = WidgetParser.parseAttrValue(CommonAttrDefine.COLOR_N, attrs),
        fontSize = WidgetParser.parseAttrValue(CommonAttrDefine.FONT_SIZE_N, attrs),
        fontFamilies = fontFamilyNames,
        fontStyle = WidgetParser.parseAttrValue(CommonAttrDefine.FONT_STYLE_N, attrs),
        height = WidgetParser.parseAttrValue(TextParser.ATTR_HEIGHT, attrs),
        topRatio = WidgetParser.parseAttrValue(TextParser.ATTR_TOP_RATIO, attrs),
        letterSpacing = WidgetParser.parseAttrValue(TextParser.ATTR_LETTER_SPACING, attrs),
        wordSpacing = WidgetParser.parseAttrValue(TextParser.ATTR_WORD_SPACING, attrs),
        locale = WidgetParser.parseAttrValue(TextParser.ATTR_LOCALE, attrs),
        baselineMode = WidgetParser.parseAttrValue(TextParser.ATTR_BASELINE_MODE, attrs),
        fontEdging = WidgetParser.parseAttrValue(TextParser.ATTR_FONT_EDGING, attrs),
        fontHinting = WidgetParser.parseAttrValue(TextParser.ATTR_FONT_HINTING, attrs),
        subpixel = WidgetParser.parseAttrValue(TextParser.ATTR_SUBPIXEL, attrs),
    ).takeUnless { it.isEmpty() }
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
