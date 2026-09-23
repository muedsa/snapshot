package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.parser.attr.StringAttrDefine
import com.muedsa.snapshot.parser.attr.required.AttrDefine
import com.muedsa.snapshot.parser.image.DataUriImageDecoder
import org.jetbrains.skia.Image

internal const val DATA_URI_ATTR_NAME = "dataUri"

internal enum class ImageSource { URL, DATA_URI }

internal fun Element.imageSource(): ImageSource {
    val urlAttr = attrs[CommonAttrDefine.URL.name]
    val dataUriAttr = attrs[DATA_URI_ATTR_NAME]
    if (urlAttr == null && dataUriAttr == null) {
        throw ParseException(pos.copy(), "Tag [${widgetParser.id}] requires either [url] or [dataUri]")
    }
    if (urlAttr != null && dataUriAttr != null) {
        throw ParseException(dataUriAttr.nameStartPos.copy(), "Tag [${widgetParser.id}] cannot use [url] and [dataUri] together")
    }
    val noCacheAttr = attrs[CommonAttrDefine.NO_CACHE.name]
    if (dataUriAttr != null && noCacheAttr != null) {
        throw ParseException(noCacheAttr.nameStartPos.copy(), "Attr [noCache] is only available with [url]")
    }
    return if (urlAttr != null) ImageSource.URL else ImageSource.DATA_URI
}

internal fun Element.decodeDataUriImage(): Image = WidgetParser.parseAttrValue(
    DataUriImageAttrDefine(owner!!.dataUriImageDecoder),
    attrs,
)

private class DataUriImageAttrDefine(
    private val decoder: DataUriImageDecoder,
    name: String = DATA_URI_ATTR_NAME,
) : AttrDefine<Image>(name) {

    override fun parseValue(valueStr: String?): Image {
        val dataUri = StringAttrDefine.onlyCheck(this, valueStr)
        return decoder.decode(dataUri)
    }

    override fun copyWith(name: String): AttrDefine<Image> = DataUriImageAttrDefine(decoder, name)
}
