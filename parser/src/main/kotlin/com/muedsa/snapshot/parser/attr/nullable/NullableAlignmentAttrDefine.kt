package com.muedsa.snapshot.parser.attr.nullable

import com.muedsa.geometry.BoxAlignment
import com.muedsa.snapshot.parser.attr.AlignmentAttrDefine
import com.muedsa.snapshot.parser.attr.DefaultValueAttrDefine

class NullableAlignmentAttrDefine(name: String) :
    DefaultValueAttrDefine<BoxAlignment?>(name = name, defaultValue = null) {

    override fun parseValue(valueStr: String?): BoxAlignment =
        AlignmentAttrDefine.parseAlignmentFromText(valueStr, this)

    override fun copyWith(name: String, defaultValue: BoxAlignment?): NullableAlignmentAttrDefine =
        NullableAlignmentAttrDefine(name)

}