package com.muedsa.snapshot.parser.attr

import com.muedsa.snapshot.rendering.flex.MainAxisSize

class MainAxisSizeAttrDefine(name: String, defaultValue: MainAxisSize = MainAxisSize.MAX) :
    DefaultValueAttrDefine<MainAxisSize>(name = name, defaultValue = defaultValue) {

    override val valueInfo get() = AttributeValueInfo.enum(MainAxisSize.entries)

    override fun parseValue(valueStr: String?): MainAxisSize {
        requireNotNull(valueStr)
        return MainAxisSize.valueOf(valueStr)
    }

    override fun copyWith(name: String, defaultValue: MainAxisSize): DefaultValueAttrDefine<MainAxisSize> =
        MainAxisSizeAttrDefine(name, defaultValue)
}
