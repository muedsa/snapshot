package com.muedsa.snapshot.parser.attr

import org.jetbrains.skia.paragraph.Direction

class DirectionAttrDefine(name: String, defaultValue: Direction = Direction.LTR) :
    DefaultValueAttrDefine<Direction>(name = name, defaultValue = defaultValue) {

    override fun parseValue(valueStr: String?): Direction {
        requireNotNull(valueStr)
        return Direction.valueOf(valueStr)
    }

    override fun copyWith(name: String, defaultValue: Direction): DefaultValueAttrDefine<Direction> =
        DirectionAttrDefine(name, defaultValue)
}