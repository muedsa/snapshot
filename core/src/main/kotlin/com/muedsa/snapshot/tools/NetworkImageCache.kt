package com.muedsa.snapshot.tools

import org.jetbrains.skia.Image

interface NetworkImageCache {

    val name: String

    fun getImage(url: String): Image

    fun clearAll()

    fun clearImage(url: String)

    fun count(): Int

    fun size(): Int

    operator fun get(url: String): Image = getImage(url)
}