package com.muedsa.snapshot.paint.decoration

import com.muedsa.geometry.Offset
import com.muedsa.geometry.center
import com.muedsa.geometry.makeRectFromCircle
import com.muedsa.geometry.shortestSide
import com.muedsa.snapshot.paint.gradient.Gradient
import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.Path
import org.jetbrains.skia.PathBuilder
import org.jetbrains.skia.Rect

class BoxDecoration(
    val color: Int? = null,
    val image: DecorationImage? = null,
    val border: BoxBorder? = null,
    val borderRadius: BorderRadius? = null,
    val boxShadow: Array<BoxShadow>? = null,
    val gradient: Gradient? = null,
    val backgroundBlendMode: BlendMode? = null,
    val shape: BoxShape = BoxShape.RECTANGLE,
) : Decoration() {

    init {
        assert(
            backgroundBlendMode == null || color != null || gradient != null
        ) {
            "backgroundBlendMode applies to BoxDecoration's background color or " +
                    "gradient, but no color or gradient was provided."
        }
    }

    override fun getClipPath(rect: Rect): Path {
        return when (shape) {
            BoxShape.CIRCLE -> {
                val center: Offset = rect.center
                val radius: Float = rect.shortestSide / 2f
                val square: Rect = makeRectFromCircle(center, radius)
                PathBuilder().addOval(square).detach()
            }

            BoxShape.RECTANGLE -> {
                if (borderRadius != null) {
                    PathBuilder().addRRect(borderRadius.toRRect(rect)).detach()
                } else {
                    PathBuilder().addRect(rect).detach()
                }
            }
        }
    }

    override fun createBoxPainter(): BoxPainter = BoxDecorationPainter(this)
}