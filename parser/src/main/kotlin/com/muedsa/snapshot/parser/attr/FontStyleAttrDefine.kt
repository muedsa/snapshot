package com.muedsa.snapshot.parser.attr

import org.jetbrains.skia.FontStyle


class FontStyleAttrDefine(name: String, defaultValue: FontStyle = FontStyle.NORMAL) :
    DefaultValueAttrDefine<FontStyle>(name = name, defaultValue = defaultValue) {

    override fun parseValue(valueStr: String?): FontStyle {
        requireNotNull(valueStr)
        return parseFormStr(valueStr)
    }

    override fun copyWith(name: String, defaultValue: FontStyle): FontStyleAttrDefine =
        FontStyleAttrDefine(name, defaultValue)

    companion object {

        fun parseFormStr(valueStr: String): FontStyle =
            when (valueStr) {
                "BOLD" -> FontStyle.BOLD
                "BOLD_ITALIC" -> FontStyle.BOLD_ITALIC
                "ITALIC" -> FontStyle.ITALIC
                "NORMAL" -> FontStyle.NORMAL
                else -> throw IllegalArgumentException("Unexpected font style $valueStr")
            }
    }
}