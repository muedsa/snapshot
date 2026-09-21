package com.muedsa.snapshot.parser.attr

import com.muedsa.snapshot.rendering.stack.StackFit

class StackFitAttrDefine(
    name: String,
    defaultValue: StackFit = StackFit.LOOSE,
) : DefaultValueAttrDefine<StackFit>(name = name, defaultValue = defaultValue) {

    override fun parseValue(valueStr: String?): StackFit {
        requireNotNull(valueStr) { "Attr [$name] value can not be null" }
        return StackFit.valueOf(valueStr)
    }

    override fun copyWith(name: String, defaultValue: StackFit): StackFitAttrDefine =
        StackFitAttrDefine(name, defaultValue)
}
