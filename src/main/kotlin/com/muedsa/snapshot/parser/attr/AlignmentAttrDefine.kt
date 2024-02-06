package com.muedsa.snapshot.parser.attr

import com.muedsa.geometry.BoxAlignment
import com.muedsa.snapshot.parser.attr.required.AttrDefine

class AlignmentAttrDefine(name: String, defaultValue: BoxAlignment = BoxAlignment.CENTER) :
    DefaultValueAttrDefine<BoxAlignment>(name = name, defaultValue = defaultValue) {

    override fun parseValue(valueStr: String?): BoxAlignment = parseAlignmentFromText(valueStr, this)

    override fun copyWith(name: String, defaultValue: BoxAlignment): AttrDefine<BoxAlignment> =
        AlignmentAttrDefine(name, defaultValue)

    companion object {

        fun parseAlignmentFromText(valueStr: String?, attrDefine: AttrDefine<*>): BoxAlignment {
            requireNotNull(valueStr) { "Attr [${attrDefine.name}] value can not be null" }
            return when (valueStr) {
                "TOP_LEFT" -> BoxAlignment.TOP_LEFT
                "TOP_CENTER" -> BoxAlignment.TOP_CENTER
                "TOP_RIGHT" -> BoxAlignment.TOP_RIGHT
                "CENTER_LEFT" -> BoxAlignment.CENTER_LEFT
                "CENTER" -> BoxAlignment.CENTER
                "CENTER_RIGHT" -> BoxAlignment.CENTER_RIGHT
                "BOTTOM_LEFT" -> BoxAlignment.BOTTOM_LEFT
                "BOTTOM_CENTER" -> BoxAlignment.BOTTOM_CENTER
                "BOTTOM_RIGHT" -> BoxAlignment.BOTTOM_RIGHT
                else -> {
                    val result = AttrStrValueConst.TWO_FLOAT_VALUE_REGEX.find(valueStr)
                    requireNotNull(result) { "Attr [${attrDefine.name}] value format error" }
                    require(result.groupValues.size == 3) { "Attr [${attrDefine.name}] value format error" }
                    val x = result.groupValues[1].toFloat()
                    val y = result.groupValues[2].toFloat()
                    BoxAlignment(x, y)
                }
            }
        }
    }
}