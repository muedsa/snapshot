package com.muedsa.snapshot.paint

import com.muedsa.geometry.Offset
import org.jetbrains.skia.FilterBlurMode
import org.jetbrains.skia.MaskFilter
import org.jetbrains.skia.Paint
import org.jetbrains.skia.paragraph.Shadow
import kotlin.math.min

class BoxShadow(
    val color: Int,
    val offset: Offset,
    val blurRadius: Float,
    val spreadRadius: Float = 0f,
    val blurStyle: FilterBlurMode = FilterBlurMode.NORMAL
) {

    val blurSigma: Float by lazy {
        convertRadiusToSigma(blurRadius)
    }


    open fun toPaint(): Paint = Paint().apply {
        color = this@BoxShadow.color
        maskFilter = MaskFilter.makeBlur(blurStyle, blurSigma)
    }

    open fun scale(factor: Float): BoxShadow =  BoxShadow(
        color = color,
        offset = offset * factor,
        blurRadius = blurRadius * factor,
        spreadRadius = spreadRadius * factor,
        blurStyle = blurStyle
    )

    fun toSkShadow(): Shadow = Shadow(
        color = color,
        offset = offset.toSkPoint(),
        blurSigma = blurSigma.toDouble()
    )

    companion object {

        @JvmStatic
        fun convertRadiusToSigma(radius: Float): Float {
            return if(radius > 0f) (radius * 0.57735 + 0.5).toFloat() else 0f
        }

        @JvmStatic
        fun lerp(a: BoxShadow?, b: BoxShadow?, t: Float): BoxShadow? {
            if (a == b) {
                return a
            }
            if (a == null) {
                return b!!.scale(t)
            }
            if (b == null) {
                return a.scale(1f - t)
            }
            return BoxShadow(
                color = lerpColor(a.color, b.color, t)!!,
                offset = Offset.lerp(a.offset, b.offset, t)!!,
                blurRadius = lerpFloat(a.blurRadius, b.blurRadius, t),
                spreadRadius = lerpFloat(a.spreadRadius, b.spreadRadius, t),
                blurStyle = if (a.blurStyle == FilterBlurMode.NORMAL)  b.blurStyle else a.blurStyle,
            )
        }

        @JvmStatic
        fun lerpList(a: List<BoxShadow>?, b: List<BoxShadow>?, t: Float): List<BoxShadow>? {
            if (a == b) {
                return a
            }
            val ta: List<BoxShadow> = a ?: emptyList()
            val tb : List<BoxShadow> = b ?: emptyList()
            val commonLength = min(ta.size, tb.size)

            return buildList {
                for (i in 0..<commonLength) add(lerp(ta[i], tb[i], t)!!)
                if (ta.size > commonLength) {
                    for (i in commonLength..<ta.size) add(ta[i].scale(1f - t))
                }
                if (tb.size > commonLength) {
                    for (i in commonLength..<tb.size) add(tb[i].scale(t))
                }
            }
        }
    }
}