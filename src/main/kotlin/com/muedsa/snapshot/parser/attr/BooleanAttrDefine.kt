package com.muedsa.snapshot.parser.attr

class BooleanAttrDefine(name: String, defaultValue: Boolean = false) :
    DefaultValueAttrDefine<Boolean>(name = name, defaultValue = defaultValue) {

    override fun parseValue(valueStr: String?): Boolean = valueStr?.toBoolean() ?: true
}
