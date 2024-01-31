package com.muedsa.snapshot.parser.attr

class FloatNullableAttrDefine(name: String, defaultValue: Float? = null) :
    DefaultValueAttrDefine<Float?>(name = name, defaultValue = defaultValue) {

    override fun parseValue(valueStr: String?): Float? {
        return valueStr?.toFloat()
    }
}