package com.muedsa.snapshot.paint

import com.muedsa.geometry.Alignment
import org.jetbrains.skia.ColorFilter
import org.jetbrains.skia.Image
import org.jetbrains.skia.Rect

class DecorationImage(
    val image: Image,
    val colorFilter: ColorFilter? = null,
    val fit: BoxFit? = null,
    val alignment: Alignment = Alignment.CENTER,
    val centerSlice: Rect? = null,
    val repeat: ImageRepeat = ImageRepeat.NO_REPEAT,
    val scale: Float = 1f,
    val opacity: Float = 1f,
    val isAntiAlias: Boolean = false
) {

    fun createPainter(): DecorationImagePainter = InternalDecorationImagePainter(this)
}