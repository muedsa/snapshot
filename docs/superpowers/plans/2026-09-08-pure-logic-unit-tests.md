# 纯逻辑单元测试铺开实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 core 中四区"无渲染、可确定性"的纯逻辑补单测(BoxFit/FittedSizes + BoxConstraints、TextStyle、渐变参数、BorderRadius/BorderSide/BoxShadow/BorderRadiusGeometry),期望值按意图语义;语义清晰 bug 最小修复并记录。

**Architecture:** 纯 `kotlin.test`、不依赖 `:testkit`;按区各一个 GPG 提交;测试文件镜像到 core 对应包;每文件内实现一个 `approxEq(a,b,tol=1e-3f)` 私有辅助做浮点近似(不引入额外断言库)。

**Tech Stack:** Kotlin 2.4.0 / JVM、Gradle(core 模块)、skiko-awt `0.0.0-SNAPSHOT`(仅涉及 skia 值对象 RRect/Rect/Paint 字段读取,不渲染)、`kotlin.test`。

**Spec:** `docs/superpowers/specs/2026-09-08-pure-logic-unit-tests-design.md`

---

## 执行前必读(环境约定)

- 仓库根 `D:\mine\workspace\snapshot` 运行命令(git-bash 用 `./gradlew`)。
- 当前分支 `test/pure-logic-units`(base=main,已有 spec 提交);每个 Task 结束 **GPG 签名提交**(`git commit -S -m "…"`;若报 `No passphrase given` 原样重试一次;用 `git log -1 --pretty='%h %G? %s'` 确认 `G`)。
- 每个 Task 是 TDD:先写/改测试 → 跑目标测试(应红或编译红)→ 若红源于"实现与意图语义相悖的明确 bug"则最小修复产品代码 → 绿 → 提交。歧义偏差**不改**产品,提交消息注明"待议",并写入该区提交正文。
- 浮点一律用文件内 `approxEq` 或 `kotlin.test.assertEquals`(整数/精确场景);不要用裸 `==` 比 Float。
- `:core:test` 单类过滤:`./gradlew :core:test --tests 'com.muedsa.snapshot.…XxxTest' --console=plain`。
- 若本地 gradle 报 build-cache `AccessDeniedException`,加 `--no-build-cache` 重跑(与代码无关)。

---

## 涉及文件清单

**Create**
- `core/src/test/kotlin/com/muedsa/snapshot/paint/FittedSizesTest.kt`
- `core/src/test/kotlin/com/muedsa/snapshot/paint/text/TextStyleTest.kt`
- `core/src/test/kotlin/com/muedsa/snapshot/paint/gradient/GradientImpliedStopsTest.kt`
- `core/src/test/kotlin/com/muedsa/snapshot/paint/decoration/BorderRadiusTest.kt`
- `core/src/test/kotlin/com/muedsa/snapshot/paint/decoration/BorderRadiusGeometryTest.kt`
- `core/src/test/kotlin/com/muedsa/snapshot/paint/decoration/BorderSideTest.kt`
- `core/src/test/kotlin/com/muedsa/snapshot/paint/decoration/BoxShadowTest.kt`

**Modify**
- `core/src/test/kotlin/com/muedsa/snapshot/render/box/BoxConstraintsTest.kt`(追加用例;并将测试注解统一为 `kotlin.test.Test`)

---

### Task A:BoxFit/FittedSizes + BoxConstraints 补全

**Files:**
- Create: `core/src/test/kotlin/com/muedsa/snapshot/paint/FittedSizesTest.kt`
- Modify: `core/src/test/kotlin/com/muedsa/snapshot/render/box/BoxConstraintsTest.kt`

- [ ] **Step 1: 写 FittedSizesTest**

创建文件:
```kotlin
package com.muedsa.snapshot.paint

import com.muedsa.geometry.Size
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FittedSizesTest {

    private fun approxEq(a: Float, b: Float, tol: Float = 1e-3f) = abs(a - b) <= tol

    private fun assertSizeApprox(actual: Size, w: Float, h: Float, tol: Float = 1e-3f) {
        assertTrue(approxEq(actual.width, w, tol), "width expected $w but was ${actual.width}")
        assertTrue(approxEq(actual.height, h, tol), "height expected $h but was ${actual.height}")
    }

    // 输入 4:3(400x300),输出 16:9(1600x900)
    private val input = Size(400f, 300f)
    private val output = Size(1600f, 900f)

    @Test
    fun zero_when_any_dimension_non_positive() {
        listOf(BoxFit.FILL, BoxFit.CONTAIN, BoxFit.COVER, BoxFit.FIT_WIDTH, BoxFit.FIT_HEIGHT, BoxFit.NONE, BoxFit.SCALE_DOWN)
            .forEach { fit ->
                assertEquals(FittedSizes.ZERO, FittedSizes.applyBoxFit(fit, Size(0f, 300f), output))
                assertEquals(FittedSizes.ZERO, FittedSizes.applyBoxFit(fit, input, Size(1600f, 0f)))
            }
    }

    @Test
    fun fill_stretches_source_to_output() {
        val r = FittedSizes.applyBoxFit(BoxFit.FILL, input, output)
        assertEquals(input, r.source)
        assertEquals(output, r.destination)
    }

    @Test
    fun contain_fits_whole_image() {
        // output 更宽(1.78 > 1.33):按高缩放
        val r = FittedSizes.applyBoxFit(BoxFit.CONTAIN, input, output)
        assertEquals(input, r.source)
        assertSizeApprox(r.destination, 1200f, 900f) // 400 * 900 / 300
        // 窄输出 1:1(400x400):按宽缩放
        val narrow = FittedSizes.applyBoxFit(BoxFit.CONTAIN, input, Size(400f, 400f))
        assertSizeApprox(narrow.destination, 400f, 300f)
    }

    @Test
    fun cover_fills_output_cropping_source() {
        val r = FittedSizes.applyBoxFit(BoxFit.COVER, input, output)
        assertEquals(output, r.destination)
        assertSizeApprox(r.source, 400f, 225f) // 纵向裁剪:400 * 900 / 1600
    }

    @Test
    fun fit_width_and_fit_height_split_branches() {
        // 宽输出: FIT_WIDTH = cover-like, FIT_HEIGHT = contain-like
        val fw = FittedSizes.applyBoxFit(BoxFit.FIT_WIDTH, input, output)
        assertEquals(output, fw.destination)
        assertSizeApprox(fw.source, 400f, 225f)

        val fh = FittedSizes.applyBoxFit(BoxFit.FIT_HEIGHT, input, output)
        assertEquals(input, fh.source)
        assertSizeApprox(fh.destination, 1200f, 900f)
    }

    @Test
    fun none_crops_to_smaller_side() {
        val r = FittedSizes.applyBoxFit(BoxFit.NONE, Size(1600f, 1200f), output)
        assertEquals(Size(1600f, 900f), r.source)
        assertEquals(Size(1600f, 900f), r.destination)
    }

    @Test
    fun scale_down_only_downscales() {
        // 输入小于输出 → 原样
        val up = FittedSizes.applyBoxFit(BoxFit.SCALE_DOWN, input, output)
        assertEquals(input, up.destination)
        // 输入大于输出 → 缩到 4:3 且不超出输出
        val big = Size(3200f, 2400f)
        val down = FittedSizes.applyBoxFit(BoxFit.SCALE_DOWN, big, output)
        assertSizeApprox(down.destination, 1600f, 1200f)
    }
}
```
> 若某分支实际数值与上方"意图推导"不符,以 Flutter `BoxFit` 惯例为准判定:是 bug(清晰)→ 修 `FittedSizes.applyBoxFit` 对应分支;是歧义 → 不改并记录。提交信息注明"修复:…(如有)"。

- [ ] **Step 2: 扩展 BoxConstraintsTest(并把注解统一到 kotlin.test)**

在 `core/src/test/kotlin/com/muedsa/snapshot/render/box/BoxConstraintsTest.kt` 中:
- 把 `import org.junit.jupiter.api.Test` 改为 `import kotlin.test.Test`(保留现有 `kotlin.test.expect` 用例);
- 追加以下用例(期望按 BoxConstraints 语义):
```kotlin
    @Test
    fun constructors_and_derived_flags() {
        val tight = BoxConstraints.tight(Size(10f, 20f))
        expect(10f) { tight.minWidth }
        expect(10f) { tight.maxWidth }
        expect(20f) { tight.minHeight }
        expect(20f) { tight.maxHeight }
        expect(true) { tight.isTight }

        val tightFor = BoxConstraints.tightFor(width = 5f, height = 7f)
        expect(5f) { tightFor.minWidth }
        expect(7f) { tightFor.minHeight }
        expect(true) { tightFor.isTight }

        val loose = BoxConstraints.loose(Size(30f, 40f))
        expect(0f) { loose.minWidth }
        expect(30f) { loose.maxWidth }
        expect(false) { loose.hasInfiniteWidth }
        expect(true) { loose.hasBoundedWidth }

        val expand = BoxConstraints.expand(width = 12f)
        expect(12f) { expand.minWidth }
        expect(12f) { expand.maxWidth }
        expect(Float.POSITIVE_INFINITY) { expand.minHeight }

        val infinite = BoxConstraints()
        expect(true) { infinite.hasInfiniteHeight }
        expect(Float.POSITIVE_INFINITY) { infinite.biggest.height }
        expect(0f) { infinite.smallest.width }
    }

    @Test
    fun transforms_and_constrain() {
        val c = BoxConstraints(minWidth = 1f, maxWidth = 10f, minHeight = 2f, maxHeight = 20f)
        val loosened = c.loosen()
        expect(0f) { loosened.minWidth }
        expect(10f) { loosened.maxWidth }

        val tightened = c.tighten(width = 5f, height = 30f)
        expect(5f) { tightened.minWidth }
        expect(5f) { tightened.maxWidth }
        expect(20f) { tightened.maxHeight } // height 30 被 coerce 进 [2,20]

        expect(Size(3f, 4f)) { c.constrain(Size(3f, 4f)) }
        expect(Size(30f, 4f)) { c.constrain(Size(30f, 4f)) }
        expect(10f) { c.constrainWidth(999f) }
        expect(2f) { c.constrainHeight(0f) }

        val deflated = c.deflate(EdgeInsets.all(2f))
        expect(3f) { deflated.maxWidth }   // 10 - 4
        expect(0f) { deflated.minWidth }   // max(0, 1-4)
        expect(16f) { deflated.maxHeight } // 20 - 4

        expect(Size(5f, 5f)) {
            BoxConstraints(minWidth = 0f, maxWidth = 100f, minHeight = 0f, maxHeight = 100f)
                .constrainSizeAndAttemptToPreserveAspectRatio(Size(5f, 5f))
        }
    }

    @Test
    fun width_and_height_only_views() {
        val c = BoxConstraints(minWidth = 1f, maxWidth = 10f, minHeight = 2f, maxHeight = 20f)
        val w = c.widthConstraints()
        expect(1f) { w.minWidth }
        expect(10f) { w.maxWidth }
        expect(0f) { w.minHeight }
        expect(Float.POSITIVE_INFINITY) { w.maxHeight }
        val h = c.heightConstraints()
        expect(0f) { h.minWidth }
        expect(Float.POSITIVE_INFINITY) { h.maxWidth }
        expect(2f) { h.minHeight }
        expect(20f) { h.maxHeight }
    }
```
> 需要时在文件头补 `import com.muedsa.geometry.EdgeInsets`、`import com.muedsa.geometry.Size`(按现有 import 风格)。若某断言不符意图(例如 `deflate` 对 min 取 `max(0,…)`),按 spec「修复/记录」策略处理。

- [ ] **Step 3: 运行测试**

Run:
```bash
./gradlew :core:test --tests 'com.muedsa.snapshot.paint.FittedSizesTest' --tests 'com.muedsa.snapshot.render.box.BoxConstraintsTest' --console=plain
```
Expected:全绿。若红:定位是期望算错还是实现 bug(见 Step 1/2 注),最小修复产品代码或按"待议"不改,重跑到绿。

- [ ] **Step 4: 提交**

```bash
git add core/src/test/kotlin/com/muedsa/snapshot/paint/FittedSizesTest.kt \
        core/src/test/kotlin/com/muedsa/snapshot/render/box/BoxConstraintsTest.kt \
        <若修复了产品文件,一并 git add 并在消息注明>
git commit -S -m "test(core): BoxFit/FittedSizes 与 BoxConstraints 纯逻辑单测"
```

---

### Task B:TextStyle merge/映射

**Files:**
- Create: `core/src/test/kotlin/com/muedsa/snapshot/paint/text/TextStyleTest.kt`

- [ ] **Step 1: 写 TextStyleTest**

```kotlin
package com.muedsa.snapshot.paint.text

import org.jetbrains.skia.FontEdging
import org.jetbrains.skia.FontHinting
import org.jetbrains.skia.paragraph.Alignment
import org.jetbrains.skia.paragraph.BaselineMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TextStyleTest {

    @Test
    fun isEmpty_true_when_all_null() {
        assertTrue(TextStyle().isEmpty())
    }

    @Test
    fun isEmpty_false_when_any_field_set() {
        assertFalse(TextStyle(fontSize = 12f).isEmpty())
        assertFalse(TextStyle(color = 0xFFFF0000.toInt()).isEmpty())
        assertFalse(TextStyle(fontEdging = FontEdging.ANTI_ALIAS).isEmpty())
        assertFalse(TextStyle(fontHinting = FontHinting.NORMAL).isEmpty())
        assertFalse(TextStyle(subpixel = true).isEmpty())
    }

    @Test
    fun mergeFrom_child_overrides_parent_when_non_null() {
        val child = TextStyle(fontSize = 16f, color = 0xFF0000FF.toInt())
        val parent = TextStyle(fontSize = 12f, height = 1.5f)
        val merged = child.mergeFrom(parent)
        assertEquals(16f, merged.fontSize)          // 子覆盖
        assertEquals(0xFF0000FF.toInt(), merged.color)
        assertEquals(1.5f, merged.height)           // 父回填
    }

    @Test
    fun mergeFrom_empty_child_equals_parent() {
        val parent = TextStyle(fontSize = 12f, fontFamilies = listOf("Roboto"), subpixel = true)
        assertEquals(parent, TextStyle().mergeFrom(parent))
    }

    @Test
    fun mergeFrom_chain_and_font_raster_fields() {
        val base = TextStyle(fontEdging = FontEdging.SUBPIXEL_ANTI_ALIAS, fontHinting = FontHinting.NORMAL, subpixel = true)
        val mid = TextStyle(fontHinting = FontHinting.NONE).mergeFrom(base)
        assertEquals(FontHinting.NONE, mid.fontHinting)
        assertEquals(FontEdging.SUBPIXEL_ANTI_ALIAS, mid.fontEdging)
        val top = TextStyle(fontSize = 20f).mergeFrom(mid)
        assertEquals(20f, top.fontSize)
        assertEquals(true, top.subpixel)
        assertTrue(top.fontHinting == FontHinting.NONE)
    }

    @Test
    fun toSkiko_null_when_empty() {
        assertNull(TextStyle().toSkikoTextStyle())
    }

    @Test
    fun toSkiko_maps_all_key_fields() {
        val style = TextStyle(
            color = 0xFF112233.toInt(),
            fontSize = 18f,
            fontFamilies = listOf("Roboto", "Noto Sans SC"),
            height = 1.2f,
            baselineMode = BaselineMode.ALPHABETIC,
            fontEdging = FontEdging.SUBPIXEL_ANTI_ALIAS,
            fontHinting = FontHinting.FULL,
            subpixel = false,
        )
        val sk = assertNotNull(style.toSkikoTextStyle())
        assertEquals(0xFF112233.toInt(), sk.color)
        assertEquals(18f, sk.fontSize)
        assertEquals(listOf("Roboto", "Noto Sans SC"), sk.fontFamilies.toList())
        assertEquals(1.2f, sk.height)
        assertEquals(BaselineMode.ALPHABETIC, sk.baselineMode)
        assertEquals(FontEdging.SUBPIXEL_ANTI_ALIAS, sk.fontEdging)
        assertEquals(FontHinting.FULL, sk.fontHinting)
        assertEquals(false, sk.subpixel)
        assertNotNull(sk)
    }
}
```
> 若个别 skia paragraph.TextStyle 字段读取(getter)报签名差异,以编译提示调整读取方式(如 `baselineMode` 只读).断言意图不变。

- [ ] **Step 2: 运行测试**

Run:
```bash
./gradlew :core:test --tests 'com.muedsa.snapshot.paint.text.TextStyleTest' --console=plain
```
Expected:全绿。

- [ ] **Step 3: 提交**

```bash
git add core/src/test/kotlin/com/muedsa/snapshot/paint/text/TextStyleTest.kt
git commit -S -m "test(core): TextStyle merge/映射纯逻辑单测"
```

---

### Task C:渐变参数数学(impliedStops)

**Files:**
- Create: `core/src/test/kotlin/com/muedsa/snapshot/paint/gradient/GradientImpliedStopsTest.kt`

- [ ] **Step 1: 写测试**

```kotlin
package com.muedsa.snapshot.paint.gradient

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.MATH_PI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertContentEquals

/** 仅暴露 protected impliedStops(),不改产品面。 */
private class StopsProbe(colors: IntArray, stops: FloatArray? = null) : Gradient(colors, stops) {
    fun exposedStops(): FloatArray = impliedStops()
}

class GradientImpliedStopsTest {

    private fun probe(n: Int, stops: FloatArray? = null): StopsProbe =
        StopsProbe(IntArray(n) { 0xFF000000.toInt() }, stops)

    @Test
    fun implied_stops_evenly_spaced() {
        assertContentEquals(floatArrayOf(0f, 1f), probe(2).exposedStops())
        assertContentEquals(floatArrayOf(0f, 0.5f, 1f), probe(3).exposedStops(), 1e-6f)
        assertContentEquals(floatArrayOf(0f, 0.25f, 0.5f, 0.75f, 1f), probe(5).exposedStops(), 1e-6f)
        assertContentEquals(floatArrayOf(0f, 0.2f, 0.4f, 0.6f, 0.8f, 1f), probe(6).exposedStops(), 1e-6f)
    }

    @Test
    fun explicit_stops_returned_as_is() {
        val stops = floatArrayOf(0f, 0.1f, 0.9f, 1f)
        assertContentEquals(stops, probe(4, stops).exposedStops())
    }

    @Test
    fun sweep_defaults() {
        assertEquals(0f, SweepGradient(colors = intArrayOf(0xFF000000.toInt(), 0xFFFFFFFF.toInt())).startAngle)
        assertEquals(MATH_PI * 2, SweepGradient(colors = intArrayOf(0xFF000000.toInt(), 0xFFFFFFFF.toInt())).endAngle)
        assertEquals(BoxAlignment.CENTER, SweepGradient(colors = intArrayOf(0xFF000000.toInt(), 0xFFFFFFFF.toInt())).center)
    }
}
```
> `assertContentEquals(expected, actual, tolerance)` 是 kotlin 标准库对 FloatArray 的容差重载;若 IDE 报没有该重载,改用手写逐元素断言(差值 ≤1e-6f)。

- [ ] **Step 2: 运行测试**

Run:
```bash
./gradlew :core:test --tests 'com.muedsa.snapshot.paint.gradient.GradientImpliedStopsTest' --console=plain
```
Expected:全绿。

- [ ] **Step 3: 提交**

```bash
git add core/src/test/kotlin/com/muedsa/snapshot/paint/gradient/GradientImpliedStopsTest.kt
git commit -S -m "test(core): 渐变 impliedStops 与 Sweep 默认参数单测"
```

---

### Task D:边框/圆角/阴影几何

**Files:**
- Create: `core/src/test/kotlin/com/muedsa/snapshot/paint/decoration/BorderRadiusTest.kt`
- Create: `core/src/test/kotlin/com/muedsa/snapshot/paint/decoration/BorderRadiusGeometryTest.kt`
- Create: `core/src/test/kotlin/com/muedsa/snapshot/paint/decoration/BorderSideTest.kt`
- Create: `core/src/test/kotlin/com/muedsa/snapshot/paint/decoration/BoxShadowTest.kt`

- [ ] **Step 1: 写 BorderRadiusTest**

```kotlin
package com.muedsa.snapshot.paint.decoration

import com.muedsa.geometry.Radius
import org.jetbrains.skia.Rect
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BorderRadiusTest {

    private fun approx(a: Float, b: Float, tol: Float = 1e-3f) = abs(a - b) <= tol

    private fun r(v: Float) = Radius.circular(v)

    @Test
    fun factories_and_zero() {
        val all = BorderRadius.all(r(5f))
        listOf(all.topLeft, all.topRight, all.bottomLeft, all.bottomRight).forEach { assertEquals(r(5f), it) }
        val circular = BorderRadius.circular(3f)
        assertEquals(r(3f), circular.topLeft)
        val vertical = BorderRadius.vertical(top = r(1f), bottom = r(2f))
        assertEquals(r(1f), vertical.topLeft)
        assertEquals(r(2f), vertical.bottomLeft)
        val horizontal = BorderRadius.horizontal(left = r(4f), right = r(8f))
        assertEquals(r(4f), horizontal.topLeft)
        assertEquals(r(8f), horizontal.bottomRight)
        assertEquals(BorderRadius.only(), BorderRadius.ZERO)
    }

    @Test
    fun cornerwise_operators() {
        val a = BorderRadius.only(topLeft = r(1f), topRight = r(2f), bottomLeft = r(3f), bottomRight = r(4f))
        val b = BorderRadius.only(topLeft = r(10f), topRight = r(20f), bottomLeft = r(30f), bottomRight = r(40f))
        val sum = a + b
        assertEquals(r(11f), sum.topLeft)
        assertEquals(r(42f), sum.bottomRight)
        val diff = b - a
        assertEquals(r(9f), diff.topLeft)
        assertEquals(Radius.ZERO, (a - a).topLeft)
        val neg = -a
        assertEquals(Radius.circular(-1f), neg.topLeft)
        assertEquals(r(11f), (a + 10f).bottomRight)
        assertEquals(r(0.5f), (a / 2f).topLeft)
        assertEquals(r(3f), (a * 3f).topLeft)
    }

    @Test
    fun copyWith() {
        val a = BorderRadius.circular(2f)
        val b = a.copyWith(topRight = r(9f))
        assertEquals(r(2f), b.topLeft)
        assertEquals(r(9f), b.topRight)
        assertEquals(r(2f), b.bottomRight)
    }

    @Test
    fun toRRect_shape_and_corner_radius() {
        val rect = Rect.makeXYWH(0f, 0f, 100f, 50f)
        val br = BorderRadius.circular(10f)
        val rr = br.toRRect(rect)
        // 外框尺寸
        assertTrue(approx(rr.rect.left, 0f) && approx(rr.rect.top, 0f))
        assertTrue(approx(rr.rect.width, 100f))
        assertTrue(approx(rr.rect.height, 50f))
        // 角半径:skia RRect 存于 radii 向量;断言为可读属性(以编译提示为准,如 rr.radii 逐段或 getCornerRadii)
        val radii = rr.getCornerRadii()
        assertTrue(approx(radii[0].x, 10f) && approx(radii[0].y, 10f))
    }
}
```
> `rr.getCornerRadii()`/`rr.radii` 的确切 API 以编译提示为准;目标是断言每个角 x/y ≈10。

- [ ] **Step 2: 写 BorderRadiusGeometryTest**

```kotlin
package com.muedsa.snapshot.paint.decoration

import com.muedsa.geometry.Radius
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BorderRadiusGeometryTest {

    private fun r(v: Float) = Radius.circular(v)

    @Test
    fun borderRadius_op_borderRadius_stays_borderRadius() {
        val a = BorderRadius.circular(4f)
        val b = BorderRadius.only(topLeft = r(1f), topRight = r(2f))
        val sum = a.add(b)
        assertTrue(sum is BorderRadius)
        assertEquals(r(5f), (sum as BorderRadius).topLeft)
        assertEquals(r(6f), sum.topRight)
    }

    @Test
    fun borderRadius_op_other_returns_mixed() {
        val a = BorderRadius.circular(4f)
        val mixed = MixedBorderRadius(
            topLeft = r(1f), topRight = r(2f), bottomLeft = r(3f), bottomRight = r(4f),
            topStart = r(1f), topEnd = r(2f), bottomStart = r(3f), bottomEnd = r(4f),
        )
        val out = a.subtract(mixed)
        assertTrue(out is MixedBorderRadius)
        assertEquals(r(3f), out.topLeft)   // 4 - 1
        assertEquals(r(2f), out.topRight)  // 4 - 2
    }
}
```
> 若 `MixedBorderRadius` 构造参数名不同,按其实际签名调整(该测试仅校验"混入类型走基类 open 返回 MixedBorderRadius"这一语义)。

- [ ] **Step 3: 写 BorderSideTest**

```kotlin
package com.muedsa.snapshot.paint.decoration

import org.jetbrains.skia.Color
import org.jetbrains.skia.PaintMode
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BorderSideTest {

    private fun approx(a: Float, b: Float, tol: Float = 1e-3f) = abs(a - b) <= tol

    @Test
    fun stroke_geometry_by_align() {
        // INSIDE = -1
        val inside = BorderSide(width = 10f, strokeAlign = BorderSide.STROKE_ALIGN_INSIDE)
        assertTrue(approx(inside.strokeInset, 0f))
        assertTrue(approx(inside.strokeOutset, 10f))
        assertTrue(approx(inside.strokeOffset, -10f))
        // CENTER = 0
        val center = BorderSide(width = 10f, strokeAlign = BorderSide.STROKE_ALIGN_CENTER)
        assertTrue(approx(center.strokeInset, 5f))
        assertTrue(approx(center.strokeOutset, 5f))
        // OUTSIDE = 1
        val outside = BorderSide(width = 10f, strokeAlign = BorderSide.STROKE_ALIGN_OUTSIDE)
        assertTrue(approx(outside.strokeInset, 10f))
        assertTrue(approx(outside.strokeOutset, 0f))
    }

    @Test
    fun scale_zero_or_negative_yields_none() {
        val s = BorderSide(color = Color.RED, width = 4f, style = BorderStyle.SOLID)
        val t0 = s.scale(0f)
        assertEquals(0f, t0.width)
        assertEquals(BorderStyle.NONE, t0.style)
        assertEquals(Color.RED, t0.color)
        val tn = s.scale(-1f)
        assertEquals(BorderStyle.NONE, tn.style)
    }

    @Test
    fun scale_positive_keeps_style() {
        val s = BorderSide(color = Color.RED, width = 4f)
        val t = s.scale(0.5f)
        assertEquals(2f, t.width)
        assertEquals(BorderStyle.SOLID, t.style)
        assertEquals(Color.RED, t.color)
    }

    @Test
    fun canMerge_rules() {
        val a = BorderSide(color = Color.RED, width = 1f)
        val b = BorderSide(color = Color.RED, width = 2f)
        val c = BorderSide(color = Color.BLUE, width = 1f)
        val none = BorderSide.NONE
        assertTrue(BorderSide.canMerge(a, b))       // 同色同 style
        assertTrue(BorderSide.canMerge(a, none))    // 一方 none
        assertTrue(BorderSide.canMerge(none, a))
        assertFalse(BorderSide.canMerge(a, c))      // 异色
        assertFalse(BorderSide.canMerge(a, BorderSide(width = 1f, style = BorderStyle.NONE, color = Color.RED))) // width>0 的 NONE 视为非 none?按实现断言
    }

    @Test
    fun merge_none_and_solid_cases() {
        val a = BorderSide(color = Color.RED, width = 1f)
        val none = BorderSide.NONE
        assertEquals(BorderSide.NONE, BorderSide.merge(none, none))
        assertEquals(a, BorderSide.merge(none, a))
        assertEquals(a, BorderSide.merge(a, none))
        val sum = BorderSide.merge(BorderSide(color = Color.RED, width = 1f), BorderSide(color = Color.RED, width = 3f))
        assertEquals(4f, sum.width)
        assertEquals(Color.RED, sum.color)
    }

    @Test
    fun toPaint_modes() {
        val solid = BorderSide(color = Color.RED, width = 4f)
        val p = solid.toPaint()
        assertEquals(Color.RED, p.color)
        assertEquals(4f, p.strokeWidth)
        assertEquals(PaintMode.STROKE, p.mode)
        val none = BorderSide.NONE.toPaint()
        assertEquals(Color.BLACK, none.color)
        assertEquals(0f, none.strokeWidth)
    }
}
```
> `canMerge` 的边界语义(尤其 `style==NONE && width>0` 是否算 none)若与实现出入,按实现语义断言并在提交注明。

- [ ] **Step 4: 写 BoxShadowTest**

```kotlin
package com.muedsa.snapshot.paint.decoration

import com.muedsa.geometry.Offset
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BoxShadowTest {

    private fun approx(a: Float, b: Float, tol: Float = 1e-3f) = abs(a - b) <= tol

    @Test
    fun convertRadiusToSigma_bounds_and_formula() {
        assertEquals(0f, BoxShadow.convertRadiusToSigma(0f))
        assertEquals(0f, BoxShadow.convertRadiusToSigma(-5f))
        assertTrue(approx(BoxShadow.convertRadiusToSigma(10f), 10f * 0.57735f + 0.5f))
    }

    @Test
    fun blurSigma_derived() {
        val s = BoxShadow(color = 0xFF000000.toInt(), offset = Offset.ZERO, blurRadius = 4f)
        assertEquals(BoxShadow.convertRadiusToSigma(4f), s.blurSigma)
    }

    @Test
    fun scale_scales_geometry_only() {
        val s = BoxShadow(color = 0xFF112233.toInt(), offset = Offset(2f, 4f), blurRadius = 6f, spreadRadius = 8f)
        val t = s.scale(0.5f)
        assertEquals(0xFF112233.toInt(), t.color)
        assertEquals(Offset(1f, 2f), t.offset)
        assertEquals(3f, t.blurRadius)
        assertEquals(4f, t.spreadRadius)
    }
}
```

- [ ] **Step 5: 运行四个测试类**

Run:
```bash
./gradlew :core:test --tests 'com.muedsa.snapshot.paint.decoration.*' --console=plain
```
Expected:四类全绿。若红:按意图语义判定修复/待议(记录在提交正文)。

- [ ] **Step 6: 提交**

```bash
git add core/src/test/kotlin/com/muedsa/snapshot/paint/decoration/
git commit -S -m "test(core): BorderRadius/BorderSide/BoxShadow 几何纯逻辑单测"
```

---

### Task E:全量验证与修复清单核对

**Files:** 无新改动(校验)。

- [ ] **Step 1: 全量 test + jar**

Run:
```bash
./gradlew test --console=plain
./gradlew jar --console=plain
```
Expected:两个 BUILD SUCCESSFUL。若失败来自本批新增(产品修复副作用),修到绿。

- [ ] **Step 2: 静态核对**

Run:
```bash
git status --short
git log --pretty='%h %G? %s' main..HEAD
grep -rn "org.junit" core/src/test/kotlin/com/muedsa/snapshot/paint core/src/test/kotlin/com/muedsa/snapshot/render/box/BoxConstraintsTest.kt || echo "no junit in new tests"
```
Expected:工作树干净;`main..HEAD` 全 G(docs spec + 各区测试,及可能的修复);新增测试无 `org.junit`。

- [ ] **Step 3: 汇总修复清单**

把实施期做的任何"产品最小修复"与"待议项"汇总成一段(含 类.方法 → 改动 / 待议描述),准备放入 PR 正文;若无则注明"无产品改动、无待议"。
