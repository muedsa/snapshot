package com.muedsa.snapshot.parser.attr

import com.muedsa.snapshot.rendering.flex.FlexFit

class FlexFitAttrDefine(name: String, defaultValue: FlexFit = FlexFit.LOOSE) :
    DefaultValueAttrDefine<FlexFit>(name = name, defaultValue = defaultValue) {

    override fun parseValue(valueStr: String?): FlexFit {
        requireNotNull(valueStr) { "Attr [$name] value can not be null" }
        return FlexFit.valueOf(valueStr)
    }

    override fun copyWith(name: String, defaultValue: FlexFit): FlexFitAttrDefine =
        FlexFitAttrDefine(name, defaultValue)
}
