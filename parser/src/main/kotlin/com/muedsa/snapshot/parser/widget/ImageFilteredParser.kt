package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.parser.attr.required.RequiredFloatAttrDefine
import com.muedsa.snapshot.widget.ImageFiltered
import com.muedsa.snapshot.widget.Widget
import org.jetbrains.skia.ImageFilter

open class ImageFilteredParser : WidgetParser {

    override val id: String = "ImageFiltered"

    override val containerMode: ContainerMode = ContainerMode.SINGLE

    override fun buildWidget(element: Element): Widget =
        ImageFiltered(
            imageFilter = parseBlurFilter(element),
        ).also {
            WidgetParser.createWidgetForChildElement(it, element.children)
        }

    companion object {
        val SIGMA_X = RequiredFloatAttrDefine("sigmaX")
        val SIGMA_Y = RequiredFloatAttrDefine("sigmaY")

        fun parseBlurFilter(element: Element): ImageFilter {
            val sigmaX = WidgetParser.parseAttrValue(SIGMA_X, element.attrs)
            val sigmaY = WidgetParser.parseAttrValue(SIGMA_Y, element.attrs)
            require(sigmaX.isFinite() && sigmaX >= 0f) { "Attr [sigmaX] must be finite and non-negative" }
            require(sigmaY.isFinite() && sigmaY >= 0f) { "Attr [sigmaY] must be finite and non-negative" }
            return ImageFilter.makeBlur(
                sigmaX = sigmaX,
                sigmaY = sigmaY,
                mode = WidgetParser.parseAttrValue(CommonAttrDefine.FILTER_TILE_MODE, element.attrs),
            )
        }
    }
}
