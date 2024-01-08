package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.Alignment
import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import com.muedsa.snapshot.paint.BoxFit
import com.muedsa.snapshot.paint.ImageRepeat
import com.muedsa.snapshot.paint.paintImage
import com.muedsa.snapshot.rendering.PaintingContext
import org.jetbrains.skia.*

class RenderImage(
    val image: Image?,
    val width: Float? = null,
    val height: Float? = null,
    val scale: Float = 1f,
    val color: Int? = null,
    val opacity: Float = 1f,
    val colorBlendMode: BlendMode? = null,
    val fit: BoxFit? = null,
    val alignment: Alignment = Alignment.CENTER,
    val repeat: ImageRepeat = ImageRepeat.NO_REPEAT,
    val centerSlice: Rect? = null,
    val isAntiAlias: Boolean = false
) : RenderBox() {

    val colorFilter : ColorFilter? = color?.let { ColorFilter.makeBlend(it, colorBlendMode ?: BlendMode.SRC_IN) }

    private fun sizeForConstraints(constraints: BoxConstraints): Size {
        val temp = BoxConstraints.tightFor(
            width = width,
            height = height,
        ).enforce(constraints)
        return image?.let {
            temp.constrainSizeAndAttemptToPreserveAspectRatio(Size(
                it.width / scale,
                it.height / scale,
            ))
        } ?: temp.smallest
    }

    override fun performLayout() {
        size = sizeForConstraints(definiteConstraints)
    }

    override fun paint(context: PaintingContext, offset: Offset) {
        if (image != null) {
            // paint image
            paintImage(
                canvas = context.canvas,
                rect = offset combine definiteSize,
                image = image,
                scale = scale,
                opacity = opacity,
                colorFilter = colorFilter,
                fit = fit,
                alignment = alignment,
                centerSlice = centerSlice,
                repeat = repeat,
                flipHorizontally = false,
                isAntiAlias = isAntiAlias
            )
        }
    }
}