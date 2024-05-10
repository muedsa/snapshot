package com.muedsa.snapshot.parser.attr

class FloatAttrDefine(name: String, defaultValue: Float = 0f) :
    DefaultValueAttrDefine<Float>(name = name, defaultValue = defaultValue) {

    override fun parseValue(valueStr: String?): Float {
        requireNotNull(valueStr) { "Attr [$name] value can not be null" }
        return valueStr.toFloat()
    }

    override fun copyWith(name: String, defaultValue: Float): FloatAttrDefine =
        FloatAttrDefine(name, defaultValue)
}