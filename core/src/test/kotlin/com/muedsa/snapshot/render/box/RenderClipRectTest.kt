package com.muedsa.snapshot.render.box

import com.muedsa.snapshot.noLimitedLayout
import com.muedsa.snapshot.renderBoxToPixels
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.box.RenderClipRect
import com.muedsa.snapshot.rendering.box.RenderColoredBox
import com.muedsa.snapshot.rendering.box.RenderConstrainedBox
import org.jetbrains.skia.Color
import org.jetbrains.skia.Rect
import kotlin.test.*

class RenderClipRectTest {
    @Test
    fun clip_test() {
        val size = 200f
        val halfSize = size / 2
        val quarterSize = size / 4
        val eighthSize = size / 8

        val originRenderBox = RenderColoredBox(
            color = Color.RED,
        ).apply {
            appendChild(
                RenderConstrainedBox(
                additionalConstraints = BoxConstraints.expand(size, size)
                )
            )
        }

        val renderClip = RenderClipRect(
            clipBehavior = ClipBehavior.ANTI_ALIAS,
            clipper = {
                Rect.makeXYWH(quarterSize, quarterSize, halfSize, halfSize) // clip ▣
            },
        ).apply {
            appendChild(originRenderBox)
        }

        val renderNoneClip = RenderClipRect(
            clipBehavior = ClipBehavior.NONE,
            clipper = {
                Rect.makeXYWH(quarterSize, quarterSize, halfSize, halfSize) // clip ▣
            },
        ).apply {
            appendChild(originRenderBox)
        }

        noLimitedLayout(originRenderBox)
        val originPixels = renderBoxToPixels(originRenderBox)
        val originByteArray = originPixels.buffer.bytes

        noLimitedLayout(renderClip)
        val afterClipPixels = renderBoxToPixels(renderClip)
        val afterClipByteArray = afterClipPixels.buffer.bytes

        noLimitedLayout(renderNoneClip)
        val noneClipPixels = renderBoxToPixels(renderNoneClip)
        val noneClipByteArray = noneClipPixels.buffer.bytes

        assertFalse(originByteArray.contentEquals(afterClipByteArray))
        assertContentEquals(originByteArray, noneClipByteArray)

        val offset = eighthSize.toInt()
        // clip inner
        assertEquals(
            originPixels.getColor(x = offset * 3, y = offset * 3),
            afterClipPixels.getColor(x = offset * 3, y = offset * 3)
        )
        assertEquals(
            originPixels.getColor(x = offset * 3, y = offset * 3),
            noneClipPixels.getColor(x = offset * 3, y = offset * 3)
        )
        assertEquals(
            originPixels.getColor(x = offset * 4, y = offset * 4),
            afterClipPixels.getColor(x = offset * 4, y = offset * 4)
        )
        assertEquals(
            originPixels.getColor(x = offset * 4, y = offset * 4),
            noneClipPixels.getColor(x = offset * 4, y = offset * 4)
        )
        assertEquals(
            originPixels.getColor(x = offset * 5, y = offset * 5),
            afterClipPixels.getColor(x = offset * 5, y = offset * 5)
        )
        assertEquals(
            originPixels.getColor(x = offset * 5, y = offset * 5),
            noneClipPixels.getColor(x = offset * 5, y = offset * 5)
        )

        // clip outer
        assertNotEquals(
            originPixels.getColor(x = offset, y = offset),
            afterClipPixels.getColor(x = offset, y = offset)
        )
        assertEquals(
            originPixels.getColor(x = offset, y = offset),
            noneClipPixels.getColor(x = offset, y = offset)
        )
        assertNotEquals(
            originPixels.getColor(x = offset * 7, y = offset * 7),
            afterClipPixels.getColor(x = offset * 7, y = offset * 7)
        )
        assertEquals(
            originPixels.getColor(x = offset * 7, y = offset * 7),
            noneClipPixels.getColor(x = offset * 7, y = offset * 7)
        )
    }
}