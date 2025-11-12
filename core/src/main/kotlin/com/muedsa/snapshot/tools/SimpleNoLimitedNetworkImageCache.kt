package com.muedsa.snapshot.tools

import org.jetbrains.skia.Image
import java.io.FileNotFoundException
import java.net.URL

object SimpleNoLimitedNetworkImageCache : NetworkImageCache {

    var debug: Boolean = false

    const val NAME: String = "SimpleNoLimitedNetworkImageCache"

    private val cache: MutableMap<String, Image> = mutableMapOf()

    override val name: String
        get() = NAME

    @Synchronized
    override fun getImage(url: String, noCache: Boolean): Image {
        if (noCache) {
            if (debug) println("Thread[${Thread.currentThread().name}] get image without cache: $url")
             return Image.makeFromEncoded(getImageOverHttp(url))
        }
        if (debug) println("Thread[${Thread.currentThread().name}] try get image from cache: $url")
        return cache.computeIfAbsent(url) {
            if (debug) println("Thread[${Thread.currentThread().name}] not found in cache: $url")
            Image.makeFromEncoded(getImageOverHttp(url))
        }
    }

    @Synchronized
    override fun clearAll() {
        cache.values.forEach {
            it.close()
        }
        cache.clear()
    }

    @Synchronized
    override fun clearImage(url: String) {
        val image = cache.remove(url)
        image?.close()
    }

    @Synchronized
    override fun count(): Int = cache.size

    @Synchronized
    override fun size(): Int = cache.values.fold(0) { acc: Int, image: Image ->
        acc + image.imageInfo.computeMinByteSize()
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun getImageOverHttp(url: String): ByteArray {
        if (debug) println("Thread[${Thread.currentThread().name}] request http image: $url")
        try {
            return URL(url).openStream().use {
                val bytes = it.readBytes()
                check(ImageFormatValidator.valid(bytes)) {
                    "Data stream is not available image format, header hex: ${
                        bytes.copyOfRange(
                            0,
                            ImageFormatValidator.NECESSARY_MAGIC_LENGTH
                        ).toHexString()
                    }"
                }
                bytes
            }
        } catch (_: FileNotFoundException) {
            throw IllegalStateException("Get http 404 from $url")
        }
    }
}