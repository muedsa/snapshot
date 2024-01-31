package com.muedsa.snapshot.parser.token

import com.muedsa.snapshot.parser.CharacterReader
import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.Tag
import com.muedsa.snapshot.parser.TrackPos
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
    internal var tagPending: Token.Tag = startPending // tag we are building up: start or end pending
    private val charPending: Token.Character = Token.Character()
    private var lastStartTag: String? = null
    private var lastStartCloseSeq: String? = null

    private var markupStartPos: Int = TrackPos.UNSET
    private var charStartPos: Int = TrackPos.UNSET

    fun read(): Token {
        while (!isEmitPending) {
            state.read(this, reader)
        }

        // if emit is pending, a non-character token was found: return any chars in buffer, and leave token for next read:
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
            lastStartCloseSeq = null // only lazy inits
        } else if (token is Token.EndTag) {
            if (token.hasAttrs) error("Attributes incorrectly present on end tag [/${token.tagName}]")
        }
    }

    internal fun emit(str: String) {
        // buffer strings up until last string token found, to emit only one token for a run of character refs etc.
        // does not set isEmitPending; read checks that
        if (charsString == null) {
            charsString = str
        } else {
            if (charsBuilder.isEmpty()) { // switching to string builder as more than one emit before read
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

    // variations to limit need to create temp strings
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
        // track markup / data position on state transitions
        when (newState) {
            TokenizerState.TAG_OPEN -> markupStartPos = reader.pos()
            TokenizerState.DATA -> {
                if (charStartPos == TrackPos.UNSET) // don't reset when we are jumping between e.g data -> char ref -> data
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
            check(Tag.SNAPSHOT.id == tagPending.tagName) {
                "First tag must be '${Tag.SNAPSHOT.id}', but get '${tagPending.tagName}'"
            }
            shouldAsFirstTagChecked = false
        } else {
            if (tagPending is Token.StartTag) {
                check(Tag.SNAPSHOT.id != tagPending.tagName) {
                    "Tag '${Tag.SNAPSHOT.id}' only be used as the first"
                }
            }
        }
        emit(tagPending)
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
     * Returns the closer sequence `</lastStart`
     */
    internal fun appropriateEndTagSeq(): String {
        if (lastStartCloseSeq == null) // reset on start tag emit
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

    companion object {
        const val REPLACEMENT_CHAR: Char = '\uDFFF'
    }
}