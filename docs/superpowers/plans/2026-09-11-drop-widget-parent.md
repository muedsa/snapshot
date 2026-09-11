# 删除 Widget.parent(批次 4)Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 用"`RichText` 显式向下注入根 span"取代 `ImageEmoji` 沿 `Widget.parent` 的两级反查,然后彻底删除 `Widget.parent` 字段与它的 3 个写入点。

**Architecture:** `InlineSpan` 没有上行指针,所以"从某个 span 回推继承字号"只能靠外部注入。注入状态放进**已有的** `TextParentData`(它本就是 span 专属 parentData),由 `RichText.createRenderBox(children)` 在建 render box 前写入每个 `WidgetSpanParentDataWidget`,后者在 `applyParentData` 时落到 render box 的 `TextParentData` 上;`RenderImageEmoji` 不再持有 `rootSpan` 字段,`findFontSize()` 完全从 `parentData` 取值(BFS 与 `kDefaultFontSize` 回退逐字不动)。

**Tech Stack:** Kotlin/JVM 2.4.20、Gradle Wrapper 9.7.1、JDK 17、kotlin.test(JUnit Platform)。

**Spec:** `docs/superpowers/specs/2026-09-11-drop-widget-parent-design.md`

**前置:** 批次 1(#116)、批次 2(#117)、批次 3(#118)均已合入 `main`;本分支 `refactor/drop-widget-parent` 基于 `cb121ff`。

## Global Constraints

- 仓库根 `D:\mine\workspace\snapshot`;本计划**只**在分支 `refactor/drop-widget-parent` 上执行。
- **不要** `git push`、**不要**创建 PR——由用户手动创建与合并。
- 所有命令在 PowerShell 下用 `.\gradlew.bat`;若 Gradle 报缓存 `AccessDenied`,追加 `--no-build-cache`。
- 每个提交 GPG 签名:`git commit -S -m "…"`;提交后 `git log -1 --pretty='%h %G? %s'` 首字段必须是 `G`。报 `No passphrase given` 就原样重试一次。
- 提交信息:Conventional Commits + **中文**标题。
- **异常/断言消息一律英文**;新增 KDoc 与注释用中文。
- **不引入任何新依赖、不新增 Gradle 模块。**
- 基线:`.\gradlew.bat test --console=plain` → `BUILD SUCCESSFUL`,**217 例执行、0 失败**。
- **任务顺序不可颠倒**:必须先建好注入通道并让它通过验证,才能删 `Widget.parent`。反过来做会让 `ImageEmoji` 立刻断掉,且无法区分"注入没生效"与"反查被删了"。

---

### Task 1: 建立注入通道(此时 `Widget.parent` 仍保留但已无人读取)

**Files:**
- Modify: `core/src/main/kotlin/com/muedsa/snapshot/widget/text/TextParentData.kt`
- Modify: `core/src/main/kotlin/com/muedsa/snapshot/widget/text/WidgetSpanParentDataWidget.kt`
- Modify: `core/src/main/kotlin/com/muedsa/snapshot/widget/text/RichText.kt`
- Modify: `core/src/main/kotlin/com/muedsa/snapshot/widget/text/ImageEmojiSpan.kt`
- Test: `core/src/test/kotlin/com/muedsa/snapshot/widget/text/EmojiFontSizeTest.kt`(新)

**Interfaces:**
- Consumes: `RenderImageEmoji` / `ImageEmoji` / `WidgetSpanParentDataWidget` / `TextParentData`(均已存在)。
- Produces:
  - `TextParentData(var span: PlaceholderSpan? = null, var rootSpan: InlineSpan? = null)`
  - `WidgetSpanParentDataWidget.rootSpan: InlineSpan?`(由 `RichText` 写入)
  - `RenderImageEmoji` 的构造参数 **不再有** `rootSpan`

- [ ] **Step 1: 建立基线**

Run:
```powershell
.\gradlew.bat test --console=plain
```
Expected: `BUILD SUCCESSFUL`,217 例。失败就停下来报告。

- [ ] **Step 2: 给零覆盖的 emoji 字号路径补上守护测试**

Create `core/src/test/kotlin/com/muedsa/snapshot/widget/text/EmojiFontSizeTest.kt`:

```kotlin
package com.muedsa.snapshot.widget.text

import com.muedsa.snapshot.assertSize
import com.muedsa.snapshot.findType
import com.muedsa.snapshot.paint.text.TextSpan
import com.muedsa.snapshot.paint.text.TextStyle
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rootLayout
import com.muedsa.snapshot.testTypeface
import org.jetbrains.skia.Image
import org.jetbrains.skia.Surface
import kotlin.test.Test
import kotlin.test.assertEquals

class EmojiFontSizeTest {

    private val image: Image = Surface.makeRasterN32Premul(1, 1).makeImageSnapshot()

    // 省略 width/height 时 emoji 尺寸完全由「继承字号」决定:
    // sizeForConstraints 走 BoxConstraints.tightFor(fontSize, fontSize),
    // 1x1 图片在紧约束下做 preserveAspectRatio 仍是 (40, 40)。
    // 注入失效时 findFontSize() 返回 null、约束完全放开,尺寸退化为图片自身大小 1x1,
    // 与 40 的差异远大于容差 1f。
    @Test
    fun emoji_inherits_font_size_from_rich_text() {
        val root = rootLayout {
            RichText {
                TextSpan(style = TextStyle(fontSize = 40f, typeface = testTypeface)) {
                    ImageEmojiSpan(provider = { image })
                }
            }
        }

        val emoji = root.findType<RenderImageEmoji>()
        checkNotNull(emoji) { "找不到 RenderImageEmoji 节点" }
        emoji.assertSize(40f, 40f, tolerance = 1f)
    }

    // 没有 TextParentData(即不在 RichText 里)时 findFontSize() 返回 null,
    // 尺寸退化为按图片本身大小(1x1)决定,不得抛异常。
    // WidgetSpan 只有被 RichText 抽取才会进入 widget 树,所以这条路径只能在 render 层直接构造。
    @Test
    fun emoji_without_text_parent_data_does_not_crash() {
        val box = RenderImageEmoji(image = image)
        box.layout(BoxConstraints())

        assertEquals(1f, box.definiteSize.width, 0.01f)
        assertEquals(1f, box.definiteSize.height, 0.01f)
    }
}
```

> 若 `findType` / `assertSize` 的导入路径与上面不符,按编译器提示修正 import(它们都在 testkit 的 `com.muedsa.snapshot` 包)。`findType` 返回 `LayoutNode?`。

- [ ] **Step 3: 运行,确认两条用例此刻都通过**

Run:
```powershell
.\gradlew.bat :core:test --tests 'com.muedsa.snapshot.widget.text.EmojiFontSizeTest' --console=plain
```
Expected: **2 例全过**。

**注意:这一步不是红→绿。** 用例 1 此刻是通过**今天还活着的 `Widget.parent` 反查路径**通过的;本批的红→绿由 Step 7 的反向验证提供。此处只是先把这条零覆盖的路径纳入守护,后续改动的对错由它来判定。

- [ ] **Step 4: 把 `rootSpan` 加进 `TextParentData`**

`core/src/main/kotlin/com/muedsa/snapshot/widget/text/TextParentData.kt` 改为:

```kotlin
package com.muedsa.snapshot.widget.text

import com.muedsa.snapshot.paint.text.InlineSpan
import com.muedsa.snapshot.paint.text.PlaceholderSpan
import com.muedsa.snapshot.rendering.box.ContainerBoxParentData

class TextParentData(
    var span: PlaceholderSpan? = null,
    /** 所在 RichText 的根 span。由 [RichText] 在建 render box 前注入,emoji 靠它回推继承字号。 */
    var rootSpan: InlineSpan? = null,
) : ContainerBoxParentData()
```

- [ ] **Step 5: `WidgetSpanParentDataWidget` 接收并转交 `rootSpan`**

`core/src/main/kotlin/com/muedsa/snapshot/widget/text/WidgetSpanParentDataWidget.kt` 改为:

```kotlin
package com.muedsa.snapshot.widget.text

import com.muedsa.snapshot.paint.text.InlineSpan
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.widget.ParentDataWidget

class WidgetSpanParentDataWidget(
    val span: WidgetSpan
) : ParentDataWidget() {

    /** 由 [RichText] 在建 render box 之前注入(InlineSpan 没有上行指针,无法反查)。 */
    var rootSpan: InlineSpan? = null

    override fun applyParentData(renderBox: RenderBox) {
        val textParentData = renderBox.parentData as? TextParentData
            ?: error(
                "${renderBox::class.simpleName} requires a TextParentData " +
                    "to apply ${span::class.simpleName}"
            )
        textParentData.span = span
        textParentData.rootSpan = rootSpan
    }
}
```

- [ ] **Step 6: `RichText` 注入,`ImageEmoji` 去掉反查,`RenderImageEmoji` 去掉 `rootSpan` 参数**

6a. `core/src/main/kotlin/com/muedsa/snapshot/widget/text/RichText.kt` 的 `createRenderBox` 改为(只加注入那一行与注释):

```kotlin
    override fun createRenderBox(children: List<Widget>): RenderBox {
        // emoji 需要根 span 才能回推继承字号。InlineSpan 没有上行指针,所以在这里显式向下注入,
        // 而不是让子节点沿 Widget.parent 反查(本批起 Widget 不再持有 parent)。
        children.forEach { (it as? WidgetSpanParentDataWidget)?.rootSpan = text }
        return RenderParagraph(
            text = text,
            textAlign = textAlign,
            textDirection = textDirection,
            softWrap = softWrap,
            overflow = overflow,
            maxLines = maxLines,
            strutStyle = strutStyle,
            textWidthBasis = textWidthBasis,
            textHeightMode = textHeightMode,
        ).also { p ->
            children.createRenderBox()?.let {
                p.appendChildren(it)
            }
        }
    }
```

6b. `core/src/main/kotlin/com/muedsa/snapshot/widget/text/ImageEmojiSpan.kt` 中 `ImageEmoji.createRenderBox()` 去掉两级反查:

```kotlin
    override fun createRenderBox(): RenderBox = RenderImageEmoji(
        image = image,
        width = width,
        height = height,
        fit = fit,
        alignment = alignment,
        repeat = repeat,
        scale = scale,
        opacity = opacity,
        color = color,
        colorBlendMode = colorBlendMode,
    )
```

6c. 同一文件里 `RenderImageEmoji`:删除构造参数 `val rootSpan: InlineSpan? = null,`,并把 `findFontSize()` 改为从 `parentData` 取值(**BFS 体与 `kDefaultFontSize` 回退逐字不动**):

```kotlin
    private fun findFontSize(): Float? {
        var size: Float? = null
        val pd = parentData
        if (pd is TextParentData) {
            val rootSpan = pd.rootSpan
            val targetSpan = pd.span
            if (rootSpan != null && targetSpan != null) {
                val queue: LinkedList<InlineSpan> = LinkedList()
                val parentValueMap: MutableMap<InlineSpan, Float?> = mutableMapOf()
                parentValueMap[rootSpan] = rootSpan.style?.fontSize
                queue.offer(rootSpan)
                while (!queue.isEmpty()) {
                    val currentSpan: InlineSpan = queue.poll()
                    if (currentSpan == targetSpan) {
                        size = parentValueMap[currentSpan]
                        break
                    }

                    if (currentSpan is TextSpan) {
                        for (child in currentSpan.children) {
                            val childSize = child.style?.fontSize ?: parentValueMap[currentSpan]
                            parentValueMap[child] = childSize
                            queue.offer(child)
                        }
                    }
                }
                if (size == null) {
                    size = kDefaultFontSize
                }
            }
        }
        return size
    }
```

- [ ] **Step 7: 运行新测试,并做反向验证(本批最重要的验收动作)**

7a. Run:
```powershell
.\gradlew.bat :core:test --tests 'com.muedsa.snapshot.widget.text.EmojiFontSizeTest' --console=plain
```
Expected: **2 例全过**(现在走的是注入通道)。

7b. 临时把 `RichText.createRenderBox` 里那一行 `children.forEach { (it as? WidgetSpanParentDataWidget)?.rootSpan = text }` **注释掉**,Run:
```powershell
.\gradlew.bat :core:test --tests 'com.muedsa.snapshot.widget.text.EmojiFontSizeTest' --console=plain
```
Expected: **用例 1 必须失败**,且实际尺寸为 `1`(图片自身大小)而非 `40`(从 `assertSize` 的报错文本确认实际值)。若它仍然通过,说明测试没锁住注入通道,**停下来报告**——不要继续。

7c. 恢复那一行,再跑一遍,Expected: **2 例全过**。

- [ ] **Step 8: 全量回归**

Run:
```powershell
.\gradlew.bat test --console=plain
```
Expected: `BUILD SUCCESSFUL`,**217 + 2 = 219 例、0 失败**。

- [ ] **Step 9: 提交**

```powershell
git add core/src/main/kotlin/com/muedsa/snapshot/widget/text/TextParentData.kt `
        core/src/main/kotlin/com/muedsa/snapshot/widget/text/WidgetSpanParentDataWidget.kt `
        core/src/main/kotlin/com/muedsa/snapshot/widget/text/RichText.kt `
        core/src/main/kotlin/com/muedsa/snapshot/widget/text/ImageEmojiSpan.kt `
        core/src/test/kotlin/com/muedsa/snapshot/widget/text/EmojiFontSizeTest.kt
git commit -S -m "fix(core): emoji 继承字号改由 RichText 显式注入根 span; 补 emoji 字号测试"
git log -1 --pretty='%h %G? %s'
```
Expected: 首字段为 `G`。

---

### Task 2: 删除 `Widget.parent` 及其 3 个写入点

**Files:**
- Modify: `core/src/main/kotlin/com/muedsa/snapshot/widget/Widget.kt`
- Modify: `core/src/main/kotlin/com/muedsa/snapshot/widget/ChildSlot.kt`
- Modify: `core/src/main/kotlin/com/muedsa/snapshot/widget/SingleChildWidget.kt`
- Modify: `core/src/main/kotlin/com/muedsa/snapshot/widget/MultiChildWidget.kt`
- Modify: `core/src/main/kotlin/com/muedsa/snapshot/widget/ProxyWidget.kt`

**Interfaces:**
- Consumes: Task 1 的注入通道(`Widget.parent` 此时已无人读取)。
- Produces: `Widget` 不再有 `parent` 属性;`attach` 只负责放进子槽位。

- [ ] **Step 1: 先确认 `parent` 确实已无读者**

Run:
```powershell
Get-ChildItem -Recurse -Path core\src,testkit\src,parser\src -Filter *.kt | Select-String -Pattern '\.parent\b' |
  Where-Object { $_.Line -cnotmatch 'RenderBox|parentData|Layer|LayoutNode|Element|parentFile|absoluteOffset|offsetFromParent|val parent|_parent' } |
  ForEach-Object { "$($_.Filename):$($_.LineNumber):[$($_.Line.Trim())]" }
```
Expected: 只剩 3 处 `child.parent = this`(在三个 `attach` 里)与 KDoc 引用。**若出现任何读取点,停下来报告**——说明 Task 1 漏了东西。
> **注意(执行时实测踩到的坑):** 下面这条核对命令里的 `Where-Object` **必须用 `-cnotmatch`(大小写敏感)而不是 `-notmatch`**。PowerShell 的 `-notmatch` 默认大小写**不敏感**,如果排除列表里写了 `PARENT`(本意是排除 `TextWidthBasis.PARENT`),它会把**所有**含 "parent" 的行都排除掉,核对结果永远是空——看起来"零残留",实际漏掉了 `ChildSlotAttachTest` 里 5 处 `child.parent` 断言,导致提交了一个编译不过的版本。核对类命令一律用它的大小写敏感版本,并且要**对结果做门禁**(测试不通过就不提交)。

- [ ] **Step 2: `Widget.kt` 删除字段**

把 `Widget` 类改为:

```kotlin
@SnapshotWidgetDsl
abstract class Widget {

    abstract fun createRenderBox(): RenderBox
}
```

(删掉 `var parent: Widget? = null` 与 `internal set`;`import com.muedsa.snapshot.rendering.box.RenderBox` 保留。)

- [ ] **Step 3: 三个 `attach` 删除 `child.parent = this`**

- `SingleChildWidget.kt`:`attach` 里删掉 `child.parent = this`
- `MultiChildWidget.kt`:`attach` 里删掉 `child.parent = this`
- `ProxyWidget.kt`:`attach` 里删掉 `child.parent = this`

三个 `attach` 之后分别是:

```kotlin
    override fun attach(child: Widget) {
        check(this.child == null) {
            "${this::class.simpleName} already has a child, can not attach ${child::class.simpleName}"
        }
        this.child = child
    }
```

```kotlin
    override fun attach(child: Widget) {
        appendChild(child)
    }
```

```kotlin
    override fun attach(child: Widget) {
        check(widget == null) {
            "${this::class.simpleName} already has a widget, can not attach ${child::class.simpleName}"
        }
        this.widget = child
    }
```

- [ ] **Step 4: 更新 `ChildSlot.kt` 的 KDoc**

`ChildSlot` 的类注释与 `attach` 注释里两处 `[Widget.parent]` 引用改为不再提及 parent:

```kotlin
/**
 * 拥有子节点槽位的 [Widget]。
 *
 * DSL 函数([Padding]、[Row]、[Stack] 等)以此为接收者,因此"在不具备子槽位的 Widget 上
 * 挂子节点"会成为编译错误,而不是运行时异常。
 *
 * [attach] 是全项目唯一的挂载入口:它把 [child] 放进本节点的子槽位。它允许重新挂载
 * (不校验子槽位是否已被占用以外的状态),因为 [Container] 的 `composeWidget()` 会把已挂载的
 * 子节点重新挂到新建的包装节点上。
 */
interface ChildSlot {

    /** 把 [child] 挂到本节点的子槽位。重复挂载抛 [IllegalStateException]。 */
    fun attach(child: Widget)
}
```

- [ ] **Step 5: 编译并全量回归**

Run:
```powershell
.\gradlew.bat test --console=plain
```
Expected: `BUILD SUCCESSFUL`,**219 例、0 失败**。若报 `Unresolved reference: parent`,说明还有读取点没清,按报错定位——**不要**把字段加回来。

- [ ] **Step 6: 核对零残留**

Run:
```powershell
Get-ChildItem -Recurse -Path core\src,testkit\src,parser\src -Filter *.kt | Select-String -Pattern '\bparent\b' |
  Where-Object { $_.Line -match 'Widget' -and $_.Line -cnotmatch 'RenderBox|parentData|Layer|LayoutNode|Element' } |
  ForEach-Object { "$($_.Filename):$($_.LineNumber):[$($_.Line.Trim())]" }
```
Expected: **无输出**(`Layer.parent` / `RenderBox.parent` / `LayoutNode.parent` / `Element.parent` 均不受影响,那些是各自层里长期存在且确有用途的指针)。

- [ ] **Step 7: 提交**

```powershell
git add core/src/main/kotlin/com/muedsa/snapshot/widget/Widget.kt `
        core/src/main/kotlin/com/muedsa/snapshot/widget/ChildSlot.kt `
        core/src/main/kotlin/com/muedsa/snapshot/widget/SingleChildWidget.kt `
        core/src/main/kotlin/com/muedsa/snapshot/widget/MultiChildWidget.kt `
        core/src/main/kotlin/com/muedsa/snapshot/widget/ProxyWidget.kt
git commit -S -m "refactor(core): 删除 Widget.parent 与 attach 里的回指写入"
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
Expected: `BUILD SUCCESSFUL`,219 例;工作树干净;所有提交 `%G?` 为 `G`。

- [ ] **Step 2: 复核反向验证确实做过**

确认 Task 1 Step 7b 的执行记录还在(注释掉注入行 → 用例 1 失败 → 恢复 → 通过)。交付说明里必须写出**那一轮失败时的实际报错文本**(实际值应为 `1.0`,即图片自身大小),这是本批唯一的"测试真的锁住了行为"的证据。

- [ ] **Step 3: 汇总交付说明**

用中文写一份交付说明(供用户创建 PR),必须包含:

1. **标题**:`refactor(core): 删除 Widget.parent,emoji 继承字号改为 RichText 显式注入`
2. **背景**:`Widget.parent` 的 3 写 1 读现状;为什么它与 Flutter 不一致(Flutter 的 `Widget` 是一次性配置、不带 parent;parent 属于 `Element` 且私有、对外只给 `findAncestorWidgetOfExactType` 这类查询);snapshot 的 `Widget` 在**生命周期**上同样是"建完即弃",所以不该带 parent
3. **改动**:两个 Task 的提交 hash;`TextParentData` +`rootSpan`、`WidgetSpanParentDataWidget` 转交、`RichText` 注入、`RenderImageEmoji` 去掉 `rootSpan` 参数、`Widget.parent` 与 3 处写入删除
4. **验证**:新增 `EmojiFontSizeTest` 两例;**反向验证的实际失败文本**;全量测试结果与用例数(217 → 219);`Widget.parent` 零残留核对
5. **行为变更提示**:功能语义不变;唯一差异是 span 缺失时 `findFontSize()` 从抛 NPE 改为返回 `null`(既有路径不可达,属收敛);公共面删除了 `Widget.parent` 属性
6. **顺带说明**:该 emoji 字号路径在本批之前**零测试覆盖**,本批第一次为它建立守护
7. **后续**:四个批次(#116/#117/#118 + 本批)完成后,设计稿 `2026-09-11-widget-dsl-childslot-design.md` 列出的全部隐患(pain P1–P4 + `parent` 瘦身)均已收口

- [ ] **Step 4: 报告并交回用户**

不要 push、不要创建 PR。把交付说明完整贴给用户。
