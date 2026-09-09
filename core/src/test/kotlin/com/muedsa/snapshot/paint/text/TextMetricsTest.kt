package com.muedsa.snapshot.paint.text

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.drawPainter
import com.muedsa.snapshot.testTypeface
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * 文本度量区间断言。
 *
 * 文本测量受 OS 字体/排版引擎影响,故本套测试刻意使用保守区间,只验证量级与单调关系,
 * 不锁具体字形/像素;请勿按某台机器量到的绝对值收紧区间(易跨 OS 闪断)。
 * 文本渲染不进 golden,如需目检请看 artifact:
 * core/build/test-results/test-image-outputs/paint/text/text_metrics.png
 */
class TextMetricsTest {

    private fun layoutLine(text: String): TextPainter = TextPainter(
        text = TextSpan(text = text, style = TextStyle(fontSize = 20f, typeface = testTypeface))
    ).apply {
        layout(0f, Float.POSITIVE_INFINITY)
    }

    @Test
    fun single_line_metrics_are_positive_and_bounded() {
        val painter = layoutLine("Hello Word!")
        assertTrue(painter.width > 0f, "单行文本宽度应为正,实际 ${painter.width}")
        // fontSize=20 → 上界取 3em,仅宽松量级校验,非字形精确值
        assertTrue(painter.height in 1f..(20f * 3f), "fontSize=20 的单行高度应在合理区间,实际 ${painter.height}")
        assertTrue(
            painter.maxIntrinsicWidth >= painter.width,
            "maxIntrinsicWidth(${painter.maxIntrinsicWidth}) 应不小于 width(${painter.width})"
        )
    }

    @Test
    fun multiline_height_grows_with_line_count() {
        val single = layoutLine("line one")
        val multi = TextPainter(
            text = TextSpan(
                text = "line one\nline two\nline three",
                style = TextStyle(fontSize = 20f, typeface = testTypeface)
            )
        ).apply {
            layout(0f, 400f)
        }
        assertTrue(multi.height > single.height, "多行高(${multi.height})应大于单行高(${single.height})")
        assertTrue(multi.width > 0f, "多行文本宽度应为正,实际 ${multi.width}")
        assertTrue(multi.width <= 400f, "多行宽(${multi.width})应受 layout 宽度上限 400f 约束")
    }

    @Test
    fun artifact_for_eyeball_review() {
        val painter = layoutLine("Hello Word! 你好,世界!")
        drawPainter("paint/text/text_metrics", width = painter.width, height = painter.height) { canvas ->
            // drawPainter 默认已 clear 白底,这里只绘制文本
            painter.paint(canvas, offset = Offset.ZERO)
        }
    }
}
