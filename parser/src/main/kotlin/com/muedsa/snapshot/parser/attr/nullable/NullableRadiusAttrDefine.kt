package com.muedsa.snapshot.parser.attr.nullable

import com.muedsa.geometry.Radius
import com.muedsa.snapshot.parser.attr.NumericAttributeSyntax
import com.muedsa.snapshot.parser.attr.AttributeValueFormats
import com.muedsa.snapshot.parser.attr.DefaultValueAttrDefine
import com.muedsa.snapshot.parser.attr.required.AttrDefine

class NullableRadiusAttrDefine(name: String) :
    DefaultValueAttrDefine<Radius?>(name = name, defaultValue = null) {

    override val valueInfo get() = AttributeValueFormats.RADIUS

    override fun parseValue(valueStr: String?): Radius =
        parseRadiusFromText(valueStr, this)

    override fun copyWith(name: String, defaultValue: Radius?): NullableRadiusAttrDefine =
        NullableRadiusAttrDefine(name)

    companion object {
        fun parseRadiusFromText(valueStr: String?, attrDefine: AttrDefine<*>): Radius {
            requireNotNull(valueStr) { "Attr [${attrDefine.name}] value can not be null" }
            val text = valueStr.trim()
            if (text.startsWith('(') && text.endsWith(')')) {
                val values = NumericAttributeSyntax.pair(text, attrDefine.name)
                return Radius.elliptical(values[0], values[1])
            }
            val axes = text.split('/')
            require(axes.size in 1..2) { "Attr [${attrDefine.name}] radius must have one or two axes" }
            val values = axes.map {
                val numbers = NumericAttributeSyntax.parse(it, attrDefine.name, false)
                require(numbers.size == 1) { "Attr [${attrDefine.name}] use horizontal / vertical for an elliptical radius" }
                numbers.single()
            }
            return Radius.elliptical(values[0], values.getOrElse(1) { values[0] })
        }
    }
}
