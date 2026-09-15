package com.muedsa.snapshot.parser.token

import com.muedsa.snapshot.parser.CharacterReader
import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.TrackPos
import com.muedsa.snapshot.parser.widget.SnapshotParser
import java.io.Reader

class Tokenizer(reader: Reader) {
    private val reader: CharacterReader = CharacterReader(reader)

    private var state: TokenizerState = TokenizerState.DATA

    private var emitPending: Token? = null
    private var isEmitPending: Boolean = false
    private var charsString: String? = null
    private val charsBuilder: StringBuilder = StringBuilder(1024)
    internal val dataBuffer: StringBuilder = StringBuilder(1024)

    private var shouldAsFirstTagChecked: Boolean = true

    private val startPending: Token.StartTag = Token.StartTag(this.reader)
    private val endPending: Token.EndTag = Token.EndTag(this.reader)
    internal var tagPending: Token.Tag = startPending // 正在构建的开始或结束标签
    private val charPending: Token.Character = Token.Character()
    private var lastStartTag: String? = null
    private var lastStartCloseSeq: String? = null

    private var markupStartPos: Int = TrackPos.UNSET
    private var charStartPos: Int = TrackPos.UNSET

    fun read(): Token {
        while (!isEmitPending) {
            state.read(this, reader)
        }

        // 若已有待发射的非字符令牌，先返回缓冲区中的字符，并将该令牌留到下次读取。
        val cb = this.charsBuilder
        if (cb.isNotEmpty()) {
            val str = cb.toString()
            cb.delete(0, cb.length)
            val token: Token = charPending.data(str)
            charsString = null
            return token
        } else if (charsString != null) {
            val token: Token = charPending.data(charsString!!)
            charsString = null
            return token
        } else {
            isEmitPending = false
            assert(emitPending != null)
            return emitPending!!
        }
    }

    internal fun emit(token: Token) {
        assert(!isEmitPending)

        emitPending = token
        isEmitPending = true
        token.startPos.update(
            markupStartPos,
            reader.getLineNumber(markupStartPos),
            reader.getColumnNumber(markupStartPos)
        )
        val endPos = reader.pos()
        token.endPos.update(
            endPos,
            reader.getLineNumber(endPos),
            reader.getColumnNumber(endPos)
        )
        charStartPos = TrackPos.UNSET

        if (token is Token.StartTag) {
            lastStartTag = token.tagName
            lastStartCloseSeq = null // 仅在需要时延迟初始化
        } else if (token is Token.EndTag) {
            if (token.hasAttrs) error("Attributes incorrectly present on end tag [/${token.tagName}]")
        }
    }

    internal fun emit(str: String) {
        // 将连续字符串缓冲起来，使一串字符引用等内容只发射一个令牌。
        // 此处不设置 isEmitPending，由 read 负责检查。
        if (charsString == null) {
            charsString = str
        } else {
            if (charsBuilder.isEmpty()) { // 一次读取前多次发射时改用 StringBuilder
                charsBuilder.append(charsString)
            }
            charsBuilder.append(str)
        }
        charPending.startPos.update(
            charStartPos,
            reader.getLineNumber(charStartPos),
            reader.getColumnNumber(charStartPos)
        )
        val endPos = reader.pos()
        charPending.endPos.update(
            endPos,
            reader.getLineNumber(endPos),
            reader.getColumnNumber(endPos)
        )
    }

    // 以下重载用于减少临时字符串的创建。
    internal fun emit(str: StringBuilder) {
        if (charsString == null) {
            charsString = str.toString()
        } else {
            if (charsBuilder.isEmpty()) {
                charsBuilder.append(charsString)
            }
            charsBuilder.append(str)
        }
        charPending.startPos.update(
            charStartPos,
            reader.getLineNumber(charStartPos),
            reader.getColumnNumber(charStartPos)
        )
        val endPos = reader.pos()
        charPending.endPos.update(
            endPos,
            reader.getLineNumber(endPos),
            reader.getColumnNumber(endPos)
        )
    }

    internal fun emit(c: Char) {
        if (charsString == null) {
            charsString = c.toString()
        } else {
            if (charsBuilder.isEmpty()) {
                charsBuilder.append(charsString)
            }
            charsBuilder.append(c)
        }
        charPending.startPos.update(
            charStartPos,
            reader.getLineNumber(charStartPos),
            reader.getColumnNumber(charStartPos)
        )
        val endPos = reader.pos()
        charPending.endPos.update(
            endPos,
            reader.getLineNumber(endPos),
            reader.getColumnNumber(endPos)
        )
    }

//    internal fun emit(chars: CharArray) {
//        emit(String(chars))
//    }
//
//    internal fun emit(codepoints: IntArray) {
//        emit(String(codepoints, 0, codepoints.size))
//    }

    internal fun transition(newState: TokenizerState) {
        // 在状态切换时记录标记与文本数据的位置。
        when (newState) {
            TokenizerState.TAG_OPEN -> markupStartPos = reader.pos()
            TokenizerState.DATA -> {
                if (charStartPos == TrackPos.UNSET) // 在 DATA 与字符引用等状态之间往返时不重置
                    charStartPos = reader.pos()
            }

            else -> Unit
        }
        this.state = newState
    }

    internal fun advanceTransition(newState: TokenizerState) {
        transition(newState)
        reader.advance()
    }

    internal fun createTagPending(start: Boolean): Token.Tag {
        tagPending = if (start) startPending.reset() else endPending.reset()
        return tagPending
    }

    internal fun emitTagPending() {
        tagPending.finaliseTag()
        if (shouldAsFirstTagChecked) {
            check(SnapshotParser.id == tagPending.tagName, lazyTrackPos = { getMarkupStartTrackPos() }) {
                "First tag must be '${SnapshotParser.id}', but get '${tagPending.tagName}'"
            }
            shouldAsFirstTagChecked = false
        } else {
            if (tagPending is Token.StartTag) {
                check(SnapshotParser.id != tagPending.tagName, lazyTrackPos = { getMarkupStartTrackPos() }) {
                    "Tag '${SnapshotParser.id}' only be used as the first, but get '${tagPending.tagName}' at ${getMarkupStartTrackPos()}"
                }
            }
        }
        emit(tagPending)
    }

    private fun getMarkupStartTrackPos(): TrackPos {
        return TrackPos(
            markupStartPos,
            reader.getLineNumber(markupStartPos),
            reader.getColumnNumber(markupStartPos)
        )
    }

    internal fun createTempBuffer() {
        Token.reset(dataBuffer)
    }

    internal fun isAppropriateEndTagToken(): Boolean {
        return lastStartTag != null && tagPending.tagName.equals(lastStartTag, ignoreCase = true)
    }

    internal fun appropriateEndTagName(): String? {
        return lastStartTag
    }

    /**
     * 返回与最近开始标签对应的闭合序列 `</lastStart`。
     */
    internal fun appropriateEndTagSeq(): String {
        if (lastStartCloseSeq == null) // 发射开始标签时会重置
            lastStartCloseSeq = "</$lastStartTag"
        return lastStartCloseSeq!!
    }

    internal fun error(message: String) {
        throw ParseException(reader, message)
    }

    internal fun error(state: TokenizerState) {
        throw ParseException(reader, "Unexpected character '${reader.current()}' in input state [$state]")
    }

    internal fun eofError(state: TokenizerState) {
        throw ParseException(reader, "Unexpectedly reached end of file (EOF) in input state [$state]")
    }

    internal fun check(value: Boolean, lazyTrackPos: () -> TrackPos, lazyMessage: () -> Any) {
        if (!value) {
            val message = lazyMessage()
            throw ParseException(lazyTrackPos(), message.toString())
        }
    }

    internal fun check(value: Boolean, lazyMessage: () -> Any) {
        if (!value) {
            val message = lazyMessage()
            throw ParseException(reader, message.toString())
        }
    }

    companion object {
        const val REPLACEMENT_CHAR: Char = '\uDFFF'
    }
}
