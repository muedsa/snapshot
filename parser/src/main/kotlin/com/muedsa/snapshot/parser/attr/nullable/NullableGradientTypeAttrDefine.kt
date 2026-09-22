package com.muedsa.snapshot.parser.attr.nullable

import com.muedsa.snapshot.parser.attr.DefaultValueAttrDefine

enum class GradientType {
    LINEAR,
    RADIAL,
    SWEEP,
}

class NullableGradientTypeAttrDefine(name: String) :
    DefaultValueAttrDefine<GradientType?>(name = name, defaultValue = null) {

    override fun parseValue(valueStr: String?): GradientType {
        requireNotNull(valueStr) { "Attr [$name] value can not be null" }
        return GradientType.valueOf(valueStr)
    }

    override fun copyWith(name: String, defaultValue: GradientType?): NullableGradientTypeAttrDefine =
        NullableGradientTypeAttrDefine(name)
}
