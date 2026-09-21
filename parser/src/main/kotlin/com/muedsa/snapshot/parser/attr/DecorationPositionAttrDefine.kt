package com.muedsa.snapshot.parser.attr

import com.muedsa.snapshot.rendering.box.DecorationPosition

class DecorationPositionAttrDefine(
    name: String,
    defaultValue: DecorationPosition = DecorationPosition.BACKGROUND,
) : DefaultValueAttrDefine<DecorationPosition>(name = name, defaultValue = defaultValue) {

    override fun parseValue(valueStr: String?): DecorationPosition {
        requireNotNull(valueStr) { "Attr [$name] value can not be null" }
        return DecorationPosition.valueOf(valueStr)
    }

    override fun copyWith(
        name: String,
        defaultValue: DecorationPosition,
    ): DecorationPositionAttrDefine = DecorationPositionAttrDefine(name, defaultValue)
}
