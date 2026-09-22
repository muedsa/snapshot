package com.muedsa.snapshot.parser.widget

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
import com.muedsa.snapshot.widget.Widget
import com.muedsa.snapshot.widget.text.RichText
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.PaintStrokeCap
import org.jetbrains.skia.PaintStrokeJoin

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
