package com.muedsa.snapshot.parser.attr.nullable

import com.muedsa.snapshot.parser.attr.DefaultValueAttrDefine
import com.muedsa.snapshot.parser.attr.StringAttrDefine
import com.muedsa.snapshot.parser.attr.required.AttrDefine

class NullableStringAttrDefine(
    name: String,
    val checker: ((AttrDefine<String?>, String?) -> Unit)? = null,
) : DefaultValueAttrDefine<String?>(name = name, defaultValue = null) {

    override fun parseValue(valueStr: String?): String =
        StringAttrDefine.onlyCheck(attr = this, valueStr = valueStr, checker = checker)

    override fun copyWith(name: String, defaultValue: String?): AttrDefine<String?> =
        NullableStringAttrDefine(name, checker = checker)
}