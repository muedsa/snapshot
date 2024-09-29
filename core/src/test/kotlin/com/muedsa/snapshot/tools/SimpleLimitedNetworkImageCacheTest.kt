package com.muedsa.snapshot.tools

import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class SimpleLimitedNetworkImageCacheTest {

    @Test
    fun limited_num_test() {
        val cache = SimpleLimitedNetworkImageCache(maxImageNum = 3, debug = true)
        cache.getImage(SimpleNoLimitedNetworkImageCacheTest.TEST_IMAGE_URL_1)
        cache.getImage(SimpleNoLimitedNetworkImageCacheTest.TEST_IMAGE_URL_2)
        cache.getImage(SimpleNoLimitedNetworkImageCacheTest.TEST_IMAGE_URL_3)
        cache.getImage(SimpleNoLimitedNetworkImageCacheTest.TEST_IMAGE_URL_3)
        assertThrows<Throwable> {
            cache.getImage(SimpleNoLimitedNetworkImageCacheTest.TEST_IMAGE_URL_4)
        }
    }

    @Test
    fun limited_image_size_test() {
        val cache = SimpleLimitedNetworkImageCache(maxSingleImageSize = 1024 * 1024 * 5, debug = true)
        cache.getImage(SimpleNoLimitedNetworkImageCacheTest.TEST_IMAGE_URL_1)
        val cache1 = SimpleLimitedNetworkImageCache(maxSingleImageSize = 10, debug = true)
        assertThrows<Throwable> {
            cache1.getImage(SimpleNoLimitedNetworkImageCacheTest.TEST_IMAGE_URL_1)
        }
    }

    @Test
    fun http_404_test() {
        assertThrows<IllegalStateException> {
            SimpleLimitedNetworkImageCache().getImage("https://pic3.zhimg.com/v2-875bc0f51908e99b6e88a2b53552")
        }
    }

    @Test
    fun no_cache_test() {
        val cache = SimpleLimitedNetworkImageCache(maxSingleImageSize = 1, maxImageNum = 1, debug = true)
        cache.getImage(SimpleNoLimitedNetworkImageCacheTest.TEST_IMAGE_URL_1, true)
        cache.getImage(SimpleNoLimitedNetworkImageCacheTest.TEST_IMAGE_URL_2, true)
        cache.getImage(SimpleNoLimitedNetworkImageCacheTest.TEST_IMAGE_URL_3, true)
        cache.getImage(SimpleNoLimitedNetworkImageCacheTest.TEST_IMAGE_URL_4, true)
    }
}