package com.muedsa.snapshot.parser.attr

import org.jetbrains.skia.paragraph.Alignment

class ParagraphAlignmentAttrDefine(
    name: String,
    defaultValue: Alignment = Alignment.START,
) : DefaultValueAttrDefine<Alignment>(name = name, defaultValue = defaultValue) {

    override fun parseValue(valueStr: String?): Alignment {
        requireNotNull(valueStr) { "Attr [$name] value can not be null" }
        return Alignment.valueOf(valueStr)
    }

    override fun copyWith(
        name: String,
        defaultValue: Alignment,
    ): ParagraphAlignmentAttrDefine = ParagraphAlignmentAttrDefine(name, defaultValue)
}
