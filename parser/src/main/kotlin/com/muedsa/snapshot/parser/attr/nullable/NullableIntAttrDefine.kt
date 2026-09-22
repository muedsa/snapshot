package com.muedsa.snapshot.parser.attr.nullable

import com.muedsa.snapshot.parser.attr.DefaultValueAttrDefine

class NullableIntAttrDefine(name: String) :
    DefaultValueAttrDefine<Int?>(name = name, defaultValue = null) {

    override fun parseValue(valueStr: String?): Int? = valueStr?.toInt()

    override fun copyWith(
        name: String,
        defaultValue: Int?,
    ): NullableIntAttrDefine = NullableIntAttrDefine(name)
}
