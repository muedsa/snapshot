package com.muedsa.snapshot.parser

import java.io.IOException
import java.io.Reader
import java.io.UncheckedIOException
import java.util.*
import kotlin.math.abs
import kotlin.math.min

class CharacterReader @JvmOverloads constructor(private val reader: Reader, bufferSize: Int = MAX_BUFFER_LEN) {

    private val charBuf: CharArray = CharArray(min(bufferSize, MAX_BUFFER_LEN))
    private var bufLength: Int = 0
    private var bufSplitPoint: Int = 0
    private var bufPos: Int = 0
    private var readerPos: Int = 0
    private var bufMark: Int = -1
    private val stringCache: Array<String?> = arrayOfNulls(STRING_CACHE_SIZE)

    fun pos(): Int = readerPos + bufPos
    var readFully: Boolean = false
        private set


    private fun bufferUp() {
        if (readFully || bufPos < bufSplitPoint) return

        val pos: Int
        val offset: Int
        if (bufMark != -1) {
            pos = bufMark
            offset = bufPos - bufMark
        } else {
            pos = bufPos
            offset = 0
        }

        val skipped = reader.skip(pos.toLong())
        reader.mark(MAX_BUFFER_LEN)
        var read = 0
        while (read <= MIN_READ_AHEAD_LEN) {
            val thisRead = reader.read(charBuf, read, charBuf.size - read)
            if (thisRead == -1) readFully = true
            if (thisRead <= 0) break
            read += thisRead
        }
        reader.reset()
        if (read > 0) {
            assert(skipped == pos.toLong()) // Previously asserted that there is room in buf to skip, so this will be a WTF
            bufLength = read
            readerPos += pos
            bufPos = offset
            if (bufMark != -1) bufMark = 0
            bufSplitPoint = min(bufLength, READ_AHEAD_LIMIT)
        }
        scanBufferForNewlines() // if enabled, we index newline positions for line number tracking
        lastIcSeq = null // cache for last containsIgnoreCase(seq)
    }


    private var newlinePositions: MutableList<Int> = ArrayList(MAX_BUFFER_LEN / 80)
    private var lineNumberOffset: Int = 1

    fun currentLineNumber(): Int = getLineNumber(pos())

    fun getLineNumber(pos: Int): Int {
        val i: Int = getLineNumberIndex(pos)
        if (i == -1) return lineNumberOffset // first line
        return i + lineNumberOffset + 1
    }

    fun currentColumnNumber(): Int = getColumnNumber(pos())

    fun getColumnNumber(pos: Int): Int {
        val i: Int = getLineNumberIndex(pos)
        if (i == -1) return pos + 1
        return pos - newlinePositions[i] + 1
    }

    private fun getLineNumberIndex(pos: Int): Int {
        var i = Collections.binarySearch(newlinePositions, pos)
        if (i < -1) i = abs(i) - 2
        return i
    }

    private fun scanBufferForNewlines() {
        if (newlinePositions.size > 0) {
            // work out the line number that we have read up to (as we have likely scanned past this point)
            var index: Int = getLineNumberIndex(readerPos)
            if (index == -1) index = 0 // first line
            val linePos = newlinePositions[index]
            lineNumberOffset += index // the num lines we've read up to
            newlinePositions.clear()
            newlinePositions.add(linePos) // roll the last read pos to first, for cursor num after buffer
        }

        for (i in bufPos until bufLength) {
            if (charBuf[i] == '\n') newlinePositions.add(1 + readerPos + i)
        }
    }

    fun isEmpty(): Boolean {
        bufferUp()
        return bufPos >= bufLength
    }

    private fun isEmptyNoBufferUp(): Boolean {
        return bufPos >= bufLength
    }

    fun current(): Char? {
        bufferUp()
        return if (isEmptyNoBufferUp()) CharConst.EOF else charBuf[bufPos]
    }

    fun consume(): Char? {
        bufferUp()
        val current = if (isEmptyNoBufferUp()) CharConst.EOF else charBuf[bufPos]
        bufPos++
        return current
    }

    /**
     * Unconsume one character (bufPos--). MUST only be called directly after a consume(), and no chance of a bufferUp.
     */
    fun unconsume() {
        if (bufPos < 1) throw UncheckedIOException(IOException("WTF: No buffer left to unconsume.")) // a bug if this fires, need to trace it.
        bufPos--
    }

    /**
     * Moves the current position by one.
     */
    fun advance() {
        bufPos++
    }

    fun mark() {
        // make sure there is enough look ahead capacity
        if (bufLength - bufPos < MIN_READ_AHEAD_LEN) bufSplitPoint = 0

        bufferUp()
        bufMark = bufPos
    }

    fun unmark() {
        bufMark = -1
    }

    fun rewindToMark() {
        if (bufMark == -1) throw UncheckedIOException(IOException("Mark invalid"))

        bufPos = bufMark
        unmark()
    }

    /**
     * Returns the number of characters between the current position and the next instance of the input char
     * @param c scan target
     * @return offset between current position and next instance of target. -1 if not found.
     */
    fun nextIndexOf(c: Char): Int {
        // doesn't handle scanning for surrogates
        bufferUp()
        for (i in bufPos until bufLength) {
            if (c == charBuf[i]) return i - bufPos
        }
        return -1
    }

    /**
     * Returns the number of characters between the current position and the next instance of the input sequence
     *
     * @param seq scan target
     * @return offset between current position and next instance of target. -1 if not found.
     */
    fun nextIndexOf(seq: CharSequence): Int {
        bufferUp()
        // doesn't handle scanning for surrogates
        val startChar = seq[0]
        var offset = bufPos
        while (offset < bufLength) {
            // scan to first instance of startchar:
            if (startChar != charBuf[offset]) while (++offset < bufLength && startChar != charBuf[offset]) { /* empty */
            }
            var i = offset + 1
            val last = i + seq.length - 1
            if (offset < bufLength && last <= bufLength) {
                var j = 1
                while (i < last && seq[j] == charBuf[i]) {
                    i++
                    j++
                }
                if (i == last) // found full sequence
                    return offset - bufPos
            }
            offset++
        }
        return -1
    }

    /**
     * Reads characters up to the specific char.
     * @param c the delimiter
     * @return the chars read
     */
    fun consumeTo(c: Char): String {
        val offset = nextIndexOf(c)
        if (offset != -1) {
            val consumed: String = cacheString(charBuf, stringCache, bufPos, offset)
            bufPos += offset
            return consumed
        } else {
            return consumeToEnd()
        }
    }

    fun consumeTo(seq: String): String {
        val offset = nextIndexOf(seq)
        if (offset != -1) {
            val consumed: String = cacheString(charBuf, stringCache, bufPos, offset)
            bufPos += offset
            return consumed
        } else if (bufLength - bufPos < seq.length) {
            // nextIndexOf() did a bufferUp(), so if the buffer is shorter than the search string, we must be at EOF
            return consumeToEnd()
        } else {
            // the string we're looking for may be straddling a buffer boundary, so keep (length - 1) characters
            // unread in case they contain the beginning of the search string
            val endPos = bufLength - seq.length + 1
            val consumed: String = cacheString(charBuf, stringCache, bufPos, endPos - bufPos)
            bufPos = endPos
            return consumed
        }
    }

    /**
     * Read characters until the first of any delimiters is found.
     * @param chars delimiters to scan for
     * @return characters read up to the matched delimiter.
     */
    fun consumeToAny(vararg chars: Char): String {
        bufferUp()
        var pos = bufPos
        val start = pos
        val remaining = bufLength
        val tempBuf = charBuf
        val charLen = chars.size
        var i: Int

        OUTER@ while (pos < remaining) {
            i = 0
            while (i < charLen) {
                if (tempBuf[pos] == chars[i]) break@OUTER
                i++
            }
            pos++
        }

        bufPos = pos
        return if (pos > start) cacheString(
            charBuf,
            stringCache,
            start,
            pos - start
        ) else ""
    }

    fun consumeToAnySorted(vararg chars: Char): String {
        bufferUp()
        var pos = bufPos
        val start = pos
        val remaining = bufLength
        val tempBuf = charBuf

        while (pos < remaining) {
            if (Arrays.binarySearch(chars, tempBuf[pos]) >= 0) break
            pos++
        }
        bufPos = pos
        return if (bufPos > start) cacheString(
            charBuf,
            stringCache,
            start,
            pos - start
        ) else ""
    }

    fun consumeData(): String {
        // &, <, null
        //bufferUp(); // no need to bufferUp, just called consume()
        var pos = bufPos
        val start = pos
        val remaining = bufLength
        val tempBuf = charBuf

        OUTER@ while (pos < remaining) {
            when (tempBuf[pos]) {
                '&', '<', Char.MIN_VALUE -> break@OUTER
                else -> pos++
            }
        }
        bufPos = pos
        return if (pos > start) cacheString(
            charBuf,
            stringCache,
            start,
            pos - start
        ) else ""
    }

    fun consumeAttributeQuoted(single: Boolean): String {
        // null, " or ', &
        //bufferUp(); // no need to bufferUp, just called consume()
        var pos = bufPos
        val start = pos
        val remaining = bufLength
        val tempBuf = charBuf

        OUTER@ while (pos < remaining) {
            when (tempBuf[pos]) {
                '&', CharConst.NULL -> break@OUTER
                '\'' -> if (single) break@OUTER
                '"' -> if (!single) break@OUTER
            }
            pos++
        }
        bufPos = pos
        return if (pos > start) cacheString(
            charBuf,
            stringCache,
            start,
            pos - start
        ) else ""
    }


    fun consumeRawData(): String {
        // <, null
        //bufferUp(); // no need to bufferUp, just called consume()
        var pos = bufPos
        val start = pos
        val remaining = bufLength
        val tempBuf = charBuf

        OUTER@ while (pos < remaining) {
            when (tempBuf[pos]) {
                '<', CharConst.NULL -> break@OUTER
                else -> pos++
            }
        }
        bufPos = pos
        return if (pos > start) cacheString(
            charBuf,
            stringCache,
            start,
            pos - start
        ) else ""
    }

    fun consumeTagName(): String {
        // '\t', '\n', '\r', '\f', ' ', '/', '>'
        // NOTE: out of spec, added '<' to fix common author bugs; does not stop and append on nullChar but eats
        bufferUp()
        var pos = bufPos
        val start = pos
        val remaining = bufLength
        val tempBuf = charBuf

        OUTER@ while (pos < remaining) {
            when (tempBuf[pos]) {
                '\t', '\n', '\r', '\u000c', ' ', '/', '>', '<' -> break@OUTER
            }
            pos++
        }

        bufPos = pos
        return if (pos > start) cacheString(
            charBuf,
            stringCache,
            start,
            pos - start
        ) else ""
    }

    fun consumeToEnd(): String {
        bufferUp()
        val data: String = cacheString(charBuf, stringCache, bufPos, bufLength - bufPos)
        bufPos = bufLength
        return data
    }

    fun consumeLetterSequence(): String {
        bufferUp()
        val start = bufPos
        while (bufPos < bufLength) {
            val c = charBuf[bufPos]
            if ((c in 'A'..'Z') || (c in 'a'..'z') || Character.isLetter(c)) bufPos++
            else break
        }

        return cacheString(charBuf, stringCache, start, bufPos - start)
    }

    fun consumeLetterThenDigitSequence(): String {
        bufferUp()
        val start = bufPos
        while (bufPos < bufLength) {
            val c = charBuf[bufPos]
            if ((c in 'A'..'Z') || (c in 'a'..'z') || Character.isLetter(c)) bufPos++
            else break
        }
        while (!isEmptyNoBufferUp()) {
            val c = charBuf[bufPos]
            if (c in '0'..'9') bufPos++
            else break
        }

        return cacheString(charBuf, stringCache, start, bufPos - start)
    }

    fun consumeHexSequence(): String {
        bufferUp()
        val start = bufPos
        while (bufPos < bufLength) {
            val c = charBuf[bufPos]
            if ((c in '0'..'9') || (c in 'A'..'F') || (c in 'a'..'f')) bufPos++
            else break
        }
        return cacheString(charBuf, stringCache, start, bufPos - start)
    }

    fun consumeDigitSequence(): String {
        bufferUp()
        val start = bufPos
        while (bufPos < bufLength) {
            val c = charBuf[bufPos]
            if (c in '0'..'9') bufPos++
            else break
        }
        return cacheString(charBuf, stringCache, start, bufPos - start)
    }

    fun matches(c: Char): Boolean {
        return !isEmpty() && charBuf[bufPos] == c
    }

    fun matches(seq: String): Boolean {
        bufferUp()
        val scanLength = seq.length
        if (scanLength > bufLength - bufPos) return false

        for (offset in 0 until scanLength) if (seq[offset] != charBuf[bufPos + offset]) return false
        return true
    }

    fun matchesIgnoreCase(seq: String): Boolean {
        bufferUp()
        val scanLength = seq.length
        if (scanLength > bufLength - bufPos) return false

        for (offset in 0 until scanLength) {
            val upScan = seq[offset].uppercaseChar()
            val upTarget = charBuf[bufPos + offset].uppercaseChar()
            if (upScan != upTarget) return false
        }
        return true
    }

    fun matchesAny(vararg seq: Char): Boolean {
        if (isEmpty()) return false

        bufferUp()
        val c = charBuf[bufPos]
        for (seek in seq) {
            if (seek == c) return true
        }
        return false
    }

    fun matchesAnySorted(seq: CharArray): Boolean {
        bufferUp()
        return !isEmpty() && Arrays.binarySearch(seq, charBuf[bufPos]) >= 0
    }

    fun matchesLetter(): Boolean {
        if (isEmpty()) return false
        val c = charBuf[bufPos]
        return (c in 'A'..'Z') || (c in 'a'..'z') || Character.isLetter(c)
    }

    /**
     * Checks if the current pos matches an ascii alpha (A-Z a-z) per https://infra.spec.whatwg.org/#ascii-alpha
     * @return if it matches or not
     */
    fun matchesAsciiAlpha(): Boolean {
        if (isEmpty()) return false
        val c = charBuf[bufPos]
        return (c in 'A'..'Z') || (c in 'a'..'z')
    }

    fun matchesDigit(): Boolean {
        if (isEmpty()) return false
        val c = charBuf[bufPos]
        return (c in '0'..'9')
    }

    fun matchConsume(seq: String): Boolean {
        bufferUp()
        if (matches(seq)) {
            bufPos += seq.length
            return true
        } else {
            return false
        }
    }

    fun matchConsumeIgnoreCase(seq: String): Boolean {
        if (matchesIgnoreCase(seq)) {
            bufPos += seq.length
            return true
        } else {
            return false
        }
    }

    // we maintain a cache of the previously scanned sequence, and return that if applicable on repeated scans.
    // that improves the situation where there is a sequence of <p<p<p<p<p<p<p...</title> and we're bashing on the <p
    // looking for the </title>. Resets in bufferUp()
    private var lastIcSeq: String? = null // scan cache
    private var lastIcIndex = 0 // nearest found indexOf

    /** Used to check presence of ,  when we're in RCData and see a <xxx. Only finds consistent case.></xxx.>  */
    fun containsIgnoreCase(seq: String): Boolean {
        if (seq == lastIcSeq) {
            if (lastIcIndex == -1) return false
            if (lastIcIndex >= bufPos) return true
        }
        lastIcSeq = seq

        val loScan = seq.lowercase()
        val lo = nextIndexOf(loScan)
        if (lo > -1) {
            lastIcIndex = bufPos + lo
            return true
        }

        val hiScan = seq.uppercase()
        val hi = nextIndexOf(hiScan)
        val found = hi > -1
        lastIcIndex = if (found) bufPos + hi else -1 // we don't care about finding the nearest, just that buf contains
        return found
    }

    init {
        assert(reader.markSupported())
        bufferUp()
    }

    companion object {
        const val MAX_BUFFER_LEN: Int = 1024 * 32
        val READ_AHEAD_LIMIT: Int = (MAX_BUFFER_LEN * 0.75f).toInt()
        private const val MIN_READ_AHEAD_LEN = 1024

        private const val MAX_STRING_CACHE_lEN: Int = 12
        const val STRING_CACHE_SIZE = 512

        private fun cacheString(charBuf: CharArray, stringCache: Array<String?>, start: Int, count: Int): String {
            // limit (no cache):
            if (count > MAX_STRING_CACHE_lEN) return String(charBuf, start, count)
            if (count < 1) return ""

            // calculate hash:
            var hash = 0
            for (i in 0 until count) {
                hash = 31 * hash + charBuf[start + i].code
            }

            // get from cache
            val index = hash and STRING_CACHE_SIZE - 1
            var cached = stringCache[index]
            if (cached != null && rangeEquals(charBuf, start, count, cached)
            ) // positive hit
                return cached
            else {
                cached = String(charBuf, start, count)
                stringCache[index] = cached // add or replace, assuming most recently used are most likely to recur next
            }
            return cached
        }

        fun rangeEquals(charBuf: CharArray, start: Int, count: Int, cached: String): Boolean {
            var flag = count
            if (count == cached.length) {
                var i = start
                var j = 0
                while (flag-- != 0) {
                    if (charBuf[i++] != cached[j++]) return false
                }
                return true
            }
            return false
        }
    }
}