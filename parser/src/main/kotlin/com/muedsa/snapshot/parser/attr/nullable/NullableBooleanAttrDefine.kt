package com.muedsa.snapshot.parser.attr.nullable

import com.muedsa.snapshot.parser.attr.DefaultValueAttrDefine

class NullableBooleanAttrDefine(name: String) :
    DefaultValueAttrDefine<Boolean?>(name = name, defaultValue = null) {

    override fun parseValue(valueStr: String?): Boolean = valueStr?.toBoolean() ?: false

    override fun copyWith(name: String, defaultValue: Boolean?): NullableBooleanAttrDefine =
        NullableBooleanAttrDefine(name)
}
