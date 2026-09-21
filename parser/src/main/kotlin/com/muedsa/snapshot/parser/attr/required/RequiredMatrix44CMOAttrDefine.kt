package com.muedsa.snapshot.parser.attr.required

import com.muedsa.geometry.Matrix44CMO

class RequiredMatrix44CMOAttrDefine(name: String) : AttrDefine<Matrix44CMO>(name = name) {

    override fun parseValue(valueStr: String?): Matrix44CMO {
        requireNotNull(valueStr) { "Attr [$name] value can not be null" }
        require(valueStr.startsWith('(') && valueStr.endsWith(')')) {
            "Attr [$name] value format error"
        }
        val values = valueStr.substring(1, valueStr.lastIndex).split(',')
        require(values.size == 16) { "Attr [$name] must contain 16 values" }
        val matrix = values.map { value ->
            require(value == value.trim()) { "Attr [$name] must not contain whitespace" }
            value.toFloat().also {
                require(it.isFinite()) { "Attr [$name] values must be finite" }
            }
        }
        return Matrix44CMO(*matrix.toFloatArray())
    }

    override fun copyWith(name: String): RequiredMatrix44CMOAttrDefine = RequiredMatrix44CMOAttrDefine(name)
}
