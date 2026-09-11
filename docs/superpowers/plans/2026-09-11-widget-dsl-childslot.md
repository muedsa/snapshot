# Widget DSL 批次 2(类型化子槽位 ChildSlot)Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 把全部 Widget DSL 函数的接收者从 `Widget.` 改成 `ChildSlot.`,删除 `buildChild` / `bind` 两个薄封装,删除所有 Widget 类的 `parent` 构造参数,并把 8 处入口签名收敛为 `ChildSlot.() -> Unit`——使"在没有子槽位的 Widget 上挂子节点"与"helper 忘记挂载"从运行时异常变成编译错误。

**Architecture:** 批次 1 已把挂载原语收敛为 `ChildSlot.attach`(实现在 `ProxyWidget` / `SingleChildWidget` / `MultiChildWidget`,内部含重复挂载校验与 `parent` 回指),且 `buildChild` / `bind` 已是它的薄封装。本批只做"类型化 + 去封装":DSL 函数体从 `buildChild(widget = X(...), content = content)` 变为 `attach(X(...).apply(content))`,接收者收窄到 `ChildSlot`,入口 lambda 接收者同步收窄。`Widget.parent` 的唯一写入点仍只有 `attach`。

**Tech Stack:** Kotlin/JVM 2.4.20、Gradle Wrapper 9.7.1、JDK 17、kotlin.test(JUnit Platform)。

**Spec:** `docs/superpowers/specs/2026-09-11-widget-dsl-childslot-design.md`(批次 2 一节)

**前置:** 批次 1 已合入 `main`(PR #116 = `a111a38`)。本分支 `refactor/widget-dsl-childslot` 基于它。

## Global Constraints

- 仓库根 `D:\mine\workspace\snapshot`;本计划**只**在分支 `refactor/widget-dsl-childslot` 上执行。
- **不要** `git push`、**不要**创建 PR——由用户手动创建与合并。
- 所有命令在 PowerShell 下执行,用 `.\gradlew.bat`(**不要**用 `./gradlew`)。若 Gradle 报缓存 `AccessDenied`,追加 `--no-build-cache` 重跑。
- 每个提交必须 GPG 签名:`git commit -S -m "…"`;提交后跑 `git log -1 --pretty='%h %G? %s'`,首字段必须是 `G`。若报 `No passphrase given`,原样重试一次。
- 提交信息:Conventional Commits + **中文**标题。
- **异常/断言消息一律英文**;**新增 KDoc 用中文**。
- **不引入任何新依赖、不新增 Gradle 模块、不改 `build.gradle.kts`。**
- **本批是原子的**:接收者改成 `ChildSlot.` 后,入口签名不改就编译不过。因此 Task 1 必须一次性改完再编译,**不要**试图拆成能各自编译的多个提交。
- 本批**不动** `parentData` 行为(`renderBox.child!!`、`children.size` 静默跳过)、**不动** `appendChild` 的 `@Synchronized` 与 O(n²) `contains`、**不删** `Widget.parent` 字段——那是批次 3/4。
- 基线:`.\gradlew.bat test --console=plain` → `BUILD SUCCESSFUL`,**216 例执行,0 失败**。

## 改动清单(执行前已实测盘点)

| 类别 | 数量 | 位置 |
|---|---|---|
| DSL 函数接收者 `Widget.` → `ChildSlot.` | **30** | `core/src/main/.../widget/**`(见 Task 1 Step 2 的名单) |
| DSL 函数体 `buildChild(...)` → `attach(X(...).apply(content))` | **32** | 同上(实测 `buildChild(` 出现在 32 个文件) |
| 类构造参数 `parent: Widget?` 删除 | **30 个 widget 类 + 5 个基类** | `Widget` / `SingleChildWidget` / `MultiChildWidget` / `ProxyWidget` / `ParentDataWidget` + 各 Widget |
| 入口/helper 的 `Widget.() -> Unit` → `ChildSlot.() -> Unit` | **17** | `core/Snapshot.kt`(6)、`testkit/TestKit.kt`(5)、`testkit/TestTool.kt`(1)、`widget/ProxyWidget.kt`(1)、`widget/text/WidgetSpan.kt`(1)、`core/src/test/widget/ClipTest.kt`(1)、`ImageFilteredTest.kt`(1)、`BackdropFilterTest.kt`(1) |
| 测试里的 `fun Widget.xxx()` helper → `fun ChildSlot.xxx()` | **28** | `core/src/test/**` |
| `.bind(...)` 调用点 → `attach` | **12** | `Container.composeWidget()`(10,含 9 处 `.bind(current)` 与 1 处 `.bind(ConstrainedBox(...))`)、`text/WidgetSpan.kt`(1)、`parser/WidgetParser.kt`(1) |
| 文档同步 | 1 | `docs/usage/README.md` §4.1 |

保持不变(不在本批范围):`Fun Flex.Expanded` / `Flex.Flexible` / `Stack.Positioned` 的窄接收者;`fun TextSpan.*` 系列(`TextSpan` / `ImageEmojiSpan` / `WidgetSpan`)。

---

### Task 1: 类型化子槽位核心重构

**Files:**
- Modify: `core/src/main/kotlin/com/muedsa/snapshot/widget/` 下全部 `.kt`(约 34 个)
- Modify: `core/src/main/kotlin/com/muedsa/snapshot/Snapshot.kt`
- Modify: `core/src/test/kotlin/com/muedsa/snapshot/**`(约 20 个测试文件)
- Modify: `testkit/src/main/kotlin/com/muedsa/snapshot/TestKit.kt`、`TestTool.kt`
- Modify: `parser/src/main/kotlin/com/muedsa/snapshot/parser/Element.kt`、`parser/widget/WidgetParser.kt`

**Interfaces:**
- Consumes(批次 1 已有):`interface ChildSlot { fun attach(child: Widget) }`;`@SnapshotWidgetDsl`。
- Produces:
  - 所有 Widget DSL 函数接收者为 `ChildSlot`,形如 `inline fun ChildSlot.Padding(padding: EdgeInsets, content: Padding.() -> Unit = {})`
  - 入口 `Snapshot` / `SnapshotImage` / `SnapshotPNG` / `SnapshotJPEG` / `SnapshotWEBP` / `layoutWidget` / `rootLayout` / `golden` / `snapshotPixels` / `snapshotImage` / `ProxyWidget.buildWidget` / `TextSpan.WidgetSpan` 的 content 参数均为 `ChildSlot.() -> Unit`
  - `ProxyWidget.buildWidget` 变为 `@PublishedApi internal`
  - **删除**:`Widget.buildChild`、`Widget.bind`、所有 Widget 类的 `parent` 构造参数

- [ ] **Step 1: 确认基线**

Run:
```powershell
.\gradlew.bat test --console=plain
```
Expected: `BUILD SUCCESSFUL`。失败就停下来报告。

- [ ] **Step 2: 脚本 A —— DSL 函数接收者 `Widget.` → `ChildSlot.`**

只为 `fun Widget.<大写字母开头>` 的定义改名(`fun Widget.buildChild` 不在本次改动内,它将在 Step 7 被删除):

```powershell
$enc = New-Object System.Text.UTF8Encoding($false)
Get-ChildItem -Recurse -Path core\src\main\kotlin\com\muedsa\snapshot\widget -Filter *.kt | ForEach-Object {
  $t = [System.IO.File]::ReadAllText($_.FullName)
  $n = $t -replace '(?m)^(inline )?fun Widget\.([A-Z])', '$1fun ChildSlot.$2'
  if ($n -ne $t) { [System.IO.File]::WriteAllText($_.FullName, $n, $enc); Write-Output "receiver: $($_.Name)" }
}
```
Expected: 打印 **30** 个文件名,覆盖 `Align / BackdropFilter / CachedNetworkImage / Center / ClipOval / ClipPath / ClipRect / ClipRRect / ColoredBox / Column / ColorFiltered / ConstrainedBox / Container / DecoratedBox / Flex / ImageFiltered / LimitedBox / Opacity / OverflowBox / Padding / ProviderImage / RawImage / Row / SizedBox / SizedOverflowBox / Stack / Transform / RichText(widget/text) / Text / RichText(widget/text/Text.kt)`。

- [ ] **Step 3: 脚本 B —— 删除 DSL 函数体里的 `parent = this`**

```powershell
$enc = New-Object System.Text.UTF8Encoding($false)
Get-ChildItem -Recurse -Path core\src\main\kotlin\com\muedsa\snapshot\widget -Filter *.kt | ForEach-Object {
  $t = [System.IO.File]::ReadAllText($_.FullName)
  $n = $t -replace '(?m)^[ \t]*parent = this,\r?\n', ''
  if ($n -ne $t) { [System.IO.File]::WriteAllText($_.FullName, $n, $enc); Write-Output "drop parent=this: $($_.Name)" }
}
```
Expected: 约 **30** 个文件(以脚本实际输出为准)。该模式**不会**误伤 `it.parent = this`(`text/RichText.kt`)、`child.parent = this`(`Widget.kt` 的 `attach`)、`parent = null`(`Container.composeWidget`)。

- [ ] **Step 4: 脚本 C —— `buildChild(...)` 函数体改写成 `attach(X(...).apply(content))`**

按行处理,保留原有缩进;`content = content` 时给构造调用尾加 `.apply(content)`,`content = {}` / `content = { }` 时直接去掉 content 实参:

```powershell
$enc = New-Object System.Text.UTF8Encoding($false)
Get-ChildItem -Recurse -Path core\src\main\kotlin\com\muedsa\snapshot\widget -Filter *.kt | ForEach-Object {
  $lines = [System.IO.File]::ReadAllLines($_.FullName)
  $out = New-Object System.Collections.Generic.List[string]
  $hit = $false
  foreach ($line in $lines) {
    $t = $line.Trim()
    $ind = $line -replace '\S.*$', ''
    if ($t -eq 'buildChild(') { $out.Add($ind + 'attach('); $hit = $true; continue }
    if ($t -like 'widget = *') { $out.Add($ind + $t.Substring(9)); continue }
    if ($t -eq 'content = content') { $out[$out.Count - 1] = $out[$out.Count - 1] -replace '\),$', ').apply(content)'; continue }
    if ($t -eq 'content = {}' -or $t -eq 'content = { }') { $out[$out.Count - 1] = $out[$out.Count - 1] -replace '\),$', ')'; continue }
    $out.Add($line)
  }
  if ($hit) { [System.IO.File]::WriteAllLines($_.FullName, $out, $enc); Write-Output "body: $($_.Name)" }
}
```
Expected: 约 **32** 个文件(以脚本实际输出为准)。改完后每个函数体形如:

```kotlin
inline fun ChildSlot.Padding(padding: EdgeInsets, content: Padding.() -> Unit = {}) {
    attach(
        Padding(
            padding = padding
        ).apply(content)
    )
}
```

- [ ] **Step 5: 脚本 D —— 删除全部 `parent` 构造参数与转发**

```powershell
$enc = New-Object System.Text.UTF8Encoding($false)
$roots = @('core\src\main\kotlin\com\muedsa\snapshot\widget')
Get-ChildItem -Recurse -Path $roots -Filter *.kt | ForEach-Object {
  $t = [System.IO.File]::ReadAllText($_.FullName)
  $o = $t
  $t = $t -replace '(?m)^[ \t]*parent: Widget\? = null,\r?\n', ''      # 有默认值的构造/工厂参数
  $t = $t -replace '(?m)^[ \t]*parent: Widget\?,\r?\n', ''             # 无默认值的构造参数
  $t = $t -replace '(?m)^[ \t]*parent = parent,?\r?\n', ''             # 转发给父类构造
  $t = $t -replace ', parent: Widget\? = null\)', ')'                  # 单行工厂签名(SizedBox / Positioned)
  $t = $t -replace '\(parent: Widget\? = null\)', '()'
  $t = $t -replace 'Widget\(parent = parent\)', 'Widget()'
  $t = $t -replace 'ProxyWidget\(parent = parent\)', 'ProxyWidget()'
  $t = $t -replace 'SingleChildWidget\(parent = parent\)', 'SingleChildWidget()'
  if ($t -ne $o) { [System.IO.File]::WriteAllText($_.FullName, $t, $enc); Write-Output "ctor: $($_.Name)" }
}
```
Expected: 约 **35** 个文件。**脚本处理后必须逐个复核这 5 个基类的声明**,它们要手工改成最终形态(Step 5b):

- [ ] **Step 5b: 手工确认 5 个基类声明与 `Widget` 的 `init`**

`Widget.kt`:
```kotlin
@SnapshotWidgetDsl
abstract class Widget {
    var parent: Widget? = null
        internal set

    abstract fun createRenderBox(): RenderBox
}
```
(`Widget` 的 `init { this.parent = parent }` 整块删除——`parent` 现在只由 `attach` 写入)

`SingleChildWidget.kt` → `abstract class SingleChildWidget : Widget(), ChildSlot {`
`MultiChildWidget.kt` → `abstract class MultiChildWidget : Widget(), ChildSlot {`
`ProxyWidget.kt` → `open class ProxyWidget : Widget(), ChildSlot {`
`ParentDataWidget.kt` → `abstract class ParentDataWidget : ProxyWidget() {`

- [ ] **Step 6: 脚本 E —— 入口与 helper 的 `Widget.() -> Unit` → `ChildSlot.() -> Unit`,测试 helper 接收者改名**

```powershell
$enc = New-Object System.Text.UTF8Encoding($false)
$files = @()
$files += Get-ChildItem -Recurse -Path core\src\main,core\src\test,testkit\src\main,testkit\src\test -Filter *.kt
foreach ($f in $files) {
  $t = [System.IO.File]::ReadAllText($f.FullName)
  $o = $t
  $t = $t -replace 'Widget\.\(\) -> Unit', 'ChildSlot.() -> Unit'
  $t = $t -replace '(?m)^(\s*(?:private |internal |public )?fun )Widget\.', '$1ChildSlot.'
  if ($t -ne $o) { [System.IO.File]::WriteAllText($f.FullName, $t, $enc); Write-Output "entry/helper: $($f.FullName.Replace($PWD.Path + '\',''))" }
}
```
Expected: 约 **40** 个文件(17 处入口/参数 + 28 个测试 helper)。

- [ ] **Step 7: 手工 —— `Widget.kt` 删除 `buildChild` 与 `bind`,只留注解类与 `Widget`**

`core/src/main/kotlin/com/muedsa/snapshot/widget/Widget.kt` 的最终形态:

```kotlin
package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBox

/**
 * 标记 snapshot 的 Widget DSL 作用域。
 *
 * `@DslMarker` 会遮蔽同属一个 DSL 的外层隐式接收者:最近的接收者胜出;若某次调用只能
 * 绑定到被遮蔽的外层接收者,则报编译错误。因此 `Stack { Row { Positioned(…) } }` 这类
 * "代码嵌套结构与实际挂载父节点不一致"的写法不再静默通过。
 */
@DslMarker
annotation class SnapshotWidgetDsl

@SnapshotWidgetDsl
abstract class Widget {
    var parent: Widget? = null
        internal set

    abstract fun createRenderBox(): RenderBox
}
```

- [ ] **Step 8: 手工 —— `Container.composeWidget()` 的 10 处 `.bind(...)`**

实测 `composeWidget()` 里共有 **10** 处 `.bind(`:9 处 `.bind(current)` + 1 处 `.bind(ConstrainedBox(...))`。改法:

- `X(...).bind(current)` → `X(...).apply { current?.let { attach(it) } }`
- 首分支的 `.bind(ConstrainedBox(constraints = BoxConstraints.expand(), parent = null))` → `.apply { attach(ConstrainedBox(constraints = BoxConstraints.expand())) }`

`current?.let { … }` 里读到的仍是**旧值**:Kotlin 先把右侧表达式(含 `apply` 块)求值完才赋值给 `current`,所以在 lambda 内读 `current` 是安全的。若担心可读性,也可以按该文件既有风格先取局部快照 `val snapshotCurrent = current`——两种都正确。

包装顺序(Align → Padding → ColoredBox → ClipPath → DecoratedBox(BACKGROUND) → DecoratedBox(FOREGROUND) → ConstrainedBox → Padding(margin) → Transform)**必须逐字保持不变**。

- [ ] **Step 9: 手工 —— `RichText` / `WidgetSpan` / `ProxyWidget.buildWidget` / `layoutWidget`**

`widget/text/RichText.kt` 的 `init`(删除手写 parent):
```kotlin
    init {
        WidgetSpan.extractFromInlineSpan(text).forEach { attach(it) }
    }
```

`widget/text/WidgetSpan.kt` 的 `extractFromInlineSpan`:
```kotlin
                widgets.add(
                    WidgetSpanParentDataWidget(span = span).apply { attach(span.child) }
                )
```
同时把 `TextSpan.WidgetSpan` 的 `crossinline content: Widget.() -> Unit` 改成 `crossinline content: ChildSlot.() -> Unit`(Step 6 脚本已改参数类型,这里确认)。

`widget/ProxyWidget.kt` 的伴生对象:
```kotlin
    companion object {
        /** 用一段 DSL 构造一棵游离的 Widget 树,返回其根节点。 */
        @PublishedApi
        internal fun buildWidget(content: ChildSlot.() -> Unit): Widget {
            val proxy = ProxyWidget()
            proxy.content()
            val widget = proxy.widget
            checkNotNull(widget) { "buildWidget produced an empty widget tree" }
            return widget
        }
    }
```

`core/src/main/kotlin/com/muedsa/snapshot/Snapshot.kt` 的 `layoutWidget` 改为复用它(消除重复实现):
```kotlin
inline fun layoutWidget(
    content: ChildSlot.() -> Unit,
): RenderBox {
    val rootRenderBox = ProxyWidget.buildWidget(content).createRenderBox()
    rootRenderBox.layout(constraints = BoxConstraints())
    return rootRenderBox
}
```

- [ ] **Step 10: 手工 —— `text/RichText.kt` 与 `text/ImageEmojiSpan.kt` 删除不再存在的 `buildChild` import**

- `core/src/main/kotlin/com/muedsa/snapshot/widget/text/RichText.kt`:删除 `import com.muedsa.snapshot.widget.buildChild`
- `core/src/main/kotlin/com/muedsa/snapshot/widget/text/ImageEmojiSpan.kt`:删除 `import com.muedsa.snapshot.widget.buildChild`,并把 `WidgetSpan { … }` 体内的 `buildChild(widget = ImageEmoji(...), content = { })` 手工改为 `attach(ImageEmoji(...))`

- [ ] **Step 11: 手工 —— parser 两处**

`parser/src/main/kotlin/com/muedsa/snapshot/parser/Element.kt`:删除顶部 `import com.muedsa.snapshot.widget.bind`(它从未被使用;不删会编译失败)。

`parser/src/main/kotlin/com/muedsa/snapshot/parser/widget/WidgetParser.kt` 的 `createWidgetForChildElement`:
```kotlin
        fun createWidgetForChildElement(widget: Widget, children: List<Element>) {
            if (children.isEmpty()) return
            val slot = widget as? ChildSlot
                ?: error("${widget::class.simpleName} has no child slot but got ${children.size} child element(s)")
            children.forEach { slot.attach(it.createWidget()) }
        }
```
补 import `com.muedsa.snapshot.widget.ChildSlot`。解析期的子节点数量校验已由 `Element.appendChild` 依 `containerMode` 完成,这里不重复校验。

- [ ] **Step 12: 手工 —— 调整 `ChildSlotAttachTest`**

批次 1 用例 `build_child_on_widget_without_slot_reports_class_name` 测的是 `buildChild` 的运行时兜底,而本批**删除了 `buildChild`**,该场景已变成编译错误(由负例③覆盖)。因此:

- 删除该测试方法
- 删除文件末尾的 `private class LeafStub : Widget() { … }`
- 删除随之不再使用的 3 个 import:`BoxConstraints`、`RenderBox`、`RenderConstrainedBox`

其余 7 条用例**保持不变**(它们直接调用 `ChildSlot.attach`,不依赖 `buildChild`)。

- [ ] **Step 13: 编译并逐个修复**

Run:
```powershell
.\gradlew.bat test --console=plain
```
Expected: 首轮大概率**编译失败**(本批是一次性原子重构)。逐个读 error 修复,常见几类:

| error | 原因 | 修法 |
|---|---|---|
| `Inline function 'fun ChildSlot.X(…): Unit' cannot be recursive` / `Argument type mismatch: actual type is 'Unit', but 'Widget' was expected` | **DSL 函数名与 Widget 类名同名**,去掉 `parent = this` 后 `X(…)` 同时匹配构造函数与函数自身,重载解析选中了后者(实测**显式类型局部变量也不能消歧**) | 给该处构造函数加**包限定**:`com.muedsa.snapshot.widget.X(…)`。这是本批最大的一处计划外发现,详见 spec 做法决策 15 |
| `Unresolved reference: buildChild` | 还有函数体没用脚本 C 改写(含多行/异形写法) | 手工按 `attach(X(...).apply(content))` 改写 |
| `Unresolved reference: bind` | `Container.composeWidget` 或 parser 漏改 | 按 Step 8/11 改 |
| `No value passed for parameter 'parent'` | 某个类的 `parent` 参数没删干净,或调用点还在传 | 删净参数与实参 |
| `Type mismatch: inferred type is Widget but ChildSlot was expected` | 入口签名漏改,或 helper 接收者漏改 | 按 Step 6 补 |
| `Cannot access 'widget'/'child': it is protected` | 某处直接写了子槽位 | 改为走 `attach` |
| `Public-API inline function cannot access non-public-API` | `buildWidget` 漏了 `@PublishedApi` | 按 Step 9 补 |

**修完必须再跑一遍**,直到 `BUILD SUCCESSFUL`。不要靠放宽可见性或加 `@Suppress` 绕过。

- [ ] **Step 14: 全量测试**

Run:
```powershell
.\gradlew.bat test --console=plain
```
Expected: `BUILD SUCCESSFUL`,**216 例执行、0 失败、0 错误**(与批次 1 基线同量——本批是纯类型化重构,不改行为)。若某例失败,先判断是不是 `attach` 顺序/重复挂载校验暴露了既有写法的问题,再决定归属。

- [ ] **Step 15: 确认工作树与提交**

```powershell
git status --porcelain
git add -A
git commit -S -m "refactor(core): Widget DSL 接收者类型化为 ChildSlot, 删除 buildChild/bind 与 parent 构造参数"
git log -1 --pretty='%h %G? %s'
```
Expected: 首字段为 `G`。

---

### Task 2: 文档同步 + 编译期验证 + 出口验收

**Files:**
- Modify: `docs/usage/README.md`(§4.1)
- Temporary(验证后删除,**不入库**):`core/src/test/kotlin/com/muedsa/snapshot/widget/_NegativeScratch.kt`

**Interfaces:**
- Consumes: Task 1 的全部产物。
- Produces: 供用户手动创建 PR 的标题与描述素材。

- [ ] **Step 1: 同步 `docs/usage/README.md` §4.1**

在"三类父节点"表格(现约第 244-251 行)之后补一段:

```markdown
DSL 函数的接收者是 `ChildSlot`(`ProxyWidget` / `SingleChildWidget` / `MultiChildWidget` 都实现了它)。因此**在没有子槽位的 Widget 上挂子节点是编译错误**,而不是运行时异常;写树时把节点挂到了错误的父节点上(例如 `Stack { Row { Positioned(…) } }`——`Positioned` 的接收者是 `Stack`)同样是编译错误。
```

同时把表格里"（直接继承 `Widget`）| 0 个"那一行的说明补一句:这些叶子 Widget **不是** `ChildSlot`。

- [ ] **Step 2: 负例清单验证(③ 本批新增生效;①② 需仍然生效)**

创建 `core/src/test/kotlin/com/muedsa/snapshot/widget/_NegativeScratch.kt`:

```kotlin
package com.muedsa.snapshot.widget

import com.muedsa.geometry.EdgeInsets
import com.muedsa.snapshot.layoutWidget

// 负例①:Positioned 的接收者是 Stack,而最近接收者是 Row
fun negative1() = layoutWidget { Stack { Row { Positioned(left = 0f) { } } } }

// 负例②:Expanded 的接收者是 Flex,而最近接收者是 Padding
fun negative2() = layoutWidget { Row { Padding(padding = EdgeInsets.all(8f)) { Expanded { } } } }

// 负例③(本批新增):helper 用 Widget 接收者,里面写 Widget 树 —— Widget 不是 ChildSlot
fun Widget.negative3() {
    Padding(padding = EdgeInsets.all(8f)) { }
}
```

Run:
```powershell
.\gradlew.bat :core:compileTestKotlin --console=plain
```
Expected: `BUILD FAILED`,三条负例各对应至少一条 `error:`(文案以实际输出为准,记录原文)。`Widget` 未实现 `ChildSlot`,因此 ③ 报的应是"接收者类型不匹配/无法解析"一类错误。

- [ ] **Step 3: 正向清单验证**

把 scratch 文件内容替换为**只有正向清单**,Run:
```powershell
.\gradlew.bat :core:compileTestKotlin --console=plain
```
Expected: `BUILD SUCCESSFUL`,零 `error`:

```kotlin
package com.muedsa.snapshot.widget

import com.muedsa.geometry.EdgeInsets
import com.muedsa.snapshot.layoutWidget

fun positive1() = layoutWidget { Row { Column { Expanded { Container(width = 1f, height = 1f) } } } }

fun positive2() = layoutWidget { Stack { Positioned(left = 0f) { Container(width = 1f, height = 1f) } } }

fun positive3() = layoutWidget {
    Container(width = 10f, height = 10f) {
        Row { Container(width = 1f, height = 1f) }
    }
}

// helper 改成 ChildSlot 后仍可编译,并且可以在里面继续挂节点
fun ChildSlot.positive4() {
    Padding(padding = EdgeInsets.all(4f)) {
        Container(width = 1f, height = 1f)
    }
}
```

- [ ] **Step 4: 删除 scratch 并全量回归**

Run:
```powershell
Remove-Item core\src\test\kotlin\com\muedsa\snapshot\widget\_NegativeScratch.kt
.\gradlew.bat test --console=plain
git status --porcelain
```
Expected: `BUILD SUCCESSFUL`;`git status` 只列出 `docs/usage/README.md` 被修改。

- [ ] **Step 5: 提交文档**

```powershell
git add docs/usage/README.md
git commit -S -m "docs: 使用手册补 ChildSlot 类型约束说明"
git log -1 --pretty='%h %G? %s'
```
Expected: 首字段为 `G`。

- [ ] **Step 6: 出口验收与交付说明**

Run:
```powershell
git status --porcelain
git log --pretty='%h %G? %s' main..HEAD
git diff --stat main..HEAD
```
Expected: 工作树干净;`%G?` 全部为 `G`。

然后用中文写一份交付说明(供用户创建 PR),必须包含:

1. **标题**:`refactor(core): Widget DSL 接收者类型化为 ChildSlot(挂错父节点/无槽位挂载变编译错误)`
2. **背景**:spec 的 P1/P2/P4 三条,以及批次 1 已经做了什么(PR #116)
3. **改动**:Task 1/2 的提交 hash;30 个 DSL 函数 + 35 个类 + 17 处入口 + 28 个测试 helper 的规模;`buildChild` / `bind` 已删除
4. **验证**:负例①②③ 的编译器 error 原文;正向清单零 error;`./gradlew test` 结果与用例数
5. **行为变更提示**:DSL 接收者收窄意味着**下游用 `fun Widget.xxx()` 写的 helper 需要改成 `fun ChildSlot.xxx()`**;`Widget.buildChild` / `Widget.bind` / `ProxyWidget.buildWidget`(收为 `@PublishedApi internal`)属于公共面变化;所有 Widget 类的 `parent` 构造参数被删除
6. **非目标**:未动 parentData 行为、未动 `appendChild` 的同步与 O(n²) contains、未删 `Widget.parent` 字段——批次 3/4

- [ ] **Step 7: 报告并交回用户**

不要 push、不要创建 PR。把 Step 6 的交付说明完整贴给用户。
