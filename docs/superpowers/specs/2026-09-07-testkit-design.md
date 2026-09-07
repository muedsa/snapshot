# 测试框架(TestKit)与单元测试体系重构设计

日期:2026-09-07
状态:设计(已逐节与用户确认)

## 背景与动机

`snapshot` 是 Flutter 风格的声明式 UI + skia 渲染库(core 模块)+ XML 解析(parser 模块)。当前测试体系的问题:

1. **绝大多数渲染测试是"冒烟测试"**:`widget/*`、`render/*`、`paint/*` 的测试(`ColoredBoxTest`/`ClipTest`/`TextPainterTest`/`LinearGradientTest` 等)只是把渲染结果写成 PNG 到 `build/test-results/test-image-outputs/`,**不比对任何基准**,能抓崩溃、抓不住视觉回归。仅少数(`BoxConstraintsTest`/`OpacityLayerTest`/`ClipPathLayerTest`)有真实断言。
2. **测试基础设施在 core 与 parser 各复制一份**:两份 `TestTool.kt`(core 的更全,parser 的最小)内容高度重合。
3. **断言与注解框架混用**:`kotlin.test.Test` 与 `org.junit.jupiter.api.Test`/`assertThrows` 并存。
4. **外部/网络依赖混入默认 suite**:`CachedNetworkImageTest`、`ColorFilteredTest` 等直连 `samples-files.com`/`flutter.github.io`,慢、抖、离线必挂。
5. **迁移遗留无回归网**:skiko `0.0.0-SNAPSHOT` 迁移(已在 main)遗留 2 个"视觉微确认"(`Color4f(int)` 非预乘语义、`Gradient.Interpolation` 默认的 `colorSpace/hueMethod`),因无像素断言无法自动验证。
6. 大量 `println` 调试噪音。

用户决定:**完全重构单元测试体系,优先搭建一套可复用的测试框架**,让"为后续新功能添加功能测试"成为顺滑的填空式动作。

## 目标与完成标准(本阶段)

目标:
1. 新增共享模块 `:testkit`,承载全部测试基础设施(core/parser 及未来模块复用,不再各写一份)。
2. 在 testkit 提供**两层断言 DSL**(布局/数值层 + 像素/基准层)+ 采样断言 + artifact 输出,并配作者手册,让新功能测试可顺滑添加。
3. 迁移 4~5 个代表性用例到新框架作为**示范**(E1–E5,见下),覆盖 布局数值 / 采样 / 确定性 golden / 文本区间 四种形态。
4. 本阶段**不动**其余旧测试(继续可跑、全绿);其整体迁移列为后续阶段。

完成标准(验收):
- [ ] `:testkit` 编译通过;core/parser 测试依赖它。
- [ ] `./gradlew test`(core + parser)**全绿**:含新示范 E1–E5 与未迁移的旧测试。
- [ ] golden 三态本地验证:`record` 新 id → `verify` 通过;人为改色 → `verify` 报失配并产出 actual/diff → `update` 吸收 → 再 `verify` 通过。
- [ ] `./gradlew jar` 通过。
- [ ] 作者手册 `docs/testing/README.md` 就位。
- [ ] 提交按 docs-在前 分组、GPG 签名,推分支并开 PR(分支 `feat/testkit-framework`,base=main)。

## 已确认的做法决策(逐条)

1. 重构目标 = 框架 + 样例迁移(非全量迁移、非仅框架)。
2. 核心断言形态 = **布局/数值断言 与 像素/基准比对 两层并重分层**:数值层快、稳、跨平台;像素层兜底视觉回归。
3. 像素/基准层"信任模型" = **确定性内容入库比对**:全图基准只用于可确定复现的内容(纯几何/渐变/本地位图/纯 shader);文本(OS 字体渲染)、外部网络图片**不进**像素门禁——前者走数值/采样或 artifact 人眼检视。
4. 共享层形态 = **独立 Gradle 模块 `:testkit`**(main 源集承载框架);core/parser 测试 `testImplementation(project(":testkit"))`。
5. 数值层的布局遍历绕过 core `internal` 封装 = **在 core main 加最小公开布局自省 API**(`LayoutNode` + 树构建),见"产品面变更"。
6. 本阶段新写/迁移测试统一 `kotlin.test`;不引入 `org.junit.jupiter`。
7. 沿用上轮交付约定:新分支 `feat/testkit-framework`(base=main);commit docs 在前 + 代码;全部 GPG 签名;推送后开 PR。

## 架构总览

```
依赖方向(编译期,均为 main 对 main):
  :parser main ──implementation──▶ :core main
  :testkit main ──implementation──▶ :core main      (新)
测试依赖:
  :core test  ──testImplementation──▶ :testkit
  :parser test──testImplementation──▶ :testkit
运行时 skiko 原生库:core/parser 各自 testImplementation 拉对应 OS/arch runtime(现状不变)。

:testkit 包结构(全部 main):
  com.muedsa.snapshot         沿用兼容层:drawWidget / drawPainter / getTestPngFile /
                              renderBoxToPixels / layerToPixels / pictureToPixels …(由旧 TestTool.kt 逐字搬入)
  com.muedsa.snapshot.testkit golden 引擎等内部实现(不面向测试文件直接 import)
  根 DSL 入口函数(snapshotPixels / rootLayout / golden / …)放 com.muedsa.snapshot,减少测试 import。
```

- 数值层几何读取依赖 core main 新增的公开 `LayoutNode`(产品面,见后)。
- golden 基准图提交于各模块 `src/test/resources/golden/<id>.png`;actual/diff 落 `build/`,不入库。

## 涉及文件(逐文件改动)

### A. Gradle 布线

- **Modify** `settings.gradle.kts`:追加 `include(":testkit")`。
- **Create** `testkit/build.gradle.kts`:与 core 同款 JVM/Kotlin 插件与 `group/version`;依赖 `implementation(project(":core"))`(skiko-awt 经 core 的 `api` 传递);不配测试任务、不拉 skiko 运行时。
- **Modify** `core/build.gradle.kts`:`dependencies` 增 `testImplementation(project(":testkit"))`;`tasks.test` 增
  ```kotlin
  systemProperty("snapshotTest.mode", providers.gradleProperty("snapshotTest.mode").getOrElse("verify"))
  systemProperty("snapshotTest.goldenRoot", providers.gradleProperty("snapshotTest.goldenRoot").orNull)  // 可选覆盖
  ```
- **Modify** `parser/build.gradle.kts`:同上两行(testImplementation + mode/goldenRoot 透传)。

### B. 产品面变更(core main,最小公开自省 API)

**Create** `core/src/main/kotlin/com/muedsa/snapshot/rendering/LayoutTree.kt`:

```kotlin
package com.muedsa.snapshot.rendering

import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import org.jetbrains.skia.Rect

/** 只读布局自省:给出 layout 后每个节点的全局几何。无行为变更、不参与渲染。 */
class LayoutNode internal constructor(
    val renderBox: RenderBox,
    val offsetFromParent: Offset,   // = child.parentData 的 offset(实现期确认父类层级后读取)
    parent: LayoutNode?,
    children: List<LayoutNode>,
) {
    val parent: LayoutNode? = parent
    val children: List<LayoutNode> = children
    val size: Size get() = renderBox.definiteSize
    val absoluteOffset: Offset = (parent?.absoluteOffset ?: Offset.ZERO) + offsetFromParent
    val rect: Rect get() = absoluteOffset combine size
}

/** 建树。children 规则:RenderContainerBox → children 逐递归;RenderSingleChildBox → child 非空递归;其它 → 空。 */
fun RenderBox.toLayoutNode(): LayoutNode
```

要点:
- 只读、无行为变更;纯为测试/调试提供"摆好的树 + 全局几何"。
- 偏移语义:子节点经 `parentData!!.offset` 定位(与 `RenderContainerBox.defaultPaint` 一致);根 offset 为 ZERO。
- 对 `StackParentData`/`Positioned` 等非 `BoxParentData` 偏移模型,若实现期发现 offset 不落在该字段,该族数值测试降级(见 E 与"非目标"),不为此扩产品面。
- **实现期确认**:`ContainerBoxParentData` 是否继承 `BoxParentData`(影响 `as? BoxParentData` 判定);未继承时以编译/运行提示微调读取路径。

### C. 沿用兼容层搬迁(去重且不破编译)

把旧 helper **原样搬进 testkit main、包名保持 `com.muedsa.snapshot`**,使现有测试文件的 import 全部不用改即可继续编译,随后删掉两份重复源文件:

- **Move** core `src/test/.../com/muedsa/snapshot/TestTool.kt` 全部内容(顶层 `noLimitedLayout`、`testImagesDirection`、`rootDirection`、`getTestPngFile`、`drawWidget`、`drawPainter`×2、`renderBoxToPixels`、`painterToPicture`、`layerToPixels`、`layersToPixels`、`pictureToPixels` 及 import)→ `testkit/src/main/kotlin/com/muedsa/snapshot/TestTool.kt`(同名函数,package 不变)。
- **Delete** `core/src/test/kotlin/com/muedsa/snapshot/TestTool.kt`。
- **Delete** `parser/src/test/kotlin/com/muedsa/snapshot/TestTool.kt`(其 `getTestPngFile`/`testImagesDirection`/`rootDirection` 与 core 版本同签名同语义,并入上一条;parser 测试 `import com.muedsa.snapshot.getTestPngFile` 无需改动)。

### D. testkit DSL 与 golden 引擎(新文件)

全部位于 `testkit/src/main/kotlin/com/muedsa/snapshot/testkit/`(golden 引擎内部实现),根 DSL 函数放 `com.muedsa.snapshot` 包。

**D1. 渲染桥(com.muedsa.snapshot)**

```kotlin
fun snapshotPixels(background: Int = Color.WHITE, debug: Boolean = false,
                   content: Widget.() -> Unit): Pixmap   // 复用 layoutWidget + PaintingContext.paintRoot + LayerPaintContext.composite + Surface.makeRasterN32Premul + peekPixels
fun painterPixels(width: Float, height: Float, background: Int = Color.WHITE,
                  painter: (Canvas) -> Unit): Pixmap     // 由既有 painterToPicture/画布绘制封装
```

**D2. 数值/采样层(com.muedsa.snapshot)**

```kotlin
fun rootLayout(content: Widget.() -> Unit): LayoutNode                 // layoutWidget → toLayoutNode
fun assertApproxEq(actual: Float, expected: Float,
                   tolerance: Float = precisionErrorTolerance)          // 抛 AssertionError,信息含 actual/expected/tolerance
fun LayoutNode.assertSize(width: Float, height: Float,
                          tolerance: Float = precisionErrorTolerance)
fun LayoutNode.assertGlobalRect(left: Float, top: Float, width: Float, height: Float,
                                tolerance: Float = precisionErrorTolerance)
inline fun <reified T : RenderBox> LayoutNode.findByType(): LayoutNode?  // 前序查找(renderBox is T)
fun expectColorAt(pixmap: Pixmap, x: Int, y: Int, expectedColor: Int)   // 失败信息含 actual
fun Pixmap.colorStats(rect: Rect): RegionColorStats                     // 平均色/非透明计数/透明计数
fun expectRegionOpaque(pixmap: Pixmap, rect: Rect)
fun expectRegionTransparent(pixmap: Pixmap, rect: Rect)
fun expectRegionUniform(pixmap: Pixmap, rect: Rect, expectedColor: Int, channelTolerance: Int = 0)
```

> 数值/采样断言抛 `AssertionError`(java 内建),JUnit 正常判失败;不依赖 `kotlin.test`(testkit main 免测框架依赖)。

**D3. golden 层(com.muedsa.snapshot + testkit 内部引擎)**

```kotlin
// 便捷形态 ① 整棵 widget;② 裸 painter 场景;③ 底层任意像素/图
fun golden(id: String, background: Int = Color.WHITE, content: Widget.() -> Unit)
fun goldenPixels(id: String, width: Float, height: Float, background: Int = Color.WHITE,
                 painter: (Canvas) -> Unit)
fun assertPixmapMatchesBaseline(pixmap: Pixmap, id: String,
                                perPixelTolerance: Int = 0, allowMismatchRatio: Double = 0.0)
fun assertImageMatchesBaseline(image: Image, id: String,
                               perPixelTolerance: Int = 0, allowMismatchRatio: Double = 0.0)
```

内部引擎(testkit 包):
- 路径约定:`<id>` 形如 `widget/clip/star` → 基准 `<模块>/src/test/resources/golden/widget/clip/star.png`;classpath 资源 `/golden/<id>.png`。
- **模式**:系统属性 `snapshotTest.mode` ∈ `verify`(默认)/`record`/`update`;兼容环境变量 `SNAPSHOT_TEST_MODE`。
  - `verify`:基准缺失 → 直接失败;读基准(PNG decode 到 N32Premul Pixmap 统一格式)→ 逐像素比对 → 失配写 `build/test-results/golden/<id>/actual.png`、`diff.png`(差异像素标红)并抛错(信息含 changed 比例、两文件路径)。
  - `record`:渲染 actual → 写 `src/test/resources/golden/<id>.png`;若文件已存在 → 抛错(要求显式 `update`)。写完打印路径。不比对。
  - `update`:渲染 actual → 覆盖同名基准。不比对。
  - 写源树路径解析:默认 `File("src/test/resources/golden")`(Gradle 下 test 工作目录 = 模块目录);可用 `snapshotTest.goldenRoot` 覆盖。
- **比对指标**:RGBA 各通道差 > `perPixelTolerance`(0..255,默认 0)计为 changed;changed 像素占比 > `allowMismatchRatio`(0..1,默认 0)即失配。图像尺寸不一致直接失配。
- **确定性原则**(文档+代码注释):文本(OS 字体)、外网、随机、时变内容禁止进 golden。

**D4. 顶层命名冲突检查**:testkit main 新增/搬迁的所有顶层名字不得与 core main `com.muedsa.snapshot` 现有顶层(`Snapshot`/`SnapshotImage`/`SnapshotPNG`/`SnapshotWEBP`/`layoutWidget`/`drawRenderBox`/`precisionErrorTolerance`/`debugCurrentRepaintColor`)重名。已核对:无冲突。

### E. 示范用例(E1–E5)

| # | 动作 | 文件 | 形态要点 |
|---|---|---|---|
| E1 | **迁移** `core/src/test/.../widget/ColoredBoxTest.kt` | 布局 + 采样 | 场景 `Container(200×200, alignment=CENTER){ SizedBox(100×100){ ColoredBox(c) } }`,背景 WHITE。断言:ColoredBox 的 LayoutNode `assertGlobalRect(50,50,100,100)`;`snapshotPixels` 后 `expectColorAt` 四角 WHITE、中心 c;遍历 BLACK/WHITE/RED/GREEN/BLUE/YELLOW/CYAN/MAGENTA。去除 debug 文本与 `println`。 |
| E2 | **迁移** `core/src/test/.../widget/ColumnTest.kt` | 布局/数值 | 固定约束场景(如外层 200×200 + Column 内 SizedBox(100×30)、SizedBox(50×40)):断言根尺寸、各子 `assertGlobalRect` 与主轴/交叉对齐偏移、flex 分配后的子宽高。数值先按语义推导、再运行校准,不臆测。 |
| E3 | **新增** `core/src/test/.../paint/gradient/GradientGoldenTest.kt` | golden | linear/radial/sweep 三场景(纯色两停点,600×200 或方形,无文本)→ `golden`/`goldenPixels` 入库。直接为上次迁移的 `Color4f(int)` premul 与 `Interpolation` 默认两个微确认上回归网。 |
| E4 | **新增** `core/src/test/.../widget/ClipGoldenTest.kt` | golden | 确定性裁剪几何(纯色 + 抗锯齿边),示范 `perPixelTolerance`/`allowMismatchRatio` 用法;含一个低容差与一个高容差断言。 |
| E5 | **新增** `core/src/test/.../paint/text/TextMetricsTest.kt` | 数值区间 + artifact | 对 `TextPainter.layout` 度量做**区间**断言(单行宽>0;高度在 `fontSize` 的合理区间;多行高度随行数累加;`maxIntrinsicWidth>=width`),并用 artifact(沿用 `getTestPngFile`)输出供人眼检视。不比对具体字形、不进 golden。 |

> 迁移 E1/E2 时,原测试的绘图形态若仍有保留价值,可保留 artifact 输出(人眼检视),但断言改为新 DSL。

### F. 文档

- **Create** `docs/testing/README.md`(作者手册):
  1. 分层选择规则(数值 / 采样 / golden / artifact 何时用,含"禁止进 golden 的内容");
  2. golden 三态命令(`./gradlew :core:test -PsnapshotTest.mode=record` 等);
  3. 最小可用示例(E3 作活模板);
  4. 新功能测试的推荐文件位置/命名。

## 验证策略

1. `./gradlew :testkit:compileKotlin` → `BUILD SUCCESSFUL`。
2. `./gradlew test`(core + parser)→ 全绿(旧测试 + E1–E5)。
3. golden 三态演示(见"完成标准"第 3 条):本地依次 record → verify → 改色 verify(应失配,看 actual/diff)→ update → verify。
4. `./gradlew jar` → `BUILD SUCCESSFUL`。
5. 静态核对:两份 `TestTool.kt` 已删;core main 仅新增 LayoutTree(只读);无 `org.junit.jupiter` 进入新/迁移测试。

## 非目标 / 后续阶段(延后项)

- 本阶段**不迁移**其余旧冒烟测试到新框架(全量迁移单独排期)。
- **不做** `@Tag("network")` 默认排除的全局改造(先定框架与文档;后续统一执行)。
- **不**把 legacy 测试的 `org.junit.jupiter` 全局改成 `kotlin.test`;不删 `junit-jupiter-engine` 依赖(留给全量迁移阶段)。
- **不**做按 OS/架构分目录的多套基准(信任模型已定为"确定性内容"单套)。
- **不**为 Stack/Positioned 等非常规偏移模型扩产品面(若实现期受阻则其数值测试降级/改采样)。
- **不**发布 `:testkit` 独立 artifact(本阶段仅本构建内共享)。

## 参考

- core main 顶层入口:`Snapshot.kt`(`layoutWidget`/`SnapshotImage`/…)。
- render 树:`RenderBox`(`parentData`/`parent` 为 internal)、`RenderSingleChildBox.child`、`RenderContainerBox.children`、`BoxParentData.offset`、`precisionErrorTolerance=1e-10f`(`Const.kt`)。
- 既有像素断言范式:`ClipPathLayerTest`/`OpacityLayerTest`(Pixmap `getColor(x,y)`)、`renderBoxToPixels`/`layerToPixels`。
- 同仓库先例:上一轮 skiko 迁移的 spec/plan(本目录)。
