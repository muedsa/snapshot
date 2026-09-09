# 旧渲染冒烟迁移 Batch-1c 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 把 `ContainerParserTest`/`StackParserTest`/`RowParserTest`/`ColorFilteredTest` 四个零断言的旧冒烟测试重写为布局/采样/语义断言(Stack 对齐矩阵用不变量,ColorFiltered 用语义色值)。

**Architecture:** 复用 testkit 的 `rootLayout`/`assertSize`/`assertApproxEq`/`snapshotPixels`/`expectColorAt`/`expectRegionTransparent`/`golden`;Stack 的 51 个场景不硬编坐标,而是断言**方向语义不变量**(镜像/方向无关),另加 3 档代表值 + 3 张 golden;Row 逐档断言实测偏移;ColorFiltered 换本地几何后用 `MODULATE`/`SATURATION` 的数学语义断言。

**Tech Stack:** Kotlin, skiko, kotlin.test, Gradle 9.7.1, 仓库内 `:testkit` 模块。

**Spec:** `docs/superpowers/specs/2026-09-09-migrate-widget-smoke-1c-design.md`(含全部探针实测值)

## Global Constraints

- 分支 `test/migrate-widget-smoke-1c`,base = `origin/main`(`68907d2`);工作目录 `D:\mine\workspace\snapshot`。
- **提交必须 GPG 签名**(`git commit -S`);偶发 `gpg: signing failed: No passphrase given` **原样重试一次即成功**。提交后 `git log -1 --format="%h %G?"` 应为 `G`。
- **测试命令统一加 `--offline`**:本机到 `maven.pkg.jetbrains.space` 的 TLS 握手失败,`:core` 的 `skiko-awt:0.0.0-SNAPSHOT` 只能走本地缓存。若遇 `AccessDenied` 再加 `--no-build-cache`。
- 测试框架只用 **kotlin.test**;四文件内**禁止**出现 `println` / `drawWidget` / `drawPainter` / `org.junit` / 网络 import。
- **本计划中所有坐标与色值均为探针实测值**(见 spec)。测试失败时**不得改动期望值去迁就**,先判定是实现 bug 还是理解偏差;清晰 bug → 最小修复并记录,模糊 → 注释"待议"。
- golden 基准与测试**同一次提交**;先 `record` 生成再 `verify` 跑绿。
- **本批是"测试重写",没有实现步骤**:每个 Task 的循环是"写测试 → 运行 → 期望通过(或先 record golden 再 verify)→ 提交"。`Step 2` 里若失败,按上一条处置。

---

### Task 1: `ContainerParserTest` 重写

**Files:**
- Modify: `core/src/test/kotlin/com/muedsa/snapshot/widget/ContainerParserTest.kt`(整文件替换)

**Interfaces:**
- Consumes: `com.muedsa.snapshot.rootLayout`、`LayoutNode.assertSize`、`snapshotPixels`、`expectRegionTransparent`、`golden`。
- Produces: golden 基准 `core/src/test/resources/golden/widget/container/sized.png`。

- [ ] **Step 1: 用下面的内容整体替换该文件**

```kotlin
package com.muedsa.snapshot.widget

import com.muedsa.snapshot.assertSize
import com.muedsa.snapshot.expectRegionTransparent
import com.muedsa.snapshot.golden
import com.muedsa.snapshot.rootLayout
import com.muedsa.snapshot.snapshotPixels
import org.jetbrains.skia.Color
import org.jetbrains.skia.Rect
import kotlin.test.Test

class ContainerParserTest {

    // 无 color/decoration 的 Container:只参与布局、不绘制任何像素(实测整幅透明)。
    private fun Widget.sizedContainer() {
        Container(width = 300f, height = 300f)
    }

    @Test
    fun sized_test() {
        rootLayout { sizedContainer() }.assertSize(300f, 300f)

        // 背景用 TRANSPARENT,才能区分"没画"与"画了白色"
        val pixmap = snapshotPixels(background = Color.TRANSPARENT) { sizedContainer() }
        expectRegionTransparent(pixmap, Rect.makeXYWH(0f, 0f, 300f, 300f))
    }

    @Test
    fun sized_golden() {
        // 空白基准:白底 300x300,任何多余的绘制都会导致失配
        golden("widget/container/sized") { sizedContainer() }
    }
}
```

- [ ] **Step 2: 运行**

Run: `./gradlew :core:test --tests "*ContainerParserTest*" --offline`
Expected: 首次 **FAIL**——`sized_golden` 报基准不存在(verify 模式)。

- [ ] **Step 3: 录制基准并复跑**

Run: `./gradlew :core:test --tests "*ContainerParserTest*" --offline -PsnapshotTest.mode=record`
Expected: BUILD SUCCESSFUL,生成 `core/src/test/resources/golden/widget/container/sized.png`。

Run: `./gradlew :core:test --tests "*ContainerParserTest*" --offline`
Expected: BUILD SUCCESSFUL(verify 通过)。

- [ ] **Step 4: 提交**

```bash
git add core/src/test/kotlin/com/muedsa/snapshot/widget/ContainerParserTest.kt core/src/test/resources/golden/widget/container/sized.png
git commit -S -m "test(core): ContainerParserTest 重写(布局尺寸 + 透明区域断言)"
```

---

### Task 2: `StackParserTest` 重写

**Files:**
- Modify: `core/src/test/kotlin/com/muedsa/snapshot/widget/StackParserTest.kt`(整文件替换)

**Interfaces:**
- Consumes: `rootLayout`、`LayoutNode.assertSize`/`assertGlobalRect`、`assertApproxEq`、`golden`。
- Produces: golden 基准 `golden/widget/stack/alignment_{top_start,center,bottom_end}.png`。

**背景(实测):** 三个子盒 200×80 / 50×150 / 100×100,`Stack`(LOOSE,无外部约束)尺寸恒为 **200×150**;17 档里 11 档与 `textDirection` 无关、6 档在 RTL 下是 LTR 的水平镜像。

- [ ] **Step 1: 用下面的内容整体替换该文件**

```kotlin
package com.muedsa.snapshot.widget

import com.muedsa.geometry.AlignmentDirectional
import com.muedsa.geometry.AlignmentGeometry
import com.muedsa.geometry.BoxAlignment
import com.muedsa.snapshot.assertApproxEq
import com.muedsa.snapshot.assertGlobalRect
import com.muedsa.snapshot.assertSize
import com.muedsa.snapshot.golden
import com.muedsa.snapshot.rendering.LayoutNode
import com.muedsa.snapshot.rootLayout
import org.jetbrains.skia.Color
import org.jetbrains.skia.paragraph.Direction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StackParserTest {

    private companion object {
        const val STACK_WIDTH = 200f
        const val STACK_HEIGHT = 150f
        const val EPS = 0.01f
    }

    // 三个半透明色块;Stack(LOOSE)尺寸由最大子盒决定 = 200x150(实测)。
    private fun Widget.stackScene(alignment: AlignmentGeometry, direction: Direction) {
        Stack(alignment = alignment, textDirection = direction) {
            Container(width = 200f, height = 80f, color = Color.withA(Color.RED, 128))
            Container(width = 50f, height = 150f, color = Color.withA(Color.YELLOW, 128))
            Container(width = 100f, height = 100f, color = Color.withA(Color.GREEN, 128))
        }
    }

    private fun layoutOf(alignment: AlignmentGeometry, direction: Direction): LayoutNode =
        rootLayout { stackScene(alignment, direction) }

    // 方向无关组(11):start == 0 的 AlignmentDirectional 三档 + AlignmentDirectional.CENTER + BoxAlignment 八档
    private val directionIndependent = listOf(
        AlignmentDirectional.TOP_CENTER, AlignmentDirectional.CENTER, AlignmentDirectional.BOTTOM_CENTER,
        BoxAlignment.TOP_CENTER, BoxAlignment.TOP_RIGHT,
        BoxAlignment.CENTER_LEFT, BoxAlignment.CENTER, BoxAlignment.CENTER_RIGHT,
        BoxAlignment.BOTTOM_LEFT, BoxAlignment.BOTTOM_CENTER, BoxAlignment.BOTTOM_RIGHT,
    )

    // 镜像组(6):start == ±1 的 AlignmentDirectional 六档
    private val mirrored = listOf(
        AlignmentDirectional.TOP_START, AlignmentDirectional.TOP_END,
        AlignmentDirectional.CENTER_START, AlignmentDirectional.CENTER_END,
        AlignmentDirectional.BOTTOM_START, AlignmentDirectional.BOTTOM_END,
    )

    private fun assertSameRects(a: LayoutNode, b: LayoutNode, message: String) {
        assertEquals(a.children.size, b.children.size, "$message: 子节点数不同")
        a.children.forEachIndexed { i, la ->
            val lb = b.children[i]
            assertApproxEq(lb.rect.left, la.rect.left, EPS)
            assertApproxEq(lb.rect.top, la.rect.top, EPS)
            assertApproxEq(lb.rect.width, la.rect.width, EPS)
            assertApproxEq(lb.rect.height, la.rect.height, EPS)
        }
    }

    private fun assertMirrored(ltr: LayoutNode, rtl: LayoutNode, alignment: AlignmentGeometry) {
        assertEquals(ltr.children.size, rtl.children.size, "$alignment: 子节点数不同")
        ltr.children.forEachIndexed { i, l ->
            val r = rtl.children[i]
            // RTL 是 LTR 的水平镜像:x' = W - x - w,y/尺寸不变
            assertApproxEq(r.rect.left, STACK_WIDTH - l.rect.left - l.rect.width, EPS)
            assertApproxEq(r.rect.top, l.rect.top, EPS)
            assertApproxEq(r.rect.width, l.rect.width, EPS)
            assertApproxEq(r.rect.height, l.rect.height, EPS)
        }
    }

    private fun assertInsideStack(node: LayoutNode) {
        val rect = node.rect
        assertTrue(rect.left >= -EPS, "left=${rect.left} 越界")
        assertTrue(rect.top >= -EPS, "top=${rect.top} 越界")
        assertTrue(rect.right <= STACK_WIDTH + EPS, "right=${rect.right} 越界")
        assertTrue(rect.bottom <= STACK_HEIGHT + EPS, "bottom=${rect.bottom} 越界")
    }

    @Test
    fun alignment_direction_invariants_test() {
        // 方向无关:同一档位在 LTR/RTL 下矩形逐点相同
        directionIndependent.forEach { alignment ->
            val ltr = layoutOf(alignment, Direction.LTR)
            val rtl = layoutOf(alignment, Direction.RTL)
            ltr.assertSize(STACK_WIDTH, STACK_HEIGHT)
            rtl.assertSize(STACK_WIDTH, STACK_HEIGHT)
            assertSameRects(ltr, rtl, "$alignment 应方向无关")
        }

        // 镜像:START/END 档在 RTL 下是 LTR 的水平镜像
        mirrored.forEach { alignment ->
            val ltr = layoutOf(alignment, Direction.LTR)
            val rtl = layoutOf(alignment, Direction.RTL)
            ltr.assertSize(STACK_WIDTH, STACK_HEIGHT)
            rtl.assertSize(STACK_WIDTH, STACK_HEIGHT)
            assertMirrored(ltr, rtl, alignment)
        }

        // 全 51 个场景:子盒必须落在 Stack 内
        (directionIndependent + mirrored).forEach { alignment ->
            listOf(Direction.LTR, Direction.RTL).forEach { direction ->
                layoutOf(alignment, direction).children.forEach { assertInsideStack(it) }
            }
        }
    }

    @Test
    fun alignment_representative_offsets_test() {
        // 期望值来自探针实测(格式 left, top, width, height;顺序同三个子盒)
        val topStart = layoutOf(AlignmentDirectional.TOP_START, Direction.LTR)
        topStart.assertSize(STACK_WIDTH, STACK_HEIGHT)
        topStart.children[0].assertGlobalRect(0f, 0f, 200f, 80f)
        topStart.children[1].assertGlobalRect(0f, 0f, 50f, 150f)
        topStart.children[2].assertGlobalRect(0f, 0f, 100f, 100f)

        val center = layoutOf(AlignmentDirectional.CENTER, Direction.LTR)
        center.assertSize(STACK_WIDTH, STACK_HEIGHT)
        center.children[0].assertGlobalRect(0f, 35f, 200f, 80f)
        center.children[1].assertGlobalRect(75f, 0f, 50f, 150f)
        center.children[2].assertGlobalRect(50f, 25f, 100f, 100f)

        val bottomEnd = layoutOf(AlignmentDirectional.BOTTOM_END, Direction.LTR)
        bottomEnd.assertSize(STACK_WIDTH, STACK_HEIGHT)
        bottomEnd.children[0].assertGlobalRect(0f, 70f, 200f, 80f)
        bottomEnd.children[1].assertGlobalRect(150f, 0f, 50f, 150f)
        bottomEnd.children[2].assertGlobalRect(100f, 50f, 100f, 100f)
    }

    @Test
    fun alignment_representative_golden_test() {
        // 半透明纯色叠加是确定的(OpacityTest 先例),代表档上整图基准
        golden("widget/stack/alignment_top_start") {
            stackScene(AlignmentDirectional.TOP_START, Direction.LTR)
        }
        golden("widget/stack/alignment_center") {
            stackScene(AlignmentDirectional.CENTER, Direction.LTR)
        }
        golden("widget/stack/alignment_bottom_end") {
            stackScene(AlignmentDirectional.BOTTOM_END, Direction.LTR)
        }
    }
}
```

- [ ] **Step 2: 运行**

Run: `./gradlew :core:test --tests "*StackParserTest*" --offline`
Expected: 首次 **FAIL**——`alignment_representative_golden_test` 报 3 个基准不存在。

- [ ] **Step 3: 录制基准并复跑**

Run: `./gradlew :core:test --tests "*StackParserTest*" --offline -PsnapshotTest.mode=record`
Expected: BUILD SUCCESSFUL,生成 3 张 `golden/widget/stack/alignment_*.png`。

Run: `./gradlew :core:test --tests "*StackParserTest*" --offline`
Expected: BUILD SUCCESSFUL。

- [ ] **Step 4: 提交**

```bash
git add core/src/test/kotlin/com/muedsa/snapshot/widget/StackParserTest.kt core/src/test/resources/golden/widget/stack
git commit -S -m "test(core): StackParserTest 重写(对齐方向不变量 + 代表档 golden)"
```

---

### Task 3: `RowParserTest` 重写(删除字体基线用例)

**Files:**
- Modify: `core/src/test/kotlin/com/muedsa/snapshot/widget/RowParserTest.kt`(整文件替换)
- Delete: 同文件内的 `baseline_test`(含 `RichText`,字体基线归字体批次)

**Interfaces:**
- Consumes: `rootLayout`、`LayoutNode.assertSize`、`assertApproxEq`、`golden`。
- Produces: golden 基准 `golden/widget/row/cross_axis_center.png`。

**背景(实测):** main 轴位置恒为 0 / 100 / 400;`rowH` 在 START/END/CENTER/BASELINE 档为 300,STRETCH 档 Row 与子盒均为 1000×1000。

- [ ] **Step 1: 用下面的内容整体替换该文件**

```kotlin
package com.muedsa.snapshot.widget

import com.muedsa.snapshot.assertApproxEq
import com.muedsa.snapshot.assertSize
import com.muedsa.snapshot.golden
import com.muedsa.snapshot.rendering.LayoutNode
import com.muedsa.snapshot.rendering.flex.CrossAxisAlignment
import com.muedsa.snapshot.rootLayout
import org.jetbrains.skia.Color
import org.jetbrains.skia.paragraph.BaselineMode
import org.jetbrains.skia.paragraph.Direction
import kotlin.test.Test

class RowParserTest {

    private companion object {
        const val EPS = 0.01f
    }

    // 三个纯色盒 100x100 / 300x300 / 200x200;main 轴位置恒为 0 / 100 / 400(实测)。
    private fun Widget.rowScene(crossAxisAlignment: CrossAxisAlignment) {
        if (crossAxisAlignment == CrossAxisAlignment.STRETCH) {
            // STRETCH 需要有限的 cross 轴约束才能观察到拉伸;
            // LimitedBox(1000x1000) 同时把 Row 撑成 1000x1000(mainAxisSize=MAX + 有限 maxWidth)。
            LimitedBox(maxWidth = 1000f, maxHeight = 1000f) {
                Row(crossAxisAlignment = crossAxisAlignment, textDirection = Direction.LTR) {
                    Container(width = 100f, height = 100f, color = Color.RED)
                    Container(width = 300f, height = 300f, color = Color.GREEN)
                    Container(width = 200f, height = 200f, color = Color.BLUE)
                }
            }
        } else {
            Row(
                crossAxisAlignment = crossAxisAlignment,
                textDirection = Direction.LTR,
                textBaseline = if (crossAxisAlignment == CrossAxisAlignment.BASELINE) BaselineMode.ALPHABETIC else null
            ) {
                Container(width = 100f, height = 100f, color = Color.RED)
                Container(width = 300f, height = 300f, color = Color.GREEN)
                Container(width = 200f, height = 200f, color = Color.BLUE)
            }
        }
    }

    private fun rowNode(crossAxisAlignment: CrossAxisAlignment): LayoutNode {
        val root = rootLayout { rowScene(crossAxisAlignment) }
        return if (crossAxisAlignment == CrossAxisAlignment.STRETCH) root.children[0] else root
    }

    private fun LayoutNode.assertMainAxisStarts() {
        assertApproxEq(children[0].rect.left, 0f, EPS)
        assertApproxEq(children[1].rect.left, 100f, EPS)
        assertApproxEq(children[2].rect.left, 400f, EPS)
    }

    private fun LayoutNode.assertCrossTops(y0: Float, y1: Float, y2: Float) {
        assertApproxEq(children[0].rect.top, y0, EPS)
        assertApproxEq(children[1].rect.top, y1, EPS)
        assertApproxEq(children[2].rect.top, y2, EPS)
    }

    @Test
    fun cross_axis_start_top_aligned() {
        val row = rowNode(CrossAxisAlignment.START)
        row.assertSize(600f, 300f)
        row.assertMainAxisStarts()
        row.assertCrossTops(0f, 0f, 0f)
    }

    @Test
    fun cross_axis_end_bottom_aligned() {
        val row = rowNode(CrossAxisAlignment.END)
        row.assertSize(600f, 300f)
        row.assertMainAxisStarts()
        row.assertCrossTops(200f, 0f, 100f)
    }

    @Test
    fun cross_axis_center() {
        val row = rowNode(CrossAxisAlignment.CENTER)
        row.assertSize(600f, 300f)
        row.assertMainAxisStarts()
        row.assertCrossTops(100f, 0f, 50f)
    }

    @Test
    fun cross_axis_stretch_equal_heights() {
        val row = rowNode(CrossAxisAlignment.STRETCH)
        row.assertSize(1000f, 1000f)
        row.assertMainAxisStarts()
        row.assertCrossTops(0f, 0f, 0f)
        row.children.forEach { assertApproxEq(it.rect.height, 1000f, EPS) }
    }

    @Test
    fun cross_axis_baseline_without_real_baseline_matches_end() {
        // 待议:实测 y = rowH - h(与 END 相同),而 CrossAxisAlignment.BASELINE 的 KDoc 写的是
        // "Children who report no baseline will be top-aligned."。
        // 根因:RenderSingleChildBox.computeDistanceToActualBaseline 委托子盒时用
        // child?.getDistanceToBaseline(baseline)(onlyReal 默认 false),链底返回 definiteSize.height,
        // 于是无基线子盒报告的是"底边基线"而非 null,RenderFlex 走了 distance != null 分支。
        // 本批不改产品代码(修复需透传 onlyReal,牵涉 RenderBox/RenderSingleChildBox 签名),按实测断言。
        val row = rowNode(CrossAxisAlignment.BASELINE)
        row.assertSize(600f, 300f)
        row.assertMainAxisStarts()
        row.assertCrossTops(200f, 0f, 100f)
    }

    @Test
    fun cross_axis_center_golden() {
        golden("widget/row/cross_axis_center") { rowScene(CrossAxisAlignment.CENTER) }
    }
}
```

- [ ] **Step 2: 运行**

Run: `./gradlew :core:test --tests "*RowParserTest*" --offline`
Expected: 首次 **FAIL**——`cross_axis_center_golden` 报基准不存在;其余 5 个用例应已通过。

- [ ] **Step 3: 录制基准并复跑**

Run: `./gradlew :core:test --tests "*RowParserTest*" --offline -PsnapshotTest.mode=record`
Expected: BUILD SUCCESSFUL,生成 `golden/widget/row/cross_axis_center.png`。

Run: `./gradlew :core:test --tests "*RowParserTest*" --offline`
Expected: BUILD SUCCESSFUL。

- [ ] **Step 4: 提交**

```bash
git add core/src/test/kotlin/com/muedsa/snapshot/widget/RowParserTest.kt core/src/test/resources/golden/widget/row
git commit -S -m "test(core): RowParserTest 重写(逐档 cross 轴偏移;删除字体基线用例)"
```

---

### Task 4: `ColorFilteredTest` 重写

**Files:**
- Modify: `core/src/test/kotlin/com/muedsa/snapshot/widget/ColorFilteredTest.kt`(整文件替换)

**Interfaces:**
- Consumes: `snapshotPixels`、`expectColorAt`。
- Produces: 无 golden(色滤合成不上整图基准)。

**背景(实测):** 六色块横排 50×100,总 300×100,块中心采样点 `(25 + 50i, 50)`;`MODULATE(RED)` 逐块得 `(R,0,0)`;`SATURATION(0xFF9E9E9E)` 得等通道灰度,精确等于 `0.30R + 0.59G + 0.11B` 取整(红 76 / 绿 150 / 蓝 28 / 青 178 / 品红 105 / 黄 227)。

- [ ] **Step 1: 用下面的内容整体替换该文件**

```kotlin
package com.muedsa.snapshot.widget

import com.muedsa.snapshot.expectColorAt
import com.muedsa.snapshot.snapshotPixels
import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.Color
import org.jetbrains.skia.ColorFilter
import org.jetbrains.skia.paragraph.Direction
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ColorFilteredTest {

    // 六色块横排 50x100(总 300x100);块中心采样点 x = 25 + 50i, y = 50。
    private val blockColors = intArrayOf(
        Color.RED, Color.GREEN, Color.BLUE, Color.CYAN, Color.MAGENTA, Color.YELLOW
    )

    private fun Widget.colorGrid() {
        Row(textDirection = Direction.LTR) {
            blockColors.forEach { c -> Container(width = 50f, height = 100f, color = c) }
        }
    }

    private fun Widget.colorFilteredScene(filter: ColorFilter) {
        ColorFiltered(colorFilter = filter) { colorGrid() }
    }

    private fun sampleX(index: Int): Int = 25 + index * 50

    @Test
    fun red_modulate_multiplies_channels() {
        // MODULATE 逐通道相乘:结果 = src * RED = (R, 0, 0)。
        // 实测:红/品红/黄 → 0xffff0000,绿/蓝/青 → 0xff000000。
        val filter = ColorFilter.makeBlend(Color.RED, BlendMode.MODULATE)
        val pixmap = snapshotPixels { colorFilteredScene(filter) }
        val expected = intArrayOf(
            0xffff0000.toInt(), 0xff000000.toInt(), 0xff000000.toInt(),
            0xff000000.toInt(), 0xffff0000.toInt(), 0xffff0000.toInt(),
        )
        expected.forEachIndexed { i, color -> expectColorAt(pixmap, sampleX(i), 50, color) }
    }

    @Test
    fun red_modulate_differs_from_unfiltered_twin() {
        // 孪生互比:同一场景有/无滤镜,绿块中心必然不同
        val filter = ColorFilter.makeBlend(Color.RED, BlendMode.MODULATE)
        val plain = snapshotPixels { colorGrid() }
        val filtered = snapshotPixels { colorFilteredScene(filter) }
        assertNotEquals(plain.getColor(sampleX(1), 50), filtered.getColor(sampleX(1), 50))
    }

    @Test
    fun gray_saturation_produces_rec601_luma() {
        // SATURATION(灰) 把饱和度置 0 → 等通道灰度。
        // 实测灰度精确等于 0.30R + 0.59G + 0.11B 取整:红 76 / 绿 150 / 蓝 28 / 青 178 / 品红 105 / 黄 227。
        // 系数取自 skiko 当前 SATURATION 矩阵实测;若升级 skiko 改变系数,需同步更新期望值。
        val filter = ColorFilter.makeBlend(0xFF9E9E9E.toInt(), BlendMode.SATURATION)
        val pixmap = snapshotPixels { colorFilteredScene(filter) }
        val expectedGray = intArrayOf(76, 150, 28, 178, 105, 227)
        blockColors.forEachIndexed { i, source ->
            val actual = pixmap.getColor(sampleX(i), 50)
            val r = (actual shr 16) and 0xFF
            val g = (actual shr 8) and 0xFF
            val b = actual and 0xFF
            assertTrue(
                abs(r - g) <= 1 && abs(g - b) <= 1,
                "块$i(源 0x${source.toUInt().toString(16)})应为等通道灰度,实际 R=$r G=$g B=$b"
            )
            assertTrue(
                abs(r - expectedGray[i]) <= 2,
                "块$i 灰度应≈${expectedGray[i]},实际 R=$r G=$g B=$b"
            )
        }
    }
}
```

- [ ] **Step 2: 运行**

Run: `./gradlew :core:test --tests "*ColorFilteredTest*" --offline`
Expected: BUILD SUCCESSFUL(三个用例全通过;若 `gray_saturation` 超容差,先核对 `actual` 三通道是否相等再判定)。

- [ ] **Step 3: 提交**

```bash
git add core/src/test/kotlin/com/muedsa/snapshot/widget/ColorFilteredTest.kt
git commit -S -m "test(core): ColorFilteredTest 重写(本地几何 + MODULATE/SATURATION 语义断言)"
```

---

### Task 5: 全量验证与静态核对

**Files:**
- 无改动(仅验证)

- [ ] **Step 1: 全量测试**

Run: `./gradlew test --offline`
Expected: BUILD SUCCESSFUL(含 1a/1b 既有用例与新增基准)。

- [ ] **Step 2: 打包**

Run: `./gradlew jar --offline`
Expected: BUILD SUCCESSFUL。

- [ ] **Step 3: 工作树干净**

Run: `git status --short`
Expected: 无输出(四文件不再往 `build/test-results/test-image-outputs/` 之外写任何东西)。

- [ ] **Step 4: 静态核对**

Run:
```bash
grep -n "println\|drawWidget\|drawPainter\|org.junit\|NetworkImage\|https://" \
  core/src/test/kotlin/com/muedsa/snapshot/widget/ContainerParserTest.kt \
  core/src/test/kotlin/com/muedsa/snapshot/widget/StackParserTest.kt \
  core/src/test/kotlin/com/muedsa/snapshot/widget/RowParserTest.kt \
  core/src/test/kotlin/com/muedsa/snapshot/widget/ColorFilteredTest.kt
```
Expected: 无匹配(退出码 1)。

- [ ] **Step 5: 签名核对**

Run: `git log main..HEAD --format="%h %G? %s"`
Expected: 每行 `%G?` 均为 `G`。

- [ ] **Step 6: 汇报**

汇总:提交清单、golden 基准新增清单、`BASELINE` 档"待议"记录、任何实现侧发现。

---

## 收尾(不在任务内,由主会话执行)

- 更新记忆:`migrate-widget-smoke-batch1`(1c 完成状态与后续批)。
- 推送分支并开 PR:PR 文案备好(标题建议 `test(core): widget 冒烟迁移 Batch-1c(对齐矩阵 + ColorFiltered)`)。本会话到 github 曾可连通,`gh` 未安装,推送需 `git push` 直连。
