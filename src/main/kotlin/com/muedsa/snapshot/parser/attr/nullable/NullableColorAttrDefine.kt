package com.muedsa.snapshot.parser.attr.nullable

import com.muedsa.snapshot.parser.attr.ColorAttrDefine
import com.muedsa.snapshot.parser.attr.DefaultValueAttrDefine

class NullableColorAttrDefine(name: String) :
    DefaultValueAttrDefine<Int?>(name = name, defaultValue = null) {

    override fun parseValue(valueStr: String?): Int = ColorAttrDefine.parseColorFromText(valueStr, this)

    override fun copyWith(name: String, defaultValue: Int?): NullableColorAttrDefine =
        NullableColorAttrDefine(name)
}