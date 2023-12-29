package com.muedsa.snapshot.tools

object NetworkImageCacheManager {

    private val map: MutableMap<String, NetworkImageCache> = mutableMapOf(
        SimpleNoLimitedNetworkImageCache.name to SimpleNoLimitedNetworkImageCache
    )

    var defaultCache: NetworkImageCache = SimpleNoLimitedNetworkImageCache

    @Synchronized
    fun register(cache: NetworkImageCache, asDefault: Boolean = false) {
        map[cache.name] = cache
        if (asDefault) {
            defaultCache = cache
        }
    }

    fun getOrDefault(name: String?): NetworkImageCache = if(name != null) this[name] ?: defaultCache else defaultCache

    operator fun get(name: String) = map[name]
}