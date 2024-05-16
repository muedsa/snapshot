package com.muedsa.snapshot.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.snapshot.paint.BoxFit
import com.muedsa.snapshot.paint.ImageRepeat
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderImage
import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.Image

fun Widget.RawImage(
    image: Image,
    width: Float? = null,
    height: Float? = null,
    fit: BoxFit? = null,
    alignment: BoxAlignment = BoxAlignment.CENTER,
    repeat: ImageRepeat = ImageRepeat.NO_REPEAT,
    scale: Float = 1f,
    opacity: Float = 1f,
    color: Int? = null,
    colorBlendMode: BlendMode? = null,
) {
    buildChild(
        widget = RawImage(
            image = image,
            width = width,
            height = height,
            fit = fit,
            alignment = alignment,
            repeat = repeat,
            scale = scale,
            opacity = opacity,
            color = color,
            colorBlendMode = colorBlendMode,
            parent = this
        ),
        content = {}
    )
}

open class RawImage(
    var image: Image,
    var width: Float? = null,
    var height: Float? = null,
    var fit: BoxFit? = null,
    var alignment: BoxAlignment = BoxAlignment.CENTER,
    var repeat: ImageRepeat = ImageRepeat.NO_REPEAT,
    var scale: Float = 1f,
    var opacity: Float = 1f,
    var color: Int? = null,
    var colorBlendMode: BlendMode? = null,
    parent: Widget? = null,
) : Widget(parent = parent) {

    override fun createRenderBox(): RenderBox {
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
}