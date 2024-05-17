package com.muedsa.snapshot.parser.attr.nullable

import com.muedsa.snapshot.parser.attr.DefaultValueAttrDefine
import com.muedsa.snapshot.parser.attr.FontStyleAttrDefine
import com.muedsa.snapshot.parser.attr.required.AttrDefine
import org.jetbrains.skia.FontStyle

class NullableFontStyleAttrDefine(name: String):
    DefaultValueAttrDefine<FontStyle?>(name = name, defaultValue = null) {

    override fun parseValue(valueStr: String?): FontStyle? =
        valueStr?.let { FontStyleAttrDefine.parseFormStr(valueStr) }

    override fun copyWith(name: String, defaultValue: FontStyle?): AttrDefine<FontStyle?> =
        NullableFontStyleAttrDefine(name)
}