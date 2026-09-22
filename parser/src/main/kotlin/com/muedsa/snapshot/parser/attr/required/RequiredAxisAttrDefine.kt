package com.muedsa.snapshot.parser.attr.required

import com.muedsa.snapshot.paint.Axis

class RequiredAxisAttrDefine(name: String) : AttrDefine<Axis>(name = name) {

    override fun parseValue(valueStr: String?): Axis {
        requireNotNull(valueStr) { "Attr [$name] value can not be null" }
        return Axis.valueOf(valueStr)
    }

    override fun copyWith(name: String): RequiredAxisAttrDefine = RequiredAxisAttrDefine(name)
}
