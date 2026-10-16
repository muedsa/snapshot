package com.muedsa.snapshot.parser.attr

import com.muedsa.snapshot.parser.attr.required.AttrDefine

class StringAttrDefine(
    name: String,
    defaultValue: String,
    override val valueInfo: AttributeValueInfo,
    val checker: ((AttrDefine<String>, String?) -> Unit)? = null,
) : DefaultValueAttrDefine<String>(name = name, defaultValue = defaultValue) {

    constructor(
        name: String,
        defaultValue: String,
        checker: ((AttrDefine<String>, String?) -> Unit)? = null,
    ) : this(name, defaultValue, AttributeValueFormats.STRING, checker)

    override fun parseValue(valueStr: String?): String = onlyCheck(this, valueStr, checker)

    override fun copyWith(name: String, defaultValue: String): StringAttrDefine =
        StringAttrDefine(name, defaultValue, valueInfo, checker)

    companion object {

        fun <T> onlyCheck(
            attr: AttrDefine<T>,
            valueStr: String?,
            checker: ((AttrDefine<T>, String?) -> Unit)? = null,
        ): String {
            requireNotNull(valueStr) { "Attr [${attr.name}] value can not be null" }
            checker?.invoke(attr, valueStr)
            return valueStr
        }
    }
}
