package com.muedsa.snapshot.parser.widget

import com.muedsa.geometry.Size
import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.attr.AttributeValueInfo
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.parser.attr.StringAttrDefine
import com.muedsa.snapshot.parser.attr.nullable.NullableViewBoxAttrDefine
import com.muedsa.snapshot.parser.attr.nullable.NullableViewBoxAttrDefine.ViewBox
import com.muedsa.snapshot.parser.attr.required.RequiredSvgPathAttrDefine
import com.muedsa.snapshot.widget.ClipPath
import com.muedsa.snapshot.widget.Widget
import kotlin.math.min
import org.jetbrains.skia.Matrix33
import org.jetbrains.skia.PathBuilder
import org.jetbrains.skia.PathFillMode

open class ClipPathParser : WidgetParser {

    override val id: String = "ClipPath"

    override val containerMode: ContainerMode = ContainerMode.SINGLE

    override fun buildWidget(element: Element): Widget {
        val viewBox = WidgetParser.parseAttrValue(VIEW_BOX, element.attrs)
        val fit = WidgetParser.parseAttrValue(FIT, element.attrs)
        val fillRule = WidgetParser.parseAttrValue(FILL_RULE, element.attrs)
        val clipBehavior = WidgetParser.parseAttrValue(CommonAttrDefine.CLIP_BEHAVIOR, element.attrs)
        // 创建 Widget 时解析一次；布局只负责把路径映射到实际尺寸。
        val path = WidgetParser.parseAttrValue(PATH, element.attrs).apply {
            fillMode = if (fillRule == "EVEN_ODD") PathFillMode.EVEN_ODD else PathFillMode.WINDING
        }
        return ClipPath(
            clipper = { size ->
                if (viewBox == null) path else PathBuilder(path).use {
                    it.transform(viewBox.transformTo(size, fit)).detach()
                }
            },
            clipBehavior = clipBehavior,
        ).also {
            WidgetParser.createWidgetForChildElement(it, element.children)
        }
    }

    companion object {
        val PATH = RequiredSvgPathAttrDefine("path")
        val VIEW_BOX = NullableViewBoxAttrDefine("viewBox")
        val FIT = StringAttrDefine("fit", "FILL", AttributeValueInfo.choices(listOf("FILL", "CONTAIN"))) { attr, value ->
            require(value == "FILL" || value == "CONTAIN") { "Attr [${attr.name}] must be FILL or CONTAIN" }
        }
        val FILL_RULE = StringAttrDefine("fillRule", "NON_ZERO", AttributeValueInfo.choices(listOf("NON_ZERO", "EVEN_ODD"))) { attr, value ->
            require(value == "NON_ZERO" || value == "EVEN_ODD") { "Attr [${attr.name}] must be NON_ZERO or EVEN_ODD" }
        }

        private fun ViewBox.transformTo(size: Size, fit: String): Matrix33 {
            // 中间值用 Double，避免先缩放原点时发生不必要的 Float 溢出。
            var scaleX = size.width.toDouble() / width
            var scaleY = size.height.toDouble() / height
            if (fit == "CONTAIN") {
                val scale = min(scaleX, scaleY)
                scaleX = scale
                scaleY = scale
            }
            val translateX = (size.width - width * scaleX) / 2 - minX * scaleX
            val translateY = (size.height - height * scaleY) / 2 - minY * scaleY
            val values = listOf(scaleX, scaleY, translateX, translateY).map { it.toFloat() }
            require(values.all { it.isFinite() }) { "ClipPath viewBox cannot be mapped to size $size" }
            return Matrix33(values[0], 0f, values[2], 0f, values[1], values[3], 0f, 0f, 1f)
        }
    }
}
