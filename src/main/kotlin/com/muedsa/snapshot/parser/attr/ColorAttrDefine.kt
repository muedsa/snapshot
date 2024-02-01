package com.muedsa.snapshot.parser.attr

import com.muedsa.snapshot.parser.attr.required.AttrDefine
import org.jetbrains.skia.Color

class ColorAttrDefine(name: String, defaultValue: Int = Color.TRANSPARENT) :
    DefaultValueAttrDefine<Int>(name = name, defaultValue = defaultValue) {

    override fun parseValue(valueStr: String?): Int = parseColorFromText(valueStr, this)
    override fun copyWith(name: String, defaultValue: Int): ColorAttrDefine =
        ColorAttrDefine(name, defaultValue)

    companion object {

        @OptIn(ExperimentalStdlibApi::class)
        fun parseColorFromText(valueStr: String?, attrDefine: AttrDefine<*>): Int {
            requireNotNull(valueStr) { "Attr ${attrDefine.name} value can not be null" }
            require(valueStr.startsWith("#")) { "Attr ${attrDefine.name} value must start with #" }
            require(valueStr.length == 1 + 8 || valueStr.length == 1 + 6) { "Attr ${attrDefine.name} value must be ARGB or RGB color hex" }
            return if (valueStr.length == 1 + 6) {
                Color.makeARGB(
                    a = 255,
                    r = valueStr.substring(1, 3).hexToInt(),
                    g = valueStr.substring(3, 5).hexToInt(),
                    b = valueStr.substring(5, 7).hexToInt()
                )
            } else {
                Color.makeARGB(
                    a = valueStr.substring(1, 3).hexToInt(),
                    r = valueStr.substring(3, 5).hexToInt(),
                    g = valueStr.substring(5, 7).hexToInt(),
                    b = valueStr.substring(7, 9).hexToInt()
                )
            }
        }
    }
}