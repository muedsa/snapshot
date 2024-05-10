package com.muedsa.snapshot.parser.token

import com.muedsa.snapshot.parser.CharConst
import com.muedsa.snapshot.parser.CharacterReader
import com.muedsa.snapshot.parser.TrackPos

abstract class Token private constructor(
    val type: TokenType,
) {
    val startPos: TrackPos = TrackPos()
    val endPos: TrackPos = TrackPos()

    open fun reset(): Token {
        startPos.reset()
        endPos.reset()
        return this
    }

    open fun toStringWithPos(): String = "[$startPos] ${toString()} [$endPos]"

    abstract override fun toString(): String

    companion object {
        fun reset(sb: StringBuilder?) {
            sb?.clear()
        }
    }

    abstract class Tag(type: TokenType, val reader: CharacterReader) : Token(type = type) {
        var tagName: String? = null
            internal set
        var selfClosing: Boolean = false
            internal set
        protected val _attrList: MutableList<RawAttr> = mutableListOf()
        val attrList: List<RawAttr> = _attrList
        val hasAttrs: Boolean get() = attrList.isNotEmpty()


        // 正在解析中的
        private var attrName: String? = null
        private val attrNameSb = StringBuilder()
        private var hasAttrName = false

        private var attrValue: String? = null
        private val attrValueSb = java.lang.StringBuilder()
        private var hasAttrValue = false
        private var hasEmptyAttrValue = false

        private val attrNameStartPos: TrackPos = TrackPos()
        private val attrNameEndPos: TrackPos = TrackPos()
        private val attrValueStartPos: TrackPos = TrackPos()
        private val attrValueEndPos: TrackPos = TrackPos()


        override fun reset(): Tag {
            super.reset()
            tagName = null
            selfClosing = false
            _attrList.clear()
            resetPendingAttr()
            return this
        }

        private fun resetPendingAttr() {
            reset(attrNameSb)
            attrName = null
            hasAttrName = false

            reset(attrValueSb)
            attrValue = null
            hasEmptyAttrValue = false
            hasAttrValue = false

            attrNameStartPos.reset()
            attrNameEndPos.reset()
            attrValueStartPos.reset()
            attrValueEndPos.reset()
        }

        fun newAttribute() {
            if (hasAttrName && attrList.size < MAX_ATTRIBUTES) {
                // the tokeniser has skipped whitespace control chars, but trimming could collapse to empty for other control codes, so verify here
                var name = if (attrNameSb.isNotEmpty()) attrNameSb.toString() else attrName!!
                name = name.trim()
                if (name.isNotEmpty()) {
                    val value = if (hasAttrValue)
                        if (attrValueSb.isNotEmpty()) attrValueSb.toString() else attrValue
                    else
                        if (hasEmptyAttrValue) "" else null
                    // note that we add, not put. So that the first is kept, and rest are deduped, once in a context where case sensitivity is known, and we can warn for duplicates.
                    _attrList.add(
                        RawAttr(
                            name = name,
                            value = value,
                            nameStartPos = attrNameStartPos.copy(),
                            nameEndPos = attrNameEndPos.copy(),
                            valueStartPos = if (hasAttrValue) attrValueStartPos.copy() else TrackPos(),
                            valueEndPos = if (hasAttrValue) attrValueEndPos.copy() else TrackPos(),
                        )
                    )
                }
            }
            resetPendingAttr()
        }

        fun hasAttr(key: String): Boolean = attrList.any { t -> t.name == key }

        fun hasAttrIgnoreCase(key: String): Boolean = attrList.any { t -> t.name.equals(key, ignoreCase = true) }

        fun finaliseTag() {
            // finalises for emit
            if (hasAttrName) {
                newAttribute()
            }
        }

        fun appendTagName(append: String) {
            // might have null chars - need to replace with null replacement character
            val appendReplaced = append.replace(CharConst.NULL, Tokenizer.REPLACEMENT_CHAR)
            tagName = if (tagName == null) appendReplaced else tagName + appendReplaced
        }

        fun appendTagName(append: Char) {
            appendTagName(append.toString())
        }

        fun appendAttributeName(append: String, startPos: Int, endPos: Int) {
            // might have null chars because we eat in one pass - need to replace with null replacement character
            val appendReplaced = append.replace(CharConst.NULL, Tokenizer.REPLACEMENT_CHAR)
            ensureAttrName(startPos, endPos)
            if (attrNameSb.isEmpty()) {
                attrName = appendReplaced
            } else {
                attrNameSb.append(appendReplaced)
            }
        }

        fun appendAttributeName(append: Char, startPos: Int, endPos: Int) {
            ensureAttrName(startPos, endPos)
            attrNameSb.append(append)
        }

        fun appendAttributeValue(append: String, startPos: Int, endPos: Int) {
            ensureAttrValue(startPos, endPos)
            if (attrValueSb.isEmpty()) {
                attrValue = append
            } else {
                attrValueSb.append(append)
            }
        }

        fun appendAttributeValue(append: Char, startPos: Int, endPos: Int) {
            ensureAttrValue(startPos, endPos)
            attrValueSb.append(append)
        }

        fun appendAttributeValue(appendCodepoints: IntArray, startPos: Int, endPos: Int) {
            ensureAttrValue(startPos, endPos)
            for (codepoint in appendCodepoints) {
                attrValueSb.appendCodePoint(codepoint)
            }
        }

        fun setEmptyAttributeValue() {
            hasEmptyAttrValue = true
        }

        private fun ensureAttrName(startPos: Int, endPos: Int) {
            hasAttrName = true
            // if on second hit, we'll need to move to the builder
            if (attrName != null) {
                attrNameSb.append(attrName)
                attrName = null
            }
            attrNameStartPos.updateWhenUnset(startPos, reader.getLineNumber(startPos), reader.getColumnNumber(startPos))
            attrNameEndPos.update(endPos, reader.getLineNumber(endPos), reader.getColumnNumber(endPos))
        }

        private fun ensureAttrValue(startPos: Int, endPos: Int) {
            hasAttrValue = true
            // if on second hit, we'll need to move to the builder
            if (attrValue != null) {
                attrValueSb.append(attrValue)
                attrValue = null
            }

            attrValueStartPos.updateWhenUnset(
                startPos,
                reader.getLineNumber(startPos),
                reader.getColumnNumber(startPos)
            )
            attrValueEndPos.update(endPos, reader.getLineNumber(endPos), reader.getColumnNumber(endPos))
        }

        companion object {
            const val MAX_ATTRIBUTES: Int = 512
        }
    }

    class StartTag(reader: CharacterReader) : Tag(type = TokenType.START_TAG, reader = reader) {
        override fun reset(): Tag {
            super.reset()
            _attrList.clear()
            return this
        }

        override fun toString(): String {
            val attrString: String = if (hasAttrs) " $attrList" else ""
            return if (selfClosing) {
                "<${tagName ?: "unset"}$attrString/>"
            } else {
                "<${tagName ?: "unset"}$attrString>"
            }
        }
    }

    class EndTag(reader: CharacterReader) : Tag(type = TokenType.END_TAG, reader = reader) {
        override fun toString(): String = "</${tagName ?: "unset"}>"
    }

    open class Character(var data: String? = null) : Token(type = TokenType.CHARACTER), Cloneable {

        override fun reset(): Token {
            super.reset()
            data = null
            return this
        }

        override fun toString(): String {
            return data ?: "unset"
        }

        fun data(data: String): Character {
            this.data = data
            return this
        }
    }

    class CDATA(data: String) : Character(data = data) {
        override fun toString(): String = "<![CDATA[$data]]>"
    }

    class EOF : Token(type = TokenType.EOF) {
        override fun toString(): String = ""
    }

}