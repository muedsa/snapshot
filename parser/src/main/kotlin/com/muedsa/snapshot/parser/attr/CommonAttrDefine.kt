package com.muedsa.snapshot.parser.attr

import com.muedsa.geometry.BoxAlignment
import com.muedsa.snapshot.paint.ImageRepeat
import com.muedsa.snapshot.parser.attr.nullable.*
import com.muedsa.snapshot.parser.attr.required.RequiredStringAttrDefine
import com.muedsa.snapshot.rendering.flex.CrossAxisAlignment
import com.muedsa.snapshot.rendering.flex.MainAxisAlignment
import com.muedsa.snapshot.rendering.flex.MainAxisSize
import com.muedsa.snapshot.rendering.flex.VerticalDirection
import org.jetbrains.skia.Color
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.paragraph.BaselineMode
import org.jetbrains.skia.paragraph.Direction
import org.jetbrains.skia.paragraph.PlaceholderAlignment
import java.net.URL

object CommonAttrDefine {

    val MIN_WIDTH: FloatAttrDefine = FloatAttrDefine(name = "minWidth", defaultValue = 0f)
    val MAX_WIDTH: FloatAttrDefine = FloatAttrDefine(name = "maxWidth", defaultValue = Float.POSITIVE_INFINITY)
    val MIN_HEIGHT: FloatAttrDefine = FloatAttrDefine(name = "minHeight", defaultValue = 0f)
    val MAX_HEIGHT: FloatAttrDefine = FloatAttrDefine(name = "maxHeight", defaultValue = Float.POSITIVE_INFINITY)

    val MAIN_AXIS_ALIGNMENT: MainAxisAlignmentAttrDefine = MainAxisAlignmentAttrDefine(
        name = "mainAxisAlignment",
        defaultValue = MainAxisAlignment.START
    )
    val MAIN_AXIS_SIZE: MainAxisSizeAttrDefine = MainAxisSizeAttrDefine(
        name = "mainAxisSize",
        defaultValue = MainAxisSize.MAX
    )
    val CROSS_AXIS_ALIGNMENT: CrossAxisAlignmentAttrDefine = CrossAxisAlignmentAttrDefine(
        name = "crossAxisAlignment",
        defaultValue = CrossAxisAlignment.CENTER
    )
    val DIRECTION: DirectionAttrDefine = DirectionAttrDefine(
        name = "direction",
        defaultValue = Direction.LTR
    )
    val VERTICAL_DIRECTION: VerticalDirectionAttrDefine = VerticalDirectionAttrDefine(
        name = "verticalDirection",
        defaultValue = VerticalDirection.DOWN
    )
    val BASELINE: BaselineAttrDefine = BaselineAttrDefine(
        name = "baseline",
        defaultValue = BaselineMode.ALPHABETIC
    )
    val ALIGNMENT: AlignmentAttrDefine = AlignmentAttrDefine(name = "alignment", defaultValue = BoxAlignment.CENTER)
    val REPEAT: ImageRepeatAttrDefine = ImageRepeatAttrDefine(name = "repeat", defaultValue = ImageRepeat.NO_REPEAT)
    val SCALE: FloatAttrDefine = FloatAttrDefine(name = "scale", defaultValue = 1f)
    val OPACITY: FloatAttrDefine = FloatAttrDefine(name = "opacity", defaultValue = 1f)
    val COLOR: ColorAttrDefine = ColorAttrDefine(name = "color", defaultValue = Color.BLACK)
    val FONT_SIZE = FloatAttrDefine(name = "fontSize", defaultValue = 14f)
    val FONT_STYLE = FontStyleAttrDefine("fontStyle", defaultValue = FontStyle.NORMAL)
    val PLACEHOLDER_ALIGNMENT: PlaceholderAlignmentAttrDefine = PlaceholderAlignmentAttrDefine(name = "alignment", defaultValue = PlaceholderAlignment.BASELINE)
    val RAW = BooleanAttrDefine(name = "raw", defaultValue = false)
    val NO_CACHE = BooleanAttrDefine(name = "noCache", defaultValue = false)
    // _N 后缀表示可以为空的

    val ALIGNMENT_N: NullableAlignmentAttrDefine = NullableAlignmentAttrDefine(name = "alignment")
    val PADDING_N: NullableEdgeInsetsAttrDefine = NullableEdgeInsetsAttrDefine("padding")
    val COLOR_N: NullableColorAttrDefine = NullableColorAttrDefine(name = "color")
    val WIDTH_N: NullableFloatAttrDefine = NullableFloatAttrDefine(name = "width")
    val HEIGHT_N: NullableFloatAttrDefine = NullableFloatAttrDefine(name = "height")
    val LEFT_N: NullableFloatAttrDefine = NullableFloatAttrDefine(name = "left")
    val TOP_N: NullableFloatAttrDefine = NullableFloatAttrDefine(name = "top")
    val RIGHT_N: NullableFloatAttrDefine = NullableFloatAttrDefine(name = "right")
    val BOTTOM_N: NullableFloatAttrDefine = NullableFloatAttrDefine(name = "bottom")
    val MARGIN_N: NullableEdgeInsetsAttrDefine = NullableEdgeInsetsAttrDefine("margin")
    val BASELINE_N: NullableBaselineAttrDefine = NullableBaselineAttrDefine("baseline")
    val FIT_N: NullableBoxFitAttrDefine = NullableBoxFitAttrDefine("fit")
    val BLEND_MODE_N: NullableBlendModeAttrDefine = NullableBlendModeAttrDefine("blendMode")
    val FONT_FAMILY_N = NullableStringAttrDefine(name = "fontFamily")
    val BORDER_N: NullableBorderSideAttrDefine = NullableBorderSideAttrDefine("border")
    val BORDER_LEFT_N: NullableBorderSideAttrDefine = NullableBorderSideAttrDefine("borderLeft")
    val BORDER_TOP_N: NullableBorderSideAttrDefine = NullableBorderSideAttrDefine("borderTop")
    val BORDER_RIGHT_N: NullableBorderSideAttrDefine = NullableBorderSideAttrDefine("borderRight")
    val BORDER_BOTTOM_N: NullableBorderSideAttrDefine = NullableBorderSideAttrDefine("borderBottom")
    val BORDER_RADIUS_N: NullableRadiusAttrDefine = NullableRadiusAttrDefine("borderRadius")
    val BORDER_RADIUS_TOP_LEFT_N: NullableRadiusAttrDefine = NullableRadiusAttrDefine("borderRadiusTopLeft")
    val BORDER_RADIUS_TOP_RIGHT_N: NullableRadiusAttrDefine = NullableRadiusAttrDefine("borderRadiusTopRight")
    val BORDER_RADIUS_BOTTOM_LEFT_N: NullableRadiusAttrDefine = NullableRadiusAttrDefine("borderRadiusBottomLeft")
    val BORDER_RADIUS_BOTTOM_RIGHT_N: NullableRadiusAttrDefine = NullableRadiusAttrDefine("borderRadiusBottomRight")
    val BOX_SHADOW_N: NullableBoxShadowAttrDefine = NullableBoxShadowAttrDefine("boxShadow")
    val TEXT_N: NullableStringAttrDefine = NullableStringAttrDefine("text")
    val FONT_SIZE_N = NullableFloatAttrDefine(name = "fontSize")
    val FONT_STYLE_N = NullableFontStyleAttrDefine("fontStyle")

    // 必须的
    val URL = RequiredStringAttrDefine("url") { _, valueStr ->
        // 简单校验
        URL(valueStr)
    }
    val TEXT = RequiredStringAttrDefine("text")
}