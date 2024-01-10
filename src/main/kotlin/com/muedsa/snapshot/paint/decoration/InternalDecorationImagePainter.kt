package com.muedsa.snapshot.paint.decoration

import com.muedsa.snapshot.paint.paintImage
import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Path
import org.jetbrains.skia.Rect

internal class InternalDecorationImagePainter(
    private val details: DecorationImage
) : DecorationImagePainter {

    override fun paint(canvas: Canvas, rect: Rect, clipPath: Path?, blend: Float, blendMode: BlendMode) {
        if (clipPath != null) {
            canvas.save()
            canvas.clipPath(clipPath)
        }
        assert(!details.image.isClosed && !details.image.isEmpty)
        paintImage(
            canvas = canvas,
            rect = rect,
            image = details.image,
            scale = details.scale,
            colorFilter = details.colorFilter,
            fit = details.fit,
            alignment = details.alignment,
            centerSlice = details.centerSlice,
            repeat = details.repeat,
            flipHorizontally = false,
            opacity = details.opacity * blend,
            isAntiAlias = details.isAntiAlias,
            blendMode = blendMode,
        )

        if (clipPath != null) {
            canvas.restore()
        }
    }


    override fun dispose() {
        if (!details.image.isClosed) {
            details.image.close()
        }
    }
}