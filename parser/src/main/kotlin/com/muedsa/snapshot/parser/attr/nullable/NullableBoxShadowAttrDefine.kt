package com.muedsa.snapshot.parser.attr.nullable

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.material.ELEVATION_MAP
import com.muedsa.snapshot.paint.decoration.BoxShadow
import com.muedsa.snapshot.parser.attr.AttributeValueFormats
import com.muedsa.snapshot.parser.attr.ColorAttrDefine
import com.muedsa.snapshot.parser.attr.CssColorSyntax
import com.muedsa.snapshot.parser.attr.DefaultValueAttrDefine
import com.muedsa.snapshot.parser.attr.EnumAttributeSyntax
import com.muedsa.snapshot.parser.attr.NumericAttributeSyntax
import org.jetbrains.skia.Color
import org.jetbrains.skia.FilterBlurMode

class NullableBoxShadowAttrDefine(name: String) :
    DefaultValueAttrDefine<Array<BoxShadow>?>(name = name, defaultValue = null) {

    override val valueInfo get() = AttributeValueFormats.BOX_SHADOW

    override fun parseValue(valueStr: String?): Array<BoxShadow> {
        requireNotNull(valueStr)
        val keyword = EnumAttributeSyntax.normalize(valueStr)
        if (keyword.startsWith(ELEVATION_PREFIX)) {
            val result: MatchResult? = ELEVATION_REGEX.find(keyword)
            check(result != null) { "elevation only allow use $ELEVATION_REGEX${ELEVATION_MAP.keys}" }
            val e = result.groupValues[1].toInt()
            val boxShadows = ELEVATION_MAP[e]
            check(boxShadows != null) {
                "not exist elevation $e, only use $ELEVATION_REGEX${ELEVATION_MAP.keys}"
            }
            return boxShadows
        }
        val entries = CssColorSyntax.splitOn(valueStr, ',')
        require(entries.none(String::isEmpty)) { "Attr [$name] contains an empty shadow" }
        if (entries.any { EnumAttributeSyntax.normalize(it) == "NONE" }) {
            require(entries.size == 1) { "Attr [$name] value NONE can not be combined with other shadows" }
            return emptyArray()
        }
        return entries.map(::parseShadow).toTypedArray()
    }

    private fun parseShadow(value: String): BoxShadow {
        val params = CssColorSyntax.splitOnWhitespace(value)
        require(params.size in 2..6) { "Attr [$name] shadow requires offsetX offsetY and optional blurRadius/spreadRadius/color/blurStyle" }
        val offset = Offset(
            x = NumericAttributeSyntax.parse(params[0], name, false).single(),
            y = NumericAttributeSyntax.parse(params[1], name, false).single(),
        )
        val radii = mutableListOf<Float>()
        var color = Color.BLACK
        var blurStyle = FilterBlurMode.NORMAL
        var hasColor = false
        var hasBlurStyle = false
        for (param in params.drop(2)) {
            when {
                NumericAttributeSyntax.isNumber(param) -> {
                    require(radii.size < 2) { "Attr [$name] shadow has too many radii" }
                    radii += NumericAttributeSyntax.parse(param, name, false).single()
                }
                ColorAttrDefine.isColorCandidate(param) -> {
                    require(!hasColor) { "Attr [$name] shadow contains duplicate color" }
                    color = ColorAttrDefine.parseColorFromText(param, this)
                    hasColor = true
                }
                else -> {
                    require(!hasBlurStyle) { "Attr [$name] shadow contains duplicate blur style" }
                    blurStyle = FilterBlurMode.valueOf(EnumAttributeSyntax.normalize(param))
                    hasBlurStyle = true
                }
            }
        }
        val blurRadius = radii.getOrElse(0) { 0f }
        require(blurRadius >= 0f) { "Attr [$name] blur radius must be non-negative" }
        return BoxShadow(color, offset, blurRadius, radii.getOrElse(1) { 0f }, blurStyle)
    }

    override fun copyWith(name: String, defaultValue: Array<BoxShadow>?): NullableBoxShadowAttrDefine =
        NullableBoxShadowAttrDefine(name)

    companion object {
        const val ELEVATION_PREFIX = "ELEVATION_"
        val ELEVATION_REGEX: Regex = """^$ELEVATION_PREFIX(\d+)$""".toRegex()
    }
}
