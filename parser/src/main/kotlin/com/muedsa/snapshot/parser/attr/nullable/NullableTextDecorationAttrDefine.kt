package com.muedsa.snapshot.parser.attr.nullable

import com.muedsa.snapshot.parser.attr.DefaultValueAttrDefine

enum class TextDecorationLine {
    UNDERLINE,
    OVERLINE,
    LINE_THROUGH,
}

class NullableTextDecorationAttrDefine(name: String) :
    DefaultValueAttrDefine<Set<TextDecorationLine>?>(name = name, defaultValue = null) {

    override fun parseValue(valueStr: String?): Set<TextDecorationLine> {
        requireNotNull(valueStr) { "Attr [$name] value can not be null" }
        val values = valueStr.split(',').map(String::trim)
        require(values.none(String::isEmpty)) { "Attr [$name] contains an empty decoration name" }
        require(values.distinct().size == values.size) { "Attr [$name] contains duplicate decoration names" }
        if ("NONE" in values) {
            require(values.size == 1) { "Attr [$name] value NONE can not be combined with other decorations" }
            return emptySet()
        }
        return values.mapTo(linkedSetOf(), TextDecorationLine::valueOf)
    }

    override fun copyWith(
        name: String,
        defaultValue: Set<TextDecorationLine>?,
    ): NullableTextDecorationAttrDefine = NullableTextDecorationAttrDefine(name)
}
