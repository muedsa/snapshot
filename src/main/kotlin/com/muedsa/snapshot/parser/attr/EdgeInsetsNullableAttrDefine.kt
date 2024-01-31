package com.muedsa.snapshot.parser.attr

import com.muedsa.geometry.EdgeInsets

class EdgeInsetsNullableAttrDefine(name: String, defaultValue: EdgeInsets? = null) :
    DefaultValueAttrDefine<EdgeInsets?>(name = name, defaultValue = defaultValue) {

    override fun parseValue(valueStr: String?): EdgeInsets =
        EdgeInsetsAttrDefine.parseEdgeInsetsFromText(valueStr, this)
}