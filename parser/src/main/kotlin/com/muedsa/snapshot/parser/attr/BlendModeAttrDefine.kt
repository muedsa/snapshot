package com.muedsa.snapshot.parser.attr

import org.jetbrains.skia.BlendMode

class BlendModeAttrDefine(
    name: String,
    defaultValue: BlendMode = BlendMode.SRC_OVER,
) : DefaultValueAttrDefine<BlendMode>(name = name, defaultValue = defaultValue) {

    override fun parseValue(valueStr: String?): BlendMode {
        requireNotNull(valueStr) { "Attr [$name] value can not be null" }
        return BlendMode.valueOf(valueStr)
    }

    override fun copyWith(name: String, defaultValue: BlendMode): BlendModeAttrDefine =
        BlendModeAttrDefine(name, defaultValue)
}
