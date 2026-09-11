# Widget 挂载路径硬化(批次 3)Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 把挂载路径上所有"静默失败 / 未检查强转 / 死循环"的隐患改成显式失败,并修掉 `RenderBox.parent` setter 的祖先遍历死循环缺陷。

**Architecture:** 批次 1/2 已经把 Widget 层的挂载收敛为 `ChildSlot.attach`,并把 `Widget.parent` 的祖先遍历去掉。本批做三件事:①`RenderBox.parent` 的同类遍历有个**真 bug**(`temp = value?.parent` 使 `temp` 不再前进,只要新父节点自己已有父节点就死循环),修掉并加回归测试;②挂载路径上的 `!!` 与静默跳过守卫改为带消息的显式失败;③去掉 `appendChild` 上误导性的 `@Synchronized`(这两个类整体都不是线程安全的,同步块只制造错觉)。

**Tech Stack:** Kotlin/JVM 2.4.20、Gradle Wrapper 9.7.1、JDK 17、kotlin.test(JUnit Platform)。

**Spec:** `docs/superpowers/specs/2026-09-11-widget-dsl-childslot-design.md`(批次 3 一节)

**前置:** 批次 1(PR #116)、批次 2(PR #117)均已合入 `main`;本分支 `refactor/attach-hygiene` 基于 `4a0943e`。

## Global Constraints

- 仓库根 `D:\mine\workspace\snapshot`;本计划**只**在分支 `refactor/attach-hygiene` 上执行。
- **不要** `git push`、**不要**创建 PR——由用户手动创建与合并。
- 所有命令在 PowerShell 下用 `.\gradlew.bat`;若 Gradle 报缓存 `AccessDenied`,追加 `--no-build-cache`。
- 每个提交 GPG 签名:`git commit -S -m "…"`;提交后 `git log -1 --pretty='%h %G? %s'` 首字段必须是 `G`。报 `No passphrase given` 就原样重试一次。
- 提交信息:Conventional Commits + **中文**标题。
- **异常/断言消息一律英文**(对齐 `core/src/main` 既有风格);新增 KDoc 用中文。
- **不引入任何新依赖、不新增 Gradle 模块。**
- 基线:`.\gradlew.bat test --console=plain` → `BUILD SUCCESSFUL`,**215 例执行、0 失败**。
- **本批不改任何行为语义**,除 Task 2 修掉的死循环。所有改动都是"把静默/未定义行为变成显式失败"。

## 范围决定(与 spec 批次 3 一节的差异,已与用户确认)

spec 批次 3 列了 6 项,逐项核实后**建议做 5 项、放弃 2 项、新增 1 项**:

| 项 | 决定 | 理由 |
|---|---|---|
| `renderBox.child!!` → 带消息检查 | **做** | 裸 NPE → 可诊断 |
| `MultiChildWidget` 的 `children.size` 静默跳过 → 显式报错 | **做** | 静默不应用 parentData = 布局静默错 |
| `children` 活视图契约 | **做** | `createRenderBox` 用拷贝、`forEachIndexed` 用活视图,自相矛盾 |
| 去掉 `@Synchronized` | **做** | 这两个类整体非线程安全,同步块只制造错觉 |
| `contains` 的 O(n²) 优化 | **放弃** | 实测规模下非问题(1000 子节点约 50 万次恒等比较,毫秒级);引入 IdentityHashMap 是净增复杂度 |
| `content` 加 `crossinline` | **放弃** | 需要改 30+ 处公共签名,且**禁止非局部 return 是对使用者可见的语义变更**,收益远小于代价 |
| `RenderBox.parent` setter 死循环 | **新增(本批最高价值)** | 真 bug,已被批次 1 漏掉;同类缺陷在 render 层仍在 |
| `RenderContainerBox.appendChild` 的 `parentData!! as ContainerBoxParentData` | **新增** | 与 `renderBox.child!!` 同类:裸 `!!` + 无检查强转 |

---

### Task 1: 挂载路径的显式失败(Widget 层 + Render 层)

**Files:**
- Modify: `core/src/main/kotlin/com/muedsa/snapshot/widget/SingleChildWidget.kt`
- Modify: `core/src/main/kotlin/com/muedsa/snapshot/widget/MultiChildWidget.kt`
- Modify: `core/src/main/kotlin/com/muedsa/snapshot/rendering/box/RenderContainerBox.kt`
- Test: `core/src/test/kotlin/com/muedsa/snapshot/widget/MultiChildWidgetGuardTest.kt`(新)

**Interfaces:**
- Consumes: `ChildSlot.attach`(批次 1/2 已有)。
- Produces: 挂载路径上的失败一律是带类名消息的 `IllegalStateException`;`MultiChildWidget.appendChild` / `appendChildren` 不再是 `@Synchronized`。

- [ ] **Step 1: 写失败的测试**

Create `core/src/test/kotlin/com/muedsa/snapshot/widget/MultiChildWidgetGuardTest.kt`:

```kotlin
package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderContainerBox
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MultiChildWidgetGuardTest {

    /** 一个"吞掉"所有子节点的 RenderContainerBox:用来构造 renderBox.children 与 widget.children 数量不一致的情形。 */
    private class SwallowingContainerBox : RenderContainerBox() {
        override fun performLayout() {
            size = BoxConstraints().constrain(com.muedsa.geometry.Size.ZERO)
        }
    }

    /** 有 1 个子 Widget,但产出的 RenderContainerBox 里 0 个 RenderBox。 */
    private class MismatchedWidget : MultiChildWidget() {
        override fun createRenderBox(children: List<Widget>): RenderBox = SwallowingContainerBox()
    }

    @Test
    fun render_box_child_count_mismatch_fails_loudly() {
        val widget = MismatchedWidget()
        widget.attach(SizedBox(width = 1f, height = 1f))

        val error = assertFailsWith<IllegalStateException> { widget.createRenderBox() }

        assertTrue(
            error.message!!.contains("MismatchedWidget"),
            "message should contain the widget class name, but was: ${error.message}"
        )
        assertTrue(
            error.message!!.contains("1"),
            "message should mention the widget child count, but was: ${error.message}"
        )
    }
}
```

> 若 `RenderContainerBox` 的抽象成员不止 `performLayout`(例如还要求 `paint`),按编译器提示补齐最小实现——`RenderContainerBox` 已实现 `paint`,`performLayout` 由 `RenderBox` 声明为抽象。以实际编译结果为准。

- [ ] **Step 2: 运行,确认失败**

Run:
```powershell
.\gradlew.bat :core:test --tests 'com.muedsa.snapshot.widget.MultiChildWidgetGuardTest' --console=plain
```
Expected: FAIL —— 当前实现是**静默跳过** `applyParentData`,不抛异常,因此 `assertFailsWith` 失败(报 "Expected an exception ... to be thrown")。

- [ ] **Step 3: 让 `MultiChildWidget` 显式失败 + 统一快照契约 + 去同步**

把 `core/src/main/kotlin/com/muedsa/snapshot/widget/MultiChildWidget.kt` 改为:

```kotlin
package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderContainerBox

abstract class MultiChildWidget : Widget(), ChildSlot {
    private val _children: MutableList<Widget> = mutableListOf()

    /** 子节点的只读视图。**注意是活视图不是快照**:遍历期间不要并发 append。 */
    val children: List<Widget> = _children

    fun appendChild(child: Widget) {
        check(!_children.contains(child)) {
            "${this::class.simpleName} cant append duplicate child ${child::class.simpleName}"
        }
        _children.add(child)
    }

    fun appendChildren(list: List<Widget>) {
        if (list.isNotEmpty()) {
            list.forEach { appendChild(child = it) }
        }
    }

    override fun attach(child: Widget) {
        appendChild(child)
        child.parent = this
    }

    protected abstract fun createRenderBox(children: List<Widget>): RenderBox

    final override fun createRenderBox(): RenderBox {
        // 全程用同一份快照,避免"拷贝出来的 RenderBox 树"与"活视图"对不上
        val currentChildren = _children.toList()
        val renderBox = createRenderBox(currentChildren)
        if (renderBox is RenderContainerBox) {
            check(renderBox.children.size == currentChildren.size) {
                "${this::class.simpleName} produced ${renderBox.children.size} render box(es) " +
                    "for ${currentChildren.size} child widget(s); parent data can not be applied"
            }
            currentChildren.forEachIndexed { index, child ->
                if (child is ParentDataWidget) {
                    child.applyParentData(renderBox.children[index])
                }
            }
        }
        return renderBox
    }
}

fun List<Widget>.createRenderBox(): List<RenderBox>? =
    if (isEmpty()) null else map { it.createRenderBox() }
```

注意 `@Synchronized` 两处都已去掉;`_children` 改为 `private`(对外只经 `children` 读取)。

- [ ] **Step 4: 让 `SingleChildWidget` 的裸 `!!` 带上消息**

把 `core/src/main/kotlin/com/muedsa/snapshot/widget/SingleChildWidget.kt` 的 `createRenderBox()` 改为:

```kotlin
    final override fun createRenderBox(): RenderBox {
        val child = this.child
        val renderBox = createRenderBox(child)
        if (child is ParentDataWidget && renderBox is RenderSingleChildBox) {
            val target = renderBox.child
            checkNotNull(target) {
                "${this::class.simpleName} produced a ${renderBox::class.simpleName} without a child " +
                    "render box, so parent data of ${child::class.simpleName} can not be applied"
            }
            child.applyParentData(target)
        }
        return renderBox
    }
```

- [ ] **Step 5: 让 `RenderContainerBox` 的裸 `!!` + 无检查强转带上消息,并去同步**

`core/src/main/kotlin/com/muedsa/snapshot/rendering/box/RenderContainerBox.kt` 的 `appendChild` / `appendChildren` 去掉 `@Synchronized`,并把 `appendChild` 的父数据取值改为显式检查:

```kotlin
    fun appendChild(child: RenderBox) {
        check(!_children.contains(child)) {
            "RenderContainerBox cant append duplicate child"
        }
        _children.add(child)
        val index = _children.size - 1
        child.parent = this
        val parentData = child.parentData
        check(parentData is ContainerBoxParentData) {
            "${this::class.simpleName} requires a ContainerBoxParentData from setupParentData(), " +
                "but got ${parentData?.let { it::class.simpleName } ?: "null"}"
        }
        if (index > 0) {
            parentData.previousSibling = _children[index - 1]
        }
        if (index < _children.size - 1) {
            parentData.nextSibling = _children[index + 1]
        }
    }
```

- [ ] **Step 6: 运行 Task 1 的测试**

Run:
```powershell
.\gradlew.bat :core:test --tests 'com.muedsa.snapshot.widget.MultiChildWidgetGuardTest' --console=plain
```
Expected: PASS。

- [ ] **Step 7: 全量回归**

Run:
```powershell
.\gradlew.bat test --console=plain
```
Expected: `BUILD SUCCESSFUL`,**215 + 1 = 216 例、0 失败**。若 `RenderContainerBox` 的父数据检查在既有路径上被触发,说明有 RenderBox 子类没覆写 `setupParentData`——按报错定位,不要放宽检查。

- [ ] **Step 8: 提交**

```powershell
git add core/src/main/kotlin/com/muedsa/snapshot/widget/SingleChildWidget.kt `
        core/src/main/kotlin/com/muedsa/snapshot/widget/MultiChildWidget.kt `
        core/src/main/kotlin/com/muedsa/snapshot/rendering/box/RenderContainerBox.kt `
        core/src/test/kotlin/com/muedsa/snapshot/widget/MultiChildWidgetGuardTest.kt
git commit -S -m "fix(core): 挂载路径的静默失败与未检查强转改为显式报错; 去掉误导性的 @Synchronized"
git log -1 --pretty='%h %G? %s'
```
Expected: 首字段为 `G`。

---

### Task 2: 修 `RenderBox.parent` setter 的祖先遍历死循环(TDD)

**Files:**
- Modify: `core/src/main/kotlin/com/muedsa/snapshot/rendering/box/RenderBox.kt`
- Test: `core/src/test/kotlin/com/muedsa/snapshot/render/box/RenderBoxParentTest.kt`(新)

**Interfaces:**
- Consumes: 无。
- Produces: `RenderBox.parent` 的 setter 在"新父节点自己已有父节点"时**能正常返回**(修复前死循环),且环检测真正生效。

**缺陷说明(执行前已核实):**

```kotlin
internal var parent: RenderBox? = null
    set(value) {
        var temp = value
        while (temp != null) {
            assert(temp != this) { "render tree circulate" }
            temp = value?.parent      // ← BUG:应为 temp.parent;temp 永远停在 value.parent
        }
        ...
    }
```

`temp` 被反复赋成同一个 `value.parent`,只要它非 null 就永不退出。今天没暴露,是因为 RenderBox 树自底向上构建、挂载时 `value.parent` 恰好总是 `null`。

- [ ] **Step 1: 写失败的测试**

Create `core/src/test/kotlin/com/muedsa/snapshot/render/box/RenderBoxParentTest.kt`:

```kotlin
package com.muedsa.snapshot.render.box

import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.box.RenderConstrainedBox
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertSame

class RenderBoxParentTest {

    private fun box() = RenderConstrainedBox(additionalConstraints = BoxConstraints())

    /**
     * 回归:把子节点挂到一个"自己已经有父节点"的 RenderBox 上必须能返回。
     * 修复前 setter 里的 `temp = value?.parent` 使 temp 不再前进,此处会死循环。
     * 用带超时的工作线程来跑,避免测试 JVM 直接挂死。
     */
    @Test
    fun attaching_to_an_already_parented_box_terminates() {
        val grandParent = box()
        val parent = box()
        val child = box()
        grandParent.appendChild(parent)   // parent.parent == grandParent

        val worker = Thread { parent.appendChild(child) }
        worker.isDaemon = true
        worker.start()
        worker.join(5_000)

        assertFalse(worker.isAlive, "attaching to an already parented box did not terminate")
        assertSame(parent, child.parent)
    }
}
```

- [ ] **Step 2: 运行,确认失败**

Run:
```powershell
.\gradlew.bat :core:test --tests 'com.muedsa.snapshot.render.box.RenderBoxParentTest' --console=plain
```
Expected: FAIL,消息为 `attaching to an already parented box did not terminate`(工作线程 5 秒内没返回)。测试 JVM 本身不会挂死——工作线程是 daemon,断言失败后 Gradle 能正常收尾。

- [ ] **Step 3: 修复 `RenderBox.parent` setter**

`core/src/main/kotlin/com/muedsa/snapshot/rendering/box/RenderBox.kt` 中把 setter 改为:

```kotlin
    internal var parent: RenderBox? = null
        set(value) {
            var temp = value
            while (temp != null) {
                assert(temp != this) { "render tree circulate" }
                temp = temp.parent
            }
            if (field != value) {
                value?.setupParentData(this)
            }
            field = value
        }
```

唯一改动是 `temp = value?.parent` → `temp = temp.parent`。

**不要**顺手删掉这段遍历:与 `Widget.parent`(批次 1 已删)不同,这里的 setter 还承担 `setupParentData` 的触发职责,保留环检测更安全;修好后的遍历是 O(深度) 且**结论正确**。

- [ ] **Step 4: 运行,确认通过**

Run:
```powershell
.\gradlew.bat :core:test --tests 'com.muedsa.snapshot.render.box.RenderBoxParentTest' --console=plain
```
Expected: PASS。

- [ ] **Step 5: 全量回归**

Run:
```powershell
.\gradlew.bat test --console=plain
```
Expected: `BUILD SUCCESSFUL`,`216 + 1 = 217` 例、0 失败。

- [ ] **Step 6: 提交**

```powershell
git add core/src/main/kotlin/com/muedsa/snapshot/rendering/box/RenderBox.kt `
        core/src/test/kotlin/com/muedsa/snapshot/render/box/RenderBoxParentTest.kt
git commit -S -m "fix(core): RenderBox.parent setter 祖先遍历不再前进导致死循环"
git log -1 --pretty='%h %G? %s'
```
Expected: 首字段为 `G`。

---

### Task 3: 出口验收与交付说明

**Files:** 无新改动(纯校验与汇总)。

- [ ] **Step 1: 全量测试与工作树**

Run:
```powershell
.\gradlew.bat test --console=plain
git status --porcelain
git log --pretty='%h %G? %s' main..HEAD
git diff --stat main..HEAD
```
Expected: `BUILD SUCCESSFUL`;工作树干净;所有提交 `%G?` 为 `G`。

- [ ] **Step 2: 核对无遗留的裸 `!!`**

Run:
```powershell
Get-ChildItem -Recurse -Path core\src\main -Filter *.kt | Select-String -Pattern '!!' | ForEach-Object { "$($_.Filename):$($_.LineNumber):[$($_.Line.Trim())]" }
```
Expected: 只剩**语义安全**的用法(前一行的 `check`/`assert` 已保证非空,或 `?: return` 之后的短路),不应再有挂载路径上的裸 `!!`。

- [ ] **Step 3: 汇总交付说明**

用中文写一份交付说明(供用户创建 PR),必须包含:

1. **标题**:`fix(core): 挂载路径显式失败 + 修 RenderBox.parent 祖先遍历死循环`
2. **背景**:批次 1/2 已合入(#116/#117);本批收尾"静默失败"与一个被批次 1 漏掉的同类缺陷
3. **改动**:两个 Task 的提交 hash;逐条说明 5 项改动
4. **验证**:两个新测试文件覆盖的场景、全量测试结果与用例数、裸 `!!` 核对结果
5. **行为变更提示**:功能语义不变;变化集中在"原本静默或未定义的行为现在显式抛 `IllegalStateException`",以及 `MultiChildWidget.appendChild/appendChildren` 与 `RenderContainerBox.appendChild/appendChildren` 不再是 `@Synchronized`
6. **范围说明**:本批**放弃**了 spec 里的 `contains` O(n²) 优化(实测非问题)与 `content` 加 `crossinline`(破坏性语义变更、收益低),理由写清
7. **后续**:批次 4(删 `Widget.parent` 字段 + `RenderImageEmoji.rootSpan` 显式注入)需另立 spec

- [ ] **Step 4: 报告并交回用户**

不要 push、不要创建 PR。把交付说明完整贴给用户。
