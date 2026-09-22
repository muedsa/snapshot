package com.muedsa.snapshot.parser.attr.nullable

import com.muedsa.geometry.Matrix44CMO
import com.muedsa.snapshot.parser.attr.DefaultValueAttrDefine
import com.muedsa.snapshot.parser.attr.required.RequiredMatrix44CMOAttrDefine

class NullableMatrix44CMOAttrDefine(name: String) :
    DefaultValueAttrDefine<Matrix44CMO?>(name = name, defaultValue = null) {

    override fun parseValue(valueStr: String?): Matrix44CMO =
        RequiredMatrix44CMOAttrDefine.parseMatrix44CMOFromText(valueStr, this)

    override fun copyWith(name: String, defaultValue: Matrix44CMO?): NullableMatrix44CMOAttrDefine =
        NullableMatrix44CMOAttrDefine(name)
}
