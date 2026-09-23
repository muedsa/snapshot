package com.muedsa.snapshot.widget

import com.muedsa.snapshot.painterImage
import org.jetbrains.skia.Color
import kotlin.test.Test
import kotlin.test.assertEquals

class ProviderImageTest {

    @Test
    fun provider_is_called_once_when_widget_is_constructed() {
        val image = painterImage(2f, 2f, Color.RED) { }
        var calls = 0
        try {
            val widget = ProviderImage(provider = { calls++; image })
            assertEquals(1, calls)
            widget.createRenderBox()
            assertEquals(1, calls, "创建渲染对象不应再次取图")
        } finally {
            image.close()
        }
    }
}
