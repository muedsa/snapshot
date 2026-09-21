package com.muedsa.snapshot.parser.attr.nullable

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.parser.attr.AttrStrValueConst
import com.muedsa.snapshot.parser.attr.DefaultValueAttrDefine

class NullableOffsetAttrDefine(name: String) :
    DefaultValueAttrDefine<Offset?>(name = name, defaultValue = null) {

    override fun parseValue(valueStr: String?): Offset {
        requireNotNull(valueStr) { "Attr [$name] value can not be null" }
        val result = AttrStrValueConst.TWO_FLOAT_VALUE_REGEX.matchEntire(valueStr)
        requireNotNull(result) { "Attr [$name] value format error" }
        val x = result.groupValues[1].toFloat()
        val y = result.groupValues[2].toFloat()
        require(x.isFinite() && y.isFinite()) { "Attr [$name] values must be finite" }
        return Offset(x, y)
    }

    override fun copyWith(name: String, defaultValue: Offset?): NullableOffsetAttrDefine =
        NullableOffsetAttrDefine(name)
}
