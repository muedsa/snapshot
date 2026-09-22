package com.muedsa.snapshot.parser.widget

import com.muedsa.geometry.Radius
import com.muedsa.snapshot.paint.decoration.Border
import com.muedsa.snapshot.paint.decoration.BorderRadius
import com.muedsa.snapshot.paint.decoration.BorderSide
import com.muedsa.snapshot.paint.decoration.BoxDecoration
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

            return BoxDecoration(
                color = color,
                border = Border(
                    left = borderLeft,
                    top = borderTop,
                    right = borderRight,
                    bottom = borderBottom
                ),
                borderRadius = parseBorderRadius(element, prefix),
                boxShadow = WidgetParser.parseAttrValue(
                    CommonAttrDefine.BOX_SHADOW_N.copyWith(
                        prefixedName(prefix, CommonAttrDefine.BOX_SHADOW_N.name)
                    ),
                    element.attrs
                )
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

        fun isNullBorder(boxDecoration: BoxDecoration): Boolean {
            var flag = when (val border = boxDecoration.border) {
                null -> true
                is Border -> border.left == BorderSide.NONE
                        && border.top == BorderSide.NONE
                        && border.right == BorderSide.NONE
                        && border.bottom == BorderSide.NONE

                else -> false
            }
            val borderRadius = boxDecoration.borderRadius
            flag = if (borderRadius == null) flag
            else (flag && borderRadius.topLeft == Radius.ZERO
                    && borderRadius.topRight == Radius.ZERO
                    && borderRadius.bottomLeft == Radius.ZERO
                    && borderRadius.bottomRight == Radius.ZERO)
            flag = flag && boxDecoration.boxShadow.isNullOrEmpty()
            return flag
        }
    }
}
