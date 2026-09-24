package com.muedsa.snapshot.parser.token

import com.muedsa.snapshot.parser.CharConst
import com.muedsa.snapshot.parser.CharacterReader

enum class TokenizerState {
    DATA {
        override fun read(t: Tokenizer, r: CharacterReader) {
            when (r.current()) {

                '<' -> t.advanceTransition(TAG_OPEN)

                CharConst.NULL -> {
                    t.error(this)
//                  t.emit(CharConst.NULL)
                }

                CharConst.EOF -> t.emit(Token.EOF())

                else -> t.emit(r.consumeData())
            }
        }
    },
    RCDATA {
        override fun read(t: Tokenizer, r: CharacterReader) {
            when (r.current()) {

                '<' -> t.advanceTransition(RCDATA_LESS_THAN_SIGN)

                CharConst.NULL -> {
                    t.error(this)
//                  r.advance()
//                  t.emit(CharConst.NULL_REPLACEMENT_CHAR)
                }

                CharConst.EOF -> t.emit(Token.EOF())

                else -> t.emit(r.consumeData())
            }
        }
    },
    RAWTEXT {
        override fun read(t: Tokenizer, r: CharacterReader) {
            readRawData(t, r, this, RAWTEXT_LESS_THAN_SIGN)
        }
    },
    TAG_OPEN {
        // 从 DATA 状态中的 < 进入。
        override fun read(t: Tokenizer, r: CharacterReader) {
            when (r.current()) {

                '!' -> t.advanceTransition(MARKUP_DECLARATION_OPEN)

                '/' -> t.advanceTransition(END_TAG_OPEN)

                else -> {
                    if (r.matchesAsciiAlpha()) {
                        t.createTagPending(true)
                        t.transition(TAG_NAME)
                    } else {
                        t.error(this)
//                      t.emit('<') // 触发当前状态的字符
//                      t.transition(DATA)
                    }
                }
            }
        }
    },
    END_TAG_OPEN {
        override fun read(t: Tokenizer, r: CharacterReader) {
            if (r.isEmpty()) {
                t.error(this)
//              t.emit("</")
//              t.transition(DATA)
            } else if (r.matchesAsciiAlpha()) {
                t.createTagPending(false)
                t.transition(TAG_NAME)
            } else if (r.matches('>')) {
                t.error(this)
//                t.advanceTransition(DATA)
            } else {
                t.error(this)
//              t.createTagPending(false)
//              t.transition(TAG_NAME)
            }
        }
    },
    TAG_NAME {
        // 从 DATA 状态中的 < 或 </ 进入，此时已有待处理的开始或结束标签。
        override fun read(t: Tokenizer, r: CharacterReader) {
            // 前一个 TAG_OPEN 状态没有消费字符，因此当前位置应为字母。
            val tagName = r.consumeTagName()
            t.tagPending.appendTagName(tagName)

            when (val c: Char? = r.consume()) {

                '\t', '\n', '\r', '\u000C', ' ' -> t.transition(BEFORE_ATTR_NAME)

                '/' -> t.transition(SELF_CLOSING_START_TAG)

                '<' -> {  // 注意：不属于规范行为，但输入意图明确。
                    r.unconsume()
                    t.error(this)
                    // 按遇到下一个 > 的情况继续处理。
                }

                '>' -> {
                    t.emitTagPending()
                    t.transition(DATA)
                }

                CharConst.NULL -> t.tagPending.appendTagName(REPLACEMENT_STR) // 使用替换字符。

                CharConst.EOF -> { // 是否应发射待处理标签？
                    t.eofError(this)
//                  t.transition(DATA)
                }

                else -> t.tagPending.appendTagName(c!!)
            }

        }
    },
    RCDATA_LESS_THAN_SIGN {
        // 从 RCDATA 状态中的 < 进入。
        override fun read(t: Tokenizer, r: CharacterReader) {
            if (r.matches('/')) {
                t.createTempBuffer()
                t.advanceTransition(RCDATA_END_TAG_OPEN)
            } else if (r.readFully && r.matchesAsciiAlpha() && t.appropriateEndTagName() != null && !r.containsIgnoreCase(
                    t.appropriateEndTagSeq()
                )
            ) {
                // 与规范不同：遇到了开始标签，但不存在对应的结束标签（如 </title>）。
                // 不继续消费到文件末尾，而是在此退出。
                t.tagPending = t.createTagPending(false).apply {
                    tagName = t.appropriateEndTagName()
                }
                t.emitTagPending()
                t.transition(TAG_OPEN) // 当前字符来自 < 且后续形似开始标签，因此直接进入 TAG_OPEN。
            } else {
                t.emit("<")
                t.transition(RCDATA)
            }
        }

    },
    RCDATA_END_TAG_OPEN {
        override fun read(t: Tokenizer, r: CharacterReader) {
            if (r.matchesAsciiAlpha()) {
                t.createTagPending(false)
                t.tagPending.appendTagName(r.current()!!)
                t.dataBuffer.append(r.current())
                t.advanceTransition(RCDATA_END_TAG_NAME)
            } else {
                t.emit("</")
                t.transition(RCDATA)
            }
        }

    },
    RCDATA_END_TAG_NAME {
        override fun read(t: Tokenizer, r: CharacterReader) {
            if (r.matchesAsciiAlpha()) {
                val name = r.consumeLetterSequence()
                t.tagPending.appendTagName(name)
                t.dataBuffer.append(name)
                return
            }


            when (r.consume()) {

                '\t', '\n', '\r', '\u000C', ' ' -> {
                    if (t.isAppropriateEndTagToken()) t.transition(BEFORE_ATTR_NAME)
                    else anythingElse(t, r)
                }

                '/' -> {
                    if (t.isAppropriateEndTagToken()) t.transition(SELF_CLOSING_START_TAG)
                    else anythingElse(t, r)
                }

                '>' -> {
                    if (t.isAppropriateEndTagToken()) {
                        t.emitTagPending()
                        t.transition(DATA)
                    } else anythingElse(t, r)
                }

                else -> anythingElse(t, r)
            }
        }

        private fun anythingElse(t: Tokenizer, r: CharacterReader) {
            t.emit("</")
            t.emit(t.dataBuffer)
            r.unconsume()
            t.transition(RCDATA)
        }
    },
    RAWTEXT_LESS_THAN_SIGN {
        override fun read(t: Tokenizer, r: CharacterReader) {
            if (r.matches('/')) {
                t.createTempBuffer()
                t.advanceTransition(RAWTEXT_END_TAG_OPEN)
            } else {
                t.emit('<')
                t.transition(RAWTEXT)
            }
        }
    },
    RAWTEXT_END_TAG_OPEN {
        override fun read(t: Tokenizer, r: CharacterReader) {
            readEndTag(t, r, RAWTEXT_END_TAG_NAME, RAWTEXT)
        }
    },
    RAWTEXT_END_TAG_NAME {
        override fun read(t: Tokenizer, r: CharacterReader) {
            handleDataEndTag(t, r, RAWTEXT)
        }
    },
    BEFORE_ATTR_NAME {
        override fun read(t: Tokenizer, r: CharacterReader) {
            when (r.consume()) {

                '\t', '\n', '\r', '\u000C', ' ' -> Unit // 忽略空白字符。

                '/' -> t.transition(SELF_CLOSING_START_TAG)

                '<' -> {
                    // 注意：不属于规范行为；规范会将其视为属性名的一部分。
                    r.unconsume()
                    t.error(this)
                    // 按遇到 > 的情况继续处理。
                }

                '>' -> {
                    t.emitTagPending()
                    t.transition(DATA)
                }

                CharConst.NULL -> {
                    r.unconsume()
                    t.error(this)
//                  t.tagPending.newAttribute()
//                  t.transition(ATTR_NAME)
                }

                CharConst.EOF -> {
                    t.eofError(this)
//                  t.transition(DATA)
                }

                '"', '\'', '=' -> {
                    t.error(this)
//                  t.tagPending.newAttribute()
//                  t.tagPending.appendAttributeName(c, r.pos() - 1, r.pos())
//                  t.transition(ATTR_NAME)
                }

                else -> {
                    // A-Z 或其他字符。
                    t.tagPending.newAttribute()
                    r.unconsume()
                    t.transition(ATTR_NAME)
                }
            }
        }
    },
    ATTR_NAME {
        override fun read(t: Tokenizer, r: CharacterReader) {
            var pos: Int = r.pos()
            val name =
                r.consumeToAnySorted(*CharConst.ATTR_NAME_CHARS_SORTED) // spec deviate - consume and emit nulls in one hit vs stepping
            t.tagPending.appendAttributeName(name, pos, r.pos())
            pos = r.pos()
            when (val c: Char? = r.consume()) {

                '\t', '\n', '\r', '\u000C', ' ' -> t.transition(AFTER_ATTR_NAME)

                '/' -> t.transition(SELF_CLOSING_START_TAG)

                '=' -> t.transition(BEFORE_ATTR_VALUE)

                '>' -> {
                    t.emitTagPending()
                    t.transition(DATA)
                }

                CharConst.EOF -> {
                    t.eofError(this)
//                  t.transition(DATA)
                }

                '"', '\'', '<' -> {
                    t.error(this)
//                  t.tagPending.appendAttributeName(c, pos, r.pos())
                }

                else -> t.tagPending.appendAttributeName(c!!, pos, r.pos()) // 缓冲区数据不足。
            }
        }
    },
    AFTER_ATTR_NAME {
        override fun read(t: Tokenizer, r: CharacterReader) {
            when (r.consume()) {

                '\t', '\n', '\r', '\u000C', ' ' -> Unit // 忽略空白字符。

                '/' -> t.transition(SELF_CLOSING_START_TAG)

                '=' -> t.transition(BEFORE_ATTR_VALUE)

                '>' -> {
                    t.emitTagPending()
                    t.transition(DATA)
                }

                CharConst.NULL -> {
                    t.error(this)
//                  t.tagPending.appendAttributeName(CharConst.NULL_REPLACEMENT_CHAR, r.pos() - 1, r.pos())
//                  t.transition(ATTR_NAME)
                }

                CharConst.EOF -> {
                    t.eofError(this)
//                  t.transition(DATA)
                }

                '"', '\'', '<' -> {
                    t.error(this)
//                  t.tagPending.newAttribute()
//                  t.tagPending.appendAttributeName(c, r.pos() - 1, r.pos())
//                  t.transition(ATTR_NAME)
                }

                // A-Z 或其他字符。
                else -> {
                    t.tagPending.newAttribute()
                    r.unconsume()
                    t.transition(ATTR_NAME)
                }
            }
        }
    },
    BEFORE_ATTR_VALUE {
        override fun read(t: Tokenizer, r: CharacterReader) {

            when (r.consume()) {

                '\t', '\n', '\r', '\u000C', ' ' -> Unit // 忽略空白字符。

                '"' -> t.transition(ATTR_VALUE_DOUBLE_QUOTED)

                '&' -> {
                    r.unconsume()
                    t.transition(ATTR_VALUE_UNQUOTED)
                }

                '\'' -> t.transition(ATTR_VALUE_SINGLE_QUOTED)

                CharConst.NULL -> {
                    t.error(this)
//                  t.tagPending.appendAttributeName(CharConst.NULL_REPLACEMENT_CHAR, r.pos() - 1, r.pos())
//                  t.transition(ATTR_NAME)
                }

                CharConst.EOF -> {
                    t.eofError(this)
//                  t.emitTagPending()
//                  t.transition(DATA)
                }

                '>' -> {
                    t.error(this)
//                   t.emitTagPending()
//                   t.transition(DATA)
                }

                '<', '=', '`' -> {
                    t.error(this)
//                  t.tagPending.appendAttributeValue(c, r.pos() - 1, r.pos())
//                  t.transition(ATTR_VALUE_UNQUOTED)
                }

                else -> {
                    r.unconsume()
                    t.transition(ATTR_VALUE_UNQUOTED)
                }
            }
        }
    },
    ATTR_VALUE_DOUBLE_QUOTED {
        override fun read(t: Tokenizer, r: CharacterReader) {
            var pos: Int = r.pos()
            val value: String = r.consumeAttributeQuoted(false)
            if (value.isNotEmpty()) {
                t.tagPending.appendAttributeValue(value, pos, r.pos())
            } else {
                t.tagPending.setEmptyAttributeValue()
            }

            pos = r.pos()
            when (val c: Char? = r.consume()) {

                '"' -> t.transition(AFTER_ATTR_VALUE_QUOTED)

                CharConst.NULL -> {
                    t.error(this)
//                  t.tagPending.appendAttributeValue(CharConst.NULL_REPLACEMENT_CHAR, pos, r.pos())
                }

                CharConst.EOF -> {
                    t.eofError(this)
//                  t.transition(DATA)
                }

                // 首次读取已到缓冲区末尾，但仍处于属性值中。
                else -> {
                    t.tagPending.appendAttributeValue(c!!, pos, r.pos())
                }
            }
        }
    },
    ATTR_VALUE_SINGLE_QUOTED {
        override fun read(t: Tokenizer, r: CharacterReader) {
            var pos: Int = r.pos()
            val value: String = r.consumeAttributeQuoted(true)
            if (value.isNotEmpty()) {
                t.tagPending.appendAttributeValue(value, pos, r.pos())
            } else {
                t.tagPending.setEmptyAttributeValue()
            }

            pos = r.pos()
            when (val c: Char? = r.consume()) {

                '\'' -> t.transition(AFTER_ATTR_VALUE_QUOTED)

                CharConst.NULL -> {
                    t.error(this)
//                  t.tagPending.appendAttributeValue(CharConst.NULL_REPLACEMENT_CHAR, pos, r.pos())
                }

                CharConst.EOF -> {
                    t.eofError(this)
//                  t.transition(DATA)
                }

                // 首次读取已到缓冲区末尾，但仍处于属性值中。
                else -> t.tagPending.appendAttributeValue(c!!, pos, r.pos())
            }
        }
    },
    ATTR_VALUE_UNQUOTED {
        override fun read(t: Tokenizer, r: CharacterReader) {
            var pos: Int = r.pos()
            val value: String = r.consumeToAnySorted(*CharConst.ATTR_VALUE_UNQUOTED_ARRAY)
            if (value.isNotEmpty()) {
                t.tagPending.appendAttributeValue(value, pos, r.pos())
            }

            pos = r.pos()
            when (val c: Char? = r.consume()) {

                '\t', '\n', '\r', '\u000C', ' ' -> t.transition(BEFORE_ATTR_NAME)

                '>' -> {
                    t.emitTagPending()
                    t.transition(DATA)
                }

                CharConst.NULL -> {
                    t.error(this)
//                  t.tagPending.appendAttributeValue(CharConst.NULL_REPLACEMENT_CHAR, pos, r.pos())
                }

                CharConst.EOF -> {
                    t.eofError(this)
//                  t.transition(DATA)
                }

                '"', '\'', '<', '=', '`' -> {
                    t.error(this)
//                  t.tagPending.appendAttributeValue(c, pos, r.pos())
                }

                // 首次读取已到缓冲区末尾，但仍处于属性值中。
                else -> t.tagPending.appendAttributeValue(c!!, pos, r.pos())
            }
        }
    },
    AFTER_ATTR_VALUE_QUOTED {
        override fun read(t: Tokenizer, r: CharacterReader) {
            when (r.consume()) {

                '\t', '\n', '\r', '\u000C', ' ' -> t.transition(BEFORE_ATTR_NAME)

                '/' -> t.transition(SELF_CLOSING_START_TAG)

                '>' -> {
                    t.emitTagPending()
                    t.transition(DATA)
                }

                CharConst.EOF -> {
                    t.eofError(this)
//                  t.transition(DATA)
                }

                else -> {
                    r.unconsume()
                    t.error(this)
//                  t.transition(BEFORE_ATTR_NAME)
                }
            }
        }
    },
    SELF_CLOSING_START_TAG {
        override fun read(t: Tokenizer, r: CharacterReader) {
            when (r.consume()) {

                '>' -> {
                    t.tagPending.selfClosing = true
                    t.emitTagPending()
                    t.transition(DATA)
                }

                CharConst.EOF -> {
                    t.eofError(this)
//                  t.transition(DATA)
                }

                else -> {
                    r.unconsume()
                    t.error(this)
//                  t.transition(BEFORE_ATTR_NAME)
                }
            }
        }
    },
    MARKUP_DECLARATION_OPEN {
        override fun read(t: Tokenizer, r: CharacterReader) {
            if (r.matchConsume("[CDATA[")) {
                t.createTempBuffer()
                t.transition(CDATA_SECTION)
            } else if (r.matchConsume("--")) {
                t.transition(COMMENT)
            } else {
                t.error(this)
//              t.createTagPending(true)
//              t.transition(TAG_NAME)
            }
        }
    },
    COMMENT {
        override fun read(t: Tokenizer, r: CharacterReader) {
            r.consumeTo("-->")
            if (r.matchConsume("-->")) {
                t.transition(DATA)
            } else if (r.isEmpty()) {
                t.eofError(this)
            }
        }
    },
    CDATA_SECTION {
        override fun read(t: Tokenizer, r: CharacterReader) {
            val data: String = r.consumeTo("]]>")
            t.dataBuffer.append(data)
            if (r.matchConsume("]]>") || r.isEmpty()) {
                t.emit(Token.CDATA(t.dataBuffer.toString()))
                t.transition(DATA)
            } // 其他情况表示缓冲区数据不足，保持当前数据状态。
        }
    }
    ;

    abstract fun read(t: Tokenizer, r: CharacterReader)

    companion object {
        const val REPLACEMENT_STR: String = CharConst.NULL_REPLACEMENT_CHAR.toString()

        /**
         * 统一处理 RawtextEndTagName、ScriptDataEndTagName 和 ScriptDataEscapedEndTagName。
         * 三者主体逻辑相同，仅未匹配时退出到的状态不同。
         */
        private fun handleDataEndTag(t: Tokenizer, r: CharacterReader, elseTransition: TokenizerState) {
            if (r.matchesLetter()) {
                val name = r.consumeLetterSequence()
                t.tagPending.appendTagName(name)
                t.dataBuffer.append(name)
                return
            }
            var needsExitTransition = false
            if (t.isAppropriateEndTagToken() && !r.isEmpty()) {
                when (val c: Char? = r.consume()) {
                    '\t', '\n', '\r', '\u000C', ' ' -> t.transition(BEFORE_ATTR_NAME)
                    '/' -> t.transition(SELF_CLOSING_START_TAG)
                    '>' -> {
                        t.emitTagPending()
                        t.transition(DATA)
                    }

                    else -> {
                        t.dataBuffer.append(c!!)
                        needsExitTransition = true
                    }
                }
            } else {
                needsExitTransition = true
            }

            if (needsExitTransition) {
                t.emit("</")
                t.emit(t.dataBuffer)
                t.transition(elseTransition)
            }
        }

        private fun readRawData(t: Tokenizer, r: CharacterReader, current: TokenizerState, advance: TokenizerState) {
            when (r.current()) {
                '<' -> t.advanceTransition(advance)
                CharConst.NULL -> {
                    t.error(current)
//                  r.advance()
//                  t.emit(CharConst.NULL_REPLACEMENT_CHAR)
                }

                CharConst.EOF -> t.emit(Token.EOF())
                else -> t.emit(r.consumeRawData())
            }
        }

        private fun readEndTag(t: Tokenizer, r: CharacterReader, a: TokenizerState, b: TokenizerState) {
            if (r.matchesAsciiAlpha()) {
                t.createTagPending(false)
                t.transition(a)
            } else {
                t.emit("</")
                t.transition(b)
            }
        }
    }
}
