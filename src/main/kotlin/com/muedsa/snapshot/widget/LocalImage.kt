package com.muedsa.snapshot.widget

import com.muedsa.geometry.Alignment
import com.muedsa.snapshot.annotation.MustCallSuper
import com.muedsa.snapshot.paint.BoxFit
import com.muedsa.snapshot.paint.ImageRepeat
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderImage
import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.Image

open class LocalImage(
    val image: Image,
    val width: Float? = null,
    val height: Float? = null,
    val fit: BoxFit? = null,
    val alignment: Alignment = Alignment.CENTER,
    val repeat: ImageRepeat = ImageRepeat.NO_REPEAT,
    val scale: Float = 1f,
    val opacity: Float = 1f,
    val color: Int? = null,
    val colorBlendMode: BlendMode? = null
) : Widget() {

    override fun createRenderTree(): RenderBox {
        return RenderImage(
            image = image,
            width = width,
            height = height,
            fit = fit,
            alignment = alignment,
            repeat = repeat,
            scale = scale,
            opacity = opacity,
            color = color,
            colorBlendMode = colorBlendMode
        )
    }

    @MustCallSuper
    override fun dispose() {
        image.close()
        super.dispose()
    }
}