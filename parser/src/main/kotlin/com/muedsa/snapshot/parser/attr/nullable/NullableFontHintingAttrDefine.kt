package com.muedsa.snapshot.parser.attr.nullable

import com.muedsa.snapshot.parser.attr.DefaultValueAttrDefine
import org.jetbrains.skia.FontHinting

class NullableFontHintingAttrDefine(name: String) :
    DefaultValueAttrDefine<FontHinting?>(name = name, defaultValue = null) {

    override fun parseValue(valueStr: String?): FontHinting {
        requireNotNull(valueStr) { "Attr [$name] value can not be null" }
        return FontHinting.valueOf(valueStr)
    }

    override fun copyWith(name: String, defaultValue: FontHinting?): NullableFontHintingAttrDefine =
        NullableFontHintingAttrDefine(name)
}
