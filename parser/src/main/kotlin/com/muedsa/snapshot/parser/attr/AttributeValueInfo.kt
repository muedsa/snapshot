package com.muedsa.snapshot.parser.attr

/** 属性值的填写说明；仅用于诊断，不改变解析和校验规则。 */
data class AttributeValueInfo(
    val format: String,
    val examples: List<String> = emptyList(),
    /** 可用的枚举项或关键字；复合格式中可能仅对应其中一个字段。 */
    val allowedValues: List<String> = emptyList(),
) {
    companion object {
        fun enum(values: List<Enum<*>>): AttributeValueInfo = choices(values.map { it.name })

        fun choices(values: List<String>): AttributeValueInfo = AttributeValueInfo(
            format = "以下取值之一，区分大小写",
            examples = values.take(1),
            allowedValues = values,
        )
    }
}
