package com.muedsa.snapshot.parser.attr

import com.muedsa.geometry.BoxAlignment

class AlignmentNullableAttrDefine(name: String, defaultValue: BoxAlignment? = null) :
    DefaultValueAttrDefine<BoxAlignment?>(name = name, defaultValue = defaultValue) {

    override fun parseValue(valueStr: String?): BoxAlignment =
        AlignmentAttrDefine.parseAlignmentFromText(valueStr, this)

}