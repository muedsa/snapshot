package com.muedsa.snapshot.parser.attr.nullable

import com.muedsa.snapshot.parser.attr.DefaultValueAttrDefine
import org.jetbrains.skia.FontEdging

class NullableFontEdgingAttrDefine(name: String) :
    DefaultValueAttrDefine<FontEdging?>(name = name, defaultValue = null) {

    override fun parseValue(valueStr: String?): FontEdging {
        requireNotNull(valueStr) { "Attr [$name] value can not be null" }
        return FontEdging.valueOf(valueStr)
    }

    override fun copyWith(name: String, defaultValue: FontEdging?): NullableFontEdgingAttrDefine =
        NullableFontEdgingAttrDefine(name)
}
