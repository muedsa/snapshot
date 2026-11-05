package com.muedsa.snapshot.parser.attr

/** 只在属性存在时调用；未提供属性的默认值仍由 DefaultValueAttrDefine 处理。 */
internal object BooleanAttributeSyntax {
    fun parse(value: String?, attrName: String): Boolean {
        if (value == null) return true
        val text = value.trim()
        return when {
            text.equals("true", ignoreCase = true) -> true
            text.equals("false", ignoreCase = true) -> false
            else -> throw IllegalArgumentException("Attr [$attrName] expects true or false")
        }
    }
}
