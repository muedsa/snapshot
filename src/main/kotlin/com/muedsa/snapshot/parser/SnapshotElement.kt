package com.muedsa.snapshot.parser

import com.muedsa.snapshot.drawRenderBox
import com.muedsa.snapshot.parser.attr.BooleanAttrDefine
import com.muedsa.snapshot.parser.attr.ColorAttrDefine
import com.muedsa.snapshot.parser.attr.StringAttrDefine
import com.muedsa.snapshot.parser.token.RawAttr
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.tools.NetworkImageCache
import com.muedsa.snapshot.tools.SimpleLimitedNetworkImageCache
import com.muedsa.snapshot.widget.Widget
import org.jetbrains.skia.Color
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Surface
import kotlin.math.ceil

class SnapshotElement(
    attrs: MutableMap<String, RawAttr>,
    pos: TrackPos,
) : Element(tag = Tag.SNAPSHOT, attrs = attrs, pos = pos) {

    val type: String = Tag.parseAttrValue(ATTR_TYPE, attrs)
    val format = when (type) {
        "png" -> EncodedImageFormat.PNG
        "jpg" -> EncodedImageFormat.JPEG
        "webp" -> EncodedImageFormat.WEBP
        else -> EncodedImageFormat.PNG
    }
    val background: Int = Tag.parseAttrValue(ATTR_BACKGROUND, attrs)
    val debug: Boolean = Tag.parseAttrValue(ATTR_DEBUG, attrs)

    private var _networkImageCache: NetworkImageCache? = null

    @Synchronized
    fun getNetworkImageCache(): NetworkImageCache {
        if (_networkImageCache == null) {
            _networkImageCache = NETWORK_IMAGE_CACHE_BUILDER.invoke(this)
        }
        return _networkImageCache!!
    }

    override fun createWidget(): Widget {
        check(children.isNotEmpty()) { "element [Snapshot] content is empty" }
        return children[0].createWidget()
    }

    fun snapshot(): ByteArray {
        val widget = createWidget()
        val rootRenderBox = widget.createRenderBox()
        rootRenderBox.layout(constraints = BoxConstraints())
        val rootSize = rootRenderBox.definiteSize
        if (rootSize.isEmpty) {
            throw IllegalArgumentException("Layout size is empty")
        }
        if (rootSize.isInfinite) {
            throw IllegalArgumentException("Layout size is infinite")
        }
        val surface = Surface.makeRasterN32Premul(
            width = ceil(rootSize.width).toInt(),
            height = ceil(rootSize.height).toInt()
        )
        surface.canvas.drawRenderBox(renderBox = rootRenderBox, background = background, debug = debug)
        surface.flushAndSubmit()
        return surface.makeImageSnapshot().encodeToData(format = format)!!.bytes
    }

    companion object {
        val ATTR_BACKGROUND: ColorAttrDefine = ColorAttrDefine(name = "background", defaultValue = Color.TRANSPARENT)
        val ATTR_TYPE: StringAttrDefine = StringAttrDefine(name = "type", defaultValue = "png") { _, valueStr ->
            require(valueStr == "png" || valueStr == "jpg" || valueStr == "webp") {
                "Attr [type] value must be one of 'png' 'jpg' 'webp', but get '${valueStr}'"
            }
        }
        val ATTR_DEBUG: BooleanAttrDefine = BooleanAttrDefine(name = "debug", defaultValue = false)

        var MAX_IMAGE_NUM: Int = 10
        var MAX_SINGLE_IMAGE_SIZE: Int = 5 * 1024 * 1024
        var NETWORK_IMAGE_CACHE_BUILDER: (SnapshotElement) -> NetworkImageCache = {
            SimpleLimitedNetworkImageCache(
                maxImageNum = MAX_IMAGE_NUM,
                maxSingleImageSize = MAX_SINGLE_IMAGE_SIZE
            )
        }
    }
}