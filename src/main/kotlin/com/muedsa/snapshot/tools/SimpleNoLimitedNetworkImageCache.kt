package com.muedsa.snapshot.tools

import java.net.URL
import kotlin.concurrent.getOrSet

object SimpleNoLimitedNetworkImageCache : NetworkImageCache {

    var debug: Boolean = false

    const val NAME: String = "SimpleNoLimitedNetworkImageCache"

    private val threadLocalCache: InheritableThreadLocal<MutableMap<String, ByteArray>> = InheritableThreadLocal()

    private val cache: MutableMap<String, ByteArray>
        @Synchronized
        get() = threadLocalCache.getOrSet { mutableMapOf() }

    override val name: String
        get() = NAME

    @Synchronized
    override fun getImage(url: String): ByteArray {
        if (debug) println("Thread[${Thread.currentThread().name}] try get image from cache: $url")
        return cache.getOrPut(url) {
            if (debug) println("Thread[${Thread.currentThread().name}] not found in cache: $url")
            getImageOverHttp(url)
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

    @Synchronized
    override fun count(): Int = cache.size

    @Synchronized
    override fun size(): Int = cache.values.fold(0) { acc: Int, bytes: ByteArray ->
        acc + bytes.size
    }

    private fun getImageOverHttp(url: String): ByteArray {
        if (debug) println("Thread[${Thread.currentThread().name}] request http image: $url")
        return URL(url).openStream().use { it.readAllBytes() }
    }


}