package com.muedsa.snapshot.parser.attr

import com.muedsa.geometry.EdgeInsets
import com.muedsa.snapshot.parser.attr.required.AttrDefine

class EdgeInsetsAttrDefine(name: String, defaultValue: EdgeInsets = EdgeInsets.ZERO) :
    DefaultValueAttrDefine<EdgeInsets>(name = name, defaultValue = defaultValue) {

    override val valueInfo get() = AttributeValueFormats.EDGE_INSETS

    override fun parseValue(valueStr: String?): EdgeInsets = parseEdgeInsetsFromText(valueStr, this)

    override fun copyWith(name: String, defaultValue: EdgeInsets): EdgeInsetsAttrDefine =
        EdgeInsetsAttrDefine(name, defaultValue)

    companion object {

        fun parseEdgeInsetsFromText(valueStr: String?, attrDefine: AttrDefine<*>): EdgeInsets {
            requireNotNull(valueStr) { "Attr [${attrDefine.name}] value can not be null" }
            val text = valueStr.trim()
            if (text.startsWith('(') && text.endsWith(')')) {
                // 保留旧元组的左、上、右、下顺序，不能直接当作 CSS 简写。
                val values = NumericAttributeSyntax.parse(text.substring(1, text.lastIndex), attrDefine.name, true)
                return when (values.size) {
                    2 -> EdgeInsets.symmetric(vertical = values[0], horizontal = values[1])
                    4 -> EdgeInsets(values[0], values[1], values[2], values[3])
                    else -> throw IllegalArgumentException("Attr [${attrDefine.name}] tuple must contain 2 or 4 values")
                }
            }
            val values = NumericAttributeSyntax.parse(text, attrDefine.name, false)
            return when (values.size) {
                1 -> EdgeInsets.all(values[0])
                2 -> EdgeInsets.symmetric(vertical = values[0], horizontal = values[1])
                3 -> EdgeInsets(values[1], values[0], values[1], values[2])
                4 -> EdgeInsets(values[3], values[0], values[1], values[2])
                else -> throw IllegalArgumentException("Attr [${attrDefine.name}] shorthand must contain 1 to 4 values")
            }
        }
    }
}
