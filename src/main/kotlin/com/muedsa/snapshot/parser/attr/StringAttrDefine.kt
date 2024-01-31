package com.muedsa.snapshot.parser.attr

class StringAttrDefine(
    name: String,
    defaultValue: String,
    val checker: (StringAttrDefine, String?) -> Unit = { _, _ -> },
) : DefaultValueAttrDefine<String>(name = name, defaultValue = defaultValue) {

    override fun parseValue(valueStr: String?): String {
        requireNotNull(valueStr) { "Attr $name value can not be null" }
        checker.invoke(this, valueStr)
        return valueStr
    }
}