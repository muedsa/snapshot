package com.muedsa.snapshot.render.box

import com.muedsa.snapshot.noLimitedLayout
import com.muedsa.snapshot.renderBoxToPixels
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.box.RenderClipPath
import com.muedsa.snapshot.rendering.box.RenderColoredBox
import com.muedsa.snapshot.rendering.box.RenderConstrainedBox
import org.jetbrains.skia.Color
import org.jetbrains.skia.Path
import kotlin.test.*

class RenderClipPathTest {

    @Test
    fun clip_test() {
        val size = 100f

        val originRenderBox = RenderColoredBox(
            color = Color.RED,
        ).apply {
            appendChild(
                RenderConstrainedBox(
                additionalConstraints = BoxConstraints.expand(size, size)
                )
            )
        }

        val renderClip = RenderClipPath(
            clipBehavior = ClipBehavior.ANTI_ALIAS,
            clipper = {
                Path()
                    .lineTo(it.x, 0f)
                    .lineTo(it.x, it.y)
                    .lineTo(0f, 0f) // clip ◥
            },
        ).apply {
            appendChild(originRenderBox)
        }

        val renderNoneClip = RenderClipPath(
            clipBehavior = ClipBehavior.NONE,
            clipper = {
                Path()
                    .lineTo(it.x, 0f)
                    .lineTo(it.x, it.y)
                    .lineTo(0f, 0f) // clip ◥
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

        val offset = (size / 4).toInt()
        assertEquals(
            originPixels.getColor(x = offset * 3, y = offset),
            afterClipPixels.getColor(x = offset * 3, y = offset)
        )
        assertEquals(
            originPixels.getColor(x = offset * 3, y = offset),
            noneClipPixels.getColor(x = offset * 3, y = offset)
        )
        assertNotEquals(
            originPixels.getColor(x = offset, y = offset * 3),
            afterClipPixels.getColor(x = offset, y = offset * 3)
        )
        assertEquals(
            originPixels.getColor(x = offset, y = offset * 3),
            noneClipPixels.getColor(x = offset, y = offset * 3)
        )
    }
}