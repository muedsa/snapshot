package com.muedsa.snapshot.parser.widget

import com.muedsa.geometry.Radius
import com.muedsa.snapshot.paint.decoration.Border
import com.muedsa.snapshot.paint.decoration.BorderRadius
import com.muedsa.snapshot.paint.decoration.BorderSide
import com.muedsa.snapshot.paint.decoration.BoxDecoration
import com.muedsa.snapshot.paint.decoration.BoxShape
import com.muedsa.snapshot.parser.ContainerMode
import com.muedsa.snapshot.parser.Element
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.widget.DecoratedBox
import com.muedsa.snapshot.widget.Widget

open class BorderParser : WidgetParser {

    override val id: String = "Border"

    override val containerMode: ContainerMode = ContainerMode.SINGLE

    override fun buildWidget(element: Element): Widget =
        DecoratedBox(decoration = parseBorderDecoration(element))
            .also {
                WidgetParser.createWidgetForChildElement(it, element.children)
            }

    companion object {

        fun parseBorderDecoration(element: Element, prefix: String = ""): BoxDecoration {
            val color = WidgetParser.parseAttrValue(
                CommonAttrDefine.COLOR_N.copyWith(prefixedName(prefix, CommonAttrDefine.COLOR_N.name)),
                element.attrs
            )
            val defaultBorder = WidgetParser.parseAttrValue(
                CommonAttrDefine.BORDER_N.copyWith(prefixedName(prefix, CommonAttrDefine.BORDER_N.name)),
                element.attrs
            ) ?: BorderSide.NONE
            val borderLeft = WidgetParser.parseAttrValue(
                CommonAttrDefine.BORDER_LEFT_N.copyWith(prefixedName(prefix, CommonAttrDefine.BORDER_LEFT_N.name)),
                element.attrs
            ) ?: defaultBorder
            val borderTop = WidgetParser.parseAttrValue(
                CommonAttrDefine.BORDER_TOP_N.copyWith(prefixedName(prefix, CommonAttrDefine.BORDER_TOP_N.name)),
                element.attrs
            ) ?: defaultBorder
            val borderRight =
                WidgetParser.parseAttrValue(
                    CommonAttrDefine.BORDER_RIGHT_N.copyWith(
                        prefixedName(prefix, CommonAttrDefine.BORDER_RIGHT_N.name)
                    ),
                    element.attrs
                ) ?: defaultBorder
            val borderBottom =
                WidgetParser.parseAttrValue(
                    CommonAttrDefine.BORDER_BOTTOM_N.copyWith(
                        prefixedName(prefix, CommonAttrDefine.BORDER_BOTTOM_N.name)
                    ),
                    element.attrs
                ) ?: defaultBorder
            val shape = WidgetParser.parseAttrValue(
                CommonAttrDefine.BOX_SHAPE.copyWith(prefixedName(prefix, CommonAttrDefine.BOX_SHAPE.name)),
                element.attrs
            )
            val borderRadius = parseBorderRadius(element, prefix)
            require(shape != BoxShape.CIRCLE || borderRadius == BorderRadius.ZERO) {
                "Attr [${prefixedName(prefix, CommonAttrDefine.BORDER_RADIUS_N.name)}] " +
                        "can not be used with shape CIRCLE"
            }

            return BoxDecoration(
                color = color,
                border = Border(
                    left = borderLeft,
                    top = borderTop,
                    right = borderRight,
                    bottom = borderBottom
                ),
                borderRadius = if (shape == BoxShape.CIRCLE) null else borderRadius,
                boxShadow = WidgetParser.parseAttrValue(
                    CommonAttrDefine.BOX_SHADOW_N.copyWith(
                        prefixedName(prefix, CommonAttrDefine.BOX_SHADOW_N.name)
                    ),
                    element.attrs
                ),
                gradient = GradientParser.parseGradient(element, prefix),
                backgroundBlendMode = WidgetParser.parseAttrValue(
                    CommonAttrDefine.BACKGROUND_BLEND_MODE_N.copyWith(
                        prefixedName(prefix, CommonAttrDefine.BACKGROUND_BLEND_MODE_N.name)
                    ),
                    element.attrs
                ),
                shape = shape,
            )
        }

        fun parseBorderRadius(element: Element, prefix: String = ""): BorderRadius {
            val defaultRadius =
                WidgetParser.parseAttrValue(
                    CommonAttrDefine.BORDER_RADIUS_N.copyWith(
                        prefixedName(prefix, CommonAttrDefine.BORDER_RADIUS_N.name)
                    ),
                    element.attrs
                ) ?: Radius.ZERO
            return BorderRadius(
                topLeft = WidgetParser.parseAttrValue(
                    CommonAttrDefine.BORDER_RADIUS_TOP_LEFT_N.copyWith(
                        prefixedName(prefix, CommonAttrDefine.BORDER_RADIUS_TOP_LEFT_N.name)
                    ),
                    element.attrs
                )
                    ?: defaultRadius,
                topRight = WidgetParser.parseAttrValue(
                    CommonAttrDefine.BORDER_RADIUS_TOP_RIGHT_N.copyWith(
                        prefixedName(prefix, CommonAttrDefine.BORDER_RADIUS_TOP_RIGHT_N.name)
                    ),
                    element.attrs
                )
                    ?: defaultRadius,
                bottomLeft = WidgetParser.parseAttrValue(
                    CommonAttrDefine.BORDER_RADIUS_BOTTOM_LEFT_N.copyWith(
                        prefixedName(prefix, CommonAttrDefine.BORDER_RADIUS_BOTTOM_LEFT_N.name)
                    ),
                    element.attrs
                )
                    ?: defaultRadius,
                bottomRight = WidgetParser.parseAttrValue(
                    CommonAttrDefine.BORDER_RADIUS_BOTTOM_RIGHT_N.copyWith(
                        prefixedName(prefix, CommonAttrDefine.BORDER_RADIUS_BOTTOM_RIGHT_N.name)
                    ),
                    element.attrs
                )
                    ?: defaultRadius,
            )
        }

        private fun prefixedName(prefix: String, name: String): String =
            if (prefix.isEmpty()) name else prefix + name.replaceFirstChar { it.uppercaseChar() }

        /** 是否包含无法仅通过 [com.muedsa.snapshot.widget.Container.color] 表达的装饰。 */
        fun requiresBoxDecoration(boxDecoration: BoxDecoration): Boolean {
            val hasBorder = when (val border = boxDecoration.border) {
                null -> false
                is Border -> border.left != BorderSide.NONE
                        || border.top != BorderSide.NONE
                        || border.right != BorderSide.NONE
                        || border.bottom != BorderSide.NONE

                else -> true
            }
            val hasBorderRadius = boxDecoration.borderRadius?.let { it != BorderRadius.ZERO } ?: false
            return hasBorder ||
                    hasBorderRadius ||
                    !boxDecoration.boxShadow.isNullOrEmpty() ||
                    boxDecoration.image != null ||
                    boxDecoration.gradient != null ||
                    boxDecoration.backgroundBlendMode != null ||
                    boxDecoration.shape != BoxShape.RECTANGLE
        }

        @Deprecated("请改用 requiresBoxDecoration", ReplaceWith("!requiresBoxDecoration(boxDecoration)"))
        fun isNullBorder(boxDecoration: BoxDecoration): Boolean = !requiresBoxDecoration(boxDecoration)
    }
}
