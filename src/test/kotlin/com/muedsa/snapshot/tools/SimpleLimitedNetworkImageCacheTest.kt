package com.muedsa.snapshot.tools

import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class SimpleLimitedNetworkImageCacheTest {

    @Test
    fun limited_num_test() {
        val cache = SimpleLimitedNetworkImageCache(maxImageNum = 3)
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
        val cache = SimpleLimitedNetworkImageCache(maxSingleImageSize = 1024 * 1024 * 5)
        cache.getImage(SimpleNoLimitedNetworkImageCacheTest.TEST_IMAGE_URL_1)
        val cache1 = SimpleLimitedNetworkImageCache(maxSingleImageSize = 10)
        assertThrows<Throwable> {
            cache1.getImage(SimpleNoLimitedNetworkImageCacheTest.TEST_IMAGE_URL_1)
        }
    }
}