package com.muedsa.snapshot.parser.attr.nullable

import com.muedsa.snapshot.parser.attr.AttributeValueInfo
import com.muedsa.snapshot.parser.attr.DefaultValueAttrDefine

class NullableEnumAttrDefine<T>(
    name: String,
    override val valueInfo: AttributeValueInfo?,
    private val parse: (String) -> T,
) : DefaultValueAttrDefine<T?>(name = name, defaultValue = null) {

    constructor(name: String, parse: (String) -> T) : this(name, null, parse)

    override fun parseValue(valueStr: String?): T? = valueStr?.let(parse)

    override fun copyWith(
        name: String,
        defaultValue: T?,
    ): NullableEnumAttrDefine<T> = NullableEnumAttrDefine(name, valueInfo, parse)
}
