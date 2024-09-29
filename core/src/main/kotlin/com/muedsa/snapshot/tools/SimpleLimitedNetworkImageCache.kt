package com.muedsa.snapshot.tools

import org.jetbrains.skia.Image
import java.io.FileNotFoundException
import java.net.URL

class SimpleLimitedNetworkImageCache(
    var maxImageNum: Int = Int.MAX_VALUE,
    var maxSingleImageSize: Int = Int.MAX_VALUE,
    var debug: Boolean = false,
) : NetworkImageCache {

    override val name: String get() = NAME

    private val cache: MutableMap<String, Image> = mutableMapOf()

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

    override fun clearAll() {
        cache.clear()
    }

    override fun clearImage(url: String) {
        cache.remove(url)
    }

    override fun count(): Int = cache.size

    override fun size(): Int = cache.values.fold(0) { acc: Int, image: Image ->
        acc + image.imageInfo.computeMinByteSize()
    }

    private fun getImageOverHttp(url: String): ByteArray {
        if (SimpleNoLimitedNetworkImageCache.debug) println("Thread[${Thread.currentThread().name}] request http image: $url")
        check(count() + 1 <= maxImageNum) { "Exceeded maximum number [$maxImageNum] of image http requests" }
        try {
            return URL(url).openStream().use { LimitedImageInputStream(it, maxSingleImageSize).readAllBytes() }
        } catch (e: FileNotFoundException) {
            throw IllegalStateException("Get http 404 from $url")
        }
    }

    companion object {
        const val NAME: String = "SimpleLimitedNetworkImageCache"
    }
}