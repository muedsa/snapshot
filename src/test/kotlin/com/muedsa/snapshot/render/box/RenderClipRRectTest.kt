package com.muedsa.snapshot.render.box

import com.muedsa.snapshot.noLimitedLayout
import com.muedsa.snapshot.renderBoxToPixels
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.box.RenderClipRRect
import com.muedsa.snapshot.rendering.box.RenderColoredBox
import com.muedsa.snapshot.rendering.box.RenderConstrainedBox
import org.jetbrains.skia.Color
import org.jetbrains.skia.RRect
import kotlin.math.sqrt
import kotlin.test.*

class RenderClipRRectTest {

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

        val renderClip = RenderClipRRect(
            clipBehavior = ClipBehavior.ANTI_ALIAS,
            clipper = {
                // make oval
                RRect.makeXYWH(quarterSize, quarterSize, halfSize, halfSize, radius = quarterSize) // clip ◙
            },
        ).apply {
            appendChild(originRenderBox)
        }

        val renderNoneClip = RenderClipRRect(
            clipBehavior = ClipBehavior.NONE,
            clipper = {
                // make oval
                RRect.makeXYWH(quarterSize, quarterSize, halfSize, halfSize, radius = quarterSize) // clip ◙
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
        // a point (inner of rect but outer of oval)
        val radius = quarterSize
        val point = (quarterSize + (sqrt(2 * radius * radius) - radius) / 2).toInt()
        assertNotEquals(
            originPixels.getColor(x = point, y = point),
            afterClipPixels.getColor(x = point, y = point)
        )
        assertEquals(
            originPixels.getColor(x = point, y = point),
            noneClipPixels.getColor(x = point, y = point)
        )
    }
}