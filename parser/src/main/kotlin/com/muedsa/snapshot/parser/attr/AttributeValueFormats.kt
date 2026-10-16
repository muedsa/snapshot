package com.muedsa.snapshot.parser.attr

import com.muedsa.snapshot.material.ELEVATION_MAP
import com.muedsa.snapshot.paint.decoration.BorderStyle
import com.muedsa.snapshot.parser.attr.nullable.TextDecorationLine
import org.jetbrains.skia.FilterBlurMode

/** 可复用的填写说明，供普通、必填和可空属性共用。 */
internal object AttributeValueFormats {
    val FLOAT = AttributeValueInfo("浮点数；具体取值范围由所在组件约束", listOf("12.5", "100"))
    val INT = AttributeValueInfo("32 位整数；具体取值范围由所在组件约束", listOf("0", "3"))
    val STRING = AttributeValueInfo("字符串；使用引号包裹含空白的值", listOf("hello"))
    val BOOLEAN = AttributeValueInfo(
        "布尔值；true 不区分大小写，其余已赋值文本按 false 处理（兼容现有规则）",
        listOf("true", "false"),
    )
    val COLOR = AttributeValueInfo(
        "CSS 颜色：#RGB、#RGBA、#RRGGBB、#RRGGBBAA（透明度在末尾）、颜色名、transparent、rgb()/rgba()/hsl()/hsla()",
        listOf("#33669980", "red", "rgb(255 0 0 / 50%)"),
    )
    val ALIGNMENT = AttributeValueInfo(
        "对齐关键字或 (x,y)；坐标为十进制数，元组内不含空白",
        listOf("CENTER", "(-1,0)"),
        listOf("TOP_LEFT", "TOP_CENTER", "TOP_RIGHT", "CENTER_LEFT", "CENTER", "CENTER_RIGHT", "BOTTOM_LEFT", "BOTTOM_CENTER", "BOTTOM_RIGHT"),
    )
    val EDGE_INSETS = AttributeValueInfo(
        "十进制数，或 (垂直,水平)，或 (左,上,右,下)；元组内不含空白",
        listOf("8", "(8,16)", "(1,2,3,4)"),
    )
    val RADIUS = AttributeValueInfo("圆半径或 (水平半径,垂直半径)；十进制数，元组内不含空白", listOf("8", "(8,16)"))
    val OFFSET = AttributeValueInfo("(x,y)；两个有限十进制数，元组内不含空白", listOf("(4,-2)"))
    val MATRIX = AttributeValueInfo(
        "(m0,m1,…,m15)；16 个有限浮点数，列优先排列，逗号分隔且不含空白",
        listOf("(1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1)"),
    )
    val FONT_STYLE = AttributeValueInfo.choices(listOf("NORMAL", "BOLD", "ITALIC", "BOLD_ITALIC"))
    val BORDER = AttributeValueInfo(
        "宽度 样式 颜色；空白分隔，样式区分大小写，颜色使用 CSS 格式",
        listOf("1 SOLID #336699", "2 SOLID rgb(255,0,0)"),
        BorderStyle.entries.map { it.name },
    )
    val BOX_SHADOW = AttributeValueInfo(
        "ELEVATION_n（n 可选 ${ELEVATION_MAP.keys.joinToString()}），或 offsetX offsetY [blurRadius [spreadRadius]] [颜色] [模糊样式]；多个阴影用逗号分隔",
        listOf("ELEVATION_2", "0 2 4 0 #00000080 NORMAL"),
        FilterBlurMode.entries.map { it.name },
    )
    val TEXT_SHADOW = AttributeValueInfo(
        "NONE 或 offsetX offsetY [blurSigma] [颜色]；偏移须有限，blurSigma 须有限且非负，多个阴影用逗号分隔",
        listOf("NONE", "1 2 3 #00000080"),
    )
    val TEXT_DECORATION = AttributeValueInfo(
        "逗号分隔的文本装饰，不可重复；NONE 必须单独使用",
        listOf("NONE", "UNDERLINE,LINE_THROUGH"),
        listOf("NONE") + TextDecorationLine.entries.map { it.name },
    )
    val FLOAT_LIST = AttributeValueInfo("逗号分隔的有限浮点数列表", listOf("0,0.5,1"))
    val COLOR_LIST = AttributeValueInfo("逗号分隔的 CSS 颜色列表，至少两个颜色", listOf("#ff0000,#0000ff", "rgb(255,0,0),blue"))
    val FONT_FEATURES = AttributeValueInfo(
        "NONE 或空白分隔的 OpenType 特性：四字符标签、可选 + / - 前缀、[起点:终点] 范围及 =整数；范围起点不得大于终点",
        listOf("NONE", "kern -liga", "liga[0:5]=0"),
    )
    val VIEW_BOX = AttributeValueInfo(
        "minX minY width height；空白或逗号分隔的 4 个有限数，width 和 height 必须大于 0",
        listOf("0 0 100 100", "0,0,100,100"),
    )
    val SVG_PATH = AttributeValueInfo("SVG path 的 d 路径数据，非完整 SVG 文档；坐标必须有限", listOf("M0 0 L100 0 L50 100 Z"))
    val URL = AttributeValueInfo("包含协议的 URL；具体协议支持由图片缓存实现决定", listOf("https://example.com/image.png"))
    val DATA_URI = AttributeValueInfo(
        "data:image/<类型>;base64,<Base64 图片数据>；默认解码器支持 png、jpeg、webp，自定义解码器自行定义支持范围",
        listOf("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII="),
    )
}
