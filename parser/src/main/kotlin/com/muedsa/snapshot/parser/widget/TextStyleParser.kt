package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.paint.text.TextStyle
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.parser.attr.nullable.TextDecorationLine
import org.jetbrains.skia.Color
import org.jetbrains.skia.Paint
import org.jetbrains.skia.paragraph.DecorationLineStyle
import org.jetbrains.skia.paragraph.DecorationStyle

private val FOREGROUND_CONTRACT = RequiredAttributeContract(
    TextParser.ATTR_FOREGROUND_COLOR,
    TextParser.ATTR_FOREGROUND_MODE,
    TextParser.ATTR_FOREGROUND_STROKE_WIDTH,
    TextParser.ATTR_FOREGROUND_STROKE_MITER,
    TextParser.ATTR_FOREGROUND_STROKE_CAP,
    TextParser.ATTR_FOREGROUND_STROKE_JOIN,
    TextParser.ATTR_FOREGROUND_ANTI_ALIAS,
)

private val DECORATION_CONTRACT = RequiredAttributeContract(
    TextParser.ATTR_DECORATION,
    TextParser.ATTR_DECORATION_COLOR,
    TextParser.ATTR_DECORATION_LINE_STYLE,
    TextParser.ATTR_DECORATION_THICKNESS,
    TextParser.ATTR_DECORATION_GAPS,
)

internal fun Element.parseTextStyle(): TextStyle? {
    val fontFamilyNames = WidgetParser.parseAttrValue(CommonAttrDefine.FONT_FAMILY_N, attrs)?.split(",")
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

private fun Element.parseForegroundPaint(): Paint? {
    val color = WidgetParser.parseAttrValue(TextParser.ATTR_FOREGROUND_COLOR, attrs)
    FOREGROUND_CONTRACT.validate(color != null, attrs)
    if (color == null) return null

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
    DECORATION_CONTRACT.validate(decoration != null, attrs)
    if (decoration == null) return null

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
