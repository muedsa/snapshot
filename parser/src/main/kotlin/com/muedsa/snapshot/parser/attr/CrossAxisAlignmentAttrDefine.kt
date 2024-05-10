package com.muedsa.snapshot.parser.attr

import com.muedsa.snapshot.rendering.flex.CrossAxisAlignment

class CrossAxisAlignmentAttrDefine(name: String, defaultValue: CrossAxisAlignment = CrossAxisAlignment.CENTER) :
    DefaultValueAttrDefine<CrossAxisAlignment>(name = name, defaultValue = defaultValue) {

    override fun parseValue(valueStr: String?): CrossAxisAlignment {
        requireNotNull(valueStr)
        return CrossAxisAlignment.valueOf(valueStr)
    }

    override fun copyWith(name: String, defaultValue: CrossAxisAlignment): DefaultValueAttrDefine<CrossAxisAlignment> =
        CrossAxisAlignmentAttrDefine(name, defaultValue)
}