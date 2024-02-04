package com.muedsa.snapshot.tools

import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream

class LimitedInputStream(
    inputStream: InputStream,
    val limit: Int,
) : FilterInputStream(inputStream) {

    private var left: Int = limit

    private var leftMark: Int = -1

    override fun mark(readlimit: Int) {
        super.mark(readlimit)
        leftMark = left
    }

    override fun read(): Int {
        if (left < 0) {
            throw IOException("Exceeded maximum limit $limit")
        }
        val result = super.read()
        if (result != -1) {
            --left
            if (left < 0) {
                throw IOException("Exceeded maximum limit $limit")
            }
        }
        return result
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (left < 0) {
            throw IOException("Exceeded maximum limit $limit")
        }
        val result = super.read(b, off, len)
        if (result != -1) {
            if (left < result) {
                throw IOException("Exceeded maximum limit $limit")
            }
            left -= result
        }
        return result
    }

    override fun reset() {
        if (leftMark == -1) {
            throw IOException("Mark not set")
        }
        super.reset()
        left = leftMark
    }

    override fun skip(n: Long): Long {
        if (left < 0) {
            throw IOException("Exceeded maximum limit $limit")
        }
        val skipped = super.skip(n)
        if (left < skipped) {
            throw IOException("Exceeded maximum limit $limit")
        }
        left -= skipped.toInt()
        return skipped
    }

    init {
        check(limit > 0) { "Limit must be non-negative, but get '$limit'" }
    }
}