package com.muedsa.snapshot.parser.attr

import org.jetbrains.skia.paragraph.BaselineMode

class BaselineAttrDefine(name: String, defaultValue: BaselineMode = BaselineMode.ALPHABETIC) :
    DefaultValueAttrDefine<BaselineMode>(name = name, defaultValue = defaultValue) {

    override fun parseValue(valueStr: String?): BaselineMode {
        requireNotNull(valueStr)
        return BaselineMode.valueOf(valueStr)
    }

    override fun copyWith(name: String, defaultValue: BaselineMode): DefaultValueAttrDefine<BaselineMode> =
        BaselineAttrDefine(name, defaultValue)
}