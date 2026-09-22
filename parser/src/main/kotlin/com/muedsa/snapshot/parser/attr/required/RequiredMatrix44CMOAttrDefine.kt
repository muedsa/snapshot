package com.muedsa.snapshot.parser.attr.required

import com.muedsa.geometry.Matrix44CMO

class RequiredMatrix44CMOAttrDefine(name: String) : AttrDefine<Matrix44CMO>(name = name) {

    override fun parseValue(valueStr: String?): Matrix44CMO = parseMatrix44CMOFromText(valueStr, this)

    override fun copyWith(name: String): RequiredMatrix44CMOAttrDefine = RequiredMatrix44CMOAttrDefine(name)

    companion object {

        fun parseMatrix44CMOFromText(valueStr: String?, attrDefine: AttrDefine<*>): Matrix44CMO {
            requireNotNull(valueStr) { "Attr [${attrDefine.name}] value can not be null" }
            require(valueStr.startsWith('(') && valueStr.endsWith(')')) {
                "Attr [${attrDefine.name}] value format error"
            }
            val values = valueStr.substring(1, valueStr.lastIndex).split(',')
            require(values.size == 16) { "Attr [${attrDefine.name}] must contain 16 values" }
            val matrix = values.map { value ->
                require(value == value.trim()) { "Attr [${attrDefine.name}] must not contain whitespace" }
                value.toFloat().also {
                    require(it.isFinite()) { "Attr [${attrDefine.name}] values must be finite" }
                }
            }
            return Matrix44CMO(*matrix.toFloatArray())
        }
    }
}
