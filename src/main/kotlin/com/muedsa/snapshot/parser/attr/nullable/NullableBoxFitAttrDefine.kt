package com.muedsa.snapshot.parser.attr.nullable

import com.muedsa.snapshot.paint.BoxFit
import com.muedsa.snapshot.parser.attr.DefaultValueAttrDefine


class NullableBoxFitAttrDefine(name: String) :
    DefaultValueAttrDefine<BoxFit?>(name = name, defaultValue = null) {

    override fun parseValue(valueStr: String?): BoxFit {
        requireNotNull(valueStr)
        return BoxFit.valueOf(valueStr)
    }

    override fun copyWith(name: String, defaultValue: BoxFit?): DefaultValueAttrDefine<BoxFit?> =
        NullableBoxFitAttrDefine(name)
}