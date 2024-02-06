package com.muedsa.snapshot.parser.attr

import com.muedsa.geometry.EdgeInsets
import com.muedsa.snapshot.parser.attr.required.AttrDefine

class EdgeInsetsAttrDefine(name: String, defaultValue: EdgeInsets = EdgeInsets.ZERO) :
    DefaultValueAttrDefine<EdgeInsets>(name = name, defaultValue = defaultValue) {

    override fun parseValue(valueStr: String?): EdgeInsets = parseEdgeInsetsFromText(valueStr, this)

    override fun copyWith(name: String, defaultValue: EdgeInsets): EdgeInsetsAttrDefine =
        EdgeInsetsAttrDefine(name, defaultValue)

    companion object {

        private val PARSERS = listOf(
            AttrStrValueConst.ONE_FLOAT_VALUE_REGEX to { groupValues: List<String> -> EdgeInsets.all(groupValues[1].toFloat()) },
            AttrStrValueConst.TWO_FLOAT_VALUE_REGEX to { groupValues: List<String> ->
                EdgeInsets.symmetric(
                    groupValues[1].toFloat(),
                    groupValues[2].toFloat()
                )
            },
            AttrStrValueConst.FOUR_FLOAT_VALUE_REGEX to { groupValues: List<String> ->
                EdgeInsets(
                    groupValues[1].toFloat(),
                    groupValues[2].toFloat(),
                    groupValues[3].toFloat(),
                    groupValues[4].toFloat()
                )
            }
        )

        fun parseEdgeInsetsFromText(valueStr: String?, attrDefine: AttrDefine<*>): EdgeInsets {
            requireNotNull(valueStr) { "Attr [${attrDefine.name}] value can not be null" }
            var insets: EdgeInsets? = null
            for (parser in PARSERS) {
                if (insets != null) break
                insets = tryRegexParse(parser.first, valueStr, parser.second)
            }
            requireNotNull(insets) { "Attr [${attrDefine.name}] value format error" }
            return insets
        }

        private fun tryRegexParse(
            regex: Regex,
            text: String,
            groupValueParser: (List<String>) -> EdgeInsets,
        ): EdgeInsets? {
            val result = regex.find(text)
            return if (result != null) {
                groupValueParser.invoke(result.groupValues)
            } else null
        }
    }
}