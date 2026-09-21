package com.muedsa.snapshot.parser.attr

class IntAttrDefine(name: String, defaultValue: Int = 0) :
    DefaultValueAttrDefine<Int>(name = name, defaultValue = defaultValue) {

    override fun parseValue(valueStr: String?): Int {
        requireNotNull(valueStr) { "Attr [$name] value can not be null" }
        return valueStr.toInt()
    }

    override fun copyWith(name: String, defaultValue: Int): IntAttrDefine =
        IntAttrDefine(name, defaultValue)
}
