package com.muedsa.snapshot.tools

import java.net.URL

object SimpleNoLimitedNetworkImageCache : NetworkImageCache {

    const val NAME: String = "SimpleNoLimitedNetworkImageCache"

    private val cache: MutableMap<String, ByteArray> = mutableMapOf()
    override val name: String
        get() = NAME

    override fun getImage(url: String): ByteArray = cache.getOrPut(url) { getImageOverHttp(url) }

    override fun clearAll() {
        cache.clear()
    }

    override fun clearImage(url: String) {
        cache.remove(url)
    }

    private fun getImageOverHttp(url: String): ByteArray = URL(url).openStream().use { it.readAllBytes() }


}