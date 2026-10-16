package com.muedsa.snapshot.parser.attr.required

import com.muedsa.snapshot.parser.attr.AttributeValueFormats
import com.muedsa.snapshot.parser.attr.AttributeValueInfo
import com.muedsa.snapshot.parser.attr.StringAttrDefine

class RequiredStringAttrDefine(
    name: String,
    override val valueInfo: AttributeValueInfo,
    val checker: ((AttrDefine<String>, String?) -> Unit)? = null,
) : AttrDefine<String>(name = name) {

    constructor(
        name: String,
        checker: ((AttrDefine<String>, String?) -> Unit)? = null,
    ) : this(name, AttributeValueFormats.STRING, checker)

    override fun parseValue(valueStr: String?): String = StringAttrDefine.onlyCheck(this, valueStr, checker)

    override fun copyWith(name: String): AttrDefine<String> =
        RequiredStringAttrDefine(name, valueInfo, checker)
}
