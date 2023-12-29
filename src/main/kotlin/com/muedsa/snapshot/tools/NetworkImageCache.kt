package com.muedsa.snapshot.tools

interface NetworkImageCache {

    val name: String

    fun getImage(url: String): ByteArray

    fun clearAll()

    fun clearImage(url: String)

}