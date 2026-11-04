package com.muedsa.snapshot.parser.attr

/** 数值复合属性的共同词法；参数数量和排列顺序由具体属性决定。 */
internal object NumericAttributeSyntax {
    private val NUMBER = Regex("[+-]?(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)(?:[eE][+-]?[0-9]+)?")
    private val WHITESPACE = Regex("\\s+")

    fun parse(value: String, attrName: String, commaSeparated: Boolean): List<Float> {
        val text = value.trim()
        val parts = if (commaSeparated) text.split(',') else text.split(WHITESPACE)
        return parts.map { part ->
            val number = part.trim()
            require(NUMBER.matches(number)) { "Attr [$attrName] value format error: expected a number, got [$number]" }
            number.toFloat().also {
                require(it.isFinite()) { "Attr [$attrName] values must be finite" }
            }
        }
    }
}
