package com.muedsa.snapshot.parser.attr.required

import com.muedsa.snapshot.parser.token.RawAttr

abstract class AttrDefine<T>(
    val name: String,
) {
    /**
     * 当属性被指定到Element上时才会调用
     * @param valueStr
     * `<button disabled="true">` disabled属性被指定, valueStr=true
     * `<button disabled>` disabled属性被指定, 但是valueStr=NULL
     */
    protected abstract fun parseValue(valueStr: String?): T

    /**
     * @param rawAttr 当rawAttr为NULL时 说明未被指定
     */
    open fun parseValue(rawAttr: RawAttr?): T {
        check(rawAttr != null) { "Attr $name must not be null" }
        check(rawAttr.name == name) { "Attr $name not equal $rawAttr" }
        return parseValue(rawAttr.value)
    }

    abstract fun copyWith(name: String): AttrDefine<T>

    override fun toString(): String = name
}