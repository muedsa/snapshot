package com.muedsa.snapshot.parser.attr.nullable

import com.muedsa.snapshot.parser.attr.DefaultValueAttrDefine
import org.jetbrains.skia.FontFeature

class NullableFontFeatureListAttrDefine(name: String) :
    DefaultValueAttrDefine<List<FontFeature>?>(name = name, defaultValue = null) {

    override fun parseValue(valueStr: String?): List<FontFeature> {
        requireNotNull(valueStr) { "Attr [$name] value can not be null" }
        val value = valueStr.trim()
        require(value.isNotEmpty()) { "Attr [$name] value can not be empty" }
        val tokens = value.split(WHITESPACE_REGEX)
        if ("NONE" in tokens) {
            require(tokens.size == 1) { "Attr [$name] value NONE can not be combined with other font features" }
            return emptyList()
        }
        return FontFeature.parse(value).toList().also { features ->
            require(features.all { it.start <= it.end }) {
                "Attr [$name] font feature range start must not exceed its end"
            }
        }
    }

    override fun copyWith(
        name: String,
        defaultValue: List<FontFeature>?,
    ): NullableFontFeatureListAttrDefine = NullableFontFeatureListAttrDefine(name)

    companion object {
        private val WHITESPACE_REGEX = "\\s+".toRegex()
    }
}
