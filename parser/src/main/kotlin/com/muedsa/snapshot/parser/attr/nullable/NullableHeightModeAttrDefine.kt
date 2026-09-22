package com.muedsa.snapshot.parser.attr.nullable

import com.muedsa.snapshot.parser.attr.DefaultValueAttrDefine
import org.jetbrains.skia.paragraph.HeightMode

class NullableHeightModeAttrDefine(name: String) :
    DefaultValueAttrDefine<HeightMode?>(name = name, defaultValue = null) {

    override fun parseValue(valueStr: String?): HeightMode? = valueStr?.let(HeightMode::valueOf)

    override fun copyWith(
        name: String,
        defaultValue: HeightMode?,
    ): NullableHeightModeAttrDefine = NullableHeightModeAttrDefine(name)
}
