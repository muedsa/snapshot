package com.muedsa.snapshot.parser.attr.nullable

import com.muedsa.snapshot.parser.attr.DefaultValueAttrDefine

class NullableFloatListAttrDefine(name: String) :
    DefaultValueAttrDefine<FloatArray?>(name = name, defaultValue = null) {

    override fun parseValue(valueStr: String?): FloatArray {
        requireNotNull(valueStr) { "Attr [$name] value can not be null" }
        val values = valueStr.split(',')
        require(values.isNotEmpty()) { "Attr [$name] must not be empty" }
        return FloatArray(values.size) { index ->
            values[index].trim().toFloat().also { value ->
                require(value.isFinite()) { "Attr [$name] values must be finite" }
            }
        }
    }

    override fun copyWith(name: String, defaultValue: FloatArray?): NullableFloatListAttrDefine =
        NullableFloatListAttrDefine(name)
}
