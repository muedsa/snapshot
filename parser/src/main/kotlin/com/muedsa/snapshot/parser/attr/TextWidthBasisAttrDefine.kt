package com.muedsa.snapshot.parser.attr

import com.muedsa.snapshot.paint.text.TextWidthBasis

class TextWidthBasisAttrDefine(
    name: String,
    defaultValue: TextWidthBasis = TextWidthBasis.PARENT,
) : DefaultValueAttrDefine<TextWidthBasis>(name = name, defaultValue = defaultValue) {

    override fun parseValue(valueStr: String?): TextWidthBasis {
        requireNotNull(valueStr) { "Attr [$name] value can not be null" }
        return TextWidthBasis.valueOf(valueStr)
    }

    override fun copyWith(
        name: String,
        defaultValue: TextWidthBasis,
    ): TextWidthBasisAttrDefine = TextWidthBasisAttrDefine(name, defaultValue)
}
