# Widget DSL 子槽位类型化设计(ChildSlot + @DslMarker)

日期:2026-09-11
状态:设计(做法已与用户确认)

## 背景

### 现状:三套机制共同承担"把子节点挂到父节点上"

1. **`Widget.buildChild`**(`core/src/main/kotlin/com/muedsa/snapshot/widget/Widget.kt:5-16`)——运行时 `when (this)` 分派到 `ProxyWidget.widget` / `SingleChildWidget.child` / `MultiChildWidget.appendChild`;`else` 分支抛**无消息**的 `IllegalStateException()`。
2. **`Widget.bind`**(`Widget.kt:18-31`)——同样分派,但**额外**设置 `child.parent = this`;而 DSL 走的是另一条路:每个调用点手写构造参数 `parent = this`。同一个操作存在两套 parent 规则。
3. **`Widget.parent`**(`Widget.kt:36-44`)——setter 里做 O(深度) 祖先遍历查环。该遍历本身不受 `assert` 保护(`assert(temp != this)` 只有布尔比较受保护),断言关闭时仍全量执行。而该字段全仓库**只有 1 个读者**:`ImageEmoji.createRenderBox()`(`widget/text/ImageEmojiSpan.kt:120-121`,用于两级回溯到根 `RichText` 拿 `InlineSpan`)。

### 四个已核实的问题

**P1 外层 receiver 静默捕获(正确性,最严重)。** 除 `Flex.Expanded` / `Flex.Flexible` / `Stack.Positioned` 外,所有 DSL 函数的接收者都是 `Widget`,且整个 widget 包没有 `@DslMarker`。于是外层接收者在内层 lambda 里仍可解析:

```kotlin
Stack {
    Row {
        Positioned(left = 0f) { }   // 编译通过, 但被挂到外层 Stack
    }
}
Row {
    Padding(padding = EdgeInsets.all(8f)) {
        Expanded { }                // 编译通过, 但被挂到 Row
    }
}
```

产出的树与代码的嵌套结构不一致:**不报错、不崩溃,只是图不对**。

**P2 子槽位能力不在类型系统里。** `buildChild` 的 `else -> throw IllegalStateException()` 是对"该父节点没有子槽位"的运行时兜底,而该分支在 DSL 的设计里理论上不可达——需要运行时兜底说明类型信息本该在此却缺失。

**P3 双轨挂载已经漂移。** `buildChild` 不设 `parent`,靠调用点写 `parent = this`;33 处里**恰好漏了 1 处**:`CachedNetworkImage`(`widget/CachedNetworkImage.kt:24-40`)没传 `parent = this`。因此 `Row { CachedNetworkImage(...) }` 造出的 widget `parent == null`,而同族的 `RawImage`(`RawImage.kt:35`)、`ProviderImage`(`ProviderImage.kt:33`)都非 null。当前无害,只因唯一读者是 `ImageEmoji`——属纯运气。

**P4 静默覆盖。** `SingleChildWidget.child`(`SingleChildWidget.kt:9`)与 `ProxyWidget.widget`(`ProxyWidget.kt:7`)是公开 `var`,`buildChild` 直接赋值且不校验;而 `MultiChildWidget.appendChild`(`MultiChildWidget.kt:14-19`)有 `check(!_children.contains(child))`。结果同一段语法在不同父节点下语义不同:

```kotlin
Padding(padding = EdgeInsets.all(1f)) { SizedBox(width = 1f) {}; SizedBox(width = 2f) {} }  // 第一个被静默替换
Row { SizedBox(width = 1f) {}; SizedBox(width = 2f) {} }                                     // 两个都保留
```

### 环境基线

2026-09-11,`main` = `55fe451`(含 PR #113/#114)。JDK 17.0.1 / Gradle 9.7.1 / Kotlin 2.4.20。
`./gradlew test` **BUILD SUCCESSFUL(1m08s)**:73 个测试文件 / 228 个 `@Test`(默认 suite 已排除 `sample` 与 `network` 标签)。

## 目标与完成标准

1. **编译期**:错误嵌套(外层 receiver 捕获)、在没有子槽位的 Widget 上挂子节点、helper 用 `Widget.` 接收者写树——三者都必须**编译失败**。
2. **运行时**:同一父节点重复挂载子节点必须报错,且消息带父/子类名。
3. **结构**:挂载原语唯一(`ChildSlot.attach`);`Widget.parent` 只有一个写入点,且不再有 O(深度) 遍历。
4. **不破坏**:用户手写 Widget 树的代码(`Container { Row { Container(...) } }`)原样编译通过。

验收:

- [ ] 批次 1 后 `./gradlew test` BUILD SUCCESSFUL(既有 228 例全绿 + 新增用例),`git status` 干净;
- [ ] 负例 ①② 编译失败,编译器报错原文记录在 PR 描述;
- [ ] 正向清单全部编译通过(批次 1 起持续成立);
- [ ] 批次 1 新增单测:三类父节点各一例"重复挂载 → `IllegalStateException`";
- [ ] 批次 2 后全量 `test` 仍绿,负例 ③ 编译失败,正向清单仍全绿;
- [ ] `docs/usage/README.md` §4.1 同步 `ChildSlot`;
- [ ] 两个分支/PR 各自可独立合并、独立回滚。

## 做法决策(逐条)

1. **新增公共类型 `ChildSlot`**(接口,单方法 `attach(child: Widget)`),由 `ProxyWidget` / `SingleChildWidget` / `MultiChildWidget` 实现。`Widget` 本身**不**实现它——这正是"叶子 Widget 不能有子节点"的静态表达。
2. **`attach` 是全项目唯一的挂载入口**:放入子槽位 + 设置 `child.parent`,并承担重复挂载校验。`buildChild` / `bind` / 33 处 `parent = this` 全部由它取代。
3. **三个基类的 `attach` 各自实现**校验(单子节点:槽位为空;多子节点:沿用 `appendChild` 的重复检查;Proxy:槽位为空),但 `child.parent = this` 三处一致——这是唯一写入点。
4. **`@DslMarker`**:新增 `@DslMarker annotation class SnapshotWidgetDsl`,标注在 `Widget` 上。标记规则对父类/父接口生效,因此整个 Widget 层级都被标记;效果是"最近的接收者胜,绑定到被遮蔽的接收者即编译错误"——`Row { Column { Expanded { } } }` 仍合法(`Column` 是最近的 `Flex`),而 `Padding { Expanded { } }` 编译失败。
5. **`parent` 瘦身(不删字段)**:去掉 setter 里的祖先遍历;可见性收窄为 `internal set`(对外只读)。字段删除与 `rootSpan` 显式注入留作批次 4。
6. **`parent` 构造参数在批次 2 删除**:所有 Widget 类的 `parent: Widget? = null` 与其转发一并移除,使 `parent` 真的只有 `attach` 一个写入点。已核实全仓库无任何地方使用 `SizedBox.expand/shrink/fromSize/square` 与 `Positioned.fill/fromRect/fromRelativeRect/directional`,parser 侧也一律不传 `parent`。
7. **DSL 接收者分批改**:批次 1 保留 `Widget.` 接收者与 `buildChild`/`bind` 的名字与签名(内部退化为 `attach` 的薄封装),零调用点改动;批次 2 才把接收者改成 `ChildSlot.` 并删除薄封装。这样"运行时防线"与"编译期防线"分成两个可独立回滚的 PR。
8. **入口签名**(批次 2):`Snapshot` / `SnapshotImage` / `SnapshotPNG` / `SnapshotJPEG` / `SnapshotWEBP` / `layoutWidget` / `testkit.rootLayout` / `TextSpan.WidgetSpan` 的 `content: Widget.() -> Unit` → `ChildSlot.() -> Unit`。根部继续用 `ProxyWidget`(实现 `ChildSlot`)。
9. **`ProxyWidget.buildWidget` 收为 `@PublishedApi internal`**(批次 2):它的唯一调用者 `TextSpan.WidgetSpan`(`WidgetSpan.kt:23`)是 **public inline** 函数,而 public inline 函数不能调用普通 `internal` 成员——因此必须用 `@PublishedApi internal`:源码层对外不可见,字节码层保留(内联展开需要)。`layoutWidget`(`Snapshot.kt:96-104`,同为 public inline)内的 `ProxyWidget().apply(content)` 改为复用它,消除这份重复实现。
10. **parser 适配**:`WidgetParser.createWidgetForChildElement` 改为 `widget as? ChildSlot`,失败即 `error(...)`。解析期的子节点数量校验已由 `Element.appendChild`(`Element.kt:18-34`)依 `containerMode` 完成,构建期无需重复校验;`error(...)` 会被 `Element.createWidget()` 的 `catch (t: Throwable)` 包成带 `TrackPos` 的 `ParseException`。顺带删除 `Element.kt:6` 从未使用的 `import com.muedsa.snapshot.widget.bind`。
11. **`Container.composeWidget()` 的包装顺序保持逐字不变**(已核对与 Flutter `Container.build` 一致):`Align → Padding → ColoredBox → ClipPath → DecoratedBox(BACKGROUND) → DecoratedBox(FOREGROUND) → ConstrainedBox → Padding(margin) → Transform`。9 处 `.bind(current)` 改为 `attach` 形式,`current` 可为 null 时用 `?.let` 显式化。
12. **编译期防线用"负例清单 + 一次性验证"**,不新增依赖、不新增 Gradle 模块、不引入 kotlin-compile-testing。
13. **叶子 Widget 不再写 `content = {}`**:`RawImage` / `ProviderImage` / `CachedNetworkImage` / `RichText` / `ImageEmoji` 的 DSL 函数体在批次 2 简化为"构造 + attach"。
14. **挂载时序**:批次 2 的 DSL 函数体采用先配置后挂载(`attach(X(...).apply(content))`)。这与今天"先挂载后配置"的顺序不同,但不可观测:content 的接收者是新建的那个 Widget,改不到父节点的槽位状态;而 `parent` 只在 `createRenderBox()` 阶段被读取,那时整棵树已建好。以 228 例全量测试作为回归证据。

## 关键设计

### 新类型:ChildSlot

`core/src/main/kotlin/com/muedsa/snapshot/widget/ChildSlot.kt`(新文件)

```kotlin
package com.muedsa.snapshot.widget

/**
 * 拥有子节点槽位的 Widget。
 *
 * DSL 函数(Padding / Row / Stack …)以此为接收者,因此"在没有子槽位的 Widget 上挂子节点"
 * 是编译错误,而不是运行时异常。
 *
 * [attach] 是全项目唯一的挂载入口:它把 [child] 放进本节点的子槽位,并把 [Widget.parent]
 * 指向本节点。它允许重新挂载(不校验 `child.parent == null`),因为
 * `Container.composeWidget()` 会把已挂在 Container 上的子节点重新挂到新建的包装节点上。
 */
interface ChildSlot {
    fun attach(child: Widget)
}
```

### 三个基类的实现

```kotlin
// SingleChildWidget.kt(批次 2 起构造函数不再有 parent 参数)
abstract class SingleChildWidget : Widget(), ChildSlot {
    var child: Widget? = null
        protected set                    // 唯一写入点是本类的 attach
    override fun attach(child: Widget) {
        check(this.child == null) { "${this::class.simpleName} 已经有子节点, 不能重复挂载" }
        this.child = child
        child.parent = this
    }
}
```

- `MultiChildWidget.attach` = 现有 `appendChild` 的重复校验 + 赋值 + `child.parent = this`
- `ProxyWidget.attach` = `check(widget == null)` + 赋值 + `child.parent = this`

`child` / `widget` 的 setter 收为 `protected`:顶层函数(如批次 1 的 `buildChild`)与顶层内联函数都无权写 protected 成员,这从语言层面保证写入只能发生在 `attach` 内。

### Widget.parent

```kotlin
abstract class Widget {
    var parent: Widget? = null
        internal set          // 对外只读;不再做 O(深度) 祖先遍历
}
```

### DSL 函数形态(以 Padding 为例)

批次 1——签名与调用点**完全不变**,`buildChild` / `bind` 内部退化为薄封装:

```kotlin
inline fun <T : Widget> Widget.buildChild(widget: T, content: T.() -> Unit) {
    val slot = this as? ChildSlot
        ?: throw IllegalStateException(
            "${this::class.simpleName} 没有子节点槽位, 不能挂载 ${widget::class.simpleName}"
        )
    slot.attach(widget)
    widget.content()
}
```

`bind` 用同一模式改造(仍是 `child: Widget?`,null 时 no-op,签名不变)。

批次 2——接收者改 `ChildSlot.`,薄封装删除:

```kotlin
inline fun ChildSlot.Padding(padding: EdgeInsets, content: Padding.() -> Unit = {}) {
    attach(Padding(padding = padding).apply(content))
}
```

### 需要连带改造的四处内部调用点(批次 2)

```kotlin
// ① Container.composeWidget():9 处 .bind(current) —— current 可为 null
Padding(padding = snapshotPadding).apply { current?.let { attach(it) } }

// ② RichText.init:删掉手写的 parent
WidgetSpan.extractFromInlineSpan(text).forEach { attach(it) }

// ③ WidgetSpan.extractFromInlineSpan()
WidgetSpanParentDataWidget(span = span).apply { attach(span.child) }

// ④ WidgetParser.createWidgetForChildElement()
val slot = widget as? ChildSlot ?: error("${widget::class.simpleName} 没有子槽位")
children.forEach { slot.attach(it.createWidget()) }
```

## 行为变更(需在 PR 描述中明确提示)

1. **单子节点父节点的重复挂载:静默覆盖 → 报错。** 仓库内自查:`Container.composeWidget`、`RichText.init`、parser(`ContainerMode.SINGLE` 保证至多 1 个子节点)都不会触发。
2. **根级写多个 Widget(`layoutWidget { A; B }`):静默丢弃 A → 报错。** 原本写错却能出图的代码会开始抛异常。
3. **`bind(null)` 的静默 no-op 消失**,内部调用点改为显式 `?.let`。

三条都属于"把静默错误变成显式失败",与目标一致。

## 分批落地

### 批次 1 — 原语统一 + 编译期防线(分支 `refactor/widget-dsl-guard`)

差异小、零调用点改动、可独立合并。

| 改动 | 文件 |
|---|---|
| 新增 `ChildSlot` 接口 | 新 `widget/ChildSlot.kt` |
| 三个基类实现 `attach`(含校验与 parent 写入) | `SingleChildWidget.kt`、`MultiChildWidget.kt`、`ProxyWidget.kt` |
| 新增 `@SnapshotWidgetDsl` 标注并加在 `Widget` 上 | `widget/Widget.kt` |
| `buildChild` / `bind` 退化为 `attach` 薄封装(签名不变) | `widget/Widget.kt` |
| `parent` 去遍历 + `internal set` | `widget/Widget.kt` |
| 新增单测:三类父节点的重复挂载 | 新 `core/src/test/.../widget/ChildSlotAttachTest.kt` |

该测试**直接调用 `ChildSlot.attach`**(而非 `buildChild`),因为 `attach` 从批次 1 起就是公共 API,这样批次 2 删除 `buildChild` / `bind` 时该测试无需改动。

### 批次 2 — 类型化子槽位(分支 `refactor/widget-dsl-childslot`,基于批次 1 合并后的 main)

大 diff、纯机械。

约 30 个 DSL 函数改接收者 `Widget.` → `ChildSlot.`;删除 `buildChild` / `bind`;约 35 个 Widget 类删除 `parent` 构造参数;8 处入口签名改 `ChildSlot.() -> Unit`;`ProxyWidget.buildWidget` 收 `@PublishedApi internal`;上文"四处内部调用点"改造;`Element.kt` 删未用 import;`docs/usage/README.md` §4.1 同步。

### 批次 3 — 内部清理(分支视需要另开)

`SingleChildWidget.createRenderBox` 的 `renderBox.child!!` → `checkNotNull` 带消息;`MultiChildWidget` 的 `renderBox.children.size == currentChildren.size` 静默跳过 → 显式报错;`appendChild` 去 `@Synchronized` 与 O(n²) 的 `contains`;`children` 的快照/活视图契约;`content` 加 `crossinline` 关闭非局部 return。

### 批次 4 — 另立 spec

删除 `Widget.parent` 字段,`RenderImageEmoji` 的 `rootSpan` 改由 `RichText.createRenderBox()` 显式注入。

## 涉及文件

批次 1:

- Add:`core/src/main/kotlin/com/muedsa/snapshot/widget/ChildSlot.kt`
- Modify:`core/src/main/kotlin/com/muedsa/snapshot/widget/Widget.kt`
- Modify:`core/src/main/kotlin/com/muedsa/snapshot/widget/SingleChildWidget.kt`
- Modify:`core/src/main/kotlin/com/muedsa/snapshot/widget/MultiChildWidget.kt`
- Modify:`core/src/main/kotlin/com/muedsa/snapshot/widget/ProxyWidget.kt`
- Add:`core/src/test/kotlin/com/muedsa/snapshot/widget/ChildSlotAttachTest.kt`

批次 2(在上述基础上):

- Modify:`core/src/main/kotlin/com/muedsa/snapshot/widget/*.kt`(约 32 个 DSL 文件)
- Modify:`core/src/main/kotlin/com/muedsa/snapshot/widget/text/WidgetSpan.kt`、`text/RichText.kt`、`text/ImageEmojiSpan.kt`
- Modify:`core/src/main/kotlin/com/muedsa/snapshot/Snapshot.kt`
- Modify:`testkit/src/main/kotlin/com/muedsa/snapshot/TestKit.kt`
- Modify:`parser/src/main/kotlin/com/muedsa/snapshot/parser/Element.kt`、`parser/widget/WidgetParser.kt`
- Modify:`core/src/test/.../BackdropFilterTest.kt`、`LogoCreator.kt`(helper 接收者改 `ChildSlot.`)
- Modify:`docs/usage/README.md`(§4.1)

## 非目标

- 不删除 `Widget.parent` 字段(批次 4)。
- 不改 parentData 的既有行为(`!!`、静默跳过守卫)——批次 3。
- 不引入 `kotlin-compile-testing`、不新增 Gradle 模块、不加覆盖率工具。
- 不改 `Container.composeWidget()` 的包装顺序,不改约束/布局/绘制语义。
- 不动文本子系统的字号解析逻辑(`RenderImageEmoji.findFontSize` 的 BFS 保持原样)。
- 不做与本主题无关的重构(如 `LayoutTree`、`Performance`、网络缓存)。

## 验证方法

基线(开工前已确认):`.\gradlew.bat test --console=plain` → BUILD SUCCESSFUL(1m08s),73 文件 / 228 例。

编译期防线的一次性验证(每批各做一次):

1. 临时新建 `core/src/test/kotlin/com/muedsa/snapshot/widget/_NegativeScratch.kt`(**不入库**);
2. 写入负例 → `.\gradlew.bat :core:compileTestKotlin --console=plain`;
3. 断言每条负例各对应一条 `error:`,把编译器原始输出贴进 PR 描述;
4. 删除该文件 → 重跑同一命令确认恢复绿 → `git status` 确认干净。

**必须编译失败的负例**:

| # | 代码(①② 包在 `layoutWidget { … }` 内,③ 是完整函数) | 生效批次 |
|---|---|---|
| ① | `layoutWidget { Stack { Row { Positioned(left = 0f) { } } } }` | 批次 1 |
| ② | `layoutWidget { Row { Padding(padding = EdgeInsets.all(8f)) { Expanded { } } } }` | 批次 1 |
| ③ | `fun Widget.badHelper() { Padding(padding = EdgeInsets.all(8f)) { } }` | 批次 2 |

**必须仍然编译通过的正向清单**(每批都跑):

- `layoutWidget { Row { Column { Expanded { Container(width = 1f, height = 1f) } } } }`(最近的 `Flex` 胜出)
- `layoutWidget { Stack { Positioned(left = 0f) { Container(width = 1f, height = 1f) } } }`
- `layoutWidget { Container(width = 10f, height = 10f) { Row { Container(width = 1f, height = 1f) } } }`
- `layoutWidget { RichText { TextSpan(style = TextStyle(fontSize = 20f)) { ImageEmojiSpan(provider = { image }, width = 20f, height = 20f) } } }`
  (注意:内嵌表情的 DSL 名是 `TextSpan.ImageEmojiSpan`——**不存在** `ImageEmoji` DSL 函数;`RichText { }` 与 `TextSpan { }` 的接收者都是 `TextSpan`,不是 Widget)
- 仓库内既有 helper(`LogoCreator.logoContent`、`BackdropFilterTest.cornerStack`)改动接收者后仍可编译

## 风险

1. **`@DslMarker` 可能让现有代码编译不过。** 规则是"最近的接收者胜",现有测试/sample 的嵌套写法(`FlexibleTest.kt:43-54`、`LogoCreator.kt:38-62`、`BackdropFilterTest.kt:24-32`)静态推断均为"最近者即意图",但**尚未编译验证**。批次 1 的首要验收就是全量编译通过;若出现反例,逐个核对是"写法本来就错"还是"需要放宽"。
2. **批次 2 的面较大**(约 30 个 DSL 函数 + 约 35 个构造参数),但改动是机械的,且批次 1 已把运行时语义锁定,失败面被限制在签名层。
3. **`protected set` 迫使写入只能走 `attach`**:若某个子类需要直接改 `child`,会编译失败——这是期望的,但需要在实施时逐个确认(当前仓库内无此需求)。
4. **公开面变化**:`ChildSlot` 为新增公共类型;批次 2 删除 `buildChild` / `bind`、把 `ProxyWidget.buildWidget` 收为 `@PublishedApi internal`。下游(demo / taffy-pvp-card-sw)若使用了这些半公开 API 需跟进。`docs/usage/README.md` 只记录了 `Snapshot*` 入口与 `ProxyWidget` 的概念,不涉及这些函数。

## 参考

- `docs/usage/README.md` §4.1(三类父节点表格,需同步)
- [KEEP-57: Scope control for implicit receivers](https://github.com/Kotlin/KEEP/blob/745ef12e2940649f515d966ba6e94b8494e76bf6/proposals/scope-control-for-implicit-receivers.md) —— `@DslMarker` 的标记与遮蔽规则
- `docs/superpowers/specs/2026-09-08-test-ci-hygiene-design.md`(本仓库设计稿格式样板)
