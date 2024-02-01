package com.muedsa.snapshot.parser.attr

import com.muedsa.snapshot.rendering.flex.VerticalDirection

class VerticalDirectionAttrDefine(name: String, defaultValue: VerticalDirection = VerticalDirection.DOWN) :
    DefaultValueAttrDefine<VerticalDirection>(name = name, defaultValue = defaultValue) {

    override fun parseValue(valueStr: String?): VerticalDirection {
        requireNotNull(valueStr)
        return VerticalDirection.valueOf(valueStr)
    }

    override fun copyWith(name: String, defaultValue: VerticalDirection): DefaultValueAttrDefine<VerticalDirection> =
        VerticalDirectionAttrDefine(name, defaultValue)
}