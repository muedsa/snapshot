package com.muedsa.snapshot.parser.attr.nullable

import com.muedsa.snapshot.parser.attr.AttributeValueFormats
import com.muedsa.snapshot.parser.attr.DefaultValueAttrDefine

/** SVG 坐标窗口；保留原点与宽高，避免用矩形端点相减损失精度。 */
class NullableViewBoxAttrDefine(name: String) :
    DefaultValueAttrDefine<NullableViewBoxAttrDefine.ViewBox?>(name, null) {

    data class ViewBox(val minX: Float, val minY: Float, val width: Float, val height: Float)

    override val valueInfo get() = AttributeValueFormats.VIEW_BOX

    override fun parseValue(valueStr: String?): ViewBox {
        requireNotNull(valueStr) { "Attr [$name] value can not be null" }
        val match = FORMAT.matchEntire(valueStr)
        requireNotNull(match) { "Attr [$name] must contain minX minY width height" }
        val values = match.groupValues.drop(1).map { value ->
            value.toFloat().also {
                require(it.isFinite()) { "Attr [$name] values must be finite" }
            }
        }
        require(values[2] > 0f && values[3] > 0f) { "Attr [$name] width and height must be positive" }
        return ViewBox(values[0], values[1], values[2], values[3])
    }

    override fun copyWith(name: String, defaultValue: ViewBox?): NullableViewBoxAttrDefine =
        NullableViewBoxAttrDefine(name)

    companion object {
        private const val NUMBER = "[+-]?(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)(?:[eE][+-]?[0-9]+)?"
        private const val SPACE = "[ \\t\\r\\n]"
        private const val SEPARATOR = "(?:$SPACE+|$SPACE*,$SPACE*)"
        private val FORMAT = Regex("$SPACE*($NUMBER)$SEPARATOR($NUMBER)$SEPARATOR($NUMBER)$SEPARATOR($NUMBER)$SPACE*")
    }
}
