package com.muedsa.snapshot.parser.attr

import com.muedsa.snapshot.parser.attr.required.AttrDefine
import org.jetbrains.skia.Color

class ColorAttrDefine(name: String, defaultValue: Int = Color.TRANSPARENT) :
    DefaultValueAttrDefine<Int>(name = name, defaultValue = defaultValue) {

    override fun parseValue(valueStr: String?): Int = parseColorFromText(valueStr, this)
    override fun copyWith(name: String, defaultValue: Int): ColorAttrDefine =
        ColorAttrDefine(name, defaultValue)

    companion object {

        fun parseColorFromText(valueStr: String?, attrDefine: AttrDefine<*>): Int {
            requireNotNull(valueStr) { "Attr [${attrDefine.name}] value can not be null" }
            return CssColorParser.parse(valueStr, attrDefine.name)
        }

        internal fun isColorCandidate(value: String): Boolean = CssColorParser.isColorCandidate(value)
    }
}
