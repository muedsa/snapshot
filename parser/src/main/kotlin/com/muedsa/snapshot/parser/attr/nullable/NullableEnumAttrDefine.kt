package com.muedsa.snapshot.parser.attr.nullable

import com.muedsa.snapshot.parser.attr.DefaultValueAttrDefine

class NullableEnumAttrDefine<T>(
    name: String,
    private val parse: (String) -> T,
) : DefaultValueAttrDefine<T?>(name = name, defaultValue = null) {

    override fun parseValue(valueStr: String?): T? = valueStr?.let(parse)

    override fun copyWith(
        name: String,
        defaultValue: T?,
    ): NullableEnumAttrDefine<T> = NullableEnumAttrDefine(name, parse)
}
