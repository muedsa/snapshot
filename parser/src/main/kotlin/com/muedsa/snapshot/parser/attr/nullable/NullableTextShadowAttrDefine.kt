package com.muedsa.snapshot.parser.attr.nullable

import com.muedsa.snapshot.parser.attr.AttributeValueFormats
import com.muedsa.snapshot.parser.attr.ColorAttrDefine
import com.muedsa.snapshot.parser.attr.CssColorSyntax
import com.muedsa.snapshot.parser.attr.DefaultValueAttrDefine
import com.muedsa.snapshot.parser.attr.EnumAttributeSyntax
import com.muedsa.snapshot.parser.attr.NumericAttributeSyntax
import org.jetbrains.skia.Color
import org.jetbrains.skia.paragraph.Shadow

class NullableTextShadowAttrDefine(name: String) :
    DefaultValueAttrDefine<List<Shadow>?>(name = name, defaultValue = null) {

    override val valueInfo get() = AttributeValueFormats.TEXT_SHADOW

    override fun parseValue(valueStr: String?): List<Shadow> {
        requireNotNull(valueStr) { "Attr [$name] value can not be null" }
        val entries = CssColorSyntax.splitOn(valueStr, ',')
        require(entries.none(String::isEmpty)) { "Attr [$name] contains an empty shadow" }
        if (entries.any { EnumAttributeSyntax.normalize(it) == "NONE" }) {
            require(entries.size == 1) { "Attr [$name] value NONE can not be combined with other shadows" }
            return emptyList()
        }
        return entries.map(::parseShadow)
    }

    private fun parseShadow(value: String): Shadow {
        val params = CssColorSyntax.splitOnWhitespace(value)
        require(params.size in 2..4) {
            "Attr [$name] shadow must contain offsetX offsetY and optional blurSigma/color"
        }
        val offsetX = NumericAttributeSyntax.parse(params[0], name, false).single()
        val offsetY = NumericAttributeSyntax.parse(params[1], name, false).single()

        var blurSigma = 0.0
        var color = Color.BLACK
        var hasBlurSigma = false
        var hasColor = false
        params.drop(2).forEach { param ->
            when {
                NumericAttributeSyntax.isNumber(param) -> {
                    require(!hasBlurSigma) { "Attr [$name] shadow contains duplicate blur sigma" }
                    blurSigma = param.toDouble()
                    require(blurSigma.isFinite() && blurSigma >= 0.0) {
                        "Attr [$name] blur sigma must be finite and non-negative"
                    }
                    hasBlurSigma = true
                }

                ColorAttrDefine.isColorCandidate(param) -> {
                    require(!hasColor) { "Attr [$name] shadow contains duplicate color" }
                    color = ColorAttrDefine.parseColorFromText(param, this)
                    hasColor = true
                }

                else -> throw IllegalArgumentException("Attr [$name] unknown shadow parameter [$param]")
            }
        }
        return Shadow(color = color, offsetX = offsetX, offsetY = offsetY, blurSigma = blurSigma)
    }

    override fun copyWith(
        name: String,
        defaultValue: List<Shadow>?,
    ): NullableTextShadowAttrDefine = NullableTextShadowAttrDefine(name)
}
