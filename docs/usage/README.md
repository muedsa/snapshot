# snapshot 使用说明

本手册面向**使用 snapshot 生成图片**的开发者：怎样搭一棵 Widget 树、怎样把它渲染成 PNG/JPEG/WebP、各个 Widget 与属性的确切含义、以及类 DOM 文本解析器的全部标签与取值格式。

> 一句话总纲：**用 Flutter 风格的 Widget 树描述版面，`Snapshot*` 把它渲染成图片；不想写 Kotlin 时，用 `:parser` 把一段类 DOM 文本解析成同一棵树。**

相关文档：

| 文档 | 面向 |
|---|---|
| 本手册 | 使用者：怎么用这套 API 出图 |
| [`README.md`](../../README.md) | 项目简介与样例配图 |
| [`docs/testing/README.md`](../testing/README.md) | 给 snapshot 写测试的作者：testkit、四层断言、golden 三态 |
| [`docs/2026-09-07-skiko-0.0.0-snapshot-api-breakage-report.md`](../2026-09-07-skiko-0.0.0-snapshot-api-breakage-report.md) | 依赖的 skiko 快照 API 变动诊断 |

目录

- [1. 它是什么](#1-它是什么)
- [2. 环境与构建](#2-环境与构建)
- [3. 快速上手](#3-快速上手)
- [4. 核心概念](#4-核心概念)
- [5. Widget 参考](#5-widget-参考)
- [6. 布局细节](#6-布局细节)
- [7. 绘制与装饰](#7-绘制与装饰)
- [8. 图像与网络缓存](#8-图像与网络缓存)
- [9. 文本与富文本](#9-文本与富文本)
- [10. 类 DOM 解析器](#10-类-dom-解析器)
- [11. 测试](#11-测试)
- [12. 常见问题](#12-常见问题)
- [附录 A:枚举取值速查](#附录-a枚举取值速查)
- [附录 B:源码索引](#附录-b源码索引)

---

## 1. 它是什么

`snapshot` 是一个 **JVM 上的声明式出图库**。它照抄 Flutter 的三件事：

1. **Widget 树**：用嵌套的组件描述版面（`Column`/`Row`/`Stack`/`Container`/`Text`/`Image`…）。
2. **约束布局**：父节点下发 `BoxConstraints`，子节点在约束内决定自己的尺寸——和 Flutter 的 Box 协议一致。
3. **绘制/图层**：`RenderBox` 把内容画到 Skia `Canvas` 上，裁剪、透明度、滤镜等以 Layer 形式合成。

底层图形能力来自 [skiko](https://github.com/JetBrains/skiko)（Skia 的 Kotlin 绑定），因此渲染结果与 Skia 一致。

### 模块结构

| 模块 | Gradle 路径 | 产出 jar | 职责 |
|---|---|---|---|
| core | `:core` | `snapshot-core` | Widget、布局、渲染、绘制、文本、几何、网络图片缓存 |
| parser | `:parser` | `snapshot-parser` | 类 DOM 文本 → core Widget 的解析器（依赖 `:core`） |
| testkit | `:testkit` | `snapshot-testkit` | 测试基础设施：布局/像素/golden 断言、渲染桥、内置测试字体 |

三者 `group = "com.muedsa.snapshot"`、`version = "0.0.0-SNAPSHOT"`。

### 两种用法

```kotlin
// ① Kotlin DSL:直接写 Widget 树
val png: ByteArray = SnapshotPNG {
    Container(width = 200f, height = 200f, color = Color.RED)
}
```

```kotlin
// ② 类 DOM 文本:标签 + 属性,解析后渲染
val text = """<Snapshot background="#FFFFFFFF"><Container color="#FF0000" width="200" height="200"/></Snapshot>"""
val png: ByteArray = Parser().parse(StringReader(text)).snapshot()
```

两条路径最终都落到同一套 `Widget` → `RenderBox` → `Canvas` 流程。

---

## 2. 环境与构建

### 运行环境

| 项 | 值 | 来源 |
|---|---|---|
| 构建工具 | Gradle Wrapper **9.7.1** | `gradle/wrapper/gradle-wrapper.properties` |
| JVM 工具链（编译） | **Java 11**（`subprojects { toolchain 11 }`） | `build.gradle.kts` |
| CI 使用的 JDK | **17**（temurin） | `.github/workflows/*.yaml` |
| Kotlin | **2.4.10** | `gradle/libs.versions.toml` |
| 图形依赖 | `org.jetbrains.skiko:skiko-awt:0.0.0-SNAPSHOT` | `gradle/libs.versions.toml` |
| 依赖仓库 | `mavenCentral()` + `https://maven.pkg.jetbrains.space/public/p/compose/dev` | `settings.gradle.kts` |

> ⚠️ skiko 用的是 **`0.0.0-SNAPSHOT`**（JetBrains compose dev 仓库的滚动快照），不是稳定版。同一份代码在不同时间拉到的快照可能编译不过——仓库里已经踩过三次（`Path` 不可变、`Shader` 渐变工厂改签名、`FontRastrSettings` 弃用），详见[诊断报告](../2026-09-07-skiko-0.0.0-snapshot-api-breakage-report.md)。**固定可复现构建时请显式锁版本。**

### 常用命令

```bash
./gradlew build                 # 编译 + 测试
./gradlew jar                   # 只产出各模块 jar（CI: Build Jar）
./gradlew test                  # 全量测试（默认排除 sample / network 标签）
./gradlew :core:test            # 只测 core
./gradlew :core:compileKotlin   # 只编译 core
./gradlew projects              # 查看模块
```

测试相关的三态命令（golden 录制/校验/更新）与标签开关见 [`docs/testing/README.md` 第 10 节](../testing/README.md#10-运行与-golden-三态命令)。速记：

```bash
./gradlew :core:test --tests 'com.muedsa.snapshot.widget.ColoredBoxTest' --console=plain
./gradlew :core:test -PsnapshotTest.mode=record --tests '*MyWidgetTest' --console=plain
./gradlew :core:test -PincludeSamples        # 只跑 @Tag("sample") 的样例再生成
./gradlew :core:test -PincludeNetwork        # 只跑 @Tag("network") 的联网用例
```

### 作为依赖引入

仓库**目前没有配置 `maven-publish`**，也没有发布到 Maven Central，所以拿不到 `implementation("com.muedsa.snapshot:snapshot-core:0.0.0-SNAPSHOT")`。三种可行方式：

1. **同仓库开发**（推荐）：直接在 `:core` / `:parser` 的源码或测试里使用。
2. **本地 jar**：`./gradlew jar` 后从 `core/build/libs/`、`parser/build/libs/` 取 jar，自行放进 classpath；同时必须提供 skiko（`skiko-awt` + 对应平台的 `skiko-awt-runtime-<os>-<arch>`）。
3. **Composite build**：在目标工程的 `settings.gradle.kts` 里 `includeBuild("path/to/snapshot")`，然后依赖 `com.muedsa.snapshot:snapshot-core`。

平台相关 skiko 运行时坐标（`libs.versions.toml` 已定义，坐标为 `org.jetbrains.skiko:skiko-awt-runtime-{windows,linux,macos}-{x64,arm64}`）：

| OS | Arch | Artifact |
|---|---|---|
| Windows | x64 / arm64 | `skiko-awt-runtime-windows-x64` / `-windows-arm64` |
| Linux | x64 / arm64 | `skiko-awt-runtime-linux-x64` / `-linux-arm64` |
| macOS | x64 / arm64 | `skiko-awt-runtime-macos-x64` / `-macos-arm64` |

> 注意：`skiko-awt` 是 **`api` 依赖**（`core` 用 `api(libs.skiko.awt)` 声明），所以 `Color`、`BlendMode`、`ImageFilter` 等 `org.jetbrains.skia.*` 类型会直接出现在 snapshot 的公开签名里，使用方也需要能解析到 skiko。

---

## 3. 快速上手

### 3.1 最小示例

```kotlin
import com.muedsa.snapshot.SnapshotPNG
import com.muedsa.snapshot.widget.Container
import org.jetbrains.skia.Color
import java.io.File

File("out.png").writeBytes(
    SnapshotPNG {
        Container(width = 200f, height = 200f, color = Color.RED)
    }
)
```

`SnapshotPNG { ... }` 的 lambda 接收者是根 `Widget`，里面每次调用 Widget 工厂函数都会把新节点挂到当前节点下。

### 3.2 渲染入口

全部位于 `com.muedsa.snapshot`（`core/src/main/kotlin/com/muedsa/snapshot/Snapshot.kt`）：

| 函数 | 返回 | 用途 |
|---|---|---|
| `Snapshot(background, debug, initSurface) { ... }` | `org.jetbrains.skia.Surface` | 需要自己操作 Surface（自定义光栅化/GPU） |
| `SnapshotImage(...) { ... }` | `Image` | 拿到 Skia `Image`，自己编码或直接绘制 |
| `SnapshotPNG(...) { ... }` | `ByteArray` | PNG 字节流（HTTP 响应、写文件） |
| `SnapshotJPEG(...) { ... }` | `ByteArray` | JPEG 字节流 |
| `SnapshotWEBP(...) { ... }` | `ByteArray` | WebP 字节流 |
| `layoutWidget { ... }` | `RenderBox` | **只布局不渲染**，拿布局树做断言/自省 |
| `Canvas.drawRenderBox(renderBox, background, debug)` | `Unit` | 把已布局的树画到任意 Canvas（用于离屏/实时窗口） |

`Snapshot` / `SnapshotImage` / `SnapshotPNG` / `SnapshotJPEG` / `SnapshotWEBP` 的参数含义完全一致：

| 参数 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `background` | `Int` (ARGB) | 不透明白 `0xFFFFFFFF` | 画布底色，渲染前 `canvas.clear(background)` |
| `debug` | `Boolean` | `false` | 打开后绘制调试辅助（如空盒灰块、占位框） |
| `initSurface` | `(width, height) -> Surface` | `Surface.makeRasterN32Premul(...)` | 自定义 Surface；默认 **CPU 光栅化** |

自定义 Surface 的例子：

```kotlin
SnapshotPNG(
    background = 0x00000000,          // 透明底:0xAARRGGBB
    initSurface = { width, height ->
        // 默认就是这一句(CPU 光栅化);可换成任何返回 Surface 的实现
        Surface.makeRasterN32Premul(width, height)
    }
) {
    /* ... */
}
```

> ⚠️ `initSurface` 收到的是**布局尺寸向上取整后**的像素宽高，而 `drawRenderBox` 会先 `clear(background)`、再以 **1:1 从原点**绘制。所以返回一个更大的 Surface 只会让背景变大、内容留在左上角，**不会起到"2 倍图"的作用**。需要放大输出请放大 widget 树本身的尺寸。

### 3.3 尺寸规则与常见异常

渲染流程是「**先布局，再按布局尺寸建 Surface**」：

1. `layoutWidget` 用 `BoxConstraints()`（最小 0、最大无限）对根节点做一次布局；
2. 取根的 `definiteSize`；
3. Surface 尺寸 = `ceil(width) × ceil(height)`（**像素向上取整**）；
4. `clear(background)` 后绘制整棵树。

因此：

- **图片尺寸由内容决定**，不需要（也没有）显式指定画布宽高；想要固定画布就用带 `width`/`height` 的根 `Container`/`SizedBox`。
- 根尺寸为 **0** → `IllegalArgumentException: layout size is empty`。
- 根尺寸为 **无限** → `IllegalArgumentException: layout size is infinite`。

```kotlin
SnapshotPNG { Container(width = 200f, height = 200f, color = Color.RED) }  // ✅ 200×200
SnapshotPNG { Container() }                                               // ❌ layout size is infinite
SnapshotPNG { Row {} }                                                    // ❌ layout size is empty
SnapshotPNG { SizedBox(width = 200f) { Text("hi") } }                     // ⚠️ 高度由文本决定
```

> 原因：空 `Container()` 没有子节点又没有约束时，内部会组合出 `ConstrainedBox(BoxConstraints.expand())`（即"尽可能大"），在"最大无限"的根约束下就变成无限大。给它 `width`/`height`/`constraints` 即可。

### 3.4 接到 HTTP 服务

`snapshot()` 返回的就是可直接写响应的字节数组（Web Demo <https://snapshot.muedsa.com> 的 `POST /snapshot` 就是这么做的）：

```kotlin
// 伪代码:Ktor / Spring 均可
post("/snapshot") {
    val body = call.receiveText()                       // 类 DOM 文本
    val bytes = Parser().parse(StringReader(body)).snapshot()
    call.respondBytes(bytes, ContentType.Image.PNG)     // Content-Type: image/png
}
```

解析出的图片格式由根标签 `type` 决定（`png` / `jpg` / `webp`，默认 `png`）。

---

## 4. 核心概念

### 4.1 Widget 树 → RenderBox 树

`Widget` 是**配置对象**（可变属性 + 子节点），`RenderBox` 是**布局与绘制的执行体**。调用 `createRenderBox()` 时才把 Widget 转成 RenderBox：

```
Widget 树                     RenderBox 树
ProxyWidget(根)          →    (透传)
  └ Container            →      Padding / DecoratedBox / ConstrainedBox …
      └ Row              →        RenderFlex(HORIZONTAL)
          ├ Container    →          RenderColoredBox…
          └ Container    →          RenderColoredBox…
```

三类父节点，决定了"能不能有子节点、能有几个"：

| 基类 | 子节点 | 说明 |
|---|---|---|
| `ProxyWidget` | 1 个（`widget`） | 透传包装，`Container` 等复合 Widget 内部靠它拼装 |
| `SingleChildWidget` | 1 个（`child`） | 绝大多数布局/装饰 Widget |
| `MultiChildWidget` | 多个（`children`） | `Flex`/`Row`/`Column`/`Stack`/`RichText` |
| （直接继承 `Widget`） | 0 个 | 叶子：`RawImage`、`ProviderImage`、`CachedNetworkImage` |

`ParentDataWidget` 是一类特殊 `ProxyWidget`：它**不新增 RenderBox 节点**（`createRenderBox()` 直接转发给子节点），而是把布局参数**写进子 RenderBox 的 parentData**。`Expanded`/`Flexible`（写给 `FlexParentData`）和 `Positioned`（写给 `StackParentData`）都是它——这也是为什么它们只能作为对应父节点的直接子节点。

### 4.2 约束模型

布局遵循与 Flutter 相同的 Box 协议：

- 父节点用 `BoxConstraints(minWidth, maxWidth, minHeight, maxHeight)` 约束子节点；
- 子节点在这组约束内算出自己的 `Size`；
- 父节点再决定子节点的位置（通过 parentData）。

`BoxConstraints` 常用工厂（`com.muedsa.snapshot.rendering.box`）：

| 工厂 | 含义 |
|---|---|
| `BoxConstraints()` | `0..∞` × `0..∞`（完全放开） |
| `BoxConstraints.tightFor(width, height)` | 指定边收紧为定值，其余放开 |
| `BoxConstraints.tight(size)` | 宽高都锁死 |
| `BoxConstraints.loose(size)` | `0..size` |
| `BoxConstraints.expand(width, height)` | 尽可能大（默认 `∞`） |

常用成员：`isTight`、`biggest`、`smallest`、`enforce()`、`tighten()`、`deflate()`、`constrain(size)`。

### 4.3 颜色与数值

- **颜色统一是 `Int`，格式 `0xAARRGGBB`**（Skia 的 `Color.makeARGB`）。也常用 `org.jetbrains.skia.Color` 里的常量（`Color.RED`、`Color.TRANSPARENT`…）。
- 所有尺寸/坐标都是 **`Float`，单位是逻辑像素**；最终 Surface 尺寸向上取整。
- 浮点比较请带容差（`com.muedsa.snapshot.precisionErrorTolerance = 1e-10f`）。

### 4.4 对齐与方向

- `BoxAlignment(x, y)`：`x`/`y ∈ [-1, 1]`，`(-1,-1)` 左上、`(0,0)` 居中、`(1,1)` 右下。预置常量 `TOP_LEFT`…`BOTTOM_RIGHT`（`com.muedsa.geometry.BoxAlignment`）。注意它是**普通类 + 伴生常量**，不是 Kotlin 枚举，所以可以直接 `BoxAlignment(0.2f, -0.2f)` 自定义。
- `AlignmentDirectional(start, y)`：方向相关版本（`TOP_START`…`BOTTOM_END`），**必须**结合 `textDirection` 才能 resolve——`Stack` 的默认对齐就是 `AlignmentDirectional.TOP_START`。
- `EdgeInsets`：`EdgeInsets.all(v)` / `symmetric(vertical, horizontal)` / `only(...)` / `fromLTRB(...)` / `ZERO`。
- `Offset`、`Size`、`Rect`、`Radius`（`Radius.circular(r)` / `Radius.elliptical(x, y)`）、`BorderRadius`（`circular`/`all`/`only`/`vertical`/`horizontal`/`ZERO`）、`Matrix44CMO` 都在 `com.muedsa.geometry`。

---

## 5. Widget 参考

所有 Widget 工厂都是 `Widget` 的扩展函数，包 `com.muedsa.snapshot.widget`（文本类在 `com.muedsa.snapshot.widget.text`）。下面 `content` 参数即"子节点 lambda"，标 `—` 表示该 Widget 没有子节点。

### 5.1 总表

| Widget | 子节点 | 作用 |
|---|---|---|
| `Container` | 单 | 瑞士军刀：尺寸 + 内边距 + 外边距 + 底色/装饰 + 对齐 + 裁剪 + 变换 |
| `SizedBox` | 单 | 强制宽/高 |
| `ConstrainedBox` | 单 | 施加额外约束 |
| `LimitedBox` | 单 | **仅当约束无限时**才施加最大宽/高 |
| `OverflowBox` | 单 | 给子节点不同于自己的约束（可溢出） |
| `SizedOverflowBox` | 单 | 自身按指定尺寸、子节点按父约束（可溢出） |
| `Padding` | 单 | 内边距 |
| `Align` / `Center` | 单 | 对齐（`Center` = 居中版 `Align`） |
| `Flex` / `Row` / `Column` | 多 | 一维弹性布局 |
| `Expanded` / `Flexible` | 单 | Flex 子项伸缩参数（**只能放在 Flex/Row/Column 里**） |
| `Stack` | 多 | 层叠布局 |
| `Positioned` | 单 | Stack 子项定位（**只能放在 Stack 里**） |
| `Transform` | 单 | 仿射变换（旋转/平移/自定义矩阵） |
| `ColoredBox` | 单 | 填充底色 |
| `DecoratedBox` | 单 | 背景/前景装饰（边框、圆角、阴影、渐变、图片） |
| `Opacity` | 单 | 整体透明度 |
| `ColorFiltered` | 单 | 颜色滤镜 |
| `ImageFiltered` | 单 | 图像滤镜 |
| `BackdropFilter` | 单 | 背景滤镜（模糊下层已绘制内容） |
| `ClipRect` / `ClipRRect` / `ClipOval` / `ClipPath` | 单 | 裁剪 |
| `RawImage` | — | 绘制一张已有 `Image` |
| `ProviderImage` | — | 通过 `() -> Image` 惰性取图 |
| `CachedNetworkImage` | — | 通过网络 URL + 缓存取图 |
| `Text` | — | 单样式文本 |
| `RichText` | 多 | 富文本（`TextSpan` 树） |

### 5.2 布局 Widget

#### Container

```kotlin
fun Widget.Container(
    alignment: BoxAlignment? = null,
    padding: EdgeInsets? = null,
    color: Int? = null,
    decoration: Decoration? = null,
    foregroundDecoration: Decoration? = null,
    width: Float? = null,
    height: Float? = null,
    constraints: BoxConstraints? = null,
    margin: EdgeInsets? = null,
    transform: Matrix44CMO? = null,
    transformAlignment: BoxAlignment? = null,
    clipBehavior: ClipBehavior = ClipBehavior.NONE,
    content: Container.() -> Unit = {},
)
```

`Container` 本身**不是**一个 RenderBox，而是按需把若干 Widget 叠起来。源码的包裹顺序（先写的在**内**层，后写的在**外**层）是：

| # | 条件 | 包裹的 Widget |
|---|---|---|
| 0 | `child == null` 且（`constraints == null` 或 `constraints.isTight`） | 用 `LimitedBox(0,0)` + `ConstrainedBox(expand())` 充当子节点 |
| 1 | 未命中 0 且 `alignment != null` | `Align(alignment)` |
| 2 | `padding != null` | `Padding(padding)` |
| 3 | `color != null` | `ColoredBox(color)` |
| 4 | `clipBehavior != NONE` | `ClipPath(decoration.getClipPath(...), clipBehavior)` |
| 5 | `decoration != null` | `DecoratedBox(decoration, BACKGROUND)` |
| 6 | `foregroundDecoration != null` | `DecoratedBox(foregroundDecoration, FOREGROUND)` |
| 7 | `constraints != null` | `ConstrainedBox(constraints)` |
| 8 | `margin != null` | `Padding(margin)` |
| 9 | `transform != null` | `Transform(transform, transformAlignment)` |

即外层到内层依次为：`Transform → Padding(margin) → ConstrainedBox → DecoratedBox(前景) → DecoratedBox(背景) → ClipPath → ColoredBox → Padding(padding) → Align → child`。

- **约束**：`check(decoration != null || clipBehavior == ClipBehavior.NONE)`——用 `clipBehavior` 必须同时给 `decoration`。
- **约束**：`check(color == null || decoration == null)`——颜色和装饰不能同时给；要两者都要，用 `decoration = BoxDecoration(color = ...)`。
- `width`/`height` 不会单独建节点，而是并进 `constraints`：`constraints?.tighten(width, height) ?: BoxConstraints.tightFor(width, height)`。
- 注意 `ClipPath` 在 `DecoratedBox(背景)` **内**层，也就是**装饰本身不会被 `clipBehavior` 裁掉**，被裁的是子节点内容。
- **无子节点时的特殊行为**（第 0 行）：会变成"占满可用空间"。有界约束下就是"铺满父容器"，无限约束下会报 *layout size is infinite*（见 [3.3](#33-尺寸规则与常见异常)）。

```kotlin
SnapshotPNG {
    Container(
        width = 300f, height = 200f,
        margin = EdgeInsets.all(10f),
        padding = EdgeInsets.symmetric(vertical = 8f, horizontal = 16f),
        alignment = BoxAlignment.CENTER,
        decoration = BoxDecoration(
            color = 0xFFFFFFFF.toInt(),
            borderRadius = BorderRadius.circular(12f),
            border = Border.all(color = 0xFF333333.toInt(), width = 2f),
            boxShadow = arrayOf(BoxShadow(color = 0x33000000, offset = Offset(0f, 4f), blurRadius = 8f))
        ),
        clipBehavior = ClipBehavior.ANTI_ALIAS
    ) {
        Text("Hello", style = TextStyle(fontSize = 24f))
    }
}
```

#### SizedBox / ConstrainedBox / LimitedBox

```kotlin
fun Widget.SizedBox(width: Float? = null, height: Float? = null, content: SizedBox.() -> Unit = {})
fun Widget.ConstrainedBox(constraints: BoxConstraints, content: ConstrainedBox.() -> Unit = {})
fun Widget.LimitedBox(maxWidth: Float = Float.POSITIVE_INFINITY, maxHeight: Float = Float.POSITIVE_INFINITY, content: LimitedBox.() -> Unit = {})
```

- `SizedBox`：宽度/高度为 `null` 表示"该方向不额外约束"。快捷构造（类上）：`SizedBox.expand()`（无穷）、`SizedBox.shrink()`（0）、`SizedBox.square(d)`、`SizedBox.fromSize(size)`。
- `ConstrainedBox`：把 `additionalConstraints` **强制**施加给子节点。
- `LimitedBox`：只在**传入约束是无限**时才用 `maxWidth`/`maxHeight` 收口，有界时原样透传。它是 `Container` 空子节点场景的实现基础。

#### OverflowBox / SizedOverflowBox

```kotlin
fun Widget.OverflowBox(alignment: BoxAlignment = BoxAlignment.CENTER,
                       minWidth: Float? = null, maxWidth: Float? = null,
                       minHeight: Float? = null, maxHeight: Float? = null, content: OverflowBox.() -> Unit = {})
fun Widget.SizedOverflowBox(size: Size, alignment: BoxAlignment = BoxAlignment.CENTER, content: SizedOverflowBox.() -> Unit = {})
```

- `OverflowBox`：自己按父约束定尺寸，但**给子节点另一套约束**，子节点可以画到外面（是否被裁取决于外层是否有 `Clip*`）。
- `SizedOverflowBox`：自己固定成 `size`，子节点拿父约束、按 `alignment` 摆放。

#### Padding / Align / Center

```kotlin
fun Widget.Padding(padding: EdgeInsets, content: Padding.() -> Unit = {})
fun Widget.Align(alignment: BoxAlignment = BoxAlignment.CENTER, widthFactor: Float? = null, heightFactor: Float? = null, content: Align.() -> Unit = {})
fun Widget.Center(widthFactor: Float? = null, heightFactor: Float? = null, content: Center.() -> Unit = {})
```

`widthFactor`/`heightFactor` 为 `null` 时尽量撑满父约束；给出数值时自身尺寸 = 子尺寸 × 系数（例如 `1f` 表示"包裹内容"）。

#### Flex / Row / Column

```kotlin
fun Widget.Row(
    mainAxisAlignment: MainAxisAlignment = MainAxisAlignment.START,
    mainAxisSize: MainAxisSize = MainAxisSize.MAX,
    crossAxisAlignment: CrossAxisAlignment = CrossAxisAlignment.CENTER,
    textDirection: Direction? = null,
    verticalDirection: VerticalDirection = VerticalDirection.DOWN,
    textBaseline: BaselineMode? = null,
    clipBehavior: ClipBehavior = ClipBehavior.NONE,
    content: Row.() -> Unit = {},
)
```

`Column` 参数完全相同（仅主轴变为垂直）。`Flex` 多一个必填的 `direction: Axis`。

| 参数 | 默认 | 作用 |
|---|---|---|
| `mainAxisAlignment` | `START` | 主轴剩余空间分配：`START`/`END`/`CENTER`/`SPACE_BETWEEN`/`SPACE_AROUND`/`SPACE_EVENLY` |
| `mainAxisSize` | `MAX` | `MAX` 撑满主轴可用空间；`MIN` 仅包裹子节点 |
| `crossAxisAlignment` | `CENTER` | 交叉轴对齐：`START`/`END`/`CENTER`/`STRETCH`/`BASELINE` |
| `textDirection` | `null` → 渲染时按 `LTR` | 影响 `START`/`END` 的解释 |
| `verticalDirection` | `DOWN` | 纵向的 start/end 方向 |
| `textBaseline` | `null` | `crossAxisAlignment = BASELINE` 时**必须**给（否则 `assert` 失败） |
| `clipBehavior` | `NONE` | 溢出时是否裁剪 |

#### Expanded / Flexible

```kotlin
fun Flex.Expanded(flex: Int = 1, content: Expanded.() -> Unit = {})     // FlexFit.TIGHT
fun Flex.Flexible(flex: Int = 1, fit: FlexFit = FlexFit.LOOSE, content: Flexible.() -> Unit = {})
```

接收者是 `Flex`，所以**只能出现在 `Row`/`Column`/`Flex` 的直接子位置**。`flex` 是剩余空间的分配权重；`TIGHT` 表示必须占满分配到的空间，`LOOSE` 表示"至多"那么多。

```kotlin
SnapshotPNG {
    SizedBox(width = 300f, height = 60f) {
        Row {
            Expanded { Container(color = Color.RED) }        // 占 1/3
            Expanded(flex = 2) { Container(color = Color.GREEN) } // 占 2/3
        }
    }
}
```

#### Stack / Positioned

```kotlin
fun Widget.Stack(
    alignment: AlignmentGeometry = AlignmentDirectional.TOP_START,
    textDirection: Direction = Direction.LTR,
    fit: StackFit = StackFit.LOOSE,
    clipBehavior: ClipBehavior = ClipBehavior.HARD_EDGE,
    content: Stack.() -> Unit = {},
)

fun Stack.Positioned(
    left: Float? = null, top: Float? = null, right: Float? = null, bottom: Float? = null,
    width: Float? = null, height: Float? = null,
    content: Positioned.() -> Unit = {},
)
```

- `Stack` 按子节点加入顺序**从下往上**叠放；非 `Positioned` 的子节点按 `alignment` 对齐，尺寸策略由 `fit` 决定（`LOOSE`/`EXPAND`/`PASSTHROUGH`）。
- **注意默认裁剪**：`Stack` 的 `clipBehavior` 默认是 `HARD_EDGE`（会裁掉溢出部分），要允许溢出请显式传 `ClipBehavior.NONE`。
- `Positioned` 约束（`assert`）：`left`/`right`/`width` 三者最多给两个；`top`/`bottom`/`height` 同理。快捷构造：`Positioned.fill(...)`、`Positioned.fromRect(rect)`、`Positioned.fromRelativeRect(rr)`、`Positioned.directional(textDirection, start, end, ...)`。

```kotlin
SnapshotPNG {
    SizedBox(width = 300f, height = 300f) {
        Stack(clipBehavior = ClipBehavior.NONE) {
            Container(width = 300f, height = 300f, color = 0xFFEEEEEE.toInt())
            Positioned(left = 20f, top = 20f) {
                Container(width = 80f, height = 80f, color = Color.RED)
            }
            Positioned(bottom = 0f, right = 0f) {
                Container(width = 80f, height = 80f, color = Color.BLUE)
            }
        }
    }
}
```

#### Transform

```kotlin
fun Widget.Transform(
    transform: Matrix44CMO,
    origin: Offset? = null,
    alignment: BoxAlignment?,     // 没有默认值,必须显式给出(可传 null)
    content: Transform.() -> Unit = {},
)
```

快捷构造：`Transform.rotate(angle, origin = null, alignment = BoxAlignment.CENTER)`、`Transform.translate(offset)`。`origin` 是绝对偏移原点，`alignment` 是相对自身尺寸的原点，两者可以都传 `null`（表示以坐标原点为基准）。

### 5.3 绘制与效果 Widget

| Widget | 签名要点 | 说明 |
|---|---|---|
| `ColoredBox(color: Int, content)` | 必填颜色 | 用纯色填充自己的区域 |
| `DecoratedBox(decoration: Decoration, position: DecorationPosition = BACKGROUND, content)` | | `FOREGROUND` 会画在子节点之上 |
| `Opacity(opacity: Float = 1f, content)` | `assert(opacity in 0f..1f)` | 整棵子树做 alpha 合成 |
| `ColorFiltered(colorFilter: ColorFilter, content)` | | 传 Skia `ColorFilter`（如 `ColorFilter.makeBlend`/`makeMatrix`） |
| `ImageFiltered(imageFilter: ImageFilter, content)` | | 传 Skia `ImageFilter`（如 `ImageFilter.makeBlur`） |
| `BackdropFilter(imageFilter: ImageFilter, blendMode: BlendMode = SRC_OVER, content)` | | 对**已绘制的下层内容**做滤镜（毛玻璃效果） |
| `ClipRect(clipper: ((Size) -> Rect)? = null, clipBehavior = HARD_EDGE, content)` | 默认 **HARD_EDGE** | `clipper` 为 `null` 时裁自身区域 |
| `ClipRRect(borderRadius: BorderRadius = BorderRadius.ZERO, clipper: ((Size) -> RRect)? = null, clipBehavior = ANTI_ALIAS, content)` | 默认 **ANTI_ALIAS** | 圆角裁剪 |
| `ClipOval(clipper: ((Size) -> Rect)? = null, clipBehavior = ANTI_ALIAS, content)` | 默认 **ANTI_ALIAS** | 椭圆/圆裁剪 |
| `ClipPath(clipper: ((Size) -> Path)? = null, clipBehavior = ANTI_ALIAS, content)` | 默认 **ANTI_ALIAS** | 任意路径裁剪 |

`clipper` 形如 `{ size -> ... }`，`size` 是自身尺寸，返回裁剪几何：

```kotlin
SnapshotPNG {
    SizedBox(width = 200f, height = 200f) {
        ClipPath(clipper = { size ->
            PathBuilder()
                .addOval(Rect.makeWH(size.width, size.height))
                .detach()
        }) {
            Container(width = 200f, height = 200f, color = Color.GREEN)
        }
    }
}
```

> skiko `0.0.0-SNAPSHOT` 里 `Path` 已改为不可变，构造路径要用 `PathBuilder().xxx().detach()`。

### 5.4 图像 Widget

| Widget | 必填 | 其余参数 |
|---|---|---|
| `RawImage(image: Image, ...)` | `image` | `width`, `height`, `fit`, `alignment`, `repeat`, `scale`, `opacity`, `color`, `colorBlendMode` |
| `ProviderImage(provider: () -> Image, ...)` | `provider` | 与 `RawImage` 相同，只是把 `image` 换成惰性取图函数 |
| `CachedNetworkImage(url: String, ...)` | `url` | 与 `ProviderImage` 相同，另加 `noCache: Boolean = false`、`cache: NetworkImageCache = NetworkImageCacheManager.defaultCache` |

参数默认值：`width = null`、`height = null`、`fit = null`、`alignment = BoxAlignment.CENTER`、`repeat = ImageRepeat.NO_REPEAT`、`scale = 1f`、`opacity = 1f`、`color = null`、`colorBlendMode = null`。

尺寸规则：以 `constraints.tightFor(width, height)` 为基础，在父约束内**按原始宽高比**收缩（`constrainSizeAndAttemptToPreserveAspectRatio`）；`image` 为 `null` 时取最小尺寸。`fit` 为 `null` 时按 `BoxFit.FILL` 处理（拉伸铺满目标框；若同时给了 `centerSlice` 则按 `SCALE_DOWN`）。

```kotlin
SnapshotPNG {
    CachedNetworkImage(
        url = "https://samples-files.com/samples/images/png/480-360-sample.png",
        width = 200f, height = 200f,
        fit = BoxFit.COVER              // 覆盖裁剪
    )
}
```

### 5.5 文本 Widget

```kotlin
fun Widget.Text(
    text: String,
    style: TextStyle? = null,
    textAlign: Alignment = Alignment.START,
    textDirection: Direction = Direction.LTR,
    softWrap: Boolean = true,
    overflow: TextOverflow = TextOverflow.CLIP,
    maxLines: Int? = null,
    strutStyle: StrutStyle? = null,
    textWidthBasis: TextWidthBasis = TextWidthBasis.PARENT,
    textHeightMode: HeightMode? = null,
)
```

`Widget.Text` 内部就是构造一个 `RichText(text = TextSpan(text, style), ...)`。

```kotlin
fun Widget.RichText(text: InlineSpan, textAlign = START, textDirection = LTR, softWrap = true,
                    overflow = CLIP, maxLines = null, strutStyle = null,
                    textWidthBasis = PARENT, textHeightMode = null)

fun Widget.RichText(textAlign = START, /* 同上 */ content: TextSpan.() -> Unit)   // DSL 版
```

详见[第 9 节](#9-文本与富文本)。

---

## 6. 布局细节

### 6.1 约束怎么往下传

| Widget | 传给子节点的约束 |
|---|---|
| `ConstrainedBox` | `additionalConstraints.enforce(自身约束)` |
| `SizedBox` | 等同 `ConstrainedBox(tightFor(width, height))` |
| `LimitedBox` | 自身约束无限时才收口到 `maxWidth`/`maxHeight` |
| `Padding` | 自身约束 `deflate(padding)` 后下传，自身尺寸 = 子尺寸 + 边距 |
| `Align`/`Center` | **放松（loosen）** 后下传 |
| `OverflowBox` | 自行构造的一套约束（与自身约束无关） |
| `Flex`(非可伸缩子) | 主轴放开、交叉轴取父约束对应边 |
| `Flex`(可伸缩子) | 主轴按 flex 分配的 `min..max`，`STRETCH` 时交叉轴收紧 |
| `Stack`(非定位子) | 由 `StackFit` 决定：`LOOSE`=放松、`EXPAND`=收到最大、`PASSTHROUGH`=原样 |
| `Stack`(定位子) | 由 `Positioned` 的 `left/top/right/bottom/width/height` 反推 |
| `Opacity`/`Clip*`/`ColoredBox`/`DecoratedBox`/滤镜 | 原样透传（尺寸 = 子尺寸） |
| `RawImage`/`ProviderImage`/`CachedNetworkImage` | 叶子，自己算尺寸 |
| `RichText` | 叶子（`WidgetSpan` 占位尺寸由文本布局回填） |

### 6.2 溢出与裁剪

- `Flex` 有 `clipBehavior`（默认 `NONE`）和内部的 `overflow` 标记；
- `Stack` 默认 `HARD_EDGE`，会裁掉超出部分；
- 其他 Widget 溢出后是否可见，取决于祖先链上有没有 `Clip*`；
- 要"故意溢出"，用 `OverflowBox` / `SizedOverflowBox` / `Positioned`（配 `Stack(clipBehavior = NONE)`）。

### 6.3 布局自省（调试/测试）

`com.muedsa.snapshot.rendering.LayoutNode` 提供**只读**的布局树视图，用于确认"到底摆在哪"。入口 `rootLayout` 来自 `:testkit`（`testImplementation(project(":testkit"))`）：

```kotlin
val root: LayoutNode = rootLayout { /* 同一段 widget 场景 */ }
root.size                 // Size(definiteSize)
root.absoluteOffset       // 相对根盒的累积偏移
root.rect                 // absoluteOffset + size
root.children / root.parent
root.renderBox            // 原始 RenderBox,可 is 判型
```

> `:core` 自带的 `layoutWidget { ... }` 只返回 `RenderBox`；`rootLayout` 是 `:testkit` 在它之上包装的自省入口。

配套断言见 [`docs/testing/README.md` 第 4 节](../testing/README.md#4-布局数值断言第-1-层)。

---

## 7. 绘制与装饰

### 7.1 BoxDecoration

```kotlin
BoxDecoration(
    color: Int? = null,
    image: DecorationImage? = null,
    border: BoxBorder? = null,
    borderRadius: BorderRadius? = null,
    boxShadow: Array<BoxShadow>? = null,
    gradient: Gradient? = null,
    backgroundBlendMode: BlendMode? = null,
    shape: BoxShape = BoxShape.RECTANGLE,   // RECTANGLE | CIRCLE
)
```

- `assert(backgroundBlendMode == null || color != null || gradient != null)`。
- `shape = CIRCLE` 时用内切圆作为裁剪路径（`getClipPath`）。

配套类型：

| 类型 | 构造 |
|---|---|
| `BorderSide` | `BorderSide(color = Color.BLACK, width = 1f, style = BorderStyle.SOLID, strokeAlign = STROKE_ALIGN_INSIDE)`；常量 `BorderSide.NONE` |
| `Border` | `Border(top = BorderSide.NONE, right = …, bottom = …, left = …)`（注意参数顺序是 **top, right, bottom, left**）/ `Border.all(color, width, style)` / `Border.symmetric(vertical, horizontal)`；`BoxBorder` 是抽象基类 |
| `BorderRadius` | `BorderRadius.circular(r)` / `all(radius)` / `only(...)` / `vertical(top, bottom)` / `horizontal(left, right)` / `ZERO` |
| `Radius` | `Radius.circular(r)` / `Radius.elliptical(x, y)` / `Radius.ZERO` |
| `BoxShadow` | `BoxShadow(color, offset, blurRadius, spreadRadius = 0f, blurStyle = FilterBlurMode.NORMAL)` |
| `BoxShape` | `RECTANGLE` / `CIRCLE` |
| `BorderStyle` | `NONE` / `SOLID` |

### 7.2 渐变

`com.muedsa.snapshot.paint.gradient`，都继承 `Gradient(colors: IntArray, stops: FloatArray? = null, transform: GradientTransform? = null)`：

| 类 | 关键参数 |
|---|---|
| `LinearGradient` | `begin = BoxAlignment.CENTER_LEFT`, `end = BoxAlignment.CENTER_RIGHT`, `colors`, `stops = null`, `tileMode = FilterTileMode.CLAMP`, `transform = null` |
| `RadialGradient` | 见 `paint/gradient/RadialGradient.kt`（支持普通径向与 two-point conical） |
| `SweepGradient` | 见 `paint/gradient/SweepGradient.kt` |

- `colors` 至少 2 个；`stops` 为 `null` 时按等距自动推导（`impliedStops`）。
- 渐变只能通过 `BoxDecoration(gradient = ...)` 使用（`Gradient` 不是 Widget）。

```kotlin
SnapshotPNG {
    Container(
        width = 600f, height = 200f,
        decoration = BoxDecoration(
            gradient = LinearGradient(
                colors = intArrayOf(0xFF0000FF.toInt(), 0xFF00FF00.toInt()),
                stops = floatArrayOf(0f, 1f)
            )
        )
    )
}
```

### 7.3 阴影

- 手动：`BoxShadow(color, offset, blurRadius, spreadRadius, blurStyle)`。
- 直接用 Material 高度（`com.muedsa.snapshot.material.ELEVATION_MAP`）：键为 `0, 1, 2, 3, 4, 6, 8, 9, 12, 16, 24`。
- 解析器里写 `boxShadow="ELEVATION_8"` 即可（见 [10.3](#103-属性取值格式)）。

### 7.4 滤镜

`ColorFiltered`/`ImageFiltered`/`BackdropFilter` 的参数是 Skia 原生对象，直接用 skiko 工厂构造：

```kotlin
SnapshotPNG {
    SizedBox(width = 200f, height = 200f) {
        ImageFiltered(imageFilter = ImageFilter.makeBlur(8f, 8f, FilterTileMode.CLAMP)) {
            Container(width = 200f, height = 200f, color = Color.RED)
        }
    }
}
```

同样地，`ColorFiltered(colorFilter = ColorFilter.makeBlend(0xFF9E9E9E.toInt(), BlendMode.SATURATION))` 可以把子树转成灰度。

---

## 8. 图像与网络缓存

`CachedNetworkImage` 通过 `NetworkImageCache` 取图。默认实现与全局开关都在 `com.muedsa.snapshot.tools`：

```kotlin
object NetworkImageCacheManager {
    var defaultCache: NetworkImageCache = SimpleNoLimitedNetworkImageCache
    fun register(cache: NetworkImageCache, asDefault: Boolean = false)
    operator fun get(name: String): NetworkImageCache?
    fun getOrDefault(name: String?): NetworkImageCache
}
```

| 实现 | 特点 |
|---|---|
| `SimpleNoLimitedNetworkImageCache` | 无上限缓存，是 `NetworkImageCacheManager.defaultCache` 的默认值；`NAME = "SimpleNoLimitedNetworkImageCache"` |
| `SimpleLimitedNetworkImageCache(maxImageNum = Int.MAX_VALUE, maxSingleImageSize = Int.MAX_VALUE, debug = false)` | 限制缓存张数与单图字节数；超出时抛 `IllegalStateException` |

```kotlin
// 全局换成有上限的缓存
NetworkImageCacheManager.register(
    SimpleLimitedNetworkImageCache(maxImageNum = 100, maxSingleImageSize = 5 * 1024 * 1024),
    asDefault = true
)

// 单张图绕过缓存
CachedNetworkImage(url = url, noCache = true)
```

注意事项：

- **网络访问是同步阻塞的**：`provider` 在构建 Widget / 绘制布局时就会发起 HTTP 请求。请自行放在合适的线程上，并注意超时。
- `SimpleLimitedNetworkImageCache` 遇到 404 会抛 `IllegalStateException("Get http 404 from $url")`。
- **解析器的限额是独立的**（`SnapshotElement` 的静态字段）：

  ```kotlin
  SnapshotElement.MAX_IMAGE_NUM = 10                    // 最多缓存 10 张
  SnapshotElement.MAX_SINGLE_IMAGE_SIZE = 5 * 1024 * 1024
  SnapshotElement.NETWORK_IMAGE_CACHE_BUILDER = { element -> /* 自定义 NetworkImageCache */ }
  ```

  解析出的每个 `<Snapshot>` 文档各自持有一个缓存实例（`getNetworkImageCache()`）。

---

## 9. 文本与富文本

### 9.1 TextStyle

`com.muedsa.snapshot.paint.text.TextStyle`（`data class`，全部可空）：

| 属性 | 类型 | 说明 |
|---|---|---|
| `color` | `Int?` | 文本颜色（ARGB） |
| `fontSize` | `Float?` | 字号（未给时回落到 `kDefaultFontSize = 14f`） |
| `fontFamilies` | `List<String>?` | 字体族列表 |
| `typeface` | `Typeface?` | 直接指定字体 |
| `fontStyle` | `org.jetbrains.skia.FontStyle?` | `NORMAL` / `BOLD` / `ITALIC` / `BOLD_ITALIC` |
| `height` / `topRatio` | `Float?` | 行高（倍数）/ 基线在行内的比例 |
| `letterSpacing` / `wordSpacing` | `Float?` | 字距 / 词距 |
| `foreground` / `background` | `Paint?` | 前景/背景画刷（可做描边字） |
| `shadows` | `List<Shadow>?` | 文本阴影 |
| `decorationStyle` | `DecorationStyle?` | 下划线等装饰 |
| `fontFeatures` | `List<FontFeature>?` | OpenType 特性 |
| `locale` | `String?` | 语言区域 |
| `baselineMode` | `BaselineMode?` | 基线模式 |
| `fontEdging` / `fontHinting` / `subpixel` | | 光栅化细节 |

方法：`isEmpty()`、`mergeFrom(style)`（**自己优先**，缺失项由参数补齐）、`toSkikoTextStyle()`。

其他默认值常量（`com.muedsa.snapshot.Const`）：`kDefaultFontSize = 14f`、`kDefaultTextColor = Color.BLACK`、`kEllipsis = "…"`、`precisionErrorTolerance = 1e-10f`。

### 9.2 TextSpan 与富文本 DSL

`TextSpan(text: String? = null, style: TextStyle? = null, initChildren: List<InlineSpan>? = null)`，有公开的 `children: MutableList<InlineSpan>`。

两种等价写法：

```kotlin
// A. 显式构造
SnapshotPNG {
    RichText(
        text = TextSpan(
            text = "Hello",
            style = TextStyle(color = Color.RED, fontSize = 40f),
            initChildren = listOf(TextSpan(text = " World", style = TextStyle(color = Color.GREEN, fontSize = 30f)))
        )
    )
}
```

```kotlin
// B. DSL(推荐)
SnapshotPNG {
    Row {
        RichText {
            TextSpan(text = "Hello", style = TextStyle(color = Color.RED, fontSize = 40f))
            TextSpan(text = " World", style = TextStyle(color = Color.GREEN, fontSize = 30f))
            TextSpan(style = TextStyle(color = Color.BLUE)) {   // 带子节点的分组
                TextSpan(text = " nested", style = TextStyle(fontSize = 20f))
            }
        }
    }
}
```

`Widget.RichText { }` 的接收者是根 `TextSpan`；`TextSpan.TextSpan(...)` 两个重载分别追加"文本子 span"与"分组子 span"。样式沿树向下继承（`mergedStyle`）。

> 注意：`RichText` 的 Widget 版重载带 `check(this !is RichText)`，不要嵌套 `RichText`。

### 9.3 行内 Widget：WidgetSpan 与 ImageEmojiSpan

```kotlin
fun TextSpan.WidgetSpan(
    alignment: PlaceholderAlignment = PlaceholderAlignment.BOTTOM,
    baseline: BaselineMode? = null,
    style: TextStyle? = null,
    content: Widget.() -> Unit,
)
```

把任意 Widget 当作一个"字符"塞进文本流（高度会参与换行计算）：

```kotlin
SnapshotPNG {
    RichText {
        TextSpan(text = "价格 ", style = TextStyle(fontSize = 20f))
        WidgetSpan(alignment = PlaceholderAlignment.MIDDLE) {
            Container(width = 40f, height = 40f, color = Color.RED)
        }
    }
}
```

`ImageEmojiSpan` 是"行内图片"的便捷封装（本质是 `WidgetSpan { ImageEmoji(...) }`）：

```kotlin
fun TextSpan.ImageEmojiSpan(
    url: String,
    alignment: PlaceholderAlignment = PlaceholderAlignment.BASELINE,
    baseline: BaselineMode? = null,
    width: Float? = null,
    height: Float? = null,
    fit: BoxFit? = null,
    imageAlignment: BoxAlignment = BoxAlignment.CENTER,
    repeat: ImageRepeat = ImageRepeat.NO_REPEAT,
    scale: Float = 1f,
    opacity: Float = 1f,
    color: Int? = null,
    colorBlendMode: BlendMode? = null,
    noCache: Boolean = false,
    cache: NetworkImageCache = NetworkImageCacheManager.defaultCache,
)

fun TextSpan.ImageEmojiSpan(provider: () -> Image, alignment = BOTTOM, /* …同上(无缓存参数) */)
```

未显式给 `width`/`height` 时，尺寸按**所在 span 的 `fontSize`** 推导（`RenderImageEmoji`），所以行内 emoji 会随字号缩放。

### 9.4 文本度量：TextPainter

需要精确度量（换行、最大行宽、基线）时直接用 `TextPainter`：

```kotlin
val painter = TextPainter(
    text = TextSpan(text = "Hello\nWorld", style = TextStyle(fontSize = 20f)),
    textWidthBasis = TextWidthBasis.PARENT
).apply { layout(minWidth = 0f, maxWidth = 400f) }

painter.width               // 布局内容宽
painter.height              // 内容高
painter.size                // Size
painter.minIntrinsicWidth   // 最小固有宽
painter.maxIntrinsicWidth   // 最宽行天然宽
painter.didExceedMaxLines   // 是否超过 maxLines
painter.computeDistanceToActualBaseline(BaselineMode.ALPHABETIC)
painter.inlinePlaceholderBoxes()          // 行内占位(WidgetSpan)的框
painter.paint(canvas, offset)             // 绘制
painter.debugPaint(canvas, offset)        // 调试描边
```

静态工具：`TextPainter.computeWidth(...)`、`TextPainter.computeMaxIntrinsicWidth(...)`。

> **跨平台提示**：文本度量依赖 OS 字体，同一段文字在不同机器上宽度可能不同。写测试时请显式指定字体（`:testkit` 提供内置的 `testTypeface`），断言用区间/单调而非绝对像素——详见 [`docs/testing/README.md` 第 7 节](../testing/README.md#7-文本类怎么测不进-golden)。

---

## 10. 类 DOM 解析器

`:parser` 让你用一段类 DOM 文本描述版面，再解析成 `:core` 的 Widget 树。它是"渲染服务"的理想输入格式：前端拼字符串、后端出图。

### 10.1 入口 API

```kotlin
open class Parser(
    protected var widgetParserManager: WidgetParserManager = WidgetParserManager.DEFAULT_MANAGER,
) {
    @Synchronized
    fun parse(reader: Reader): SnapshotElement
}
```

```kotlin
class SnapshotElement : Element {
    val type: String                              // "png" | "jpg" | "webp"
    val format: EncodedImageFormat
    val background: Int
    val debug: Boolean
    fun getNetworkImageCache(): NetworkImageCache
    fun createWidget(): Widget                    // 只建 Widget 树,不渲染
    fun snapshot(): ByteArray                     // 布局 + 渲染 + 编码
}
```

```kotlin
val element = Parser().parse(StringReader(text))   // ① 结构解析
val widget  = element.createWidget()               // ② 建 Widget 树（可选）
val bytes   = element.snapshot()                   // ③ 布局 + 渲染 + 编码
```

两个阶段各自负责什么，是排查报错位置的关键：

| 阶段 | 做什么 | 会报什么错 |
|---|---|---|
| `parse()` | 词法分析 + 结构校验；**只有根标签 `<Snapshot>` 的属性**在这一阶段被解析校验 | 未知标签、重复属性、子节点数量违规、非文本标签里的文本、首标签不是 `Snapshot`、根标签属性格式错 |
| `createWidget()` / `snapshot()` | 建 Widget 树，此时才解析**其余所有标签的属性** | 属性值格式错、缺少必填属性、`<Text>` 里的非法子标签、`<Raw>`/`<Emoji>` 用错位置 |
| `snapshot()` 的渲染阶段 | 布局 + 渲染 + 编码 | 根无子节点、布局尺寸为空/无限 |

- `snapshot()` 与 `SnapshotPNG` 的差异：**`background` 默认透明**（`Color.TRANSPARENT`），且 Surface 固定为 CPU 光栅化。想要白底请在根标签写 `background="#FFFFFFFF"`。
- 未写的属性名（或大小写写错的属性名）会被**静默忽略**，不会报错——写错 `Color=` 只会得到"没设颜色"。
- ⚠️ **`Parser` 实例只能成功解析一次**：`parse()` 内部只重置了 reader/tokenizer/栈，**没有重置 `snapshotElement`**，所以同一实例第二次调用会抛 `Duplicate root element [...]`。每次解析请 `Parser()` 新建实例。

> `parse()` 标注了 `@Synchronized`，多线程下不会互相踩状态；但如上所述，实例本身不可复用。

### 10.2 标签总表

标签名、属性名**大小写敏感**，必须与下表**逐字符一致**。

| 标签 | 容器模式 | 对应 Widget | 说明 |
|---|---|---|---|
| `Snapshot` | 单子 | 根，无对应 Widget | **必须是根节点且整个文档只有一个** |
| `Container` | 单子 | `Container` | 最常用 |
| `Border` | 单子 | `DecoratedBox(BoxDecoration(...))` | 只画装饰（边框/圆角/阴影） |
| `Row` | 多子 | `Row` | |
| `Column` | 多子 | `Column` | |
| `Stack` | 多子 | `Stack` | |
| `Positioned` | 单子 | `Positioned` | 只能放在 `Stack` 里 |
| `Image` | **无子** | `CachedNetworkImage` | 网络图 |
| `Text` | 多子（行内 span） | `RichText` | 子节点只能是 `Text`/`Raw`/`Emoji` |
| `Raw` | 多子（行内 span） | 无（仅作 `Text` 的子节点） | 原样文本，**不 trim** |
| `Emoji` | **无子** | `ImageEmoji`（行内图片） | 只能作为 `Text` 的子节点 |

**没有对应标签的 Widget**（只能用 Kotlin DSL）：`Expanded`/`Flexible`、`Opacity`、`Transform`、各种 `Clip*`、`ColorFiltered`/`ImageFiltered`/`BackdropFilter`、`Align`/`Center`/`Padding`/`SizedBox`/`LimitedBox`/`OverflowBox` 等——解析器只覆盖 11 个标签。`<Border>` 的圆角/边框能力可以部分替代 `ClipRRect`。

### 10.3 属性取值格式

写属性前先看这张"格式表"，绝大多数报错都来自这里。

#### 颜色

```
#RRGGBB      → 不透明(alpha = 0xFF)
#AARRGGBB    → 带 alpha
```

- **必须以 `#` 开头**，长度必须是 **7 或 9**（`#` + 6 或 8 位十六进制）。
- 十六进制大小写均可。
- 错误信息：`Attr [xxx] value must start with #` / `must be ARGB or RGB color hex`。

#### 长度/数值

- 浮点属性直接 `toFloat()`：`width="200"`、`width="12.5"`。
- `null`（未写）走默认值；可空属性不写就是 `null`（表示"不设置"）。

#### EdgeInsets（padding / margin）

| 写法 | 含义 |
|---|---|
| `"10"` | 四边都是 10 |
| `"(1,2)"` | `symmetric(vertical = 1, horizontal = 2)` |
| `"(1,2,3,4)"` | `EdgeInsets(left=1, top=2, right=3, bottom=4)` |

> 格式严格：多值写法必须是**紧贴的圆括号 + 逗号**，不允许出现空格；也不接受三值写法。

#### 对齐 alignment

```
"TOP_LEFT" | "TOP_CENTER" | "TOP_RIGHT"
"CENTER_LEFT" | "CENTER" | "CENTER_RIGHT"
"BOTTOM_LEFT" | "BOTTOM_CENTER" | "BOTTOM_RIGHT"
"(0.5,-0.5)"        // 自定义,范围语义同 BoxAlignment
```

#### 圆角 borderRadius / borderRadius{Corner}

| 写法 | 含义 |
|---|---|
| `"12"` | `Radius.circular(12)` |
| `"(12,6)"` | `Radius.elliptical(x = 12, y = 6)` |

#### 边框 border / borderLeft / borderTop / borderRight / borderBottom

```
"<width> <STYLE> <#color>"
例:"2 SOLID #FF333333"、"1 NONE #00000000"
```

- **单空格**分隔，三段都必填；`STYLE ∈ {NONE, SOLID}`（大小写敏感）。
- `border` 是四条边的默认值；某条边单独写了就覆盖默认值。

#### 阴影 boxShadow

```
"ELEVATION_8"                                  // Material 高度,数字 ∈ {0,1,2,3,4,6,8,9,12,16,24}
"<x> <y> [blur] [spread] [#color] [blurMode]"  // 单条阴影,单空格分隔
"<阴影1>,<阴影2>"                               // 逗号分隔多条
例:"0 4 8 #33000000"、"0 2 4 1 #26000000 NORMAL"
```

- 位置参数顺序固定：`offsetX offsetY [blurRadius] [spreadRadius]`（最多到 spread）。
- `#color` 可省略（默认黑）；`blurMode` 取 `org.jetbrains.skia.FilterBlurMode` 的常量名（如 `NORMAL`）。
- 每条阴影参数个数必须在 **2..6** 之间。

#### 枚举属性

绝大多数枚举（`fit`、`repeat`、`mainAxisAlignment`、`crossAxisAlignment`、`mainAxisSize`、`verticalDirection`、`textBaseline`、`baseline`、`blendMode`、`colorBlendMode`、`alignment`(占位符)…）都用 `Enum.valueOf(str)`，即：

- **必须是源码里的精确常量名**（全大写 + 下划线）；
- 大小写敏感，写错抛 `IllegalArgumentException`。

具体可用值见[附录 A](#附录-a枚举取值速查)。

#### 布尔属性

用 Kotlin 的 `String.toBoolean()`：**只有 `"true"`（忽略大小写）为真，其他任何值都是假**。

> ⚠️ 裸写属性（`<Snapshot debug>`）在词法上 `value == null`，会**回落到默认值 `false`**，不是"置真"。要开必须写 `debug="true"` / `noCache="true"`。

#### URL

`url` 是必填属性，解析时用 `java.net.URL(valueStr)` 做一次格式校验（只校验语法，不发起请求）。

### 10.4 逐标签属性表

#### 根标签 `<Snapshot>`

| 属性 | 类型 | 必填 | 默认 | 说明 |
|---|---|---|---|---|
| `background` | color | 否 | `#00000000`（透明） | 画布底色 |
| `type` | string | 否 | `png` | `png` / `jpg` / `webp`；其他值报错 |
| `debug` | bool | 否 | `false` | 调试绘制 |

#### `<Container>`

| 属性 | 类型 | 默认 | 说明 |
|---|---|---|---|
| `width` / `height` | float | 不设置 | 收紧对应方向的约束 |
| `minWidth` / `maxWidth` / `minHeight` / `maxHeight` | float | `0 / ∞ / 0 / ∞` | **只要写了其中一个**，就构造一个完整 `BoxConstraints` 下传 |
| `alignment` | alignment | 不设置 | 子节点对齐 |
| `padding` | EdgeInsets | 不设置 | 内边距 |
| `margin` | EdgeInsets | 不设置 | 外边距 |
| `color` | color | 不设置 | 底色。**`color` 与边框装饰互斥但不会丢失**：只要写了 `border`/`borderRadius`/`boxShadow` 中任意一项，颜色就会被并入 `BoxDecoration(color = ...)`，最终仍然画得出来 |
| `border` / `borderLeft` / `borderTop` / `borderRight` / `borderBottom` | BorderSide | 不设置 | 边框 |
| `borderRadius` / `borderRadiusTopLeft` / `borderRadiusTopRight` / `borderRadiusBottomLeft` / `borderRadiusBottomRight` | Radius | 不设置 | 圆角 |
| `boxShadow` | BoxShadow | 不设置 | 阴影 |

子节点：最多 1 个。

```html
<Container color="#FF00FF00" width="400" height="300" alignment="CENTER" padding="10" margin="(1,2,4,8)">
    <Container color="#FFFF0000" width="100" height="50"/>
</Container>
```

#### `<Border>`

属性与 `<Container>` 的装饰类属性完全一致：`color`、`border`、`borderLeft/Top/Right/Bottom`、`borderRadius`、`borderRadius{Corner}`、`boxShadow`。解析成 `DecoratedBox(decoration = BoxDecoration(...))`，**不支持** `width`/`height`/`padding` 等布局属性。

```html
<Border border="4 SOLID #FF2196F3" borderRadius="16" boxShadow="ELEVATION_4">
    <Container color="#FFFFFFFF" width="200" height="100"/>
</Border>
```

#### `<Row>` / `<Column>`

| 属性 | 类型 | 默认 | 说明 |
|---|---|---|---|
| `mainAxisAlignment` | enum | `START` | `START`/`END`/`CENTER`/`SPACE_BETWEEN`/`SPACE_AROUND`/`SPACE_EVENLY` |
| `mainAxisSize` | enum | `MAX` | `MIN`/`MAX` |
| `crossAxisAlignment` | enum | `CENTER` | `START`/`END`/`CENTER`/`STRETCH`/`BASELINE` |
| `textDirection` | enum | `LTR` | `LTR`/`RTL` |
| `verticalDirection` | enum | `DOWN` | `UP`/`DOWN` |
| `textBaseline` | enum | 不设置 | `ALPHABETIC`/`IDEOGRAPHIC`（`crossAxisAlignment="BASELINE"` 时需要） |

#### `<Stack>`

| 属性 | 类型 | 默认 | 说明 |
|---|---|---|---|
| `alignment` | alignment | **`TOP_LEFT`** | 非定位子节点的对齐（注意：DSL 默认是 `TOP_START`，解析器默认是 `TOP_LEFT`） |
| `textDirection` | enum | `LTR` | `LTR`/`RTL` |

> 解析器提供的 `<Stack>` **不暴露** `fit` / `clipBehavior`，即固定 `StackFit.LOOSE` + `ClipBehavior.HARD_EDGE`。

#### `<Positioned>`

| 属性 | 类型 | 默认 |
|---|---|---|
| `left` / `top` / `right` / `bottom` | float | 不设置 |
| `width` / `height` | float | 不设置 |

约束同 DSL：`left`/`right`/`width` 最多两个；`top`/`bottom`/`height` 最多两个。

#### `<Image>`

| 属性 | 类型 | 必填 | 默认 | 说明 |
|---|---|---|---|---|
| `url` | url | **是** | — | 图片地址 |
| `width` / `height` | float | 否 | 不设置 | |
| `fit` | enum | 否 | 不设置 | `FILL`/`CONTAIN`/`COVER`/`FIT_WIDTH`/`FIT_HEIGHT`/`NONE`/`SCALE_DOWN` |
| `alignment` | alignment | 否 | `CENTER` | 图片在盒内的对齐 |
| `repeat` | enum | 否 | `NO_REPEAT` | `REPEAT`/`REPEAT_X`/`REPEAT_Y`/`NO_REPEAT` |
| `scale` | float | 否 | `1` | 缩放系数 |
| `opacity` | float | 否 | `1` | 透明度 |
| `color` | color | 否 | 不设置 | 叠加色 |
| `colorBlendMode` | enum | 否 | 不设置 | `org.jetbrains.skia.BlendMode` 常量名 |
| `noCache` | bool | 否 | `false` | 绕过缓存（需写 `"true"`） |

```html
<Image width="200" height="200" url="https://samples-files.com/samples/images/jpg/480-360-sample.jpg" fit="COVER"/>
```

#### `<Text>`

| 属性 | 类型 | 默认 | 说明 |
|---|---|---|---|
| `text` | string | 不设置 | 文本内容；**会被 trim** |
| `color` | color | 不设置 | |
| `fontSize` | float | 不设置 | |
| `fontFamily` | string | 不设置 | 多个字体用**英文逗号**分隔，且**不做 trim**：`"A, B"` 的第二个字体名会带上前导空格 |
| `fontStyle` | enum | 不设置 | `NORMAL`/`BOLD`/`ITALIC`/`BOLD_ITALIC` |

`<Text>` 可以嵌套 `<Text>`/`<Raw>`/`<Emoji>` 组成富文本；子 span 的样式会覆盖/继承父 span。

```html
<Text color="#FF0000" fontSize="40">Hello<Text color="#00FF00" fontSize="30"> World</Text></Text>
```

#### `<Raw>`

属性与 `<Text>` 完全相同（`text`、`color`、`fontSize`、`fontFamily`、`fontStyle`），区别只有一个：**文本不做 trim**，首尾空白与换行原样保留（源码里走 `parseTextSpan(raw = true)`）。仅可作为 `<Text>` 的子节点。

#### `<Emoji>`

| 属性 | 类型 | 必填 | 默认 | 说明 |
|---|---|---|---|---|
| `url` | url | **是** | — | emoji 图片地址 |
| `alignment` | enum | 否 | `BASELINE` | `PlaceholderAlignment`：`BASELINE` / `ABOVE_BASELINE` / `BELOW_BASELINE` / `TOP` / `BOTTOM` / `MIDDLE`（注意是**占位符对齐**，不是 `BoxAlignment`） |
| `baseline` | enum | 否 | 不设置 | `BaselineMode`：`ALPHABETIC`/`IDEOGRAPHIC` |
| `width` / `height` | float | 否 | 不设置 | 不给则按所在 span 的 `fontSize` 推导 |
| `fit` | enum | 否 | 不设置 | 同 `<Image>` |
| `imageAlignment` | alignment | 否 | `CENTER` | 图片在占位盒内的对齐 |
| `repeat` | enum | 否 | `NO_REPEAT` | |
| `scale` | float | 否 | `1` | |
| `opacity` | float | 否 | `1` | |
| `color` | color | 否 | 不设置 | |
| `colorBlendMode` | enum | 否 | 不设置 | |

仅可作为 `<Text>` 的子节点。

```html
<Text fontSize="40">你好<Emoji url="https://example.com/smile.png" width="40" height="40"/>世界</Text>
```

### 10.5 文本、空白与 CDATA

- **CDATA 可用**：`<![CDATA[ ... ]]>` 中的内容原样作为文本，`<`、`>`、`&` 都无需转义。

  ```html
  <Text color="#0000FF" fontSize="20">哈哈 233<![CDATA[ken_test <a></a> 233 哈哈]]>哈🤣🤣🤣</Text>
  ```

- 🚨 **文本内容里不要出现裸 `&`**。tokenizer 没有实现字符引用解码，而 DATA 状态遇到 `&` 时既不消费字符也不产生 token，会让 `Tokenizer.read()` 的循环**永远空转（挂死）**，而不是抛异常。这是从源码推导出的结论（本机无法运行 JVM 实测），但风险足够高，含 `&` 的文本请一律用 CDATA 包裹：

  ```html
  <!-- 想表达 A & B -->
  <!-- ✗ 不要这样:&amp; 不会被解码成 &,而且裸 & 会让 tokenizer 挂死 -->
  <Text>A &amp; B</Text>
  <!-- ✓ 这样写 -->
  <Text><![CDATA[A & B]]></Text>
  ```

  属性值里的 `&` 是安全的（会被当作普通字符保留，同样不解码）。
- **不支持 HTML 实体**：`&lt;` 会**原样**保留成四个字符 `&lt;`（前提是它没有触发上面的挂死问题）。
- **不支持注释与 DOCTYPE**：`<!-- ... -->`、`<!DOCTYPE ...>` 都会报 `Unexpected character '<' in input state [MARKUP_DECLARATION_OPEN]`。只有 `<![CDATA[` 这一种标记声明。
- **`<Text>` 内的文本会被 trim**，首尾空白丢失；`<Raw>` 不 trim、原样保留（内部换行也保留）。
- **非文本标签内不允许出现非空白字符**：`<Container>hello</Container>` 会抛 `ParseException: Not Support RAWTEXT: hello`。标签之间的缩进/换行属于纯空白，会被忽略。
- **标签可以不闭合**：`parse()` 结束时会自动闭合栈内剩余标签。结束标签会在最多 **256 层**（`Parser.MAX_QUEUE_DEPTH`）栈深度内按标签名回溯匹配，**找不到就静默忽略**；因此写错闭合标签往往不会报错，只是结构和你以为的不一样。
- 开始标签**不能重复**：第一个被解析的标签必须是 `<Snapshot>`，之后再出现 `<Snapshot>` 开始标签会报错。

### 10.6 错误处理

解析与建树阶段的错误统一包装成 `com.muedsa.snapshot.parser.ParseException`（`RuntimeException` 子类），`toString()` 形如 `Pos[行:列]~偏移: 消息`（行列从 1 起，偏移从 0 起；未设置显示 `UNSET`）；原异常在 `cause` 里。

**`parse()` 阶段（结构性错误）**

| 触发条件 | 消息（节选） |
|---|---|
| 第一个标签不是 `Snapshot` | `First tag must be 'Snapshot', but get 'Xxx'` |
| 之后又出现 `Snapshot` 开始标签 | `Tag 'Snapshot' only be used as the first, but get 'Snapshot' at Pos[…]` |
| 未知标签 | `Unknown element tag [Xxx]` |
| 标签名为空 | `element tag name can not be null` |
| 同一标签重复属性 | `Element tag [Xxx] exist duplicate attr` |
| 给无子节点的标签加了子节点 | `Tag Image can not have child element Container` |
| 给单子节点标签加了第二个子节点 | `Tag Container only can have one child, but get other Positioned` |
| 非文本标签内出现文本 | `Not Support RAWTEXT: …` |
| 结束标签带属性 | `Attributes incorrectly present on end tag [/Text]` |
| 不支持 `<!--`/`<!DOCTYPE` | `Unexpected character '<' in input state [MARKUP_DECLARATION_OPEN]` |
| 中途 EOF | `Unexpectedly reached end of file (EOF) in input state [STATE]` |
| **根标签**属性格式错 | 同下表，但位置在 `parse()` 阶段就抛出 |

**`createWidget()` / `snapshot()` 阶段（属性值错误）**

| 触发条件 | 消息（节选） |
|---|---|
| 缺必填属性 | `Attr [url] must not be null` |
| 颜色格式错 | `Attr [color] value must start with #` / `must be ARGB or RGB color hex` |
| 数值/结构格式错 | `Attr [padding] value format error`、`boxShadows format error`、`not exist elevation 111, …` |
| 枚举名写错 | `Enum.valueOf` 抛出的 `IllegalArgumentException`（消息为常量名） |
| `fontStyle` 取值非法 | `Unexpected font style XXX` |
| `<Text>` 里出现非行内标签 | `Unknown inline span type: Xxx` |
| `<Raw>`/`<Emoji>` 用在 `<Text>` 之外 | `Element [Raw] … can not buildWidget, it can only be used in the Text` |

**`snapshot()` 的渲染阶段（非 `ParseException`）**

| 触发条件 | 异常与消息 |
|---|---|
| `<Snapshot>` 没有子节点 | `IllegalStateException: element [Snapshot] content is empty` |
| 布局尺寸为空 | `IllegalArgumentException: Layout size is empty` |
| 布局尺寸无限 | `IllegalArgumentException: Layout size is infinite` |

建议调用方把 `ParseException` 当"请求不合法"（400），把 `IllegalArgumentException`/`IllegalStateException` 当"内容本身无法出图"（422）分别处理：

```kotlin
try {
    val bytes = Parser().parse(StringReader(text)).snapshot()
    // 返回图片
} catch (e: ParseException) {
    // 400 Bad Request,e.message 里带行列号
}
```

### 10.7 完整示例

```kotlin
import com.muedsa.snapshot.parser.Parser
import java.io.StringReader
import java.io.File

val text = """
<Snapshot background="#FFFFFFFF" type="png">
    <Column mainAxisAlignment="CENTER" crossAxisAlignment="CENTER">
        <Row>
            <Container color="#FF0000" width="200" height="200"/>
            <Container color="#FFFFFF" width="200" height="200" padding="10">
                <Text color="#0000FF" fontSize="20">哈哈 233<![CDATA[ken_test <a></a> 233 哈哈]]>哈🤣🤣🤣</Text>
            </Container>
        </Row>
        <Row>
            <Image width="200" height="200" url="https://samples-files.com/samples/images/jpg/480-360-sample.jpg" fit="COVER"/>
            <Border border="4 SOLID #FF2196F3" borderRadius="24" boxShadow="ELEVATION_4">
                <Container color="#FFFF00" width="192" height="192"/>
            </Border>
        </Row>
    </Column>
</Snapshot>
""".trimIndent()

File("out.png").writeBytes(Parser().parse(StringReader(text)).snapshot())
```

`<Stack>` + `<Positioned>` 示例（解析器不支持 `Opacity`/`Padding` 标签，用 `Container` 的 `padding` 代替）：

```html
<Snapshot background="#FFEEEEEE" type="png">
    <Stack alignment="TOP_LEFT">
        <Container width="400" height="300" color="#FFFFFFFF"/>
        <Positioned left="20" top="20">
            <Container width="120" height="120" color="#FFFF5722" borderRadius="16"/>
        </Positioned>
        <Positioned bottom="20" right="20" width="160" height="60">
            <Container color="#FF3F51B5" borderRadius="30" alignment="CENTER">
                <Text color="#FFFFFFFF" fontSize="24">snapshot</Text>
            </Container>
        </Positioned>
    </Stack>
</Snapshot>
```

### 10.8 扩展：自定义标签

`WidgetParserManager` 是可变的标签注册表，`Parser` 的构造参数是 `protected`，可以通过继承替换：

```kotlin
// 复制一份内置标签表,再注册自己的标签 —— 不要直接往 DEFAULT_MANAGER 里 register(那是全局单例)
val manager = WidgetParserManager().also { m ->
    WidgetParserManager.DEFAULT_MANAGER.tags.values.forEach(m::register)
    m.register(MyTagParser())
}

class MyParser : Parser(manager)

class MyTagParser : WidgetParser {
    override val id: String = "MyTag"                       // 标签名(大小写敏感)
    override val containerMode: ContainerMode = ContainerMode.SINGLE
    override fun buildWidget(element: Element): Widget =
        /* 用 element.attrs + WidgetParser.parseAttrValue(...) 构造 Widget */ TODO()
}
```

要点：

- `containerMode`：`NONE`（不允许子节点）/ `SINGLE`（最多 1 个）/ `MULTIPLE`（任意个）；
- 属性用 `AttrDefine` 描述（`DefaultValueAttrDefine` 有默认值、`required.AttrDefine` 必填），`WidgetParser.parseAttrValue(define, element.attrs)` 负责解析并把 `Throwable` 包装成带位置的 `ParseException`；
- 若要新增"行内 span"类标签，需要同时改 `Element.parseInlineSpan()` 的分支（目前只认 `TextParser`/`RawTextParser`/`EmojiParser`）。

---

## 11. 测试

`snapshot` 自己的测试基础设施在 `:testkit`，提供四层能力：

1. **布局/数值断言**（`rootLayout` + `assertSize`/`assertGlobalRect`/`findType`）——跨 OS 最稳，首选；
2. **像素采样断言**（`snapshotPixels` + `expectColorAt`/`expectRegionUniform`…）；
3. **golden 基准比对**（`golden`/`goldenPixels`，逐像素回归，三态 `record`/`verify`/`update`）；
4. **artifact**（`drawWidget`/`drawPainter`，只落盘供人眼检视，不做断言）。

完整手册（含分步指南、活模板、FAQ）：[`docs/testing/README.md`](../testing/README.md)。

```kotlin
// core/src/test/kotlin/.../MyWidgetTest.kt
class MyWidgetTest {
    @Test
    fun inner_box_is_centered() {
        val root = rootLayout {
            Container(width = 200f, height = 200f, alignment = BoxAlignment.CENTER, color = 0xFF808080.toInt()) {
                SizedBox(width = 100f, height = 100f) { ColoredBox(color = Color.RED) }
            }
        }
        root.assertSize(200f, 200f)
        root.findType<RenderColoredBox> { it.color == Color.RED }!!.assertGlobalRect(50f, 50f, 100f, 100f)
    }
}
```

---

## 12. 常见问题

**Q：`SnapshotPNG { Container() }` 为什么报 `layout size is infinite`？**
空 `Container` 在没有约束时会组合出"尽可能大"的约束；根约束的最大值是无限，于是尺寸无限。给它 `width`/`height`，或外面套 `SizedBox`/有界 `Container`。

**Q：为什么 `Row {}` 报 `layout size is empty`？**
Flex 在主轴无界时会收缩到子节点总尺寸，没有子节点就是 `0×0`。给子节点或给显式尺寸。

**Q：图片比预期大/小？**
Surface 尺寸 = `ceil()` 后的布局尺寸，内容以 1:1 从原点绘制。要精确控制画布，用根 `Container(width, height)` 或 `SizedBox`；要更大更清晰的输出，请按倍数放大 widget 树里的尺寸（而不是只放大 `initSurface` 的 Surface，那只会多出背景留白）。

**Q：`Container` 的 `color` 和 `border` 能一起用吗？**
`Container` 的 `color` 与 `decoration` 互斥（会 `check` 失败）。要"底色 + 边框"，用 `decoration = BoxDecoration(color = ..., border = ...)`。解析器里的 `<Container>` 已经做了这个转换：写了边框属性时 `color` 会自动并入 `decoration`。

**Q：`Stack` 里超出的内容被切掉了？**
`Stack` 的 `clipBehavior` 默认 `HARD_EDGE`。显式传 `ClipBehavior.NONE`。

**Q：想旋转画布上的内容？**
用 `Transform.rotate(angle, alignment = BoxAlignment.CENTER)`；`angle` 是弧度（`computeRotation`）。注意 DSL 的 `Transform(transform = ..., alignment = ...)` 没有默认 `alignment`，必须显式传（可传 `null`）。

**Q：`<Container>` 的 `padding="1 2 3 4"` 不生效？**
EdgeInsets 必须写成 `"(1,2,3,4)"` 或 `"10"` 或 `"(1,2)"`，圆括号和逗号是格式的一部分。

**Q：`<Snapshot debug>` 没打开调试？**
裸属性会被当成 `null` 并回落到默认值 `false`。写 `debug="true"`。

**Q：解析器里能用 `Expanded` 吗？**
不能——解析器只注册了 11 个标签（见 [10.2](#102-标签总表)）。需要 `Expanded`/`Opacity`/`Transform`/`Clip*` 等请在 Kotlin DSL 侧构建，或按 [10.8](#108-扩展自定义标签) 注册自定义标签。

**Q：文本里怎么写 `<` `>`？**
用 `<![CDATA[ ... ]]>`。库**不做** HTML 实体解码，`&lt;` 会原样输出；而且文本里出现**裸 `&` 会让解析器挂死**（不是报错），含 `&` 的文本一律用 CDATA 包起来（见 [10.5](#105-文本空白与-cdata)）。注释 `<!-- -->` 也不支持。

**Q：第二次调用 `Parser().parse(...)` 报 `Duplicate root element`？**
是同一个 `Parser` 实例被复用了——`parse()` 不重置内部的 `snapshotElement`。每次解析都 `Parser()` 新建一个。

**Q：解析出来什么都没变，属性像是没生效？**
先确认属性名拼写与大小写。**未知属性会被静默忽略**；另外 Boolean 属性必须显式写 `"true"`。属性值的报错要到 `createWidget()`/`snapshot()` 阶段才会抛出，`parse()` 成功不代表文档完全合法。

**Q：网络图加载是异步的吗？**
不是。`CachedNetworkImage`/`<Image>`/`<Emoji>` 在构建/绘制时同步发起 HTTP 请求。注意线程与超时。

**Q：运行时怎么调试布局？**
`Snapshot(..., debug = true)` 会画调试辅助（空盒灰块、`ClipPath` 的调试描边等）；更结构化地用 `layoutWidget { ... }` 拿到 `RenderBox` 树，或 `LayoutNode` 只读自省树。

**Q：能渲染到窗口/离屏 Surface 而不只出图吗？**
可以：`layoutWidget { ... }` 拿到 `RenderBox`，再把 `Canvas.drawRenderBox(renderBox, background, debug)` 接到 `SkiaLayer` 的渲染回调里（仓库里的 `core/src/test/kotlin/com/muedsa/snapshot/DrawToDevice.kt` 就是完整例子：`skiaLayer.renderDelegate = SkiaLayerRenderDelegate(skiaLayer, object : SkikoRenderDelegate { override fun onRender(canvas, width, height, nanoTime) { ... } })`）。

---

## 附录 A：枚举取值速查

| 枚举 | 全量取值 |
|---|---|
| `BoxAlignment` | `TOP_LEFT` `TOP_CENTER` `TOP_RIGHT` `CENTER_LEFT` `CENTER` `CENTER_RIGHT` `BOTTOM_LEFT` `BOTTOM_CENTER` `BOTTOM_RIGHT`（另可 `BoxAlignment(x, y)`） |
| `AlignmentDirectional` | `TOP_START` `TOP_CENTER` `TOP_END` `CENTER_START` `CENTER` `CENTER_END` `BOTTOM_START` `BOTTOM_CENTER` `BOTTOM_END` |
| `Axis` | `HORIZONTAL` `VERTICAL` |
| `MainAxisAlignment` | `START` `END` `CENTER` `SPACE_BETWEEN` `SPACE_AROUND` `SPACE_EVENLY` |
| `CrossAxisAlignment` | `START` `END` `CENTER` `STRETCH` `BASELINE` |
| `MainAxisSize` | `MIN` `MAX` |
| `FlexFit` | `TIGHT` `LOOSE` |
| `VerticalDirection` | `UP` `DOWN` |
| `StackFit` | `LOOSE` `EXPAND` `PASSTHROUGH` |
| `ClipBehavior` | `NONE` `HARD_EDGE` `ANTI_ALIAS` `ANTI_ALIAS_WITH_SAVE_LAYER` |
| `DecorationPosition` | `BACKGROUND` `FOREGROUND` |
| `BoxShape` | `RECTANGLE` `CIRCLE` |
| `BorderStyle` | `NONE` `SOLID` |
| `BoxFit` | `FILL` `CONTAIN` `COVER` `FIT_WIDTH` `FIT_HEIGHT` `NONE` `SCALE_DOWN` |
| `ImageRepeat` | `REPEAT` `REPEAT_X` `REPEAT_Y` `NO_REPEAT` |
| `TextOverflow` | `CLIP` `FADE` `ELLIPSIS` `VISIBLE` |
| `TextWidthBasis` | `PARENT` `LONGESTLINE` |
| `ContainerMode`（parser） | `NONE` `SINGLE` `MULTIPLE` |
| `FontStyle`（skiko） | `NORMAL` `BOLD` `ITALIC` `BOLD_ITALIC` |
| `FilterBlurMode`（skiko） | `NORMAL` `SOLID` `OUTER` `INNER` |
| `Direction`（skiko paragraph） | `LTR` `RTL` |
| `BaselineMode`（skiko paragraph） | `ALPHABETIC` `IDEOGRAPHIC` |
| `PlaceholderAlignment`（skiko paragraph） | `BASELINE` `ABOVE_BASELINE` `BELOW_BASELINE` `TOP` `BOTTOM` `MIDDLE` |

---

## 附录 B：源码索引

| 想找什么 | 去哪看 |
|---|---|
| 渲染入口 `Snapshot*` / `layoutWidget` / `drawRenderBox` | `core/src/main/kotlin/com/muedsa/snapshot/Snapshot.kt` |
| 全局常量（默认字号/颜色/容差） | `core/.../snapshot/Const.kt` |
| Widget 工厂与实现 | `core/.../snapshot/widget/`（文本在 `widget/text/`） |
| 约束与 RenderBox | `core/.../snapshot/rendering/box/` |
| Flex / Stack 布局算法 | `core/.../snapshot/rendering/flex/RenderFlex.kt`、`rendering/stack/RenderStack.kt` |
| 装饰、边框、阴影 | `core/.../snapshot/paint/decoration/` |
| 渐变 | `core/.../snapshot/paint/gradient/` |
| 文本样式与度量 | `core/.../snapshot/paint/text/` |
| 网络图片缓存 | `core/.../snapshot/tools/` |
| 布局自省 `LayoutNode` | `core/.../snapshot/rendering/LayoutTree.kt` |
| 解析入口 | `parser/.../snapshot/parser/Parser.kt` |
| 根元素与出图 | `parser/.../snapshot/parser/SnapshotElement.kt` |
| 标签注册表 | `parser/.../snapshot/parser/widget/WidgetParserManager.kt` |
| 各标签实现 | `parser/.../snapshot/parser/widget/*Parser.kt` |
| 属性取值格式 | `parser/.../snapshot/parser/attr/`（可空变体在 `attr/nullable/`，必填在 `attr/required/`） |
| 测试基建 | `testkit/src/main/kotlin/com/muedsa/snapshot/` |
| 测试手册 | `docs/testing/README.md` |
