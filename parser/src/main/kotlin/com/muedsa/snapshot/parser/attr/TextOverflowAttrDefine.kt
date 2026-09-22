package com.muedsa.snapshot.parser.attr

import com.muedsa.snapshot.paint.text.TextOverflow

class TextOverflowAttrDefine(
    name: String,
    defaultValue: TextOverflow = TextOverflow.CLIP,
) : DefaultValueAttrDefine<TextOverflow>(name = name, defaultValue = defaultValue) {

    override fun parseValue(valueStr: String?): TextOverflow {
        requireNotNull(valueStr) { "Attr [$name] value can not be null" }
        return TextOverflow.valueOf(valueStr)
    }

    override fun copyWith(
        name: String,
        defaultValue: TextOverflow,
    ): TextOverflowAttrDefine = TextOverflowAttrDefine(name, defaultValue)
}
