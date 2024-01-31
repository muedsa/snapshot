package com.muedsa.snapshot.parser.attr

object AttrStrValueConst {

    val ONE_FLOAT_VALUE_REGEX = """^([+-]?(?:\d*[.])?\d+)$""".toRegex()

    val TWO_FLOAT_VALUE_REGEX: Regex = """^\(([+-]?(?:\d*[.])?\d+),([+-]?(?:\d*[.])?\d+)\)$""".toRegex()

    val FOUR_FLOAT_VALUE_REGEX: Regex =
        """^\(([+-]?(?:\d*[.])?\d+),([+-]?(?:\d*[.])?\d+),([+-]?(?:\d*[.])?\d+),([+-]?(?:\d*[.])?\d+)\)$""".toRegex()
}