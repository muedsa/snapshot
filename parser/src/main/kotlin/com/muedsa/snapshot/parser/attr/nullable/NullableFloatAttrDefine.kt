package com.muedsa.snapshot.parser.attr.nullable

import com.muedsa.snapshot.parser.attr.AttributeValueFormats
import com.muedsa.snapshot.parser.attr.DefaultValueAttrDefine

class NullableFloatAttrDefine(name: String) :
    DefaultValueAttrDefine<Float?>(name = name, defaultValue = null) {

    override val valueInfo get() = AttributeValueFormats.FLOAT

    override fun parseValue(valueStr: String?): Float? {
        return valueStr?.toFloat()
    }

    override fun copyWith(name: String, defaultValue: Float?): NullableFloatAttrDefine =
        NullableFloatAttrDefine(name)
}
