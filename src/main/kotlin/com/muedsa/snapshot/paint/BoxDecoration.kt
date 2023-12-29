package com.muedsa.snapshot.paint

import com.muedsa.geometry.Offset
import com.muedsa.geometry.center
import com.muedsa.geometry.makeRectFromCircle
import com.muedsa.geometry.shortestSide
import org.jetbrains.skia.*

class BoxDecoration(
    val color: Int? = null,
    val image: DecorationImage,
    val border: BoxBorder,
    val borderRadius: BorderRadius?,
    val boxShadow: List<BoxShadow>,
    val gradient: Gradient? = null,
    val backgroundBlendMode: BlendMode? = null,
    val shape: BoxShape = BoxShape.RECTANGLE
) : Decoration() {

    init {
        assert(
            backgroundBlendMode == null || color != null || gradient != null
        ) { "backgroundBlendMode applies to BoxDecoration's background color or " +
                "gradient, but no color or gradient was provided."
        }
    }

    override fun getClipPath(rect: Rect): Path {
        return when(shape) {
            BoxShape.CIRCLE -> {
                val center: Offset = rect.center
                val radius: Float = rect.shortestSide / 2f
                val square: Rect = makeRectFromCircle(center, radius)
                Path().addOval(square)
            }
            BoxShape.RECTANGLE -> {
                if (borderRadius != null) {
                    Path().addRRect(borderRadius.toRRect(rect))
                } else {
                    Path().addRect(rect)
                }
            }
        }
    }

    override fun createBoxPainter(): BoxPainter {
        TODO("Not yet implemented")
    }


    fun copyWith(){
        TODO("Not yet implemented")
    }
}