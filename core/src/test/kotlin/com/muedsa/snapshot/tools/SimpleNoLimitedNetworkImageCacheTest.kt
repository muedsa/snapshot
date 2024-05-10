package com.muedsa.snapshot.tools

import org.junit.jupiter.api.assertThrows
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.expect

class SimpleNoLimitedNetworkImageCacheTest {

    @BeforeTest
    fun before() {
        SimpleNoLimitedNetworkImageCache.debug = true
        SimpleNoLimitedNetworkImageCache.clearAll()
    }

    @Test
    fun getImage_test() {
        expect(0) {
            SimpleNoLimitedNetworkImageCache.count()
        }
        for (i in 1..100) {
            SimpleNoLimitedNetworkImageCache.getImage(TEST_IMAGE_URL_1)
            SimpleNoLimitedNetworkImageCache.getImage(TEST_IMAGE_URL_2)
            SimpleNoLimitedNetworkImageCache.getImage(TEST_IMAGE_URL_1)
            SimpleNoLimitedNetworkImageCache.getImage(TEST_IMAGE_URL_3)
            SimpleNoLimitedNetworkImageCache.getImage(TEST_IMAGE_URL_2)
        }
        expect(3) {
            SimpleNoLimitedNetworkImageCache.count()
        }
    }

    @Test
    fun clear_test() {
        expect(0) {
            SimpleNoLimitedNetworkImageCache.count()
        }
        SimpleNoLimitedNetworkImageCache.getImage(TEST_IMAGE_URL_1)
        SimpleNoLimitedNetworkImageCache.getImage(TEST_IMAGE_URL_2)
        SimpleNoLimitedNetworkImageCache.getImage(TEST_IMAGE_URL_3)
        SimpleNoLimitedNetworkImageCache.getImage(TEST_IMAGE_URL_4)
        SimpleNoLimitedNetworkImageCache.getImage(TEST_IMAGE_URL_5)
        expect(5) {
            SimpleNoLimitedNetworkImageCache.count()
        }
        SimpleNoLimitedNetworkImageCache.clearImage(TEST_IMAGE_URL_1)
        expect(4) {
            SimpleNoLimitedNetworkImageCache.count()
        }
        SimpleNoLimitedNetworkImageCache.clearImage(TEST_IMAGE_URL_2)
        SimpleNoLimitedNetworkImageCache.clearImage(TEST_IMAGE_URL_3)
        expect(2) {
            SimpleNoLimitedNetworkImageCache.count()
        }
        SimpleNoLimitedNetworkImageCache.clearAll()
        expect(0) {
            SimpleNoLimitedNetworkImageCache.count()
        }
    }

    @Test
    fun size_test() {
        val size = SimpleNoLimitedNetworkImageCache.getImage(TEST_IMAGE_URL_2).imageInfo.computeMinByteSize() +
                SimpleNoLimitedNetworkImageCache.getImage(TEST_IMAGE_URL_3).imageInfo.computeMinByteSize()
        expect(size) {
            SimpleNoLimitedNetworkImageCache.size()
        }
    }

    @Test
    fun http_404_test() {
        assertThrows<IllegalStateException> {
            SimpleNoLimitedNetworkImageCache.getImage("https://pic3.zhimg.com/v2-875bc0f51908e99b6e88a2b53552")
        }
    }

    companion object {
        const val TEST_IMAGE_URL_1 = "https://samples-files.com/samples/Images/jpg/480-360-sample.jpg"
        const val TEST_IMAGE_URL_2 = "https://samples-files.com/samples/Images/jpg/640-480-sample.jpg"
        const val TEST_IMAGE_URL_3 = "https://samples-files.com/samples/Images/jpg/1280-720-sample.jpg"
        const val TEST_IMAGE_URL_4 = "https://samples-files.com/samples/Images/jpg/1920-1080-sample.jpg"
        const val TEST_IMAGE_URL_5 = "https://samples-files.com/samples/Images/jpg/3840-2160-sample.jpg"
    }
}