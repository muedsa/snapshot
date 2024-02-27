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

    companion object {

        fun parseBorderDecoration(element: Element): BoxDecoration {
            val defaultBorder = WidgetParser.parseAttrValue(CommonAttrDefine.BORDER_N, element.attrs) ?: BorderSide.NONE
            val borderLeft = WidgetParser.parseAttrValue(CommonAttrDefine.BORDER_LEFT_N, element.attrs) ?: defaultBorder
            val borderTop = WidgetParser.parseAttrValue(CommonAttrDefine.BORDER_TOP_N, element.attrs) ?: defaultBorder
            val borderRight =
                WidgetParser.parseAttrValue(CommonAttrDefine.BORDER_RIGHT_N, element.attrs) ?: defaultBorder
            val borderBottom =
                WidgetParser.parseAttrValue(CommonAttrDefine.BORDER_BOTTOM_N, element.attrs) ?: defaultBorder

            val defaultBorderRadius =
                WidgetParser.parseAttrValue(CommonAttrDefine.BORDER_RADIUS_N, element.attrs) ?: Radius.ZERO
            val borderRadiusTopLeft =
                WidgetParser.parseAttrValue(CommonAttrDefine.BORDER_RADIUS_TOP_LEFT_N, element.attrs)
                    ?: defaultBorderRadius
            val borderRadiusTopRight =
                WidgetParser.parseAttrValue(CommonAttrDefine.BORDER_RADIUS_TOP_RIGHT_N, element.attrs)
                    ?: defaultBorderRadius
            val borderRadiusBottomLeft =
                WidgetParser.parseAttrValue(CommonAttrDefine.BORDER_RADIUS_BOTTOM_LEFT_N, element.attrs)
                    ?: defaultBorderRadius
            val borderRadiusBottomRight =
                WidgetParser.parseAttrValue(CommonAttrDefine.BORDER_RADIUS_BOTTOM_RIGHT_N, element.attrs)
                    ?: defaultBorderRadius
            return BoxDecoration(
                border = Border(
                    left = borderLeft,
                    top = borderTop,
                    right = borderRight,
                    bottom = borderBottom
                ),
                borderRadius = BorderRadius(
                    topLeft = borderRadiusTopLeft,
                    topRight = borderRadiusTopRight,
                    bottomLeft = borderRadiusBottomLeft,
                    bottomRight = borderRadiusBottomRight
                ),
                boxShadow = WidgetParser.parseAttrValue(CommonAttrDefine.BOX_SHADOW_N, element.attrs)
            )
        }

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
            flag = flag && !boxDecoration.boxShadow.isNullOrEmpty()
            return flag
        }
    }
}