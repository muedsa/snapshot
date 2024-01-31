package com.muedsa.snapshot.parser.attr

import com.muedsa.snapshot.parser.token.RawAttr

abstract class DefaultValueAttrDefine<T>(
    name: String,
    val defaultValue: T,
) : AttrDefine<T>(name = name) {

    /**
     * 当属性被指定到Element上时才会调用
     * @param valueStr
     * `<button disabled="true">` disabled属性被指定, valueStr=true
     * `<button disabled>` disabled属性被指定, 但是valueStr=NULL
     */
    abstract override fun parseValue(valueStr: String?): T

    /**
     * @param rawAttr 当rawAttr为NULL时 说明未被指定, return defaultValue
     */
    override fun parseValue(rawAttr: RawAttr?): T {
        return if (rawAttr != null && rawAttr.name == name) {
            parseValue(rawAttr.value)
        } else defaultValue
    }

    override fun toString(): String = "$name = $defaultValue"
}