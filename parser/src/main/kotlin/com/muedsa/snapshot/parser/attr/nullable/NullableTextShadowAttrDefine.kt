package com.muedsa.snapshot.parser.attr.nullable

import com.muedsa.snapshot.parser.attr.AttrStrValueConst
import com.muedsa.snapshot.parser.attr.ColorAttrDefine
import com.muedsa.snapshot.parser.attr.DefaultValueAttrDefine
import org.jetbrains.skia.Color
import org.jetbrains.skia.paragraph.Shadow

class NullableTextShadowAttrDefine(name: String) :
    DefaultValueAttrDefine<List<Shadow>?>(name = name, defaultValue = null) {

    override fun parseValue(valueStr: String?): List<Shadow> {
        requireNotNull(valueStr) { "Attr [$name] value can not be null" }
        val entries = valueStr.split(',').map(String::trim)
        require(entries.none(String::isEmpty)) { "Attr [$name] contains an empty shadow" }
        if ("NONE" in entries) {
            require(entries.size == 1) { "Attr [$name] value NONE can not be combined with other shadows" }
            return emptyList()
        }
        return entries.map(::parseShadow)
    }

    private fun parseShadow(value: String): Shadow {
        val params = value.split(WHITESPACE_REGEX)
        require(params.size in 2..4) {
            "Attr [$name] shadow must contain offsetX offsetY and optional blurSigma/color"
        }
        val offsetX = params[0].toFloat()
        val offsetY = params[1].toFloat()
        require(offsetX.isFinite() && offsetY.isFinite()) { "Attr [$name] offsets must be finite" }

        var blurSigma = 0.0
        var color = Color.BLACK
        var hasBlurSigma = false
        var hasColor = false
        params.drop(2).forEach { param ->
            when {
                AttrStrValueConst.ONE_FLOAT_VALUE_REGEX.matches(param) -> {
                    require(!hasBlurSigma) { "Attr [$name] shadow contains duplicate blur sigma" }
                    blurSigma = param.toDouble()
                    require(blurSigma.isFinite() && blurSigma >= 0.0) {
                        "Attr [$name] blur sigma must be finite and non-negative"
                    }
                    hasBlurSigma = true
                }

                param.startsWith("#") -> {
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

    companion object {
        private val WHITESPACE_REGEX = "\\s+".toRegex()
    }
}
