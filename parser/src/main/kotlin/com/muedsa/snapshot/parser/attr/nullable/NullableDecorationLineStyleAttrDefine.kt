package com.muedsa.snapshot.parser.attr.nullable

import com.muedsa.snapshot.parser.attr.DefaultValueAttrDefine
import org.jetbrains.skia.paragraph.DecorationLineStyle

class NullableDecorationLineStyleAttrDefine(name: String) :
    DefaultValueAttrDefine<DecorationLineStyle?>(name = name, defaultValue = null) {

    override fun parseValue(valueStr: String?): DecorationLineStyle? =
        valueStr?.let(DecorationLineStyle::valueOf)

    override fun copyWith(
        name: String,
        defaultValue: DecorationLineStyle?,
    ): NullableDecorationLineStyleAttrDefine = NullableDecorationLineStyleAttrDefine(name)
}
