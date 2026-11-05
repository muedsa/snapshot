package com.muedsa.snapshot.parser.attr.nullable

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.parser.attr.NumericAttributeSyntax
import com.muedsa.snapshot.parser.attr.AttributeValueFormats
import com.muedsa.snapshot.parser.attr.DefaultValueAttrDefine

class NullableOffsetAttrDefine(name: String) :
    DefaultValueAttrDefine<Offset?>(name = name, defaultValue = null) {

    override val valueInfo get() = AttributeValueFormats.OFFSET

    override fun parseValue(valueStr: String?): Offset {
        requireNotNull(valueStr) { "Attr [$name] value can not be null" }
        val values = NumericAttributeSyntax.pair(valueStr, name)
        return Offset(values[0], values[1])
    }

    override fun copyWith(name: String, defaultValue: Offset?): NullableOffsetAttrDefine =
        NullableOffsetAttrDefine(name)
}
