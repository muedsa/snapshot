package com.muedsa.snapshot.parser

import java.io.IOException
import java.io.Reader
import java.io.UncheckedIOException
import java.util.*
import kotlin.math.abs
import kotlin.math.min

class CharacterReader @JvmOverloads constructor(private val reader: Reader, bufferSize: Int = MAX_BUFFER_LEN) {

    private val charBuf: CharArray = CharArray(validateBufferSize(bufferSize))
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
            assert(skipped == pos.toLong()) // 此前已确认缓冲区有足够空间可跳过，若不相等则是内部状态错误。
            bufLength = read
            readerPos += pos
            bufPos = offset
            if (bufMark != -1) bufMark = 0
            bufSplitPoint = min(bufLength, READ_AHEAD_LIMIT)
        }
        scanBufferForNewlines() // 记录换行位置，以便跟踪行号。
        lastIcSeq = null // 清除上一次 containsIgnoreCase(seq) 的缓存。
    }


    private var newlinePositions: MutableList<Int> = ArrayList(MAX_BUFFER_LEN / 80)
    private var lineNumberOffset: Int = 1

    fun currentLineNumber(): Int = getLineNumber(pos())

    fun getLineNumber(pos: Int): Int {
        val i: Int = getLineNumberIndex(pos)
        if (i == -1) return lineNumberOffset // 第一行
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
            // 计算已读取位置对应的行号，因为扫描位置通常已经越过该点。
            var index: Int = getLineNumberIndex(readerPos)
            if (index == -1) index = 0 // 第一行
            val linePos = newlinePositions[index]
            lineNumberOffset += index // 累加已经读取的行数。
            newlinePositions.clear()
            newlinePositions.add(linePos) // 保留最后一个已读位置，用于计算新缓冲区中的行列号。
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
     * 回退一个字符（`bufPos--`）。只能紧接在 [consume] 后调用，并且两者之间不得触发缓冲区更新。
     */
    fun unconsume() {
        if (bufPos < 1) throw UncheckedIOException(IOException("WTF: No buffer left to unconsume.")) // 触发此分支表示内部调用顺序有误。
        bufPos--
    }

    /**
     * 将当前位置向前移动一个字符。
     */
    fun advance() {
        bufPos++
    }

    fun mark() {
        // 确保拥有足够的向前读取容量。
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
     * 返回当前位置与下一个指定字符之间的字符数。
     * @param c 扫描目标
     * @return 当前位置到下一个目标字符的偏移量；未找到时返回 -1。
     */
    fun nextIndexOf(c: Char): Int {
        // 不处理代理字符对。
        bufferUp()
        for (i in bufPos until bufLength) {
            if (c == charBuf[i]) return i - bufPos
        }
        return -1
    }

    /**
     * 返回当前位置与下一个指定字符序列之间的字符数。
     *
     * @param seq 扫描目标
     * @return 当前位置到下一个目标序列的偏移量；未找到时返回 -1。
     */
    fun nextIndexOf(seq: CharSequence): Int {
        bufferUp()
        // 不处理代理字符对。
        val startChar = seq[0]
        var offset = bufPos
        while (offset < bufLength) {
            // 先扫描到目标序列首字符的下一处位置。
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
                if (i == last) // 已找到完整序列。
                    return offset - bufPos
            }
            offset++
        }
        return -1
    }

    /**
     * 读取指定字符之前的所有字符。
     * @param c 分隔符
     * @return 已读取的字符
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
            // nextIndexOf() 已更新缓冲区；若剩余缓冲区比目标字符串短，则必然已经到达文件末尾。
            return consumeToEnd()
        } else {
            // 目标字符串可能跨越缓冲区边界，因此保留 length - 1 个字符不读取，
            // 以免丢失目标字符串的起始部分。
            val endPos = bufLength - seq.length + 1
            val consumed: String = cacheString(charBuf, stringCache, bufPos, endPos - bufPos)
            bufPos = endPos
            return consumed
        }
    }

    /**
     * 读取字符，直到遇到任一分隔符。
     * @param chars 要扫描的分隔符
     * @return 匹配分隔符之前读取的字符
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
        // 遇到 < 或空字符时停止。字符引用不会解码，因此 '&' 属于普通文本。
        // bufferUp()：调用方刚执行过 consume()，无需再次更新缓冲区。
        var pos = bufPos
        val start = pos
        val remaining = bufLength
        val tempBuf = charBuf

        OUTER@ while (pos < remaining) {
            when (tempBuf[pos]) {
                '<', Char.MIN_VALUE -> break@OUTER
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
        // 遇到空字符、对应引号或 & 时停止。
        // bufferUp()：调用方刚执行过 consume()，无需再次更新缓冲区。
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
        // 遇到 < 或空字符时停止。
        // bufferUp()：调用方刚执行过 consume()，无需再次更新缓冲区。
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
        // 注意：为兼容常见输入错误而额外识别 '<'，这不属于规范要求；空字符仍会被读取。
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
     * 按照 https://infra.spec.whatwg.org/#ascii-alpha 检查当前位置是否为 ASCII 字母（A-Z、a-z）。
     * @return 当前位置是否匹配 ASCII 字母
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

    // 缓存上一次扫描的序列，在重复扫描时直接复用结果。
    // 这可改善在 <p<p<p<p<p<p<p...</title> 中反复跳过 <p 并查找 </title> 的场景。
    // bufferUp() 会重置该缓存。
    private var lastIcSeq: String? = null // 扫描目标缓存
    private var lastIcIndex = 0 // 最近一次找到的位置

    /** 在 RCDATA 中遇到 `<xxx` 时检查对应结束标签是否存在；仅匹配大小写一致的序列。 */
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
        lastIcIndex = if (found) bufPos + hi else -1 // 这里只关心缓冲区是否包含目标，不要求最近位置。
        return found
    }

    init {
        require(reader.markSupported()) { "reader 必须支持 mark 和 reset" }
        bufferUp()
    }

    companion object {
        const val MAX_BUFFER_LEN: Int = 1024 * 32
        val READ_AHEAD_LIMIT: Int = (MAX_BUFFER_LEN * 0.75f).toInt()
        private const val MIN_READ_AHEAD_LEN = 1024

        private const val MAX_STRING_CACHE_lEN: Int = 12
        const val STRING_CACHE_SIZE = 512

        private fun validateBufferSize(bufferSize: Int): Int {
            require(bufferSize > 0) { "bufferSize 必须大于 0，实际为 $bufferSize" }
            return min(bufferSize, MAX_BUFFER_LEN)
        }

        private fun cacheString(charBuf: CharArray, stringCache: Array<String?>, start: Int, count: Int): String {
            // 超出长度限制时不缓存。
            if (count > MAX_STRING_CACHE_lEN) return String(charBuf, start, count)
            if (count < 1) return ""

            // 计算哈希值。
            var hash = 0
            for (i in 0 until count) {
                hash = 31 * hash + charBuf[start + i].code
            }

            // 从缓存读取。
            val index = hash and STRING_CACHE_SIZE - 1
            var cached = stringCache[index]
            if (cached != null && rangeEquals(charBuf, start, count, cached)
            ) // 命中缓存。
                return cached
            else {
                cached = String(charBuf, start, count)
                stringCache[index] = cached // 添加或替换，最近使用的字符串更可能再次出现。
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
