package com.muedsa.snapshot.paint.decoration

import com.muedsa.geometry.Offset
import org.jetbrains.skia.FilterBlurMode
import org.jetbrains.skia.MaskFilter
import org.jetbrains.skia.Paint

data class BoxShadow(
    val color: Int,
    val offset: Offset,
    val blurRadius: Float,
    val spreadRadius: Float = 0f,
    val blurStyle: FilterBlurMode = FilterBlurMode.NORMAL,
) {

    val blurSigma: Float by lazy {
        convertRadiusToSigma(blurRadius)
    }


    fun toPaint(): Paint = Paint().apply {
        color = this@BoxShadow.color
        maskFilter = MaskFilter.makeBlur(blurStyle, blurSigma)
    }

    fun scale(factor: Float): BoxShadow = BoxShadow(
        color = color,
        offset = offset * factor,
        blurRadius = blurRadius * factor,
        spreadRadius = spreadRadius * factor,
        blurStyle = blurStyle
    )

    override fun toString(): String {
        return "BoxShadow(color=$color, offset=$offset, blurRadius=$blurRadius, spreadRadius=$spreadRadius, blurStyle=$blurStyle)"
    }


    companion object {

        @JvmStatic
        fun convertRadiusToSigma(radius: Float): Float {
            return if (radius > 0f) (radius * 0.57735 + 0.5).toFloat() else 0f
        }
    }
}