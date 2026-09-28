package com.muedsa.snapshot.parser.attr

/** 只在函数括号外拆分复合属性，避免拆开 rgb()/hsl() 的参数。 */
internal object CssColorSyntax {

    fun splitOn(value: String, delimiter: Char): List<String> = split(value) { it == delimiter }

    fun splitOnWhitespace(value: String): List<String> = split(value, Char::isWhitespace).filter(String::isNotEmpty)

    private fun split(value: String, isDelimiter: (Char) -> Boolean): List<String> {
        val result = mutableListOf<String>()
        var depth = 0
        var start = 0
        value.forEachIndexed { index, char ->
            when (char) {
                '(' -> depth++
                ')' -> {
                    require(depth > 0) { "Unexpected ')' in color value" }
                    depth--
                }
                else -> if (depth == 0 && isDelimiter(char)) {
                    result += value.substring(start, index).trim()
                    start = index + 1
                }
            }
        }
        require(depth == 0) { "Unclosed '(' in color value" }
        result += value.substring(start).trim()
        return result
    }
}
