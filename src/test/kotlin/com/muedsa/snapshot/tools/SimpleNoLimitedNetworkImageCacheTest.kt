package com.muedsa.snapshot.tools

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
    fun getImage_test(){
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
    fun multi_thread_test(){
        val threadArr = Array(10) {
            Thread {
                getImage_test()
            }
        }
        threadArr.forEach { it.start() }
        threadArr.forEach { it.join() }
    }

    @Test
    fun size_test() {
        val size = SimpleNoLimitedNetworkImageCache.getImage(TEST_IMAGE_URL_2).size +
                SimpleNoLimitedNetworkImageCache.getImage(TEST_IMAGE_URL_3).size
        expect(size) {
            SimpleNoLimitedNetworkImageCache.size()
        }
    }

    companion object {
//        const val TEST_IMAGE_URL_1 = "https://picsum.photos/id/1/200/300"
//        const val TEST_IMAGE_URL_2 = "https://picsum.photos/id/2/200/300"
//        const val TEST_IMAGE_URL_3 = "https://picsum.photos/id/3/200/300"
//        const val TEST_IMAGE_URL_4 = "https://picsum.photos/id/4/200/300"
//        const val TEST_IMAGE_URL_5 = "https://picsum.photos/id/5/200/300"
        const val TEST_IMAGE_URL_1 = "https://pic4.zhimg.com/v2-219efd457b49eebbb2604d3408b5716f_400x224.png"
        const val TEST_IMAGE_URL_2 = "https://pica.zhimg.com/80/v2-f905c4699d6841c9b65ce6584ff119ba_400x224.png"
        const val TEST_IMAGE_URL_3 = "https://pic4.zhimg.com/v2-7d91b996e9523b572f63b295d446031f_400x224.jpg"
        const val TEST_IMAGE_URL_4 = "https://pic1.zhimg.com/80/v2-ba7c92d4371c4a9646ee5478aedb3a31_400x224.webp?source=1def8aca"
        const val TEST_IMAGE_URL_5 = "https://pica.zhimg.com/80/v2-78319dbd88aea48d571aa05ca6d53fac_400x224.webp?source=1def8aca"
    }
}