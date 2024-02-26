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
    val FONT_STYLE = FontStyleAttrDefine("fontFamily", defaultValue = FontStyle.NORMAL)

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

    // 必须的
    val URL = RequiredStringAttrDefine("url") { _, valueStr ->
        // 简单校验
        URL(valueStr)
    }
    val TEXT = RequiredStringAttrDefine("text")
}