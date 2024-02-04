package com.muedsa.snapshot.tools

import org.jetbrains.skia.Image
import java.net.URL

class SimpleLimitedNetworkImageCache(
    var maxImageNum: Int = Int.MAX_VALUE,
    var maxSingleImageSize: Int = Int.MAX_VALUE,
    var debug: Boolean = false,
) : NetworkImageCache {

    override val name: String get() = NAME

    private val cache: MutableMap<String, Image> = mutableMapOf()

    @Synchronized
    override fun getImage(url: String): Image {
        if (debug) println("Thread[${Thread.currentThread().name}] try get image from cache: $url")
        return cache.computeIfAbsent(url) {
            if (debug) println("Thread[${Thread.currentThread().name}] not found in cache: $url")
            Image.makeFromEncoded(getImageOverHttp(url))
        }
    }

    @Synchronized
    override fun clearAll() {
        cache.clear()
    }

    @Synchronized
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
        return URL(url).openStream().use {
            LimitedInputStream(it, maxSingleImageSize).readAllBytes()
        }
    }

    companion object {
        const val NAME: String = "SimpleLimitedNetworkImageCache"
    }
}