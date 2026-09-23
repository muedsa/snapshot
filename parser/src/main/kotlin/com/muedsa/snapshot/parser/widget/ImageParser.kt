package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.widget.CachedNetworkImage
import com.muedsa.snapshot.widget.RawImage
import com.muedsa.snapshot.widget.Widget

open class ImageParser : WidgetParser {

    override val id: String = "Image"

    override val containerMode: ContainerMode = ContainerMode.NONE

    override fun buildWidget(element: Element): Widget {
        val source = element.imageSource()

        val width = WidgetParser.parseAttrValue(CommonAttrDefine.WIDTH_N, element.attrs)
        val height = WidgetParser.parseAttrValue(CommonAttrDefine.HEIGHT_N, element.attrs)
        val fit = WidgetParser.parseAttrValue(CommonAttrDefine.FIT_N, element.attrs)
        val alignment = WidgetParser.parseAttrValue(CommonAttrDefine.ALIGNMENT, element.attrs)
        val repeat = WidgetParser.parseAttrValue(CommonAttrDefine.REPEAT, element.attrs)
        val scale = WidgetParser.parseAttrValue(CommonAttrDefine.SCALE, element.attrs)
        val opacity = WidgetParser.parseAttrValue(CommonAttrDefine.OPACITY, element.attrs)
        val color = WidgetParser.parseAttrValue(CommonAttrDefine.COLOR_N, element.attrs)
        val colorBlendMode = WidgetParser.parseAttrValue(ATTR_COLOR_BLEND_MODE, element.attrs)

        return if (source == ImageSource.URL) {
            CachedNetworkImage(
                url = WidgetParser.parseAttrValue(CommonAttrDefine.URL, element.attrs),
                width = width,
                height = height,
                fit = fit,
                alignment = alignment,
                repeat = repeat,
                scale = scale,
                opacity = opacity,
                color = color,
                colorBlendMode = colorBlendMode,
                noCache = WidgetParser.parseAttrValue(CommonAttrDefine.NO_CACHE, element.attrs),
                cache = element.owner!!.getNetworkImageCache(),
            )
        } else {
            RawImage(
                image = element.decodeDataUriImage(),
                width = width,
                height = height,
                fit = fit,
                alignment = alignment,
                repeat = repeat,
                scale = scale,
                opacity = opacity,
                color = color,
                colorBlendMode = colorBlendMode,
            )
        }
    }

    companion object {
        val ATTR_COLOR_BLEND_MODE = CommonAttrDefine.BLEND_MODE_N.copyWith("colorBlendMode")
    }
}
