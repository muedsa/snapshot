package com.muedsa.snapshot.parser.attr.nullable

import com.muedsa.geometry.Radius
import com.muedsa.snapshot.parser.attr.AttrStrValueConst
import com.muedsa.snapshot.parser.attr.DefaultValueAttrDefine
import com.muedsa.snapshot.parser.attr.required.AttrDefine

class NullableRadiusAttrDefine(name: String) :
    DefaultValueAttrDefine<Radius?>(name = name, defaultValue = null) {

    override fun parseValue(valueStr: String?): Radius =
        parseRadiusFromText(valueStr, this)

    override fun copyWith(name: String, defaultValue: Radius?): NullableRadiusAttrDefine =
        NullableRadiusAttrDefine(name)

    companion object {
        private val PARSERS = listOf(
            AttrStrValueConst.ONE_FLOAT_VALUE_REGEX to { groupValues: List<String> -> Radius.circular(groupValues[1].toFloat()) },
            AttrStrValueConst.TWO_FLOAT_VALUE_REGEX to { groupValues: List<String> ->
                Radius.elliptical(
                    groupValues[1].toFloat(),
                    groupValues[2].toFloat()
                )
            },
        )

        fun parseRadiusFromText(valueStr: String?, attrDefine: AttrDefine<*>): Radius {
            requireNotNull(valueStr) { "Attr [${attrDefine.name}] value can not be null" }
            var reduis: Radius? = null
            for (parser in PARSERS) {
                if (reduis != null) break
                reduis = tryRegexParse(parser.first, valueStr, parser.second)
            }
            requireNotNull(reduis) { "Attr [${attrDefine.name}] value format error" }
            return reduis
        }

        private fun tryRegexParse(
            regex: Regex,
            text: String,
            groupValueParser: (List<String>) -> Radius,
        ): Radius? {
            val result = regex.find(text)
            return if (result != null) {
                groupValueParser.invoke(result.groupValues)
            } else null
        }
    }
}