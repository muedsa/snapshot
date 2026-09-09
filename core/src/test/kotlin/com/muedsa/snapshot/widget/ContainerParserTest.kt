package com.muedsa.snapshot.widget

import com.muedsa.snapshot.assertSize
import com.muedsa.snapshot.expectRegionTransparent
import com.muedsa.snapshot.golden
import com.muedsa.snapshot.rootLayout
import com.muedsa.snapshot.snapshotPixels
import org.jetbrains.skia.Color
import org.jetbrains.skia.Rect
import kotlin.test.Test

class ContainerParserTest {

    // 无 color/decoration 的 Container:只参与布局、不绘制任何像素(实测整幅透明)。
    private fun Widget.sizedContainer() {
        Container(width = 300f, height = 300f)
    }

    @Test
    fun sized_test() {
        rootLayout { sizedContainer() }.assertSize(300f, 300f)

        // 背景用 TRANSPARENT,才能区分"没画"与"画了白色"
        val pixmap = snapshotPixels(background = Color.TRANSPARENT) { sizedContainer() }
        expectRegionTransparent(pixmap, Rect.makeXYWH(0f, 0f, 300f, 300f))
    }

    @Test
    fun sized_golden() {
        // 空白基准:白底 300x300,任何多余的绘制都会导致失配
        golden("widget/container/sized") { sizedContainer() }
    }
}
