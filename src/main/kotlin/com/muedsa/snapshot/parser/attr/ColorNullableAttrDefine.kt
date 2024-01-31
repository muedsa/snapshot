package com.muedsa.snapshot.parser.attr

class ColorNullableAttrDefine(name: String, defaultValue: Int? = null) :
    DefaultValueAttrDefine<Int?>(name = name, defaultValue = defaultValue) {

    override fun parseValue(valueStr: String?): Int = ColorAttrDefine.parseColorFromText(valueStr, this)
}