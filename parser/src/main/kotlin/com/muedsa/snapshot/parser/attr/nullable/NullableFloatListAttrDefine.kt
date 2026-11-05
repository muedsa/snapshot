package com.muedsa.snapshot.parser.attr.nullable

import com.muedsa.snapshot.parser.attr.AttributeValueFormats
import com.muedsa.snapshot.parser.attr.DefaultValueAttrDefine
import com.muedsa.snapshot.parser.attr.NumericAttributeSyntax

class NullableFloatListAttrDefine(name: String) :
    DefaultValueAttrDefine<FloatArray?>(name = name, defaultValue = null) {

    override val valueInfo get() = AttributeValueFormats.FLOAT_LIST

    override fun parseValue(valueStr: String?): FloatArray {
        requireNotNull(valueStr) { "Attr [$name] value can not be null" }
        return NumericAttributeSyntax.parse(valueStr, name, ',' in valueStr).toFloatArray()
    }

    override fun copyWith(name: String, defaultValue: FloatArray?): NullableFloatListAttrDefine =
        NullableFloatListAttrDefine(name)
}
