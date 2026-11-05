package com.muedsa.snapshot.parser.attr

class BooleanAttrDefine(name: String, defaultValue: Boolean = false) :
    DefaultValueAttrDefine<Boolean>(name = name, defaultValue = defaultValue) {

    override val valueInfo get() = AttributeValueFormats.BOOLEAN

    override fun parseValue(valueStr: String?): Boolean = BooleanAttributeSyntax.parse(valueStr, name)

    override fun copyWith(name: String, defaultValue: Boolean): BooleanAttrDefine =
        BooleanAttrDefine(name, defaultValue)
}
