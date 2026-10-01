package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.parser.attr.required.RequiredFloatAttrDefine
import com.muedsa.snapshot.widget.ImageFiltered
import com.muedsa.snapshot.widget.Widget
import com.muedsa.snapshot.widget.blurImageFilterBounds
import org.jetbrains.skia.ImageFilter

open class ImageFilteredParser : WidgetParser {

    override val id: String = "ImageFiltered"

    override val containerMode: ContainerMode = ContainerMode.SINGLE

    override fun buildWidget(element: Element): Widget {
        val blur = parseBlurFilterSpec(element)
        return ImageFiltered(
            imageFilter = blur.imageFilter,
            outputBounds = blurImageFilterBounds(blur.sigmaX, blur.sigmaY),
        ).also {
            WidgetParser.createWidgetForChildElement(it, element.children)
        }
    }

    companion object {
        val SIGMA_X = RequiredFloatAttrDefine("sigmaX")
        val SIGMA_Y = RequiredFloatAttrDefine("sigmaY")

        private data class BlurFilterSpec(
            val sigmaX: Float,
            val sigmaY: Float,
            val imageFilter: ImageFilter,
        )

        fun parseBlurFilter(element: Element): ImageFilter = parseBlurFilterSpec(element).imageFilter

        private fun parseBlurFilterSpec(element: Element): BlurFilterSpec {
            val sigmaX = WidgetParser.parseAttrValue(SIGMA_X, element.attrs)
            val sigmaY = WidgetParser.parseAttrValue(SIGMA_Y, element.attrs)
            require(sigmaX.isFinite() && sigmaX >= 0f) { "Attr [sigmaX] must be finite and non-negative" }
            require(sigmaY.isFinite() && sigmaY >= 0f) { "Attr [sigmaY] must be finite and non-negative" }
            return BlurFilterSpec(
                sigmaX = sigmaX,
                sigmaY = sigmaY,
                imageFilter = ImageFilter.makeBlur(
                    sigmaX = sigmaX,
                    sigmaY = sigmaY,
                    mode = WidgetParser.parseAttrValue(CommonAttrDefine.FILTER_TILE_MODE, element.attrs),
                ),
            )
        }
    }
}
