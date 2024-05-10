package com.muedsa.snapshot.parser.attr

import com.muedsa.snapshot.rendering.flex.MainAxisAlignment

class MainAxisAlignmentAttrDefine(name: String, defaultValue: MainAxisAlignment = MainAxisAlignment.START) :
    DefaultValueAttrDefine<MainAxisAlignment>(name = name, defaultValue = defaultValue) {

    override fun parseValue(valueStr: String?): MainAxisAlignment {
        requireNotNull(valueStr)
        return MainAxisAlignment.valueOf(valueStr)
    }

    override fun copyWith(name: String, defaultValue: MainAxisAlignment): DefaultValueAttrDefine<MainAxisAlignment> =
        MainAxisAlignmentAttrDefine(name, defaultValue)
}