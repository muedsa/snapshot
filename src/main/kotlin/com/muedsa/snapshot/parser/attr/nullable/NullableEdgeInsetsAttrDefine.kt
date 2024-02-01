package com.muedsa.snapshot.parser.attr.nullable

import com.muedsa.geometry.EdgeInsets
import com.muedsa.snapshot.parser.attr.DefaultValueAttrDefine
import com.muedsa.snapshot.parser.attr.EdgeInsetsAttrDefine

class NullableEdgeInsetsAttrDefine(name: String) :
    DefaultValueAttrDefine<EdgeInsets?>(name = name, defaultValue = null) {

    override fun parseValue(valueStr: String?): EdgeInsets =
        EdgeInsetsAttrDefine.parseEdgeInsetsFromText(valueStr, this)

    override fun copyWith(name: String, defaultValue: EdgeInsets?): NullableEdgeInsetsAttrDefine =
        NullableEdgeInsetsAttrDefine(name)
}