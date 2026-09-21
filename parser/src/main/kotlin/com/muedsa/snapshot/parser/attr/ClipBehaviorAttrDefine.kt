package com.muedsa.snapshot.parser.attr

import com.muedsa.snapshot.rendering.ClipBehavior

class ClipBehaviorAttrDefine(
    name: String,
    defaultValue: ClipBehavior = ClipBehavior.ANTI_ALIAS,
) : DefaultValueAttrDefine<ClipBehavior>(name = name, defaultValue = defaultValue) {

    override fun parseValue(valueStr: String?): ClipBehavior {
        requireNotNull(valueStr) { "Attr [$name] value can not be null" }
        return ClipBehavior.valueOf(valueStr)
    }

    override fun copyWith(name: String, defaultValue: ClipBehavior): ClipBehaviorAttrDefine =
        ClipBehaviorAttrDefine(name, defaultValue)
}
