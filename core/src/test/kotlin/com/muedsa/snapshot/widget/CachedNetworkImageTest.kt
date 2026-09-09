package com.muedsa.snapshot.widget

import com.muedsa.snapshot.assertSize
import com.muedsa.snapshot.expectRegionOpaque
import com.muedsa.snapshot.rootLayout
import com.muedsa.snapshot.snapshotPixels
import org.jetbrains.skia.Color
import org.jetbrains.skia.Rect
import org.junit.jupiter.api.Tag
import kotlin.test.Test

@Tag("network")
class CachedNetworkImageTest {

    private companion object {
        const val IMAGE_URL = "https://samples-files.com/samples/images/jpg/1280-720-sample.jpg"
        const val SIDE = 400f
        const val CANVAS = 800f
    }

    // 两个 Column 各含两张 400x400 图片,并排 → 800x800(实测校准)。
    private fun Widget.networkImageScene(noCache: Boolean) {
        Row {
            Column {
                CachedNetworkImage(url = IMAGE_URL, width = SIDE, height = SIDE, noCache = noCache)
                CachedNetworkImage(url = IMAGE_URL, width = SIDE, height = SIDE, noCache = noCache)
            }
            Column {
                CachedNetworkImage(url = IMAGE_URL, width = SIDE, height = SIDE, noCache = noCache)
                CachedNetworkImage(url = IMAGE_URL, width = SIDE, height = SIDE, noCache = noCache)
            }
        }
    }

    private fun assertNetworkImageScene(noCache: Boolean) {
        rootLayout { networkImageScene(noCache) }.assertSize(CANVAS, CANVAS)
        // 透明底:图片未加载则整幅透明,区域不透明断言会失败
        val pixmap = snapshotPixels(background = Color.TRANSPARENT) { networkImageScene(noCache) }
        expectRegionOpaque(pixmap, Rect.makeXYWH(0f, 0f, CANVAS, CANVAS))
    }

    @Test
    fun networkImage_test() {
        assertNetworkImageScene(noCache = false)
    }

    @Test
    fun networkImage_noCache_test() {
        assertNetworkImageScene(noCache = true)
    }
}
