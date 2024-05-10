package com.muedsa.snapshot.parser.attr.required

import com.muedsa.snapshot.parser.attr.StringAttrDefine

class RequiredStringAttrDefine(
    name: String,
    val checker: ((AttrDefine<String>, String?) -> Unit)? = null,
) : AttrDefine<String>(name = name) {
    override fun parseValue(valueStr: String?): String = StringAttrDefine.onlyCheck(this, valueStr, checker)

    override fun copyWith(name: String): AttrDefine<String> =
        RequiredStringAttrDefine(name)
}