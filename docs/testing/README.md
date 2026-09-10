# snapshot 测试框架使用手册

本手册面向给 `snapshot` 写/维护测试的作者。它介绍共享测试模块 `:testkit` 提供的工具、四层断言能力,以及如何为 Widget / Render / Paint / 解析器 / 文本等功能编写单元测试。

> 一句话总纲:**能对布局数值断言就别只画图;文本不进 golden;只有"可确定复现"的视觉内容才用 golden。**

目录
- [1. 背景与模块](#1-背景与模块)
- [2. 分层决策(先读)](#2-分层决策先读)
- [3. 渲染桥:从场景拿到像素/布局](#3-渲染桥从场景拿到像素布局)
- [4. 布局/数值断言(第 1 层)](#4-布局数值断言第-1-层)
- [5. 像素采样断言(第 2 层)](#5-像素采样断言第-2-层)
- [6. golden 基准比对(第 3 层)](#6-golden-基准比对第-3-层)
- [7. 文本类怎么测(不进 golden)](#7-文本类怎么测不进-golden)
- [8. artifact:仅供人眼检视的输出](#8-artifact仅供人眼检视的输出)
- [9. 编写新功能测试:分步指南](#9-编写新功能测试分步指南)
- [10. 运行与 golden 三态命令](#10-运行与-golden-三态命令)
- [11. 约定与命名](#11-约定与命名)
- [12. 常见误区 / FAQ](#12-常见误区--faq)
- [13. 活模板(照抄起点)](#13-活模板照抄起点)

---

## 1. 背景与模块

`snapshot` 分为 `core`(渲染/布局/widget/paint)、`parser`(XML 解析)。测试基础设施集中在**共享模块 `:testkit`**(main 源集),core / parser 的测试源通过 `testImplementation(project(":testkit"))` 使用它。

`:testkit` 提供两类包:

| 包 | 内容 | 面向 |
|---|---|---|
| `com.muedsa.snapshot` | 顶层断言 DSL + 渲染桥 + 沿用兼容层(`drawWidget`/`drawPainter`/`getTestPngFile`…) | 测试作者直接 import |
| `com.muedsa.snapshot.testkit` | golden 引擎 `GoldenEngine` 内部实现 | 测试作者一般不直接 import |

产品面最小化:core main 仅新增了**只读布局自省** `LayoutNode`(`com.muedsa.snapshot.rendering`),它只读已 layout 好的渲染树,无任何渲染行为变更。

---

## 2. 分层决策(先读)

写一个测试时,按下面顺序选层,能选更稳/更快的就别往下一层:

| 层 | 能力 | 需要 | 稳定性 | 典型用途 |
|---|---|---|---|---|
| **1 布局/数值** | `rootLayout` 建树后断言 size / 全局矩形 / 容差 | 无栅格化 | 跨 OS 极稳 | 布局算法:尺寸、对齐、flex 分配、padding |
| **2 像素采样** | 对已渲染 `Pixmap` 断言某点/某区域颜色 | 栅格化一次 | 稳(纯色整比较) | "某块该是红/某角该透明"这类局部几何 |
| **3 golden** | 整图与入库基准逐像素比对 | 基准图 + 确定性内容 | 只对确定性内容稳 | 几何/渐变/裁剪等视觉回归快照 |
| **artifact** | 输出 PNG 供人眼检视 | 无断言 | —— | 文本排版等"看感觉"的场景 |

**禁止进 golden 的内容**:OS 字体渲染的文本、外部/网络图片、随机/时变输出(时间戳、随机种子、硬件相关)。原因:它们在不同机器/平台不可逐像素复现,会把"环境噪声"当成回归。

**文本类一律不进 golden**:见[第 7 节](#7-文本类怎么测不进-golden)。

---

## 3. 渲染桥:从场景拿到像素/布局

所有层都从"一段 widget 场景 或 一段画布 painter"开始。`com.muedsa.snapshot` 提供:

```kotlin
// 只布局,不画图(第 1 层入口)
fun rootLayout(content: Widget.() -> Unit): LayoutNode

// widget 场景 → 像素 / 图像(第 2/3 层入口)
fun snapshotPixels(background: Int = Color.WHITE, debug: Boolean = false, content: Widget.() -> Unit): Pixmap
fun snapshotImage(background: Int = Color.WHITE, debug: Boolean = false, content: Widget.() -> Unit): Image

// 裸画布场景(直接操作 Canvas)→ 像素 / 图像
fun painterPixels(width: Float, height: Float, background: Int = Color.WHITE, painter: (Canvas) -> Unit): Pixmap
fun painterImage(width: Float, height: Float, background: Int = Color.WHITE, painter: (Canvas) -> Unit): Image
```

- `snapshot*` 会把 widget 内容布局到**与其根尺寸一致**的画布上再渲染。
- 需要**透明底**时显式传 `background = Color.TRANSPARENT`(例如测半透明内容)。
- `Pixmap` 引用底层快照内存,应在同一测试内立即读取。

> 旧测试沿用的 `drawWidget` / `drawPainter` 依然可用(写入 `build/test-results/test-image-outputs/`),但它们只"画图不比对",新测试请优先用上面的分层入口。

---

## 4. 布局/数值断言(第 1 层)

对 widget 内容做一次无约束根布局,得到只读自省树根 [`LayoutNode`](../../core/src/main/kotlin/com/muedsa/snapshot/rendering/LayoutTree.kt)。每个节点暴露:

- `size: Size`(读 `definiteSize`)
- `absoluteOffset: Offset`(相对根盒的累积偏移)
- `rect: Rect`(`absoluteOffset` + `size`)
- `children` / `parent`
- `renderBox: RenderBox`(原始渲染对象,可 `is` 判型)

常用断言与查找(均为 `com.muedsa.snapshot` 顶层):

```kotlin
fun assertApproxEq(actual: Float, expected: Float, tolerance: Float = precisionErrorTolerance)
fun LayoutNode.assertSize(width: Float, height: Float, tolerance: Float = precisionErrorTolerance)
fun LayoutNode.assertGlobalRect(left: Float, top: Float, width: Float, height: Float,
                                tolerance: Float = precisionErrorTolerance)
fun LayoutNode.firstMatching(predicate: (RenderBox) -> Boolean): LayoutNode?
inline fun <reified T : RenderBox> LayoutNode.findType(noinline where: (T) -> Boolean = { true }): LayoutNode?
```

**示例(E1/ColoredBoxTest 的数值部分)**:`Container` 200×200 居中 100×100 的纯色盒,断言内盒全局矩形:

```kotlin
import com.muedsa.geometry.BoxAlignment
import com.muedsa.snapshot.assertGlobalRect
import com.muedsa.snapshot.assertSize
import com.muedsa.snapshot.findType
import com.muedsa.snapshot.rootLayout
import com.muedsa.snapshot.rendering.box.RenderColoredBox
import org.jetbrains.skia.Color
import kotlin.test.Test

class MyBoxTest {

    @Test
    fun inner_box_is_centered() {
        val root = rootLayout {
            Container(width = 200f, height = 200f, alignment = BoxAlignment.CENTER, color = 0xFF808080.toInt()) {
                SizedBox(width = 100f, height = 100f) {
                    ColoredBox(color = Color.RED)
                }
            }
        }
        root.assertSize(200f, 200f)
        val inner = checkNotNull(root.findType<RenderColoredBox> { it.color == Color.RED })
        inner.assertGlobalRect(50f, 50f, 100f, 100f)
    }
}
```

要点:
- 浮点断言统一带容差(默认 `precisionErrorTolerance = 1e-10`),不要用 `==` 比 Float。
- 用 `findType<RenderX> { 谓词 }` 精确定位节点,比"按位置数第几个子"更抗结构变化。
- 数值层**不栅格化**,是第 1 选择;凡能被布局数值刻画的行为都应先断言数值。

---

## 5. 像素采样断言(第 2 层)

对渲染好的 `Pixmap` 做**局部**颜色断言,无需任何基准文件。API(`com.muedsa.snapshot`):

```kotlin
fun expectColorAt(pixmap: Pixmap, x: Int, y: Int, expectedColor: Int)          // 精确相等
fun Pixmap.regionStats(rect: Rect): RegionColorStats                           // 统计(见下)
fun expectRegionOpaque(pixmap: Pixmap, rect: Rect)                              // 区域内无全透明像素
fun expectRegionTransparent(pixmap: Pixmap, rect: Rect)                         // 区域内无不透明像素
fun expectRegionUniform(pixmap: Pixmap, rect: Rect, expectedColor: Int,
                        channelTolerance: Int = 0)                              // 逐通道容差,空区域通过

data class RegionColorStats(pixelCount: Int, opaqueCount: Int,
                            transparentCount: Int, averageColor: Int)           // 平均色为 0xAARRGGBB
```

颜色格式:`Pixmap.getColor` 返回与图像颜色类型一致的颜色;`expectColorAt` / `expectRegionUniform` 的 `expectedColor` 按 `0xAARRGGBB` 传入。**对含反走样/半透明的像素**,不要用整色精确比较,改用区域断言(它们对 alpha 语义有清晰定义,见源码 KDoc)。

**示例(E1 的采样部分)**:同一场景,断言四角是背景灰、中心是该色:

```kotlin
import com.muedsa.snapshot.expectColorAt
import com.muedsa.snapshot.snapshotPixels

@Test
fun pixel_probe() {
    val background = 0xFF808080.toInt()
    val pixmap = snapshotPixels {
        Container(width = 200f, height = 200f, alignment = BoxAlignment.CENTER, color = background) {
            SizedBox(width = 100f, height = 100f) {
                ColoredBox(color = Color.RED)
            }
        }
    }
    expectColorAt(pixmap, 1, 1, background)
    expectColorAt(pixmap, 198, 198, background)
    expectColorAt(pixmap, 100, 100, Color.RED)
}
```

注意采样坐标避开抗锯齿边缘(选在纯色内部),否则需用区域断言并接受边缘像素的过渡色。

---

## 6. golden 基准比对(第 3 层)

把"可确定复现"的场景渲染成图,与**提交入库的基准图**逐像素比对;失配即失败并产出 actual/diff 供排查。API(`com.muedsa.snapshot`):

```kotlin
fun golden(id: String, background: Int = Color.WHITE, content: Widget.() -> Unit)   // widget 整树
fun goldenPixels(id: String, width: Float, height: Float, background: Int = Color.WHITE,
                 painter: (Canvas) -> Unit)                                          // 裸画布
fun assertImageMatchesBaseline(image: Image, id: String,
                              perPixelTolerance: Int = 0, allowMismatchRatio: Double = 0.0)  // 直接比一张 Image
```

**三态**由模式控制,默认 `verify`;见[第 10 节](#10-运行与-golden-三态命令):

- `verify`:要求基准存在并逐像素比对;失配抛 `AssertionError`,并写 `core/build/test-results/golden/<id>/actual.png` 与 `diff.png`(差异像素标红)。
- `record`:仅当基准**不存在**时写入 `core/src/test/resources/golden/<id>.png`;已存在会报错(逼你显式走 update)。
- `update`:无条件覆盖同名基准(先人工审阅 actual 再更新)。
- 比对指标:任一 RGBA 通道差 > `perPixelTolerance`(默认 0)记 changed;changed 占比 > `allowMismatchRatio`(默认 0)即失配。需要对抗锯齿确定性几何放宽容差时用 `assertImageMatchesBaseline(image, id, perPixelTolerance, allowMismatchRatio)`。

**示例(E4/ClipGoldenTest)**:整树 + `golden`:

```kotlin
import com.muedsa.geometry.BoxAlignment
import com.muedsa.snapshot.golden
import org.jetbrains.skia.Color
import kotlin.test.Test

class MyClipTest {
    @Test
    fun clip_oval_golden() {
        golden("widget/clip_oval_green") {
            Container(width = 300f, height = 300f, alignment = BoxAlignment.CENTER, color = Color.WHITE) {
                ClipOval {
                    Container(width = 200f, height = 200f, color = Color.GREEN)
                }
            }
        }
    }
}
```

**示例(E3 的半透明渐变)**:裸画布 + `goldenPixels`,且显式透明底以保留 alpha:

```kotlin
import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import com.muedsa.snapshot.goldenPixels
import org.jetbrains.skia.Color
import org.jetbrains.skia.Paint
import kotlin.test.Test

class MyGradientTest {
    @Test
    fun linear_translucent_golden() {
        val size = Size(600f, 200f)
        goldenPixels("gradient/linear_translucent", size.width, size.height, background = Color.TRANSPARENT) {
                canvas ->
            val gradient = LinearGradient(
                begin = BoxAlignment.TOP_LEFT,
                end = BoxAlignment.BOTTOM_RIGHT,
                colors = intArrayOf(0xFF0000FF.toInt(), 0x8000FF00.toInt()), // 半透明停点(alpha=128)
            )
            val rect = Offset.ZERO combine size
            canvas.drawRect(rect, Paint().apply { shader = gradient.createShader(rect) })
        }
    }
}
```

### golden 背后的确定性契约

引擎先把实际图与基准各自光栅化到 `N32 premul` 再逐像素比较(`GoldenEngine` 会先 `clear(Color.TRANSPARENT)`,保证半透明/AA 像素不依赖未初始化底色)。因此:

- 同一代码 + 同一 skiko 版本 → 逐像素稳定;
- **升级 skiko / 改动渲染后基准需要重录**(`update`),先人工确认视觉无误;
- 混入 OS 字体 / 外网 / 随机会破坏契约——不要这么做。

---

## 7. 文本类怎么测(不进 golden)

> **内置测试字体(2026-09-09 起)**:文本测试**一律显式指定 `testTypeface`**(来自 `:testkit`,底层是仓库内置的 Noto Sans SC,OFL-1.1)。原因:文本度量依赖 OS 字体,连"关系断言"也会闪断——实测同一条右对齐断言在 Windows 通过、Linux CI 失败(side bearing 不同)。显式指定后度量跨平台一致,容差可收紧且有据可依。
>
> ```kotlin
> import com.muedsa.snapshot.testTypeface
> TextSpan("Hello", style = TextStyle(fontSize = 20f, typeface = testTypeface))
> ```
>
> 例外:**emoji** 字形不在该字体覆盖范围内,`emoji_test` 不指定 typeface,断言只做"尺寸 > 0",渲染效果看 artifact。

文本测量受 OS 字体/排版引擎影响,无法跨机逐像素复现。正确姿势:**数值区间/单调断言 + artifact 人眼检视**。

常用度量(先 `layout` 再读取,见 [`TextPainter`](../../core/src/main/kotlin/com/muedsa/snapshot/paint/text/TextPainter.kt)):

```kotlin
painter.width                 // 布局内容宽
painter.height                // 内容高
painter.maxIntrinsicWidth     // 最宽行天然宽
painter.didExceedMaxLines     // 是否超过 maxLines
```

**示例(E5/TextMetricsTest)**:区间断言,刻意宽松、不锁字形:

```kotlin
package com.muedsa.snapshot.paint.text

import com.muedsa.snapshot.drawPainter
import com.muedsa.geometry.Offset
import kotlin.test.Test
import kotlin.test.assertTrue

class TextMetricsTest {
    // 文本测量受 OS 字体影响,本类用保守区间、不锁具体字形/像素;
    // 勿把区间收紧为某台机器量到的绝对值,否则跨 OS 闪断;目检见 build/test-results/test-image-outputs/...

    private fun layoutLine(text: String): TextPainter = TextPainter(
        text = TextSpan(text = text, style = TextStyle(fontSize = 20f))
    ).apply { layout(0f, Float.POSITIVE_INFINITY) }

    @Test
    fun single_line_metrics_are_positive_and_bounded() {
        val p = layoutLine("Hello Word!")
        assertTrue(p.width > 0f, "单行宽应为正,实际 ${p.width}")
        assertTrue(p.height in 1f..(20f * 3f), "fontSize=20 单行高在 1..3em,实际 ${p.height}") // 仅宽松量级校验
        assertTrue(p.maxIntrinsicWidth >= p.width, "maxIntrinsicWidth(${p.maxIntrinsicWidth}) >= width(${p.width})")
    }

    @Test
    fun multiline_height_grows_with_line_count() {
        val single = layoutLine("line one")
        val multi = TextPainter(
            text = TextSpan(text = "line one\nline two\nline three", style = TextStyle(fontSize = 20f))
        ).apply { layout(0f, 400f) }
        assertTrue(multi.height > single.height, "多行高(${multi.height})应大于单行高(${single.height})")
        assertTrue(multi.width <= 400f, "受 layout 宽度约束,实际 ${multi.width}")
    }

    @Test
    fun artifact_for_eyeball_review() {
        val p = layoutLine("Hello Word! 你好,世界!")
        drawPainter("paint/text/text_metrics", width = p.width, height = p.height) { canvas ->
            p.paint(canvas, offset = Offset.ZERO)
        }
    }
}
```

规则:
- 只做**正值 / 量级 / 单调**(多行随行数增高、受宽度约束)这类断言;
- 不做**具体字形 / 具体像素**断言;
- 想给人看,用 artifact(见下节)。

---

## 8. artifact:仅供人眼检视的输出

沿用兼容层提供"只落盘、不比对"的输出,产物在 `core/build/test-results/test-image-outputs/`(Gradle 下相对模块目录):

```kotlin
import com.muedsa.snapshot.drawWidget      // widget 场景 → PNG
import com.muedsa.snapshot.drawPainter     // 画布场景 → PNG
import com.muedsa.snapshot.getTestPngFile  // 拿到输出文件,自行 writeBytes

drawPainter("paint/text/text_metrics", width = 300f, height = 60f) { canvas -> ... }
```

- 用于人眼排版检查、临时调试输出、老测试的观察点;
- **不作为 CI 回归判据**(没有自动比对);
- 目录 `…/test-image-outputs/` 与 golden 失配产物目录 `…/build/test-results/golden/<id>/` 是两回事,别混。

---

## 9. 编写新功能测试:分步指南

假设要为一个新 Widget(例如 `MyWidget`)写测试。

1. **想清楚"能断言什么"**
   先给布局数值能刻画的(尺寸/对齐/子位置)列出来 → 走第 1 层;若需要"这块区域是什么颜色",补第 2 层采样;若要整图回归且内容是确定几何 → 补第 3 层 golden。
2. **定文件位置与类**
   `core/src/test/kotlin/com/muedsa/snapshot/<area>/MyWidgetTest.kt`,包名匹配目录;类名 `XxxTest`。一个测试类聚焦一个组件。
3. **写场景 helper,再加断言**
   复用同一段场景 builder,分别喂给 `rootLayout`(数值)与 `snapshotPixels`/`golden`(像素),断言少而准、无 `println`。
4. **跑单个测试**
   ```bash
   ./gradlew :core:test --tests 'com.muedsa.snapshot.<area>.MyWidgetTest' --console=plain
   # 只跑某个方法:
   ./gradlew :core:test --tests 'com.muedsa.snapshot.<area>.MyWidgetTest.someMethod' --console=plain
   ```
   若失败,先判断是"期望值算错"还是"框架没读到"(打印节点 `rect`/采样值定位)。
5. **新 golden:id 即路径**
   - 基准路径 = `core/src/test/resources/golden/<id>.png`,`<id>` 用 `area/描述`(小写下划线);
   - 先 `record` 生成 → 打开 PNG 目检 → 把测试与基准一起提交;
   - 此后日常跑默认 `verify`;因实现变更需更新时走 `update`(先看 actual 再覆盖)。
6. **跑整类 + 全量确认**
   ```bash
   ./gradlew :core:test --tests 'com.muedsa.snapshot.<area>.MyWidgetTest' --console=plain
   ```
   提交流程按仓库约定(GPG 签名、docs 先行)。

---

## 10. 运行与 golden 三态命令

默认 `./gradlew test` 对已有基准全部走 **verify**。三态:

```bash
# 1) 生成新基准(仅当基准不存在;已存在会报错,逼你显式走 update)
./gradlew :core:test -PsnapshotTest.mode=record --tests '*MyWidgetTest' --console=plain

# 2) 默认 verify(日常回归)
./gradlew :core:test --tests '*MyWidgetTest' --console=plain

# 3) 覆盖同名基准(更新前请人工审阅 actual)
./gradlew :core:test -PsnapshotTest.mode=update --tests '*MyWidgetTest' --console=plain
```

- 模式经 `-P`(Gradle 项目属性)由 core/parser 的 `tasks.test` 透传为测试 JVM 系统属性;IDE 直接跑可用环境变量 `SNAPSHOT_TEST_MODE=record|verify|update`。
- **只录一个新方法**时,用方法级过滤 `--tests '…ClassName.methodName'`,避免 record 撞上其它已存在的基准而报错。
- 失配产物:`core/build/test-results/golden/<id>/{actual.png,diff.png}`;`diff.png` 把差异像素在 actual 上标红。
- 模块差异:parser 测试的对应产物在 `parser/build/...`;Gradle 下模块工作目录即模块目录,路径以此为准。

---

## 11. 约定与命名

- **测试文件**:`core/src/test/kotlin/com/muedsa/snapshot/<area>/…Test.kt`,类名以 `Test` 结尾,包名匹配 `<area>` 目录。parser 测试在 `parser/src/test/...`;**框架自身的边界单测**在 `testkit/src/test/kotlin/com/muedsa/snapshot/`(如 `SamplingAssertionsBoundaryTest`,锁定采样/区域断言的边界语义)。
- **golden 基准**:`core/src/test/resources/golden/<id>.png`,`<id>` 与测试包/文件层级呼应(`widget/clip_oval_green`、`paint/gradient/linear_two_stop`),测试与基准图**同一提交**原子入库。
- **断言风格**:新/迁移测试统一 `kotlin.test`(`@Test`/`assertTrue`/`assertFailsWith`);不引入 `org.junit.jupiter`。
- **标签例外**:`@Tag` 来自 `org.junit.jupiter.api`(kotlin.test 无标签注解)。现有两类:`sample`(样例再生成)与 `network`(依赖外网的用例,如网络图片缓存、`CachedNetworkImage`、含 `ImageEmojiSpan`/`<Emoji>` 的测试)。默认 `./gradlew test` **排除**这两类;显式运行用 `-PincludeSamples` / `-PincludeNetwork`(可同时给出,取并集)。
- **无 `println`**:失败信息靠断言消息表达。
- **确定性原则**(代码与文档一致):golden 只装"纯几何/渐变/本地位图/纯 shader"等可确定复现内容。

---

## 12. 常见误区 / FAQ

**Q: 我的测试明明画了图却不失败?**
旧的 `drawWidget`/`drawPainter` 只写 PNG 不比对。要用断言层:数值用 `rootLayout`+`assertGlobalRect`;整图回归用 `golden`;局部颜色用 `snapshotPixels`+`expectColorAt`。

**Q: 单行高写死成具体值,换台机器挂了怎么办?**
文本测量跨 OS 不同。改回区间/单调断言(见第 7 节),不要按单机值收紧。

**Q: record 报 "already exists" 是坏了吗?**
不是。`record` 只在基准不存在时写入,防止误覆盖;确认要用新渲染替换旧基准时改走 `update`。

**Q: 为什么 verify 报 "baseline missing"?**
基准文件不在测试 classpath(`src/test/resources/golden/<id>.png` 未提交或未复制)。先 `record` 生成再提交;若改了 `snapshotTest.goldenRoot`,注意它**只用于 record/update 写入**,verify 一律从 classpath 读(读写不对称)。

**Q: 改了个颜色,好几百个 golden 全挂了?**
预期现象:引擎按失配比例报错并产 actual/diff。逐个审阅后,确认是"有意变更"就 `update`;是回归就修代码。

**Q: `Pixmap` 会不会悬垂?**
`snapshotPixels`/`painterPixels` 返回的 `Pixmap` 引用底层快照内存,随内部 Surface/Image 生命周期。请在返回后同一测试内立即读取,不要跨测试保存。

**Q: 想给渲染路径一个失败期会崩溃/异常的单测?**
golden 失配自动抛 `AssertionError` 并落 actual/diff,本身就是失败路径;也可以用 `assertFailsWith<AssertionError> { golden(...) { 故意不同内容 } }` 来锁定"该 id 基准确实会抓变化"(参考 `GoldenVerifyRegressionTest`)。注意此类测试只在默认 `verify` 下有意义(record/update 会污染),记得在方法内跳过非 verify 模式。

**Q: parser 模块要测纯解析,用得上这些吗?**
大部分纯解析测试不需要。若需要渲染解析产物(把解析结果画成图),parser 测试也已 `testImplementation(:testkit)`,同一套入口可用。

**Q: CI 在意工作树被 test 弄脏?**
仓库根有两个演示类(`Sample.kt`/`ParserSample.kt`)每次全量 test 会把示例 PNG 写到仓库根(既有行为)。CI 可用 `--tests` 排除演示类,或把其输出改到 build 目录(另见仓库 issues)。

---

## 13. 活模板(照抄起点)

按形态挑最近的现成测试抄:

| 想测什么 | 看谁 |
|---|---|
| 布局/数值 + 局部采样 | `core/src/test/.../widget/ColoredBoxTest.kt`(E1) |
| 布局/数值(flex 对齐) | `core/src/test/.../widget/ColumnTest.kt`(E2) |
| painter 裸画 + golden(渐变) | `core/src/test/.../paint/gradient/GradientGoldenTest.kt`(E3) |
| widget 整树 + golden(裁剪) | `core/src/test/.../widget/ClipGoldenTest.kt`(E4) |
| golden 失配(默认 verify 才会跑) | `core/src/test/.../widget/GoldenVerifyRegressionTest.kt` |
| 文本区间 + artifact | `core/src/test/.../paint/text/TextMetricsTest.kt`(E5) |

> 补充阅读:框架设计文档 `docs/superpowers/specs/2026-09-07-testkit-design.md`,实现计划 `docs/superpowers/plans/2026-09-07-testkit-migration.md`。
