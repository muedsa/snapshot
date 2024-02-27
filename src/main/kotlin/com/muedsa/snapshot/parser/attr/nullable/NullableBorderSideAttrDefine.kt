package com.muedsa.snapshot.parser.attr.nullable

import com.muedsa.snapshot.paint.decoration.BorderSide
import com.muedsa.snapshot.paint.decoration.BorderStyle
import com.muedsa.snapshot.parser.attr.ColorAttrDefine
import com.muedsa.snapshot.parser.attr.DefaultValueAttrDefine

class NullableBorderSideAttrDefine(name: String) :
    DefaultValueAttrDefine<BorderSide?>(name = name, defaultValue = null) {

    override fun parseValue(valueStr: String?): BorderSide {
        requireNotNull(valueStr)
        val valueArr = valueStr.split(" ")
        require(valueArr.size == 3)
        return BorderSide(
            width = valueArr[0].toFloat(),
            style = BorderStyle.valueOf(valueArr[1]),
            color = ColorAttrDefine.parseColorFromText(valueArr[2], this),
        )
    }

    override fun copyWith(name: String, defaultValue: BorderSide?): NullableBorderSideAttrDefine =
        NullableBorderSideAttrDefine(name)
}