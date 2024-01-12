package com.muedsa.snapshot.tools

import java.net.URL
import java.util.concurrent.ConcurrentHashMap

object SimpleNoLimitedNetworkImageCache : NetworkImageCache {

    var debug: Boolean = false

    const val NAME: String = "SimpleNoLimitedNetworkImageCache"

    private val threadLocalCache: ThreadLocal<MutableMap<String, ByteArray>> = ThreadLocal.withInitial {
        ConcurrentHashMap()
    }

    override val name: String
        get() = NAME

    override fun getImage(url: String): ByteArray {
        if (debug) println("Thread[${Thread.currentThread().name}] try get image from cache: $url")
        return threadLocalCache.get().computeIfAbsent(url) {
            if (debug) println("Thread[${Thread.currentThread().name}] not found in cache: $url")
            getImageOverHttp(url)
        }
    }

    override fun clearAll() {
        threadLocalCache.get().clear()
    }

    override fun clearImage(url: String) {
        threadLocalCache.get().remove(url)
    }

    override fun count(): Int = threadLocalCache.get().size

    override fun size(): Int = threadLocalCache.get().values.fold(0) { acc: Int, bytes: ByteArray ->
        acc + bytes.size
    }

    private fun getImageOverHttp(url: String): ByteArray {
        if (debug) println("Thread[${Thread.currentThread().name}] request http image: $url")
        return URL(url).openStream().use { it.readAllBytes() }
    }
}