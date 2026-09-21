package com.muedsa.snapshot.parser.attr

import org.jetbrains.skia.FilterTileMode

class FilterTileModeAttrDefine(
    name: String,
    defaultValue: FilterTileMode = FilterTileMode.CLAMP,
) : DefaultValueAttrDefine<FilterTileMode>(name = name, defaultValue = defaultValue) {

    override fun parseValue(valueStr: String?): FilterTileMode {
        requireNotNull(valueStr) { "Attr [$name] value can not be null" }
        return FilterTileMode.valueOf(valueStr)
    }

    override fun copyWith(name: String, defaultValue: FilterTileMode): FilterTileModeAttrDefine =
        FilterTileModeAttrDefine(name, defaultValue)
}
