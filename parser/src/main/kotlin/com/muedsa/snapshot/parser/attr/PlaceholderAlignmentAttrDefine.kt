package com.muedsa.snapshot.parser.attr

import com.muedsa.snapshot.parser.attr.required.AttrDefine
import org.jetbrains.skia.paragraph.PlaceholderAlignment

class PlaceholderAlignmentAttrDefine (name: String, defaultValue: PlaceholderAlignment = PlaceholderAlignment.BASELINE):
    DefaultValueAttrDefine<PlaceholderAlignment>(name = name, defaultValue = defaultValue) {

    override fun parseValue(valueStr: String?): PlaceholderAlignment {
        requireNotNull(valueStr)
        return PlaceholderAlignment.valueOf(valueStr)
    }

    override fun copyWith(name: String, defaultValue: PlaceholderAlignment): AttrDefine<PlaceholderAlignment> =
        PlaceholderAlignmentAttrDefine(name)
}