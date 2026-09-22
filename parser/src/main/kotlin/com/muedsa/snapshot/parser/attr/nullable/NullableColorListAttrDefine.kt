package com.muedsa.snapshot.parser.attr.nullable

import com.muedsa.snapshot.parser.attr.ColorAttrDefine
import com.muedsa.snapshot.parser.attr.DefaultValueAttrDefine

class NullableColorListAttrDefine(name: String) :
    DefaultValueAttrDefine<IntArray?>(name = name, defaultValue = null) {

    override fun parseValue(valueStr: String?): IntArray {
        requireNotNull(valueStr) { "Attr [$name] value can not be null" }
        val values = valueStr.split(',')
        require(values.size >= 2) { "Attr [$name] must contain at least two colors" }
        return IntArray(values.size) { index ->
            ColorAttrDefine.parseColorFromText(values[index].trim(), this)
        }
    }

    override fun copyWith(name: String, defaultValue: IntArray?): NullableColorListAttrDefine =
        NullableColorListAttrDefine(name)
}
