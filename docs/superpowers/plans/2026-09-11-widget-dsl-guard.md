# Widget DSL 批次 1(ChildSlot 原语 + @DslMarker)Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在不改动任何调用点签名的前提下,为 snapshot 的 Widget DSL 补上编译期防线(`@DslMarker`)与唯一的挂载原语(`ChildSlot.attach`),并让 `Widget.parent` 不再做 O(深度) 遍历。

**Architecture:** 新增 `interface ChildSlot { fun attach(child: Widget) }`,由 `ProxyWidget` / `SingleChildWidget` / `MultiChildWidget` 实现;`attach` 收敛"放入子槽位 + 设置 `child.parent` + 重复挂载校验"三件事,成为全项目唯一挂载入口。`Widget.buildChild` 与 `Widget.bind` 退化为 `attach` 的薄封装,**签名与行为保持不变**,因此本批零调用点改动。同时给 `Widget` 加 `@DslMarker` 标注,使"外层接收者被静默捕获"(如 `Stack { Row { Positioned(...) } }`)从"静默生成错误的树"变成编译错误。

**Tech Stack:** Kotlin/JVM 2.4.20、Gradle Wrapper 9.7.1、JDK 17、kotlin.test(JUnit Platform)。

**Spec:** `docs/superpowers/specs/2026-09-11-widget-dsl-childslot-design.md`

## Global Constraints

- 仓库根 `D:\mine\workspace\snapshot`;本计划**只**在分支 `refactor/widget-dsl-guard`(base = `main` @ `55fe451`)上执行。该分支开工前已含若干文档提交(spec、spec 的消息风格修订、本实现计划自身);本计划再追加 **3 个代码提交**。验收时以"这 3 个代码提交都在、且分支上所有提交均已签名"为准,不要断言提交总数。
- **不要** `git push`、**不要**创建 PR——由用户手动创建与合并。
- 所有命令在 PowerShell 下执行,用 `.\gradlew.bat`(**不要**用 `./gradlew`)。
- 每个提交必须 GPG 签名:`git commit -S -m "…"`;提交后跑 `git log -1 --pretty='%h %G? %s'`,首字段必须是 `G`。若报 `No passphrase given`,原样重试一次。
- 提交信息:Conventional Commits + **中文**标题(如 `fix(core): …`)。
- **异常/断言消息一律英文**(对齐 `core/src/main` 既有 34 处消息:`"layout size is empty"`、`"only TextSpan can be used in RichText widget"`、`"MultiChildWidget cant append duplicate child"`)。
- **新增 KDoc 用中文**(对齐 `Snapshot.kt` / `TestKit.kt` / `LayoutTree.kt` 等较新代码)。
- **不引入任何新依赖、不新增 Gradle 模块、不改 `build.gradle.kts`。**
- 本批**只做** `ChildSlot` + `@DslMarker` + `buildChild`/`bind`/`parent` 收敛。**不要**改 DSL 函数签名、**不要**动 `parent` 构造参数、**不要**改 `Snapshot*` / `layoutWidget` / `rootLayout` 的入口签名(那些是批次 2)、**不要**动 `parentData` 相关行为(批次 3)。
- 若 Gradle 报缓存 `AccessDenied`,追加 `--no-build-cache` 重跑。
- 基线(开工前已实测):`.\gradlew.bat test --console=plain` → `BUILD SUCCESSFUL`,73 个测试文件 / 228 个 `@Test`。

---

### Task 1: 加 `@SnapshotWidgetDsl` 阻断外层接收者静默捕获

**Files:**
- Modify: `core/src/main/kotlin/com/muedsa/snapshot/widget/Widget.kt`
- Temporary(验证后删除,**不入库**):`core/src/test/kotlin/com/muedsa/snapshot/widget/_NegativeScratch.kt`

**Interfaces:**
- Consumes: 无。
- Produces: `annotation class SnapshotWidgetDsl`(package `com.muedsa.snapshot.widget`,带 `@DslMarker` 元注解),标注在 `Widget` 上。批次 2 把 DSL 接收者改成 `ChildSlot.` 时依赖它已经存在。

- [ ] **Step 1: 建立基线**

Run:
```powershell
.\gradlew.bat test --console=plain
```
Expected: `BUILD SUCCESSFUL`,耗时约 1 分钟。若失败,先停下来报告,不要继续。

- [ ] **Step 2: 写编译期负例/正向清单的 scratch 文件**

Create `core/src/test/kotlin/com/muedsa/snapshot/widget/_NegativeScratch.kt`(**该文件只是编译器探针,验证完必须删除**):

```kotlin
package com.muedsa.snapshot.widget

import com.muedsa.geometry.EdgeInsets
import com.muedsa.snapshot.layoutWidget
import com.muedsa.snapshot.paint.text.TextSpan
import com.muedsa.snapshot.paint.text.TextStyle
import com.muedsa.snapshot.widget.text.ImageEmojiSpan
import com.muedsa.snapshot.widget.text.RichText
import org.jetbrains.skia.Image

// ===== 负例:加 @DslMarker 后,下面两处必须各出现 error =====

// ① Positioned 的接收者是 Stack,但此处的最近接收者是 Row(外层接收者被静默捕获)
fun negative1() = layoutWidget { Stack { Row { Positioned(left = 0f) { } } } }

// ② Expanded 的接收者是 Flex,但此处的最近接收者是 Padding
fun negative2() = layoutWidget { Row { Padding(padding = EdgeInsets.all(8f)) { Expanded { } } } }

// ===== 正向:任何一处出现 error 都说明 @DslMarker 过度限制 =====

fun positive1() = layoutWidget { Row { Column { Expanded { Container(width = 1f, height = 1f) } } } }

fun positive2() = layoutWidget { Stack { Positioned(left = 0f) { Container(width = 1f, height = 1f) } } }

fun positive3() = layoutWidget {
    Container(width = 10f, height = 10f) {
        Row { Container(width = 1f, height = 1f) }
    }
}

fun positive4(image: Image) = layoutWidget {
    RichText {
        TextSpan(style = TextStyle(fontSize = 20f)) {
            ImageEmojiSpan(provider = { image }, width = 20f, height = 20f)
        }
    }
}
```

- [ ] **Step 3: 运行,确认负例当前"能编译通过"(红)**

Run:
```powershell
.\gradlew.bat :core:compileTestKotlin --console=plain
```
Expected: `BUILD SUCCESSFUL`,输出中**没有任何 `error:`**。这正是要修的 bug 形态:两个嵌套错误的写法被静默接受。

- [ ] **Step 4: 在 `Widget.kt` 加注解类并标注 `Widget`**

在 `core/src/main/kotlin/com/muedsa/snapshot/widget/Widget.kt` 的 `import` 之后、`buildChild` 之前插入:

```kotlin
/**
 * 标记 snapshot 的 Widget DSL 作用域。
 *
 * `@DslMarker` 会遮蔽同属一个 DSL 的外层隐式接收者:最近的接收者胜出;若某次调用只能
 * 绑定到被遮蔽的外层接收者,则报编译错误。因此 `Stack { Row { Positioned(…) } }` 这类
 * "代码嵌套结构与实际挂载父节点不一致"的写法不再静默通过。
 */
@DslMarker
annotation class SnapshotWidgetDsl
```

并给 `Widget` 加标注(只加这一行,构造函数与 `parent` 本步不动):

```kotlin
@SnapshotWidgetDsl
abstract class Widget(
    parent: Widget? = null,
) {
```

- [ ] **Step 5: 运行,确认负例报错、正向不报错**

Run:
```powershell
.\gradlew.bat :core:compileTestKotlin --console=plain
```
Expected: `BUILD FAILED`。断言:

1. `negative1` 与 `negative2` 所在行**各出现至少一条 `error:`**;
2. `positive1` … `positive4` 所在行**不得出现任何 `error:`**。

把这两条 error 的**编译器原文**抄下来,任务结束时要写进交付说明(PR 描述素材)。报错文案以实际输出为准,不要照抄本计划的猜测。

- [ ] **Step 6: 若正向清单里出现 error,停下来报告**

不要为了让它编译通过而放宽 DSL 或退回注解。这属于 spec「风险 1」:说明现有写法里确实有依赖外层接收者的代码,需要逐个核对是"写法本来就错"还是"必须放宽"。停下来把完整 error 贴出来。

- [ ] **Step 7: 删除 scratch 文件并确认恢复绿**

Run:
```powershell
Remove-Item core\src\test\kotlin\com\muedsa\snapshot\widget\_NegativeScratch.kt
.\gradlew.bat :core:compileTestKotlin --console=plain
```
Expected: `BUILD SUCCESSFUL`。

- [ ] **Step 8: 全量回归**

Run:
```powershell
.\gradlew.bat test --console=plain
```
Expected: `BUILD SUCCESSFUL`。若现有代码出现编译错误,按 Step 6 的方式停下来报告。

- [ ] **Step 9: 确认工作树只改了这一个文件**

Run:
```powershell
git status --porcelain
```
Expected: 只有 ` M core/src/main/kotlin/com/muedsa/snapshot/widget/Widget.kt` 一行(scratch 文件必须已不存在)。

- [ ] **Step 10: 提交**

```powershell
git add core/src/main/kotlin/com/muedsa/snapshot/widget/Widget.kt
git commit -S -m "fix(core): 加 @SnapshotWidgetDsl 阻断 DSL 外层接收者静默捕获"
git log -1 --pretty='%h %G? %s'
```
Expected: 输出首字段为 `G`。

---

### Task 2: 新增 `ChildSlot` 与三个基类的 `attach`(TDD)

**Files:**
- Create: `core/src/main/kotlin/com/muedsa/snapshot/widget/ChildSlot.kt`
- Modify: `core/src/main/kotlin/com/muedsa/snapshot/widget/SingleChildWidget.kt`
- Modify: `core/src/main/kotlin/com/muedsa/snapshot/widget/MultiChildWidget.kt`
- Modify: `core/src/main/kotlin/com/muedsa/snapshot/widget/ProxyWidget.kt`
- Test: `core/src/test/kotlin/com/muedsa/snapshot/widget/ChildSlotAttachTest.kt`

**Interfaces:**
- Consumes: Task 1 的 `SnapshotWidgetDsl`(本任务不直接引用)。
- Produces:
  - `interface ChildSlot { fun attach(child: Widget) }`
  - `ProxyWidget`、`SingleChildWidget`、`MultiChildWidget` 均变为 `: Widget(...), ChildSlot`
  - 三个 `attach` 语义一致:放入子槽位、设置 `child.parent = this`、重复挂载抛 `IllegalStateException`
  - 本任务**暂不**收窄 `child` / `widget` 的 setter——它们仍在 Task 3 收窄,因为 `Widget.kt` 里的 `buildChild`/`bind` 此刻还在直接写这两个属性

- [ ] **Step 1: 写失败的测试**

> **执行时踩到的坑(2026-09-11 修正):** `Padding` 的构造函数是 `class Padding(var padding: EdgeInsets, parent: Widget?)`——**`parent` 没有默认值**(`Align` / `SizedBox` / `Row` / `ProxyWidget` 都有 `= null`,只有 `Padding` 这类少数没有)。所以直接构造 `Padding` 时必须显式传 `parent = null`,否则报 `No value passed for parameter 'parent'`。本步骤下方的代码已修正。

Create `core/src/test/kotlin/com/muedsa/snapshot/widget/ChildSlotAttachTest.kt`:

```kotlin
package com.muedsa.snapshot.widget

import com.muedsa.geometry.EdgeInsets
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ChildSlotAttachTest {

    @Test
    fun single_child_widget_attaches_child_and_sets_parent() {
        val parent = Padding(padding = EdgeInsets.all(1f), parent = null)
        val child = SizedBox(width = 1f, height = 1f)

        parent.attach(child)

        assertSame(child, parent.child)
        assertSame(parent, child.parent)
    }

    @Test
    fun single_child_widget_rejects_second_attach() {
        val parent = Padding(padding = EdgeInsets.all(1f), parent = null)
        val first = SizedBox(width = 1f, height = 1f)
        val second = SizedBox(width = 2f, height = 2f)

        parent.attach(first)
        val error = assertFailsWith<IllegalStateException> { parent.attach(second) }

        assertTrue(
            error.message!!.contains("Padding"),
            "message should contain parent class name, but was: ${error.message}"
        )
        assertTrue(
            error.message!!.contains("SizedBox"),
            "message should contain child class name, but was: ${error.message}"
        )
        assertSame(first, parent.child, "the first child must not be silently replaced")
    }

    @Test
    fun proxy_widget_attaches_widget_and_sets_parent() {
        val parent = ProxyWidget()
        val child = SizedBox(width = 1f, height = 1f)

        parent.attach(child)

        assertSame(child, parent.widget)
        assertSame(parent, child.parent)
    }

    @Test
    fun proxy_widget_rejects_second_attach() {
        val parent = ProxyWidget()
        val first = SizedBox(width = 1f, height = 1f)
        val second = SizedBox(width = 2f, height = 2f)

        parent.attach(first)
        val error = assertFailsWith<IllegalStateException> { parent.attach(second) }

        assertTrue(
            error.message!!.contains("ProxyWidget"),
            "message should contain parent class name, but was: ${error.message}"
        )
        assertSame(first, parent.widget, "the first widget must not be silently replaced")
    }

    @Test
    fun multi_child_widget_appends_two_distinct_children() {
        val parent = Row()
        val first = SizedBox(width = 1f, height = 1f)
        val second = SizedBox(width = 2f, height = 2f)

        parent.attach(first)
        parent.attach(second)

        assertEquals(listOf(first, second), parent.children)
        assertSame(parent, first.parent)
        assertSame(parent, second.parent)
    }

    @Test
    fun multi_child_widget_rejects_duplicate_attach() {
        val parent = Row()
        val child = SizedBox(width = 1f, height = 1f)

        parent.attach(child)

        assertFailsWith<IllegalStateException> { parent.attach(child) }
        assertEquals(listOf(child), parent.children)
    }

    @Test
    fun attach_allows_re_attach_to_another_parent() {
        // Container.composeWidget() 依赖此行为:把已挂载的子节点重新挂到新建的包装节点上。
        val first = Padding(padding = EdgeInsets.all(1f), parent = null)
        val second = Padding(padding = EdgeInsets.all(2f), parent = null)
        val child = SizedBox(width = 1f, height = 1f)

        first.attach(child)
        second.attach(child)

        assertSame(second, child.parent)
        assertSame(child, second.child)
    }
}
```

- [ ] **Step 2: 运行,确认编译失败**

Run:
```powershell
.\gradlew.bat :core:compileTestKotlin --console=plain
```
Expected: `BUILD FAILED`,error 形如 `Unresolved reference: attach`(以及 `attach` 在 `Padding`/`ProxyWidget`/`Row` 上不存在)。

- [ ] **Step 3: 新增 `ChildSlot`**

Create `core/src/main/kotlin/com/muedsa/snapshot/widget/ChildSlot.kt`:

```kotlin
package com.muedsa.snapshot.widget

/**
 * 拥有子节点槽位的 [Widget]。
 *
 * DSL 函数([Padding]、[Row]、[Stack] 等)以此为接收者,因此"在不具备子槽位的 Widget 上
 * 挂子节点"会成为编译错误,而不是运行时异常。
 *
 * [attach] 是全项目唯一的挂载入口:它把 [child] 放进本节点的子槽位,并把 [Widget.parent]
 * 指回本节点。它允许重新挂载(不校验 `child.parent == null`),因为 [Container] 的
 * `composeWidget()` 会把已挂载的子节点重新挂到新建的包装节点上。
 */
interface ChildSlot {

    /** 把 [child] 挂到本节点的子槽位,并设置 [Widget.parent]。重复挂载抛 [IllegalStateException]。 */
    fun attach(child: Widget)
}
```

- [ ] **Step 4: 让 `SingleChildWidget` 实现 `ChildSlot`**

把 `core/src/main/kotlin/com/muedsa/snapshot/widget/SingleChildWidget.kt` 改为:

```kotlin
package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderSingleChildBox

abstract class SingleChildWidget(
    parent: Widget?,
) : Widget(parent = parent), ChildSlot {
    var child: Widget? = null

    override fun attach(child: Widget) {
        check(this.child == null) {
            "${this::class.simpleName} already has a child, can not attach ${child::class.simpleName}"
        }
        this.child = child
        child.parent = this
    }

    protected abstract fun createRenderBox(child: Widget?): RenderBox

    final override fun createRenderBox(): RenderBox {
        val child = this.child
        val renderBox = createRenderBox(child)
        if (child is ParentDataWidget && renderBox is RenderSingleChildBox) {
            child.applyParentData(renderBox.child!!)
        }
        return renderBox
    }
}
```

- [ ] **Step 5: 让 `MultiChildWidget` 实现 `ChildSlot`**

在 `core/src/main/kotlin/com/muedsa/snapshot/widget/MultiChildWidget.kt` 中,把类声明改为 `: Widget(parent = parent), ChildSlot`,并在 `appendChildren` 之后插入:

```kotlin
    override fun attach(child: Widget) {
        appendChild(child)
        child.parent = this
    }
```

其余部分(`_children`、`children`、`appendChild`、`appendChildren`、`createRenderBox`)保持原样——包括 `appendChild` 里既有的 `check(!_children.contains(child))` 与 `@Synchronized`,**本批不动它们**。

- [ ] **Step 6: 让 `ProxyWidget` 实现 `ChildSlot`**

把 `core/src/main/kotlin/com/muedsa/snapshot/widget/ProxyWidget.kt` 改为:

```kotlin
package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBox

open class ProxyWidget(parent: Widget? = null) : Widget(parent = parent), ChildSlot {

    var widget: Widget? = null

    override fun attach(child: Widget) {
        check(widget == null) {
            "${this::class.simpleName} already has a widget, can not attach ${child::class.simpleName}"
        }
        this.widget = child
        child.parent = this
    }

    final override fun createRenderBox(): RenderBox {
        val widget = this.widget
        assert(widget != null) { "proxy null widget ??" }
        return widget!!.createRenderBox()
    }

    companion object {
        fun buildWidget(
            content: Widget.() -> Unit
        ): Widget {
            val widget = ProxyWidget().apply(content).widget
            checkNotNull(widget)
            return widget
        }
    }
}
```

- [ ] **Step 7: 运行,确认测试通过**

Run:
```powershell
.\gradlew.bat :core:test --tests 'com.muedsa.snapshot.widget.ChildSlotAttachTest' --console=plain
```
Expected: `BUILD SUCCESSFUL`,7 个用例全过。

- [ ] **Step 8: 全量回归**

Run:
```powershell
.\gradlew.bat test --console=plain
```
Expected: `BUILD SUCCESSFUL`。此刻 `buildChild`/`bind` 仍在直接写 `child`/`widget`,行为与改动前一致,因此既有 228 例必须全绿。

- [ ] **Step 9: 提交**

```powershell
git add core/src/main/kotlin/com/muedsa/snapshot/widget/ChildSlot.kt `
        core/src/main/kotlin/com/muedsa/snapshot/widget/SingleChildWidget.kt `
        core/src/main/kotlin/com/muedsa/snapshot/widget/MultiChildWidget.kt `
        core/src/main/kotlin/com/muedsa/snapshot/widget/ProxyWidget.kt `
        core/src/test/kotlin/com/muedsa/snapshot/widget/ChildSlotAttachTest.kt
git commit -S -m "feat(core): 新增 ChildSlot 与唯一挂载原语 attach(含重复挂载校验)"
git log -1 --pretty='%h %G? %s'
```
Expected: 输出首字段为 `G`。

---

### Task 3: 把 `buildChild` / `bind` / `parent` 收敛到 `attach`

**Files:**
- Modify: `core/src/main/kotlin/com/muedsa/snapshot/widget/Widget.kt`
- Modify: `core/src/main/kotlin/com/muedsa/snapshot/widget/SingleChildWidget.kt`
- Modify: `core/src/main/kotlin/com/muedsa/snapshot/widget/ProxyWidget.kt`
- Test: `core/src/test/kotlin/com/muedsa/snapshot/widget/ChildSlotAttachTest.kt`(新增一条用例)

**Interfaces:**
- Consumes: Task 2 的 `ChildSlot.attach`。
- Produces: `Widget.buildChild(widget: T, content: T.() -> Unit)` 与 `Widget.bind(child: Widget?): Widget` **签名不变**、行为不变,但内部改为委托 `attach`;`Widget.parent` 变为对外只读(`internal set`)且不再做祖先遍历。

- [ ] **Step 1: 写失败的测试**

在 `core/src/test/kotlin/com/muedsa/snapshot/widget/ChildSlotAttachTest.kt` 里追加一条用例,并在类的最后(闭括号之前)加上无槽位的桩 Widget:

```kotlin
    @Test
    fun build_child_on_widget_without_slot_reports_class_name() {
        val leaf = LeafStub()

        val error = assertFailsWith<IllegalStateException> {
            leaf.buildChild(SizedBox(width = 1f, height = 1f)) { }
        }

        assertTrue(
            error.message!!.contains("LeafStub"),
            "message should contain the parent class name, but was: ${error.message}"
        )
    }

    /** 没有子槽位的叶子 Widget,用于验证"父节点没有槽位"的报错路径。 */
    private class LeafStub : Widget() {
        override fun createRenderBox(): RenderBox =
            RenderConstrainedBox(additionalConstraints = BoxConstraints())
    }
```

同时补两个 import:

```kotlin
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderConstrainedBox
```

- [ ] **Step 2: 运行,确认测试失败**

Run:
```powershell
.\gradlew.bat :core:test --tests 'com.muedsa.snapshot.widget.ChildSlotAttachTest' --console=plain
```
Expected: `build_child_on_widget_without_slot_reports_class_name` FAIL。当前 `buildChild` 的 `else` 分支抛的是**无消息**的 `IllegalStateException()`,因此断言 `message` 时抛 `NullPointerException`(或断言失败)。其余 7 条用例仍应 PASS。

- [ ] **Step 3: 把 `Widget.kt` 的两个入口改成 `attach` 薄封装,并瘦身 `parent`**

把 `core/src/main/kotlin/com/muedsa/snapshot/widget/Widget.kt` 整体改为(注意:`SnapshotWidgetDsl` 注解类在 Task 1 已存在,此处保持原样,只贴出最终形态供核对):

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

inline fun <T : Widget> Widget.buildChild(
    widget: T,
    content: T.() -> Unit,
) {
    val slot = this as? ChildSlot
        ?: throw IllegalStateException(
            "${this::class.simpleName} has no child slot, can not attach ${widget::class.simpleName}"
        )
    slot.attach(widget)
    widget.content()
}

fun Widget.bind(
    child: Widget?,
): Widget {
    child?.let {
        val slot = this as? ChildSlot
            ?: throw IllegalStateException(
                "${this::class.simpleName} has no child slot, can not attach ${it::class.simpleName}"
            )
        slot.attach(it)
    }
    return this
}

@SnapshotWidgetDsl
abstract class Widget(
    parent: Widget? = null,
) {
    var parent: Widget? = null
        internal set

    abstract fun createRenderBox(): RenderBox

    init {
        this.parent = parent
    }
}
```

相对改动前的**唯一语义差异**:`parent` 的 setter 不再有 `while (temp != null) { … }` 的祖先遍历,`assert(temp != this) { "widget tree circulate" }` 随之删除;可见性收窄为 `internal`(对外只读)。旧 setter 里的遍历在断言关闭时也会全量执行,删掉它是本步的目的之一。

- [ ] **Step 4: 运行,确认新用例通过**

Run:
```powershell
.\gradlew.bat :core:test --tests 'com.muedsa.snapshot.widget.ChildSlotAttachTest' --console=plain
```
Expected: `BUILD SUCCESSFUL`,8 个用例全过。

- [ ] **Step 5: 收窄 `child` / `widget` 的 setter**

此刻 `Widget.kt` 已不再直接写这两个属性,可以收窄了:

- `SingleChildWidget.kt`:`var child: Widget? = null` → 追加 `protected set`
- `ProxyWidget.kt`:`var widget: Widget? = null` → 追加 `protected set`

即:

```kotlin
    var child: Widget? = null
        protected set
```

```kotlin
    var widget: Widget? = null
        protected set
```

这样"写入子槽位"在语言层面只可能发生在各自类的 `attach` 内(顶层函数无权写 `protected` 成员)。

- [ ] **Step 6: 运行,确认仍然编译且测试通过**

Run:
```powershell
.\gradlew.bat :core:test --tests 'com.muedsa.snapshot.widget.ChildSlotAttachTest' --console=plain
```
Expected: `BUILD SUCCESSFUL`。若报错说某处无法访问 `child`/`widget` 的 setter,说明还有调用点在直接写它——把它改为走 `attach`,不要回退 `protected set`。

- [ ] **Step 7: 全量回归**

Run:
```powershell
.\gradlew.bat test --console=plain
```
Expected: `BUILD SUCCESSFUL`。这是"`buildChild`/`bind` 委托 `attach` 后行为不变"的回归证据。

- [ ] **Step 8: 确认工作树**

Run:
```powershell
git status --porcelain
```
Expected: 只有 `Widget.kt`、`SingleChildWidget.kt`、`ProxyWidget.kt`、`ChildSlotAttachTest.kt` 四个文件被修改。

- [ ] **Step 9: 提交**

```powershell
git add core/src/main/kotlin/com/muedsa/snapshot/widget/Widget.kt `
        core/src/main/kotlin/com/muedsa/snapshot/widget/SingleChildWidget.kt `
        core/src/main/kotlin/com/muedsa/snapshot/widget/ProxyWidget.kt `
        core/src/test/kotlin/com/muedsa/snapshot/widget/ChildSlotAttachTest.kt
git commit -S -m "refactor(core): buildChild/bind 收敛到 attach; parent 只读且去掉祖先遍历"
git log -1 --pretty='%h %G? %s'
```
Expected: 输出首字段为 `G`。

---

### Task 4: 批次 1 出口验收与交付说明

**Files:** 无新改动(纯校验与汇总)。

**Interfaces:**
- Consumes: Task 1–3 的全部产物。
- Produces: 供用户手动创建 PR 的标题与描述素材。

- [ ] **Step 1: 全量测试**

Run:
```powershell
.\gradlew.bat test --console=plain
```
Expected: `BUILD SUCCESSFUL`。

- [ ] **Step 2: 确认工作树干净、提交数与签名**

Run:
```powershell
git status --porcelain
git log --oneline main..HEAD
git log --pretty='%h %G? %s' main..HEAD
```
Expected: 第一条命令**无输出**;`git log --oneline main..HEAD` 中能看到 Task 1/2/3 的 3 个代码提交(倒序为 Task 3、Task 2、Task 1)以及开工前已有的文档提交;`%G?` 一列**全部**为 `G`。不要断言提交总数(文档提交的数量会随计划修订变化)。

- [ ] **Step 3: 汇总交付说明**

用中文写一份交付说明(供用户创建 PR),必须包含:

1. **标题**:`fix(core): DSL 挂载原语收敛为 ChildSlot.attach 并加 @DslMarker 编译期防线`
2. **背景**:spec `docs/superpowers/specs/2026-09-11-widget-dsl-childslot-design.md` 的 P1–P4 四条隐患各一段;
3. **改动**:3 个 commit 的 hash 与本批文件清单;
4. **验证**:Task 1 Step 5 抄下的**两条编译器 error 原文**、正向清单 `positive1`–`positive4` 均无 error、`./gradlew test` 结果与用例数;
5. **行为变更提示**(必须逐条列出):
   - 单子节点父节点重复挂载:从"静默覆盖"变为抛 `IllegalStateException`;
   - `bind` 作用在无子槽位的 Widget 上:从"无消息异常"变为带父/子类名的异常;
   - `Widget.parent` 的 setter 由 public 收窄为 internal,且不再做祖先环检测(旧实现只在开启断言时才真正检测,且遍历本身总是执行)。
6. **非目标**:本批**未**改 DSL 接收者签名、未删 `parent` 构造参数、未动入口签名、未动 parentData 行为——这些是批次 2/3。

- [ ] **Step 4: 报告并交回用户**

不要 push、不要创建 PR。把 Step 3 的交付说明完整贴给用户,由用户决定分支推送与 PR 创建。
