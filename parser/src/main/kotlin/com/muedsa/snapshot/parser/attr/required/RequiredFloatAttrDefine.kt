package com.muedsa.snapshot.parser.attr.required

import com.muedsa.snapshot.parser.attr.AttributeValueFormats

class RequiredFloatAttrDefine(name: String) : AttrDefine<Float>(name = name) {

    override val valueInfo get() = AttributeValueFormats.FLOAT

    override fun parseValue(valueStr: String?): Float {
        requireNotNull(valueStr) { "Attr [$name] value can not be null" }
        return valueStr.toFloat()
    }

    override fun copyWith(name: String): RequiredFloatAttrDefine = RequiredFloatAttrDefine(name)
}
