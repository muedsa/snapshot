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
import com.muedsa.snapshot.parser.attr.nullable.NullableDecorationLineStyleAttrDefine
import com.muedsa.snapshot.parser.attr.nullable.NullableEnumAttrDefine
import com.muedsa.snapshot.parser.attr.nullable.NullableFloatAttrDefine
import com.muedsa.snapshot.parser.attr.nullable.NullableFontEdgingAttrDefine
import com.muedsa.snapshot.parser.attr.nullable.NullableFontFeatureListAttrDefine
import com.muedsa.snapshot.parser.attr.nullable.NullableFontHintingAttrDefine
import com.muedsa.snapshot.parser.attr.nullable.NullableHeightModeAttrDefine
import com.muedsa.snapshot.parser.attr.nullable.NullableIntAttrDefine
import com.muedsa.snapshot.parser.attr.nullable.NullableStringAttrDefine
import com.muedsa.snapshot.parser.attr.nullable.NullableTextDecorationAttrDefine
import com.muedsa.snapshot.parser.attr.nullable.NullableTextShadowAttrDefine
import com.muedsa.snapshot.parser.attr.nullable.TextDecorationLine
import com.muedsa.snapshot.widget.Widget
import com.muedsa.snapshot.widget.text.ImageEmoji
import com.muedsa.snapshot.widget.text.RichText
import com.muedsa.snapshot.widget.text.WidgetSpan
import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.Color
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.PaintStrokeCap
import org.jetbrains.skia.PaintStrokeJoin
import org.jetbrains.skia.paragraph.BaselineMode
import org.jetbrains.skia.paragraph.DecorationLineStyle
import org.jetbrains.skia.paragraph.DecorationStyle
import org.jetbrains.skia.paragraph.PlaceholderAlignment
import org.jetbrains.skia.paragraph.StrutStyle

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
            strutStyle = element.parseStrutStyle(),
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
        val ATTR_DECORATION = NullableTextDecorationAttrDefine("decoration")
        val ATTR_DECORATION_COLOR = CommonAttrDefine.COLOR_N.copyWith("decorationColor")
        val ATTR_DECORATION_LINE_STYLE = NullableDecorationLineStyleAttrDefine("decorationLineStyle")
        val ATTR_DECORATION_THICKNESS = NullableFloatAttrDefine("decorationThickness")
        val ATTR_DECORATION_GAPS = NullableBooleanAttrDefine("decorationGaps")
        val ATTR_TEXT_SHADOW = NullableTextShadowAttrDefine("textShadow")
        val ATTR_FONT_FEATURES = NullableFontFeatureListAttrDefine("fontFeatures")
        val ATTR_STRUT_ENABLED = NullableBooleanAttrDefine("strutEnabled")
        val ATTR_STRUT_FONT_FAMILY = CommonAttrDefine.FONT_FAMILY_N.copyWith("strutFontFamily")
        val ATTR_STRUT_FONT_STYLE = CommonAttrDefine.FONT_STYLE_N.copyWith("strutFontStyle")
        val ATTR_STRUT_FONT_SIZE = NullableFloatAttrDefine("strutFontSize")
        val ATTR_STRUT_HEIGHT = NullableFloatAttrDefine("strutHeight")
        val ATTR_STRUT_LEADING = NullableFloatAttrDefine("strutLeading")
        val ATTR_STRUT_HEIGHT_FORCED = NullableBooleanAttrDefine("strutHeightForced")
        val ATTR_STRUT_HEIGHT_OVERRIDDEN = NullableBooleanAttrDefine("strutHeightOverridden")
        val ATTR_FOREGROUND_COLOR = CommonAttrDefine.COLOR_N.copyWith("foregroundColor")
        val ATTR_FOREGROUND_MODE = NullableEnumAttrDefine("foregroundMode", PaintMode::valueOf)
        val ATTR_FOREGROUND_STROKE_WIDTH = NullableFloatAttrDefine("foregroundStrokeWidth")
        val ATTR_FOREGROUND_STROKE_MITER = NullableFloatAttrDefine("foregroundStrokeMiter")
        val ATTR_FOREGROUND_STROKE_CAP = NullableEnumAttrDefine("foregroundStrokeCap", PaintStrokeCap::valueOf)
        val ATTR_FOREGROUND_STROKE_JOIN = NullableEnumAttrDefine("foregroundStrokeJoin", PaintStrokeJoin::valueOf)
        val ATTR_FOREGROUND_ANTI_ALIAS = NullableBooleanAttrDefine("foregroundAntiAlias")
        val ATTR_BACKGROUND_COLOR = CommonAttrDefine.COLOR_N.copyWith("backgroundColor")
    }
}

private val STRUT_ATTR_NAMES = setOf(
    TextParser.ATTR_STRUT_ENABLED.name,
    TextParser.ATTR_STRUT_FONT_FAMILY.name,
    TextParser.ATTR_STRUT_FONT_STYLE.name,
    TextParser.ATTR_STRUT_FONT_SIZE.name,
    TextParser.ATTR_STRUT_HEIGHT.name,
    TextParser.ATTR_STRUT_LEADING.name,
    TextParser.ATTR_STRUT_HEIGHT_FORCED.name,
    TextParser.ATTR_STRUT_HEIGHT_OVERRIDDEN.name,
)

private fun Element.parseStrutStyle(): StrutStyle? {
    if (STRUT_ATTR_NAMES.none(attrs::containsKey)) return null

    return StrutStyle().also { style ->
        style.isEnabled = WidgetParser.parseAttrValue(TextParser.ATTR_STRUT_ENABLED, attrs) ?: true
        WidgetParser.parseAttrValue(TextParser.ATTR_STRUT_FONT_FAMILY, attrs)?.let { value ->
            val fontFamilies = value.split(',').map(String::trim)
            require(fontFamilies.none(String::isEmpty)) {
                "Attr [${TextParser.ATTR_STRUT_FONT_FAMILY.name}] contains an empty font family"
            }
            style.setFontFamilies(fontFamilies.toTypedArray())
        }
        WidgetParser.parseAttrValue(TextParser.ATTR_STRUT_FONT_STYLE, attrs)?.let(style::setFontStyle)
        WidgetParser.parseAttrValue(TextParser.ATTR_STRUT_FONT_SIZE, attrs)?.let { value ->
            require(value.isFinite() && value > 0f) {
                "Attr [${TextParser.ATTR_STRUT_FONT_SIZE.name}] must be finite and positive"
            }
            style.fontSize = value
        }
        WidgetParser.parseAttrValue(TextParser.ATTR_STRUT_HEIGHT, attrs)?.let { value ->
            require(value.isFinite() && value > 0f) {
                "Attr [${TextParser.ATTR_STRUT_HEIGHT.name}] must be finite and positive"
            }
            style.height = value
        }
        WidgetParser.parseAttrValue(TextParser.ATTR_STRUT_LEADING, attrs)?.let { value ->
            require(value.isFinite()) { "Attr [${TextParser.ATTR_STRUT_LEADING.name}] must be finite" }
            style.leading = value
        }
        WidgetParser.parseAttrValue(TextParser.ATTR_STRUT_HEIGHT_FORCED, attrs)?.let { value ->
            style.isHeightForced = value
        }
        WidgetParser.parseAttrValue(TextParser.ATTR_STRUT_HEIGHT_OVERRIDDEN, attrs)?.let { value ->
            style.isHeightOverridden = value
        }
    }
}

private fun Element.parseTextSpan(raw: Boolean = false): TextSpan {
    var text: String? = WidgetParser.parseAttrValue(CommonAttrDefine.TEXT_N, attrs)
    if (!raw) {
        text = text?.trim { it.isWhitespace() }?.trim()
    }
    return TextSpan(
        text = text,
        style = parseTextStyle(),
        initChildren = children.map { it.parseInlineSpan() }
    )
}

private fun Element.parseTextStyle(): TextStyle? {
    val fontFamilyNames: List<String>? = WidgetParser.parseAttrValue(CommonAttrDefine.FONT_FAMILY_N, attrs)?.split(",")
    return TextStyle(
        color = WidgetParser.parseAttrValue(CommonAttrDefine.COLOR_N, attrs),
        foreground = parseForegroundPaint(),
        background = parseBackgroundPaint(),
        decorationStyle = parseDecorationStyle(),
        shadows = WidgetParser.parseAttrValue(TextParser.ATTR_TEXT_SHADOW, attrs),
        fontFeatures = WidgetParser.parseAttrValue(TextParser.ATTR_FONT_FEATURES, attrs),
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
}

private val FOREGROUND_MODIFIER_ATTR_NAMES = setOf(
    TextParser.ATTR_FOREGROUND_MODE.name,
    TextParser.ATTR_FOREGROUND_STROKE_WIDTH.name,
    TextParser.ATTR_FOREGROUND_STROKE_MITER.name,
    TextParser.ATTR_FOREGROUND_STROKE_CAP.name,
    TextParser.ATTR_FOREGROUND_STROKE_JOIN.name,
    TextParser.ATTR_FOREGROUND_ANTI_ALIAS.name,
)

private fun Element.parseForegroundPaint(): Paint? {
    val color = WidgetParser.parseAttrValue(TextParser.ATTR_FOREGROUND_COLOR, attrs)
    if (color == null) {
        val unexpectedName = FOREGROUND_MODIFIER_ATTR_NAMES.firstOrNull(attrs::containsKey)
        require(unexpectedName == null) {
            "Attr [${TextParser.ATTR_FOREGROUND_COLOR.name}] is required when [$unexpectedName] is specified"
        }
        return null
    }

    return Paint().also { paint ->
        paint.color = color
        WidgetParser.parseAttrValue(TextParser.ATTR_FOREGROUND_MODE, attrs)?.let { paint.mode = it }
        WidgetParser.parseAttrValue(TextParser.ATTR_FOREGROUND_STROKE_WIDTH, attrs)?.let { value ->
            require(value.isFinite() && value >= 0f) {
                "Attr [${TextParser.ATTR_FOREGROUND_STROKE_WIDTH.name}] must be finite and non-negative"
            }
            paint.strokeWidth = value
        }
        WidgetParser.parseAttrValue(TextParser.ATTR_FOREGROUND_STROKE_MITER, attrs)?.let { value ->
            require(value.isFinite() && value > 0f) {
                "Attr [${TextParser.ATTR_FOREGROUND_STROKE_MITER.name}] must be finite and positive"
            }
            paint.strokeMiter = value
        }
        WidgetParser.parseAttrValue(TextParser.ATTR_FOREGROUND_STROKE_CAP, attrs)?.let { paint.strokeCap = it }
        WidgetParser.parseAttrValue(TextParser.ATTR_FOREGROUND_STROKE_JOIN, attrs)?.let { paint.strokeJoin = it }
        WidgetParser.parseAttrValue(TextParser.ATTR_FOREGROUND_ANTI_ALIAS, attrs)?.let { paint.isAntiAlias = it }
    }
}

private fun Element.parseBackgroundPaint(): Paint? =
    WidgetParser.parseAttrValue(TextParser.ATTR_BACKGROUND_COLOR, attrs)?.let { color ->
        Paint().also { it.color = color }
    }

private fun Element.parseDecorationStyle(): DecorationStyle? {
    val decoration = WidgetParser.parseAttrValue(TextParser.ATTR_DECORATION, attrs)
    if (decoration == null) {
        val unexpectedAttr = listOf(
            TextParser.ATTR_DECORATION_COLOR,
            TextParser.ATTR_DECORATION_LINE_STYLE,
            TextParser.ATTR_DECORATION_THICKNESS,
            TextParser.ATTR_DECORATION_GAPS,
        ).firstOrNull { attrs.containsKey(it.name) }
        require(unexpectedAttr == null) {
            "Attr [decoration] is required when [${unexpectedAttr!!.name}] is specified"
        }
        return null
    }

    val thickness = WidgetParser.parseAttrValue(TextParser.ATTR_DECORATION_THICKNESS, attrs) ?: 1f
    require(thickness.isFinite() && thickness > 0f) {
        "Attr [${TextParser.ATTR_DECORATION_THICKNESS.name}] must be finite and positive"
    }
    return DecorationStyle(
        _underline = TextDecorationLine.UNDERLINE in decoration,
        _overline = TextDecorationLine.OVERLINE in decoration,
        _lineThrough = TextDecorationLine.LINE_THROUGH in decoration,
        _gaps = WidgetParser.parseAttrValue(TextParser.ATTR_DECORATION_GAPS, attrs) ?: true,
        color = WidgetParser.parseAttrValue(TextParser.ATTR_DECORATION_COLOR, attrs) ?: Color.BLACK,
        lineStyle = WidgetParser.parseAttrValue(TextParser.ATTR_DECORATION_LINE_STYLE, attrs)
            ?: DecorationLineStyle.SOLID,
        thicknessMultiplier = thickness,
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

private fun Element.parseInlineSpan(): InlineSpan {
    return when (widgetParser) {
        is TextParser -> parseTextSpan(raw = false)
        is RawTextParser -> parseTextSpan(raw = true)
        is EmojiParser -> parseEmojiSpan()
        is WidgetSpanParser -> parseWidgetSpan()
        else -> throw IllegalArgumentException("Unknown inline span type: ${widgetParser.id}")
    }
}
