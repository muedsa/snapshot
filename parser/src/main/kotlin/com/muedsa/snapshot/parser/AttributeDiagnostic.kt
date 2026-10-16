package com.muedsa.snapshot.parser

import com.muedsa.snapshot.parser.attr.AttributeValueInfo

/** 属性解析失败的结构化信息；位置和原始原因分别读取 ParseException.pos / cause。 */
data class AttributeDiagnostic(
    val attributeName: String,
    val rawValue: String?,
    /** 区分“未提供属性”和“只写属性名、没有赋值”。 */
    val attributePresent: Boolean,
    val valueInfo: AttributeValueInfo?,
    /** 直接调用基于属性 Map 的解析接口时可能没有元素上下文。 */
    val elementName: String? = null,
) {
    internal fun describe(): String = buildList {
        elementName?.let { add("元素：<$it>") }
        add("实际值：" + when {
            !attributePresent -> "未提供属性"
            rawValue == null -> "未赋值"
            else -> "\"${preview(rawValue)}\""
        })
        valueInfo?.let {
            add("格式：${it.format}")
            if (it.allowedValues.isNotEmpty()) add("可用值：${it.allowedValues.joinToString()}")
            if (it.examples.isNotEmpty()) add("示例：${it.examples.joinToString(" / ")}")
        }
    }.joinToString("；")

    private fun preview(value: String): String = value.take(120)
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\r", "\\r")
        .replace("\n", "\\n")
        .replace("\t", "\\t") + if (value.length > 120) "…" else ""
}
