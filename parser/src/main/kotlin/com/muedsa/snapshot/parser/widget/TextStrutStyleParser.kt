package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.Element
import org.jetbrains.skia.paragraph.StrutStyle

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

internal fun Element.parseStrutStyle(): StrutStyle? {
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
