# 删除 Widget.parent(批次 4)设计

日期:2026-09-11
状态:设计(做法已与用户确认)

## 背景

### 现状

`Widget.parent` 经批次 1 瘦身后(去掉 O(深度) 祖先遍历、setter 收窄为 `internal set`)已无性能或安全代价,但仍是**一个只有单一特例读者的状态**:

- **3 个写入点**:`SingleChildWidget.attach` / `MultiChildWidget.attach` / `ProxyWidget.attach` 里的 `child.parent = this`
- **1 个读者**:`ImageEmoji.createRenderBox()`(`widget/text/ImageEmojiSpan.kt:114-116`)

它存在的唯一理由是那个读者:内嵌 emoji 需要在**建段落之前**知道自己继承的字号,于是沿 `ImageEmoji.parent → WidgetSpanParentDataWidget.parent → RichText` 两级反查,拿到 `RichText.text`(根 `InlineSpan`),交给 `RenderImageEmoji.findFontSize()` 在布局期做一次 BFS 求继承字号。

`InlineSpan` **没有任何上行指针**(只有 `children` / `visitChildren`),所以"从某个 span 反查根 span"确实只能靠外部注入——这正是该字段存在的技术原因。

### 与 Flutter 的对照(本次设计的核心依据)

Flutter 是两层:短命配置 `Widget`(`@immutable`,每次 build 新建、build 完即丢)与长期实例 `Element`(parent 在**私有**字段 `Element._parent`,由 [`Element.mount(parent, newSlot)`](https://api.flutter.dev/flutter/widgets/Element/mount.html) 建立);对外只给查询不给指针——`BuildContext`(实现即 `Element`)提供 [`findAncestorWidgetOfExactType<T>()`](https://pub.dev:443/documentation/flutter_widget_catalogue/1.1.3+2/flutter_widget_catalogue/BuildContext/findAncestorWidgetOfExactType.html)、`visitAncestorElements` 等,从不暴露 `parent` getter。`RenderObject` 则有 public `parent`(渲染树长期存在且可变)。

对照结论:

1. snapshot 的 `RenderBox.parent` 与 Flutter 一致,**不动**。
2. snapshot 的 `Widget` 在**能力**上兼任了 Element 的活(直接 `createRenderBox()`),但在**生命周期**上是 Flutter 的 `Widget`——`layoutWidget` 建完树、调一次 `createRenderBox()` 后 Widget 树即被丢弃,长期存在的是 RenderBox 树;它没有 mount/update/unmount/diff-reuse 这套 Element 机制。
3. **Flutter 的 `Widget` 之所以不带 parent,正是因为它是一次性配置。** 按同一逻辑,snapshot 的 `Widget` 也不该带。保留它才是"抄了 Element 的指针、没抄 Element 的生命周期"的半抄。
4. Flutter 从不向上找,只有"构造参数向下传"与"`InheritedWidget` 环境式下发"两条路。

另需说明:`ImageEmoji` 这段逻辑**在 Flutter 里没有对应物**。Flutter 的 `RenderParagraph` 让 `ParagraphBuilder` 在遍历 span 树时自行 push/pop 样式、placeholder 尺寸经 `PlaceholderDimensions` 传入、继承字号由排版引擎解析;snapshot 之所以要手工 BFS,是因为它把内嵌 emoji 做成**独立子 RenderBox**,必须在建段落前知道其尺寸。这是 snapshot 特有的绕行,因此不追求"抄 Flutter 的写法",只追求"不引入 Flutter 没有的向上指针"。

### 环境基线

2026-09-11,`main` = `cb121ff`(批次 1 PR #116、批次 2 PR #117、批次 3 PR #118 均已合入)。`./gradlew test` **BUILD SUCCESSFUL**,**217 例执行、0 失败**。

**覆盖率现状(自审时实测,与直觉相反)**:`ImageEmojiSpan` 与 `RenderImageEmoji.findFontSize()` 目前**没有任何测试覆盖**。`TextTest.widget_span_test` 用的是通用 `WidgetSpan { Container(width = 20f, height = 20f) }`,`WidgetSpanParentDataWidget` → `TextParentData.span` 这条链是被走过的,但**只有 emoji 才会进入 `findFontSize()`** 的那段 BFS 与 `kDefaultFontSize` 回退。也就是说本批要改的正是全仓唯一没被守护的那条路径——这直接决定了验证方法必须包含"反向验证"。

## 目标与完成标准

1. **彻底删除 `Widget.parent` 字段**及其 3 个写入点,全仓零残留。
2. **`ImageEmoji` 不再依赖任何向上反查**:根 span 由 `RichText` 显式向下注入。
3. **行为语义不变**:BFS 算法与 `kDefaultFontSize` 回退**逐字不动**,仍在布局期执行。
4. 用一条**直接断言继承字号**的测试锁住注入通道,防止"看起来接上了、实际没生效"的假通过。

验收:

- [ ] `./gradlew test` BUILD SUCCESSFUL,既有 217 例全绿 + 新增用例;
- [ ] **反向验证**:把 `RichText.createRenderBox` 里的注入那行注释掉后,新增的"继承字号生效"用例**必须失败**;恢复后必须通过;
- [ ] 全仓 `Widget.parent` 零残留(`Layer.parent` / `RenderBox.parent` / `LayoutNode.parent` / `Element.parent` / `ContainerBoxParentData` 不受影响);
- [ ] `ChildSlot` 的 KDoc 不再引用 `Widget.parent`;
- [ ] 与设计稿同步。

## 做法决策(逐条)

1. **采用 F1(删字段 + 显式向下注入)**,而非 F2(保留私有指针 + 新增 `findAncestorOfType<T>()` 查询 API)或 F3(保留现状)。理由:Flutter 的一致性论据支持"配置向下传";F2 为**一个消费者**新增公共 API 且指针照样要维护,而 snapshot 没有 Element 层,查询只能挂在 `Widget` 上,反而更差。
2. **注入状态放在已有的 `TextParentData` 上**,不新增接口。`TextParentData` 本就是"span 专属 parentData"这条现成通道(已有 `span: PlaceholderSpan?`),再放一个 `rootSpan: InlineSpan?` 是自然扩展;这样 `RenderImageEmoji` 连 `rootSpan` 字段都不需要,`findFontSize()` 完全从 `parentData` 取值。
3. **注入点在 `RichText.createRenderBox(children)`**:它是唯一同时知道"根 span"与"各 `WidgetSpanParentDataWidget` 子节点"的地方。
4. **时序**:`MultiChildWidget.createRenderBox()` 先调子类的 `createRenderBox(children)`(其中建出各子 render box),**之后**才逐个调 `applyParentData`;而 `layoutWidget` 的 `layout()` 更晚。因此"注入 → applyParentData 落地 → 布局期读取"这条链的时序是安全的。
5. **BFS 不上移、不重写**,仍留在 `RenderImageEmoji.findFontSize()` 的布局期路径上,只把 `rootSpan` 的来源从构造参数改为 `parentData`。备选方案 B(`RichText` 建树时预算字号写进 parentData)被否:它把计算时机从布局期提前到建树期,引入不易察觉的语义差异,而收益(渲染层不接触 `InlineSpan`、BFS 少跑)在本项目的规模下可忽略。
6. **`WidgetSpanParentDataWidget.applyParentData` 顺带硬化**:把 `assert(renderBox.parentData is TextParentData)` + `as` 改为 `as?` + `error(...)` 带类名消息(与批次 3 的"挂载路径显式失败"同一精神)。
7. **唯一语义差异(收敛而非放宽)**:今天 `findFontSize()` 里 `pd.span!!` 在 span 缺失时抛 NPE;改为 `targetSpan != null` 守卫后返回 `null`,进而走 `sizeForConstraints` 的 `width ?: null` 分支。既有路径下 `applyParentData` 必定设置 `span`,故不可达;属严格改善。

## 关键设计

### ① `widget/text/TextParentData.kt`

```kotlin
class TextParentData(
    var span: PlaceholderSpan? = null,
    var rootSpan: InlineSpan? = null,
) : ContainerBoxParentData()
```

### ② `widget/text/WidgetSpanParentDataWidget.kt`

```kotlin
class WidgetSpanParentDataWidget(val span: WidgetSpan) : ParentDataWidget() {

    /** 由 [RichText] 在建 render box 前注入;emoji 靠它回推继承字号。 */
    var rootSpan: InlineSpan? = null

    override fun applyParentData(renderBox: RenderBox) {
        val textParentData = renderBox.parentData as? TextParentData
            ?: error(
                "${renderBox::class.simpleName} requires a TextParentData to apply ${span::class.simpleName}"
            )
        textParentData.span = span
        textParentData.rootSpan = rootSpan
    }
}
```

### ③ `widget/text/RichText.kt`

```kotlin
    override fun createRenderBox(children: List<Widget>): RenderBox {
        // emoji 需要根 span 才能回推继承字号。InlineSpan 没有上行指针,所以在这里显式向下注入,
        // 而不是让子节点沿 Widget.parent 反查(批次 4 起 Widget 不再持有 parent)。
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

### ④ `widget/text/ImageEmojiSpan.kt`

`ImageEmoji.createRenderBox()` 去掉反查:

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

`RenderImageEmoji` 删除 `rootSpan` 构造参数;`findFontSize()` 改为:

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

### ⑤ 删除 `Widget.parent`

- `widget/Widget.kt`:删除 `var parent: Widget? = null` 与 `internal set`
- `widget/SingleChildWidget.kt`、`MultiChildWidget.kt`、`ProxyWidget.kt`:各自的 `attach` 里删除 `child.parent = this`
- `widget/ChildSlot.kt`:KDoc 中两处 `[Widget.parent]` 改为"把 child 放进本节点的子槽位"的表述

## 涉及文件

- Modify:`core/src/main/kotlin/com/muedsa/snapshot/widget/Widget.kt`
- Modify:`core/src/main/kotlin/com/muedsa/snapshot/widget/ChildSlot.kt`
- Modify:`core/src/main/kotlin/com/muedsa/snapshot/widget/SingleChildWidget.kt`
- Modify:`core/src/main/kotlin/com/muedsa/snapshot/widget/MultiChildWidget.kt`
- Modify:`core/src/main/kotlin/com/muedsa/snapshot/widget/ProxyWidget.kt`
- Modify:`core/src/main/kotlin/com/muedsa/snapshot/widget/text/TextParentData.kt`
- Modify:`core/src/main/kotlin/com/muedsa/snapshot/widget/text/WidgetSpanParentDataWidget.kt`
- Modify:`core/src/main/kotlin/com/muedsa/snapshot/widget/text/RichText.kt`
- Modify:`core/src/main/kotlin/com/muedsa/snapshot/widget/text/ImageEmojiSpan.kt`
- Add:`core/src/test/kotlin/com/muedsa/snapshot/widget/text/EmojiFontSizeTest.kt`

## 行为变更

功能语义不变。仅一处收敛:span 缺失时 `findFontSize()` 从抛 NPE 改为返回 `null`(既有路径不可达)。

## 非目标

- 不引入 Element / BuildContext 层,不提供任何祖先查询 API(那是另一个量级的设计)。
- 不动 `RenderBox.parent`、`Layer.parent`、`LayoutNode.parent`、`Element.parent`——它们是各自层里长期存在且确有用途的指针。
- 不重写 BFS、不改变字号解析时机与回退语义。
- 不改 `WidgetSpan` / `ImageEmojiSpan` 的 DSL 签名。
- 不为 `TextParentData.rootSpan` 增加新的公共 API 语义(它只是 `applyParentData` 的载体)。

## 验证方法

基线:`.\gradlew.bat test --console=plain` → BUILD SUCCESSFUL,217 例。

新增 `EmojiFontSizeTest`(`core/src/test/kotlin/com/muedsa/snapshot/widget/text/EmojiFontSizeTest.kt`):

```kotlin
class EmojiFontSizeTest {

    private val image: Image = Surface.makeRasterN32Premul(1, 1).makeImageSnapshot()

    // 省略 width/height 时,emoji 尺寸完全由「继承字号」决定:
    // sizeForConstraints 走 BoxConstraints.tightFor(width = fontSize, height = fontSize),
    // 1x1 图片在紧约束下 preserveAspectRatio 仍得 (40, 40)。
    @Test
    fun emoji_inherits_font_size_from_rich_text() {
        val root = rootLayout {
            RichText {
                TextSpan(style = TextStyle(fontSize = 40f, typeface = testTypeface)) {
                    ImageEmojiSpan(provider = { image })
                }
            }
        }

        val emoji = checkNotNull(root.findType<RenderImageEmoji>()) { "找不到 RenderImageEmoji" }
        assertEquals(40f, emoji.size.height, 1f)
        assertEquals(40f, emoji.size.width, 1f)
    }

    // 没有 TextParentData(即不在 RichText 里)时 findFontSize() 返回 null,
    // 尺寸退化为按图片本身大小(1x1)决定,不得抛异常。
    // 直接构造 render box 而不经 widget 树:WidgetSpan 只有被 RichText 抽取才会进入树,
    // 因此"没有 RichText 祖先"这条路径只能在 render 层构造。
    @Test
    fun emoji_without_text_parent_data_does_not_crash() {
        val box = RenderImageEmoji(image = image)
        box.layout(BoxConstraints())

        assertEquals(1f, box.definiteSize.width, 0.01f)
        assertEquals(1f, box.definiteSize.height, 0.01f)
    }
}
```

**反向验证(必做,本批最重要的验收动作)**:临时注释掉 `RichText.createRenderBox` 里的 `children.forEach { (it as? WidgetSpanParentDataWidget)?.rootSpan = text }`,`./gradlew :core:test --tests '*EmojiFontSizeTest*'` → **用例 1 必须失败,且实际尺寸为 `1`(图片自身大小)而非 `40`**;恢复该行后必须通过。这一步证明该测试真的锁住了注入通道,而不是恰好通过。

随后:全量 `.\gradlew.bat test`;`git status --porcelain` 干净;全仓 `Widget.parent` 零残留核对。

## 风险

1. **注入失效会静默退化成默认字号**——这正是要求"反向验证必做"的原因。且该路径**当前零测试覆盖**(见背景),没有既有用例能替你兜住。
2. **用例 1 的量化关系已核实**:`layoutInlineChildren`(`RenderParagraph.kt:137`)给内联子节点的是 `BoxConstraints(maxWidth = …)` 这种**宽松**约束;`sizeForConstraints` 走 `BoxConstraints.tightFor(width = width ?: fontSize, height = height ?: fontSize)` 再 `.enforce(...)`。省略 `width`/`height` 且注入生效时即 `tightFor(40, 40)`,对 1×1 图片做 `constrainSizeAndAttemptToPreserveAspectRatio` 仍是 `40×40`。
   **注入失效时**则 `findFontSize()` 返回 `null` → `tightFor(null, null)` 是**完全放开**的约束(`0..∞`)→ 尺寸退化为图片自身大小 `1×1`。`1` 与 `40` 的差异远大于容差 `1f`,断言可靠。
   (注意别混淆:`kDefaultFontSize = 14f` 是**另一条**失败路径——`rootSpan` 存在、但 BFS 没在它的子树里找到目标 span 时的回退值,不是"注入失效"的表现。)
3. `TextParentData` 由 `RenderParagraph.setupParentData` 创建、由 `WidgetSpanParentDataWidget.applyParentData` 填充;新增 `rootSpan` 字段不影响既有填充路径。
4. 若 `findType<RenderImageEmoji>()` 因 emoji 不在 `LayoutNode` 树里而返回 null,退路是直接按坐标查找(与 `CachedNetworkImageTest` 的查找方式一致),以实际运行结果为准。

## 参考

- Flutter [`Element.mount(parent, newSlot)`](https://api.flutter.dev/flutter/widgets/Element/mount.html) —— parent 指针属于 Element 层
- Flutter [`BuildContext.findAncestorWidgetOfExactType<T>()`](https://pub.dev:443/documentation/flutter_widget_catalogue/1.1.3+2/flutter_widget_catalogue/BuildContext/findAncestorWidgetOfExactType.html) —— 对外只给查询、不给指针
- `docs/superpowers/specs/2026-09-11-widget-dsl-childslot-design.md` 批次 4 一节
