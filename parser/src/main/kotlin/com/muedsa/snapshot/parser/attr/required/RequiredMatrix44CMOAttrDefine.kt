package com.muedsa.snapshot.parser.attr.required

import com.muedsa.geometry.Matrix44CMO
import com.muedsa.snapshot.parser.attr.AttributeValueFormats
import com.muedsa.snapshot.parser.attr.TransformAttributeSyntax

class RequiredMatrix44CMOAttrDefine(name: String) : AttrDefine<Matrix44CMO>(name = name) {

    override val valueInfo get() = AttributeValueFormats.MATRIX

    override fun parseValue(valueStr: String?): Matrix44CMO = parseMatrix44CMOFromText(valueStr, this)

    override fun copyWith(name: String): RequiredMatrix44CMOAttrDefine = RequiredMatrix44CMOAttrDefine(name)

    companion object {

        fun parseMatrix44CMOFromText(valueStr: String?, attrDefine: AttrDefine<*>): Matrix44CMO {
            requireNotNull(valueStr) { "Attr [${attrDefine.name}] value can not be null" }
            return TransformAttributeSyntax.parse(valueStr, attrDefine.name)
        }
    }
}
