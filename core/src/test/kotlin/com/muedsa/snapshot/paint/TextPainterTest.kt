package com.muedsa.snapshot.paint

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.drawPainter
import com.muedsa.snapshot.painterPixels
import com.muedsa.snapshot.paint.text.TextPainter
import com.muedsa.snapshot.paint.text.TextSpan
import com.muedsa.snapshot.paint.text.TextStyle
import com.muedsa.snapshot.testTypeface
import org.jetbrains.skia.Color
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.Pixmap
import org.jetbrains.skia.paragraph.Alignment
import org.jetbrains.skia.paragraph.Direction
import org.jetbrains.skia.paragraph.HeightMode
import org.junit.jupiter.api.Tag
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * 文本排版的区间/单调/关系断言。
 *
 * 文本测量受 OS 字体影响,本类**不锁具体字形/像素**:对齐用墨迹边界的关系、高度模式用高度序关系、
 * 字号用单调性。目检见 artifact 用例。
 */
class TextPainterTest {

    /** 扫描非透明像素,返回最左/最右列;无墨迹返回 null。 */
    private fun Pixmap.inkBoundsX(): IntRange? {
        var minX = Int.MAX_VALUE
        var maxX = -1
        for (y in 0 until info.height) {
            for (x in 0 until info.width) {
                if (((getColor(x, y) shr 24) and 0xFF) != 0) {
                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                }
            }
        }
        return if (maxX < 0) null else minX..maxX
    }

    // 对齐可见的前提:minWidth 与 maxWidth 都给,段落才被撑到 BOX_WIDTH
    // (实测:只给 maxWidth 时段落宽等于内容宽,六种对齐的墨迹完全相同)
    private fun alignedPainter(textAlign: Alignment, direction: Direction = Direction.LTR): TextPainter =
        TextPainter(
            text = TextSpan(text = "Hello Word!", style = TextStyle(typeface = testTypeface)),
            textAlign = textAlign,
            textDirection = direction
        ).apply { layout(BOX_WIDTH, BOX_WIDTH) }

    private fun TextPainter.inkX(): IntRange =
        painterPixels(BOX_WIDTH, height, background = Color.TRANSPARENT) { paint(it, Offset.ZERO) }
            .inkBoundsX()!!

    @Test
    fun textAlign_ltr_test() {
        val left = alignedPainter(Alignment.LEFT).inkX()
        val center = alignedPainter(Alignment.CENTER).inkX()
        val right = alignedPainter(Alignment.RIGHT).inkX()

        // 左对齐贴左边;右对齐贴右边;居中的墨迹中心落在画布中点
        // 容差 4px:内置字体实测 LEFT=1、RIGHT=298(即左侧留白 1、右侧留白 2),留 2px 余量
        assertTrue(left.first <= 4, "LEFT 的左边界应贴近 0,实际 ${left.first}")
        assertTrue(right.last >= BOX_WIDTH.toInt() - 4, "RIGHT 的右边界应贴近 $BOX_WIDTH,实际 ${right.last}")
        val centerMid = (center.first + center.last) / 2f
        assertTrue(
            abs(centerMid - BOX_WIDTH / 2f) <= 2f,
            "CENTER 的墨迹中心应≈${BOX_WIDTH / 2f},实际 $centerMid"
        )
        // 单调:左边界随对齐左→中→右递增
        assertTrue(
            left.first < center.first && center.first < right.first,
            "左边界应随 LEFT<CENTER<RIGHT 递增,实际 ${left.first}/${center.first}/${right.first}"
        )
        // START/END 在 LTR 下分别等价于 LEFT/RIGHT
        assertTrue(alignedPainter(Alignment.START).inkX().first <= 4, "LTR 下 START 应等价 LEFT")
        assertTrue(
            alignedPainter(Alignment.END).inkX().last >= BOX_WIDTH.toInt() - 4,
            "LTR 下 END 应等价 RIGHT"
        )
    }

    @Test
    fun textAlign_rtl_test() {
        val start = alignedPainter(Alignment.START, Direction.RTL).inkX()
        val end = alignedPainter(Alignment.END, Direction.RTL).inkX()
        // RTL 下 START 贴右、END 贴左(与 LTR 相反)
        assertTrue(start.last >= BOX_WIDTH.toInt() - 4, "RTL 下 START 应贴右,实际 ${start.last}")
        assertTrue(end.first <= 4, "RTL 下 END 应贴左,实际 ${end.first}")
    }

    @Test
    fun heightModel_test() {
        // 高度模式只有在带行高倍数时才可区分(实测:默认倍数下四档高度相同)
        fun heightOf(mode: HeightMode): Float = TextPainter(
            text = TextSpan(
                text = "Line one\nLine two",
                style = TextStyle(fontSize = 50f, height = 1.5f, typeface = testTypeface)
            ),
            textHeightMode = mode
        ).apply { layout(0f, Float.POSITIVE_INFINITY) }.height

        val heights = HeightMode.entries.associateWith { heightOf(it) }
        heights.forEach { (mode, h) ->
            assertTrue(h > 0f, "$mode 的高度应为正,实际 $h")
        }
        val all = heights.getValue(HeightMode.ALL)
        val disableAll = heights.getValue(HeightMode.DISABLE_ALL)
        // ALL 保留首行 ascent 与末行 descent → 最大;DISABLE_ALL 两者都去掉 → 最小
        assertTrue(all > disableAll, "ALL($all) 应严格大于 DISABLE_ALL($disableAll)")
        assertTrue(heights.values.all { all >= it }, "ALL 应不小于其余档位,实际 $heights")
        assertTrue(heights.values.all { disableAll <= it }, "DISABLE_ALL 应不大于其余档位,实际 $heights")
    }

    @Test
    fun emoji_test() {
        // emoji 字形不在内置字体(Noto Sans SC)覆盖范围内,渲染依赖 OS emoji 字体,
        // 故只断言"可布局、尺寸为正";渲染效果见 artifact。此处刻意不断言墨迹存在——
        // 无 emoji 字体的机器上会失败,而那不是本仓库的缺陷。
        val painter = TextPainter(
            text = TextSpan(text = "🥰💀✌️🌴", style = TextStyle(fontSize = 30f))
        ).apply { layout(0f, 600f) }
        assertTrue(painter.width > 0f, "emoji 文本宽应为正,实际 ${painter.width}")
        assertTrue(painter.height > 0f, "emoji 文本高应为正,实际 ${painter.height}")
    }

    private fun intrinsicWidthOf(text: String, fontSize: Float): Float =
        TextPainter(text = TextSpan(text = text, style = TextStyle(fontSize = fontSize, typeface = testTypeface)))
            .apply { layout(0f, Float.POSITIVE_INFINITY) }
            .maxIntrinsicWidth

    private fun textHeightOf(text: String, fontSize: Float): Float =
        TextPainter(text = TextSpan(text = text, style = TextStyle(fontSize = fontSize, typeface = testTypeface)))
            .apply { layout(0f, Float.POSITIVE_INFINITY) }
            .height

    // 逐档"严格递增"不是文本度量的真实不变量:实测 Linux 上 skia 把字形推进量化到整数像素,
    // fontSize=28/29 的中文 maxIntrinsicWidth 同为 169.0(本机为小数且逐档递增)。
    // 真实不变量是"随字号非降 + 端点严格递增"——它同样能抓住"宽度不随字号增长"的缺陷。
    private fun assertNonDecreasing(values: List<Float>, label: String) {
        values.zipWithNext().forEachIndexed { index, (prev, next) ->
            assertTrue(
                next >= prev,
                "$label 应随字号非降,但 fontSize=${index + 6}($next) 小于 fontSize=${index + 5}($prev)"
            )
        }
    }

    @Test
    fun font_size_monotonic_test() {
        val sizes = 5..40
        val widths = sizes.map { intrinsicWidthOf("[$it] Hello Word!", it.toFloat()) }
        val heights = sizes.map { textHeightOf("[$it] Hello Word!", it.toFloat()) }
        assertNonDecreasing(widths, "maxIntrinsicWidth")
        assertNonDecreasing(heights, "height")
        assertTrue(
            widths.last() > widths.first(),
            "最大字号的宽度(${widths.last()})应大于最小字号(${widths.first()})"
        )
    }

    @Test
    fun cn_font_size_monotonic_test() {
        val sizes = 5..40
        val widths = sizes.map { intrinsicWidthOf("[$it] 你好，世界！", it.toFloat()) }
        assertNonDecreasing(widths, "中文 maxIntrinsicWidth")
        assertTrue(
            widths.last() > widths.first(),
            "最大字号的中文宽度(${widths.last()})应大于最小字号(${widths.first()})"
        )
    }

    @Tag("sample")
    @Test
    fun drawLocalFontListSample() {
        // 本机字体清单:内容与耗时随本机字体数变化,故归入 sample 标签(默认不跑,需 -PincludeSamples)
        val padding = 10f
        var width = 0f
        var height = 0f
        val offsetArr = Array(FontMgr.default.familiesCount) { Offset.ZERO }
        val painterArr: Array<TextPainter> = Array(FontMgr.default.familiesCount) {
            val familyName = FontMgr.default.getFamilyName(it)
            TextPainter(
                text = TextSpan(
                    text = "$SAMPLE_TEXT ($familyName)",
                    style = TextStyle(color = Color.BLACK, fontFamilies = listOf(familyName))
                )
            ).apply {
                offsetArr[it] = Offset(padding, height)
                layout(0f, Float.POSITIVE_INFINITY)
                width = maxOf(width, this@apply.width + padding * 2)
                height += this@apply.height + padding
            }
        }
        drawPainter("paint/text/fonts", width, height) { canvas ->
            painterArr.forEachIndexed { index, painter ->
                painter.paint(canvas, offset = offsetArr[index])
            }
        }
    }

    @Test
    fun text_painter_artifact() {
        // 仅人眼检视:对齐与高度模式的观感无法由量级断言反映
        val painter = alignedPainter(Alignment.CENTER)
        drawPainter("paint/text/text_painter_samples", width = BOX_WIDTH, height = painter.height) { canvas ->
            painter.paint(canvas, offset = Offset.ZERO)
            painter.debugPaint(canvas, offset = Offset.ZERO)
        }
    }

    companion object {
        const val BOX_WIDTH = 300f
        const val SAMPLE_TEXT_EN = "Hello Word!"
        const val SAMPLE_TEXT_CN = "你好，世界！"
        const val SAMPLE_TEXT = "$SAMPLE_TEXT_EN $SAMPLE_TEXT_CN"
    }
}
