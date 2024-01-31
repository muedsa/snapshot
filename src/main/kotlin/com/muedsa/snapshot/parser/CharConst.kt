package com.muedsa.snapshot.parser

object CharConst {

    val EOF: Char? = null

    const val NULL: Char = Char.MIN_VALUE

    const val NULL_REPLACEMENT_CHAR: Char = '\uFFFD'

    // char searches. must be sorted, used in inSorted. MUST update CharConst if more arrays are added.
    val ATTR_NAME_CHARS_SORTED: CharArray = charArrayOf('\t', '\n', '\u000c', '\r', ' ', '"', '\'', '/', '<', '=', '>')

    val ATTR_VALUE_UNQUOTED_ARRAY: CharArray =
        charArrayOf(NULL, '\t', '\n', '\u000c', '\r', ' ', '"', '&', '\'', '<', '=', '>', '`')
}