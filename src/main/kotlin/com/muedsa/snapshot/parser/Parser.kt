package com.muedsa.snapshot.parser

import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.parser.token.RawAttr
import com.muedsa.snapshot.parser.token.Token
import com.muedsa.snapshot.parser.token.Tokenizer
import java.io.Reader

class Parser {

    protected lateinit var reader: Reader
    protected lateinit var tokenizer: Tokenizer

    protected val stack: MutableList<Element> = mutableListOf()
    protected var snapshotElement: SnapshotElement? = null

    @Synchronized
    fun parse(reader: Reader): SnapshotElement {
        init(reader)

        do {
            val token = tokenizer.read()
            try {
                process(token)
            } catch (pex: ParseException) {
                throw pex
            } catch (t: Throwable) {
                throw ParseException(
                    token.startPos,
                    t.message ?: "Unexpected token [$token]"
                )
            }
            token.reset()
        } while (token !is Token.EOF)

        while (stack.isNotEmpty()) pop()

        return snapshotElement!!
    }

    protected fun process(token: Token) {
        when (token) {
            is Token.StartTag -> insertElementFor(token)
            is Token.EndTag -> popStackToClose(token)
            is Token.Character -> insertCharacterFor(token)
            is Token.EOF -> Unit
            else -> throw ParseException(
                token.startPos,
                "Unexpected token [${token.type}]"
            )
        }
    }

    protected fun insertElementFor(token: Token.StartTag) {
        val tag: Tag = parseTag(token)

        val element = if (tag == Tag.SNAPSHOT) {
            SnapshotElement(
                attrs = buildTokenTagAttrMap(token),
                pos = token.startPos.copy()
            )
        } else {
            Element(
                tag = tag,
                attrs = buildTokenTagAttrMap(token),
                pos = token.startPos.copy()
            )
        }
        getCurrentElement()?.appendChild(element)
        push(element)

        if (token.selfClosing) {
            pop() // push & pop ensures onNodeInserted & onNodeClosed
        }
    }

    protected fun popStackToClose(token: Token.EndTag) {
        var firstFound: Element? = null

        val bottom = stack.size - 1
        val upper = if (bottom >= MAX_QUEUE_DEPTH) bottom - MAX_QUEUE_DEPTH else 0
        for (pos: Int in bottom downTo upper) {
            val next: Element = stack[pos]
            if (next.tag.id == token.tagName) {
                firstFound = next
                break
            }
        }

        if (firstFound == null)
            return // not found, skip

        for (pos: Int in stack.indices.reversed()) {
            val next: Element = pop()
            if (next == firstFound) {
                break
            }
        }
    }

    protected fun insertCharacterFor(token: Token.Character) {
        // println("insert character:${token.data}")
        val currentElement = getCurrentElement()
        if (currentElement?.tag == Tag.TEXT) {
            if (token.data != null) {
                val rawAttr = currentElement.attrs[CommonAttrDefine.TEXT.name]
                currentElement.attrs[CommonAttrDefine.TEXT.name] = rawAttr?.copy(
                    value = (rawAttr.value ?: "") + token.data
                ) ?: RawAttr(
                    name = CommonAttrDefine.TEXT.name,
                    value = token.data,
                    nameStartPos = token.startPos,
                    nameEndPos = token.startPos,
                    valueStartPos = token.startPos,
                    valueEndPos = token.endPos
                )
            }
        } else {
            if (!token.data.isNullOrBlank()) {
                throw ParseException(token.startPos, "Not Support RAWTEXT: ${token.data}")
            }
        }
    }


    private fun getCurrentElement(): Element? = stack.lastOrNull()

    private fun push(element: Element) {
        if (snapshotElement == null) {
            check(element is SnapshotElement) {
                "Unexpected element [${element.tag.id}], root element tag must be ${Tag.SNAPSHOT.id}"
            }
            snapshotElement = element
        } else if (stack.isEmpty()) {
            throw ParseException(element.pos, "Duplicate root element [${element.tag.id}] at ${element.pos}")
        }
        element.owner = snapshotElement
        stack.add(element)
    }

    private fun pop(): Element {
        return stack.removeLast()
    }

    private fun init(reader: Reader) {
        this.reader = reader
        tokenizer = Tokenizer(this.reader)
        stack.clear()
    }

    companion object {

        const val MAX_QUEUE_DEPTH: Int = 256

        fun parseTag(token: Token.StartTag): Tag {
            if (token.tagName == null) {
                throw ParseException(
                    token.startPos,
                    "element tag name can not be null"
                )
            }
            return Tag.TAG_MAP[token.tagName] ?: throw ParseException(
                token.startPos,
                "Unknown element tag [${token.tagName}]"
            )
        }

        private fun buildTokenTagAttrMap(token: Token.StartTag): MutableMap<String, RawAttr> {
            val attrList = token.attrList
            val map = LinkedHashMap<String, RawAttr>(attrList.size)
            attrList.forEach {
                if (map.containsKey(it.name)) {
                    throw ParseException(
                        it.nameStartPos,
                        "Element tag [${token.tagName}] exist duplicate attr"
                    )
                }
                map[it.name] = it
            }
            return map
        }
    }
}