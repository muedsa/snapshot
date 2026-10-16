package com.muedsa.snapshot.parser.attr.nullable

import com.muedsa.snapshot.parser.attr.AttributeValueInfo
import com.muedsa.snapshot.parser.attr.DefaultValueAttrDefine
import org.jetbrains.skia.BlendMode

class NullableBlendModeAttrDefine(name: String) :
    DefaultValueAttrDefine<BlendMode?>(name = name, defaultValue = null) {

    override val valueInfo get() = AttributeValueInfo.enum(BlendMode.entries)

    override fun parseValue(valueStr: String?): BlendMode {
        requireNotNull(valueStr)
        return BlendMode.valueOf(valueStr)
    }

    override fun copyWith(name: String, defaultValue: BlendMode?): DefaultValueAttrDefine<BlendMode?> =
        NullableBlendModeAttrDefine(name)
}
