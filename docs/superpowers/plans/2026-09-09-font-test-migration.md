# 字体类测试迁移实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 把 `TextTest`(4 例)与 `TextPainterTest`(5 例)从零断言冒烟改为**字体无关**的区间/单调/关系断言,并把 `drawLocalFontListSample` 归入 `sample` 标签;同时为 `RowParserTest` 补回文本基线关系用例。

**Architecture:** 沿用 `docs/testing/README.md` §7 的策略——只断言正值/量级/单调/关系,不锁字形像素,目检靠 artifact。每条断言的期望值要么是正负/量级,要么是"随 X 单调",要么是**两端都实测**的等式;`textAlign` 用墨迹边界关系、`HeightMode` 用高度序关系。

**Tech Stack:** Kotlin、skiko(paragraph)、kotlin.test、Gradle 9.7.1、仓库内 `:testkit`。

**Spec:** `docs/superpowers/specs/2026-09-09-font-test-migration-design.md`(含全部探针实测值)

## Global Constraints

- 分支 `test/font-test-migration`,base = `main`(`8c76521`);工作目录 `D:\mine\workspace\snapshot`。
- **提交必须 GPG 签名**(`git commit -S`);偶发 `No passphrase given` **原样重试一次**;`git log -1 --format="%h %G?"` 应为 `G`。
- **测试命令统一加 `--offline`**;`git fetch` 被 SSL 阻断时用 `git -c http.version=HTTP/1.1 fetch origin main`。
- **不锁字形/像素**:所有断言必须字体无关。禁止把探针实测的绝对值当作期望值(除非是关系式的一侧,两侧同源实测)。
- `drawPainter` 只允许出现在 artifact 用例,且必须带"仅人眼检视"注释。
- 探针实测约束(违反则断言不可观测):
  - `textAlign` 必须在 **`layout(boxWidth, boxWidth)`** 下才可见;
  - `HeightMode` 必须在 `TextStyle` 带**行高倍数**(`height = 1.5f`)时才可区分。

---

### Task 1: `TextTest` 重写

**Files:**
- Modify: `core/src/test/kotlin/com/muedsa/snapshot/widget/text/TextTest.kt`

**Interfaces:**
- Consumes: `rootLayout`、`snapshotPixels`、`drawPainter`;`Text`/`RichText`/`WidgetSpan`(同包)、`Container`(`com.muedsa.snapshot.widget`)。
- Produces: 无。

- [ ] **Step 1: 用下面的内容整体替换该文件**

```kotlin
package com.muedsa.snapshot.widget.text

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.drawPainter
import com.muedsa.snapshot.paint.text.TextPainter
import com.muedsa.snapshot.paint.text.TextSpan
import com.muedsa.snapshot.paint.text.TextStyle
import com.muedsa.snapshot.rootLayout
import com.muedsa.snapshot.snapshotPixels
import com.muedsa.snapshot.widget.Container
import com.muedsa.snapshot.widget.Widget
import org.jetbrains.skia.Color
import org.jetbrains.skia.Pixmap
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * 文本渲染的区间/存在性断言。
 *
 * 文本测量受 OS 字体影响,本类**只做量级与墨迹存在性断言**,不锁字形像素;
 * 目检见 artifact 用例与 build/test-results/test-image-outputs/ 下的输出。
 */
class TextTest {

    // ---- 墨迹统计(对反走样稳健:按通道阈值而非精确色值) ----

    private fun Pixmap.countInk(predicate: (Int) -> Boolean): Int {
        var count = 0
        for (y in 0 until info.height) {
            for (x in 0 until info.width) {
                val c = getColor(x, y)
                if (((c shr 24) and 0xFF) != 0 && predicate(c)) count++
            }
        }
        return count
    }

    private fun Pixmap.countReddish(): Int = countInk { c ->
        val r = (c shr 16) and 0xFF
        val g = (c shr 8) and 0xFF
        val b = c and 0xFF
        r > g + 60 && r > b + 60
    }

    private fun Pixmap.countDark(): Int = countInk { c ->
        val r = (c shr 16) and 0xFF
        val g = (c shr 8) and 0xFF
        val b = c and 0xFF
        r < 96 && g < 96 && b < 96
    }

    private fun Pixmap.countWhiteish(): Int = countInk { c ->
        val r = (c shr 16) and 0xFF
        val g = (c shr 8) and 0xFF
        val b = c and 0xFF
        r > 200 && g > 200 && b > 200
    }

    private fun Pixmap.countBlueish(): Int = countInk { c ->
        val r = (c shr 16) and 0xFF
        val g = (c shr 8) and 0xFF
        val b = c and 0xFF
        b > r + 60 && b > g + 60
    }

    @Test
    fun simple_text_test() {
        val node = rootLayout {
            Text("Hello, world!", style = TextStyle(fontSize = 20f, color = Color.RED))
        }
        assertTrue(node.size.width > 0f, "文本宽应为正,实际 ${node.size.width}")
        assertTrue(
            node.size.height in 1f..(20f * 3f),
            "fontSize=20 的单行高应在 1..3em,实际 ${node.size.height}"
        )
        // 透明底才能区分"没画"与"画了白"
        val pixmap = snapshotPixels(background = Color.TRANSPARENT) {
            Text("Hello, world!", style = TextStyle(fontSize = 20f, color = Color.RED))
        }
        assertTrue(pixmap.countReddish() > 0, "应存在红色墨迹")
    }

    @Test
    fun text_span_test() {
        fun Widget.scene() {
            RichText {
                TextSpan(text = "Hello, one!", style = TextStyle(fontSize = 20f))
                TextSpan(text = "Hello, two!", style = TextStyle(color = Color.RED))
                TextSpan("Hello, three!")
            }
        }
        val node = rootLayout { scene() }
        assertTrue(node.size.width > 0f, "文本宽应为正,实际 ${node.size.width}")
        assertTrue(node.size.height in 1f..(20f * 3f), "单行高应在 1..3em,实际 ${node.size.height}")
        val pixmap = snapshotPixels(background = Color.TRANSPARENT) { scene() }
        // 默认色 BLACK 与红色 span 各自生效
        assertTrue(pixmap.countDark() > 0, "应存在默认色(黑)墨迹")
        assertTrue(pixmap.countReddish() > 0, "应存在红色墨迹")
    }

    @Test
    fun widget_span_test() {
        fun Widget.scene() {
            RichText {
                TextSpan("Hello, one!", style = TextStyle(fontSize = 20f))
                WidgetSpan { Container(width = 20f, height = 20f, color = Color.BLUE) }
                TextSpan {
                    TextSpan("Hello, two!")
                    WidgetSpan { Container(width = 30f, height = 30f, color = Color.BLUE) }
                    TextSpan(text = "Hello, three!", style = TextStyle(fontSize = 20f, color = Color.RED))
                }
            }
        }
        val node = rootLayout { scene() }
        assertTrue(node.size.width > 0f, "文本宽应为正,实际 ${node.size.width}")
        // 30x30 的 WidgetSpan 会参与行高,行高不应小于它
        assertTrue(node.size.height >= 30f, "行高应不小于最高的 WidgetSpan(30),实际 ${node.size.height}")
        val pixmap = snapshotPixels(background = Color.TRANSPARENT) { scene() }
        // 两个蓝块理论上共 20*20+30*30=1300 像素;实测 1202(行高裁剪+抗锯齿)。
        // 下界 800 仍可区分"只画出 20x20 盒"的 ~400 量级。
        val blue = pixmap.countBlueish()
        assertTrue(blue >= 800, "蓝色 WidgetSpan 像素数应 ≥ 800,实际 $blue")
        assertTrue(pixmap.countReddish() > 0, "应存在红色墨迹")
    }

    @Test
    fun style_merge_test() {
        fun Widget.scene() {
            RichText {
                TextSpan(style = TextStyle(fontSize = 15f, color = Color.WHITE)) {
                    TextSpan("15white")
                    TextSpan(style = TextStyle(fontSize = 30f)) {
                        TextSpan("30white")
                        TextSpan("30red", style = TextStyle(color = Color.RED))
                    }
                    TextSpan("15white")
                }
            }
        }
        val node = rootLayout { scene() }
        assertTrue(node.size.width > 0f, "文本宽应为正,实际 ${node.size.width}")
        assertTrue(node.size.height in 1f..(30f * 3f), "单行高应在 1..3em(最大字号 30),实际 ${node.size.height}")
        val pixmap = snapshotPixels(background = Color.TRANSPARENT) { scene() }
        // 父级 WHITE 被子级 RED 覆盖、字号 15→30 覆盖,两种墨迹都应出现
        assertTrue(pixmap.countWhiteish() > 0, "应存在白色墨迹(父级样式生效)")
        assertTrue(pixmap.countReddish() > 0, "应存在红色墨迹(子级覆盖生效)")
    }

    @Test
    fun text_samples_artifact() {
        // 仅人眼检视:文本渲染不进 golden,量级断言无法反映字形/排版观感
        val painter = TextPainter(
            text = TextSpan("Hello, world! 你好,世界!", style = TextStyle(fontSize = 30f))
        ).apply { layout(0f, Float.POSITIVE_INFINITY) }
        drawPainter("widget/text/text_samples", width = painter.width, height = painter.height) { canvas ->
            painter.paint(canvas, offset = Offset.ZERO)
        }
    }
}
```

- [ ] **Step 2: 运行**

Run: `./gradlew :core:test --offline --console=plain --tests "*TextTest*" 2>&1 | tail -8`
Expected: BUILD SUCCESSFUL,5 个用例通过(`image_emoji_test` 已被 `network` 标签排除)。

> 若某条墨迹断言失败:先打印实际计数判断是阈值问题还是渲染问题,**不得为了变绿而把断言删成恒真**;必要时按实测下调阈值并注释。

- [ ] **Step 3: 提交**

```bash
git add core/src/test/kotlin/com/muedsa/snapshot/widget/text/TextTest.kt
git commit -S -m "test(core): TextTest 重写为量级/墨迹断言(字体无关)"
```

---

### Task 2: `TextPainterTest` 重写

**Files:**
- Modify: `core/src/test/kotlin/com/muedsa/snapshot/paint/TextPainterTest.kt`

**Interfaces:**
- Consumes: `painterPixels`、`drawPainter`、`TextPainter`。
- Produces: 无。

- [ ] **Step 1: 用下面的内容整体替换该文件**

```kotlin
package com.muedsa.snapshot.paint

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.drawPainter
import com.muedsa.snapshot.painterPixels
import com.muedsa.snapshot.paint.text.TextPainter
import com.muedsa.snapshot.paint.text.TextSpan
import com.muedsa.snapshot.paint.text.TextStyle
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

    private companion object {
        const val BOX_WIDTH = 300f
    }

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

    private fun Pixmap.hasInk(): Boolean = inkBoundsX() != null

    // 对齐可见的前提:minWidth 与 maxWidth 都给,段落才被撑到 BOX_WIDTH(实测;只给 maxWidth 时六种对齐墨迹相同)
    private fun alignedPainter(textAlign: Alignment, direction: Direction = Direction.LTR): TextPainter =
        TextPainter(
            text = TextSpan(text = "Hello Word!"),
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

        // 左对齐贴左边;右对齐贴右边;居中的墨迹中心落在画布中点(均为关系断言,与字形宽度无关)
        assertTrue(left.first <= 2, "LEFT 的左边界应贴近 0,实际 ${left.first}")
        assertTrue(right.last >= BOX_WIDTH.toInt() - 2, "RIGHT 的右边界应贴近 $BOX_WIDTH,实际 ${right.last}")
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
        assertTrue(alignedPainter(Alignment.START).inkX().first <= 2, "LTR 下 START 应等价 LEFT")
        assertTrue(
            alignedPainter(Alignment.END).inkX().last >= BOX_WIDTH.toInt() - 2,
            "LTR 下 END 应等价 RIGHT"
        )
    }

    @Test
    fun textAlign_rtl_test() {
        val start = alignedPainter(Alignment.START, Direction.RTL).inkX()
        val end = alignedPainter(Alignment.END, Direction.RTL).inkX()
        // RTL 下 START 贴右、END 贴左(与 LTR 相反)
        assertTrue(start.last >= BOX_WIDTH.toInt() - 2, "RTL 下 START 应贴右,实际 ${start.last}")
        assertTrue(end.first <= 2, "RTL 下 END 应贴左,实际 ${end.first}")
    }

    @Test
    fun heightModel_test() {
        // 高度模式只有在带行高倍数时才可区分(实测:默认倍数下四档高度相同)
        fun heightOf(mode: HeightMode): Float = TextPainter(
            text = TextSpan(text = "Line one\nLine two", style = TextStyle(fontSize = 50f, height = 1.5f)),
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
        val painter = TextPainter(
            text = TextSpan(text = "🥰💀✌️🌴", style = TextStyle(fontSize = 30f))
        ).apply { layout(0f, 600f) }
        assertTrue(painter.width > 0f, "emoji 文本宽应为正,实际 ${painter.width}")
        assertTrue(painter.height > 0f, "emoji 文本高应为正,实际 ${painter.height}")
        val pixmap = painterPixels(
            painter.width.coerceAtLeast(1f),
            painter.height.coerceAtLeast(1f),
            background = Color.TRANSPARENT
        ) { painter.paint(it, Offset.ZERO) }
        assertTrue(pixmap.hasInk(), "emoji 应渲染出墨迹(若本机缺 emoji 字体则此断言不成立,需按实测调整并注释)")
    }

    @Test
    fun font_size_monotonic_test() {
        // 字号 5..40 递增 → maxIntrinsicWidth 严格递增、height 非降(与字形无关的强单调)
        var prevWidth = -1f
        var prevHeight = -1f
        for (fontSize in 5..40) {
            val painter = TextPainter(
                text = TextSpan(text = "[$fontSize] Hello Word!", style = TextStyle(fontSize = fontSize.toFloat()))
            ).apply { layout(0f, Float.POSITIVE_INFINITY) }
            assertTrue(
                painter.maxIntrinsicWidth > prevWidth,
                "fontSize=$fontSize 的 maxIntrinsicWidth(${painter.maxIntrinsicWidth}) 应大于上一档($prevWidth)"
            )
            assertTrue(
                painter.height >= prevHeight,
                "fontSize=$fontSize 的 height(${painter.height}) 应不小于上一档($prevHeight)"
            )
            prevWidth = painter.maxIntrinsicWidth
            prevHeight = painter.height
        }
    }

    @Test
    fun cn_font_size_monotonic_test() {
        // 中文同理;若本机缺中文字体渲染成 tofu,单调性仍成立
        var prevWidth = -1f
        for (fontSize in 5..40) {
            val painter = TextPainter(
                text = TextSpan(
                    text = "[$fontSize] 你好，世界！",
                    style = TextStyle(fontSize = fontSize.toFloat(), fontFamilies = listOf("Noto Sans SC"))
                )
            ).apply { layout(0f, Float.POSITIVE_INFINITY) }
            assertTrue(
                painter.maxIntrinsicWidth > prevWidth,
                "fontSize=$fontSize 的中文 maxIntrinsicWidth(${painter.maxIntrinsicWidth}) 应大于上一档($prevWidth)"
            )
            prevWidth = painter.maxIntrinsicWidth
        }
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
        const val SAMPLE_TEXT_EN = "Hello Word!"
        const val SAMPLE_TEXT_CN = "你好，世界！"
        const val SAMPLE_TEXT = "$SAMPLE_TEXT_EN $SAMPLE_TEXT_CN"
    }
}
```

- [ ] **Step 2: 运行**

Run: `./gradlew :core:test --offline --console=plain --tests "*TextPainterTest*" 2>&1 | tail -8`
Expected: BUILD SUCCESSFUL,6 个用例通过(`drawLocalFontListSample` 被 `sample` 标签排除)。

- [ ] **Step 3: 提交**

```bash
git add core/src/test/kotlin/com/muedsa/snapshot/paint/TextPainterTest.kt
git commit -S -m "test(core): TextPainterTest 重写为关系/单调断言(字体无关)"
```

---

### Task 3: `RowParserTest` 补基线关系用例

**Files:**
- Modify: `core/src/test/kotlin/com/muedsa/snapshot/widget/RowParserTest.kt`

**Interfaces:**
- Consumes: `rootLayout`、`assertApproxEq`、`TextPainter`、`RichText`。

- [ ] **Step 1: 加 import**

在现有 import 区补:

```kotlin
import com.muedsa.snapshot.paint.text.TextPainter
import com.muedsa.snapshot.paint.text.TextSpan
import com.muedsa.snapshot.paint.text.TextStyle
import com.muedsa.snapshot.widget.text.RichText
import kotlin.math.abs
import kotlin.test.assertTrue
```

- [ ] **Step 2: 追加用例与私有辅助**

在类末尾(最后一个 `}` 之前)追加:

```kotlin
    // 用同一 API 独立测量基线距离,供基线对齐断言使用(两端同源实测,与字体无关)
    private fun baselineOf(text: String, fontSize: Float): Float =
        TextPainter(text = TextSpan(text, style = TextStyle(fontSize = fontSize)))
            .apply { layout(0f, Float.POSITIVE_INFINITY) }
            .computeDistanceToActualBaseline(BaselineMode.ALPHABETIC)

    @Test
    fun cross_axis_baseline_text_aligns_by_baseline() {
        // 两段不同字号的文本按 BASELINE 对齐:ascent 更大者顶边更高、高度更大,且基线真正对齐。
        val root = rootLayout {
            Row(
                crossAxisAlignment = CrossAxisAlignment.BASELINE,
                textDirection = Direction.LTR,
                textBaseline = BaselineMode.ALPHABETIC
            ) {
                RichText { TextSpan("Hello", style = TextStyle(fontSize = 20f)) }
                RichText { TextSpan("Hello", style = TextStyle(fontSize = 40f)) }
            }
        }
        val small = root.children[0].rect
        val large = root.children[1].rect

        assertTrue(large.top < small.top, "大字号顶边应更高,实际 large=${large.top} small=${small.top}")
        assertTrue(large.height > small.height, "大字号高度应更大,实际 large=${large.height} small=${small.height}")
        // 排除退化为 START/END:两者 top 与 bottom 都不相等
        assertTrue(abs(large.top - small.top) > EPS, "top 不应相等(否则退化为 START)")
        assertTrue(abs(large.bottom - small.bottom) > EPS, "bottom 不应相等(否则退化为 END)")

        // 基线真对齐:两端都实测
        val b20 = baselineOf("Hello", 20f)
        val b40 = baselineOf("Hello", 40f)
        assertApproxEq(small.top + b20, large.top + b40, 0.5f)
    }
```

- [ ] **Step 3: 运行**

Run: `./gradlew :core:test --offline --console=plain --tests "*RowParserTest*" 2>&1 | tail -8`
Expected: BUILD SUCCESSFUL,用例数由 7 增至 8。

- [ ] **Step 4: 提交**

```bash
git add core/src/test/kotlin/com/muedsa/snapshot/widget/RowParserTest.kt
git commit -S -m "test(core): RowParserTest 补文本基线对齐关系用例"
```

---

### Task 4: 全量验证

**Files:** 无改动(仅验证)

- [ ] **Step 1: 全量测试**

Run: `./gradlew test --offline --rerun --console=plain 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL。

Run:
```bash
for m in core parser; do grep -ho 'tests="[0-9]*" skipped="[0-9]*" failures="[0-9]*" errors="[0-9]*"' $m/build/test-results/test/*.xml | awk -F'"' -v M=$m '{t+=$2; f+=$6; e+=$8} END {print M" tests="t, "failures="f, "errors="e}'; done
```
Expected: failures=0、errors=0;core 用例数比本批前增加(TextTest 4 + TextPainterTest 6 + RowParserTest 1)。

- [ ] **Step 2: 打包**

Run: `./gradlew jar --offline --console=plain 2>&1 | tail -3`
Expected: BUILD SUCCESSFUL。

- [ ] **Step 3: 静态核对**

Run:
```bash
grep -n "println\|drawWidget" core/src/test/kotlin/com/muedsa/snapshot/widget/text/TextTest.kt core/src/test/kotlin/com/muedsa/snapshot/paint/TextPainterTest.kt core/src/test/kotlin/com/muedsa/snapshot/widget/RowParserTest.kt
```
Expected: 无匹配(退出码 1)。

Run:
```bash
grep -n "drawPainter" core/src/test/kotlin/com/muedsa/snapshot/widget/text/TextTest.kt core/src/test/kotlin/com/muedsa/snapshot/paint/TextPainterTest.kt
```
Expected: 仅出现在 artifact 用例(`text_samples_artifact`、`drawLocalFontListSample`、`text_painter_artifact`)及其上方的"仅人眼检视"注释。

Run: `git status --short`
Expected: 无输出。

- [ ] **Step 4: 汇报**

汇总:提交清单、用例数变化、任何为通过而调整过的阈值(须说明理由)。

---

## 收尾(不在任务内,由主会话执行)

- 更新记忆:`migrate-widget-smoke-batch1`(字体批完成)与 `snapshot-testkit-refactor`。
- 推送分支并备好 PR 文案(标题建议 `test(core): 字体类测试迁移(区间/单调/关系断言 + artifact)`);PR 文案只写评审所需信息。
