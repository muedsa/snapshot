package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.parser.attr.StringAttrDefine
import com.muedsa.snapshot.parser.attr.required.AttrDefine
import com.muedsa.snapshot.parser.image.DataUriImageDecoder
import com.muedsa.snapshot.widget.CachedNetworkImage
import com.muedsa.snapshot.widget.RawImage
import com.muedsa.snapshot.widget.Widget
import org.jetbrains.skia.Image

open class ImageParser : WidgetParser {

    override val id: String = "Image"

    override val containerMode: ContainerMode = ContainerMode.NONE

    override fun buildWidget(element: Element): Widget {
        val urlAttr = element.attrs[CommonAttrDefine.URL.name]
        val dataUriAttr = element.attrs[DATA_URI_ATTR_NAME]
        if (urlAttr == null && dataUriAttr == null) {
            throw ParseException(element.pos.copy(), "Tag [Image] requires either [url] or [dataUri]")
        }
        if (urlAttr != null && dataUriAttr != null) {
            throw ParseException(dataUriAttr.nameStartPos.copy(), "Tag [Image] cannot use [url] and [dataUri] together")
        }
        val noCacheAttr = element.attrs[CommonAttrDefine.NO_CACHE.name]
        if (dataUriAttr != null && noCacheAttr != null) {
            throw ParseException(noCacheAttr.nameStartPos.copy(), "Attr [noCache] is only available with [url]")
        }

        val width = WidgetParser.parseAttrValue(CommonAttrDefine.WIDTH_N, element.attrs)
        val height = WidgetParser.parseAttrValue(CommonAttrDefine.HEIGHT_N, element.attrs)
        val fit = WidgetParser.parseAttrValue(CommonAttrDefine.FIT_N, element.attrs)
        val alignment = WidgetParser.parseAttrValue(CommonAttrDefine.ALIGNMENT, element.attrs)
        val repeat = WidgetParser.parseAttrValue(CommonAttrDefine.REPEAT, element.attrs)
        val scale = WidgetParser.parseAttrValue(CommonAttrDefine.SCALE, element.attrs)
        val opacity = WidgetParser.parseAttrValue(CommonAttrDefine.OPACITY, element.attrs)
        val color = WidgetParser.parseAttrValue(CommonAttrDefine.COLOR_N, element.attrs)
        val colorBlendMode = WidgetParser.parseAttrValue(ATTR_COLOR_BLEND_MODE, element.attrs)

        return if (urlAttr != null) {
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
                image = WidgetParser.parseAttrValue(
                    DataUriImageAttrDefine(element.owner!!.dataUriImageDecoder),
                    element.attrs,
                ),
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
        const val DATA_URI_ATTR_NAME = "dataUri"
        val ATTR_COLOR_BLEND_MODE = CommonAttrDefine.BLEND_MODE_N.copyWith("colorBlendMode")
    }
}

private class DataUriImageAttrDefine(
    private val decoder: DataUriImageDecoder,
    name: String = ImageParser.DATA_URI_ATTR_NAME,
) : AttrDefine<Image>(name) {

    override fun parseValue(valueStr: String?): Image {
        val dataUri = StringAttrDefine.onlyCheck(this, valueStr)
        return decoder.decode(dataUri)
    }

    override fun copyWith(name: String): AttrDefine<Image> = DataUriImageAttrDefine(decoder, name)
}
