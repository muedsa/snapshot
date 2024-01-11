package com.muedsa.snapshot.paint.decoration



import com.muedsa.geometry.BoxAlignment
import com.muedsa.snapshot.paint.BoxFit
import com.muedsa.snapshot.paint.ImageRepeat
import org.jetbrains.skia.ColorFilter
import org.jetbrains.skia.Image
import org.jetbrains.skia.Rect

class DecorationImage(
    val image: Image,
    val colorFilter: ColorFilter? = null,
    val fit: BoxFit? = null,
    val alignment: BoxAlignment = BoxAlignment.CENTER,
    val centerSlice: Rect? = null,
    val repeat: ImageRepeat = ImageRepeat.NO_REPEAT,
    val scale: Float = 1f,
    val opacity: Float = 1f,
    val isAntiAlias: Boolean = false,
) {

    fun createPainter(): DecorationImagePainter = InternalDecorationImagePainter(this)
}