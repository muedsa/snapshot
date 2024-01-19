package com.muedsa.snapshot.tools

interface NetworkImageCache {

    val name: String

    fun getImage(url: String): ByteArray

    fun clearAll()

    fun clearImage(url: String)

    fun count(): Int

    fun size(): Int

    operator fun get(url: String): ByteArray = getImage(url)
}