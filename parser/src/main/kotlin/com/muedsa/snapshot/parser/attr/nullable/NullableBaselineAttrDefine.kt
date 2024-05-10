package com.muedsa.snapshot.parser.attr.nullable

import com.muedsa.snapshot.parser.attr.DefaultValueAttrDefine
import org.jetbrains.skia.paragraph.BaselineMode

class NullableBaselineAttrDefine(name: String) :
    DefaultValueAttrDefine<BaselineMode?>(name = name, defaultValue = null) {

    override fun parseValue(valueStr: String?): BaselineMode {
        requireNotNull(valueStr)
        return BaselineMode.valueOf(valueStr)
    }

    override fun copyWith(name: String, defaultValue: BaselineMode?): NullableBaselineAttrDefine =
        NullableBaselineAttrDefine(name)
}