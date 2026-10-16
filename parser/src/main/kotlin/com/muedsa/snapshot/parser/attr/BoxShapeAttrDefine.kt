package com.muedsa.snapshot.parser.attr

import com.muedsa.snapshot.paint.decoration.BoxShape

class BoxShapeAttrDefine(
    name: String,
    defaultValue: BoxShape = BoxShape.RECTANGLE,
) : DefaultValueAttrDefine<BoxShape>(name = name, defaultValue = defaultValue) {

    override val valueInfo get() = AttributeValueInfo.enum(BoxShape.entries)

    override fun parseValue(valueStr: String?): BoxShape {
        requireNotNull(valueStr) { "Attr [$name] value can not be null" }
        return BoxShape.valueOf(valueStr)
    }

    override fun copyWith(name: String, defaultValue: BoxShape): BoxShapeAttrDefine =
        BoxShapeAttrDefine(name, defaultValue)
}
