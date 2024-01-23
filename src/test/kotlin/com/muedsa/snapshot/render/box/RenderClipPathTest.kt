package com.muedsa.snapshot.render.box

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.PaintingContext
import com.muedsa.snapshot.rendering.box.RenderClipPath
import com.muedsa.snapshot.rendering.box.RenderColoredBox
import org.jetbrains.skia.Color
import org.jetbrains.skia.Path
import org.jetbrains.skia.Rect

class RenderClipPathTest {

    fun paint_test() {
        val paintingContext = PaintingContext.paintRoot(
            bounds = Rect.Companion.makeWH(100f, 100f),
            renderBox = RenderColoredBox(
                color = Color.RED
            )
        )
        val renderClipPath = RenderClipPath(
            clipBehavior = ClipBehavior.ANTI_ALIAS,
            clipper = {
                Path().lineTo(it.x, it.y)
            }
        )
        paintingContext.paintChild(renderClipPath, Offset.ZERO)
    }
}