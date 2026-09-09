# 字体类测试迁移设计(区间/单调/关系断言 + artifact)

日期:2026-09-09
状态:设计(做法已与用户逐节确认)

## 背景

任务 1(全量旧渲染冒烟 → testkit 断言/golden)的**字体类**批次。1a/1b/1c 已合并(PR #99/#100/#101),网络类隔离已合并(PR #103)。

字体测试的特殊性:文本测量受 OS 字体/排版引擎影响,**跨机不可逐像素复现**,故不能进 golden。仓库手册 `docs/testing/README.md` §7 已定调——**数值区间/单调断言 + artifact 人眼检视**,参照实现是已迁移的 `paint/text/TextMetricsTest`(E5)。

本批处理两个仍为零断言的旧冒烟文件,并重建 1c 删除的 Row 基线覆盖:

| 文件 | 用例数 | 现状 |
|---|---|---|
| `widget/text/TextTest.kt` | 4 本地(+1 已标 `network`) | 全 `drawWidget` + `println`,零断言 |
| `paint/TextPainterTest.kt` | 6 | 全 `drawPainter` + `println`,零断言 |
| `widget/RowParserTest.kt` | — | 1c 删除了含 `RichText` 的 `baseline_test`,字体基线覆盖缺失 |

## 目标与完成标准

- [ ] `TextTest` 4 个本地用例重写为**字体无关**断言(尺寸区间 + 墨迹存在性);
- [ ] `TextPainterTest` 5 个用例重写为**关系/单调**断言;`drawLocalFontListSample` 改打 `@Tag("sample")`;
- [ ] `RowParserTest` 新增 1 个基线关系用例(两段文本基线真对齐);
- [ ] **两个重写文件**各保留 1 个 artifact 用例,`drawPainter` 仅用于此且带"仅人眼检视"注释;
- [ ] `./gradlew test` 全绿;`git status` 干净;三个改动文件无 `println`/`drawWidget`。

## 已确认做法决策

1. **策略沿用手册 §7**:宽松区间 / 单调 / 关系断言 + artifact 目检;不上 golden、不锁字形像素。每条断言刻意做成字体无关——要么正负/量级,要么"随 X 单调",要么"两端都实测的等式"。
   > **后续修订(2026-09-09)**:本批合并后 CI(Linux)在 `textAlign_ltr_test` 与 `cn_font_size_monotonic_test` 上失败——证明"关系断言"仍会因 OS 字体差异闪断(对齐的墨迹边界取决于 side bearing)。故**追加内置字体**(`docs/superpowers/specs/2026-09-09-test-font-bundling-design.md`),所有文本测试显式指定 `testTypeface`,度量跨平台一致。原"不引入内置字体"的决策已被该 spec 取代。
2. **`drawLocalFontListSample` 改打 `@Tag("sample")`**:它遍历 `FontMgr.default` 全部字体族逐个排版,内容与耗时随本机字体数变化,断言只能退化为"字体数 > 0";归为人工触发的 artifact 生成器。
3. **Row 基线覆盖重建为关系断言**(见下),不锁具体基线值。

## 逐用例断言设计

### `widget/text/TextTest.kt`

| 用例 | 断言 |
|---|---|
| `simple_text_test` | `rootLayout{Text("Hello, world!", fontSize=20f, color=RED)}` → 宽 > 0、高 ∈ 1..3em;`snapshotPixels(background = TRANSPARENT)` 后存在不透明像素(文字确实画出来了) |
| `text_span_test` | 三个 span(默认色/红/默认色)→ 尺寸区间 + 画布上**红墨迹与黑墨迹同时存在**(各 span 样式分别生效) |
| `widget_span_test` | 内嵌 `WidgetSpan(20×20)`、`WidgetSpan(30×30)` 蓝块 → 尺寸区间 + 全画布**纯蓝像素数 ≥ 800**(实测 1202;两个盒子理论共 1300,差额来自行高裁剪与抗锯齿。下界取 800 仍可区分"只画出 20×20 盒"的 ~400 量级) |
| `style_merge_test` | 15→30 覆盖 + 子 span RED → 尺寸区间 + **红墨迹与白墨迹(alpha>0)都存在**(合并/覆盖都生效)。原用例画在白底上白字不可见,迁移后用透明底 |
| `text_samples_artifact` | 4 个场景合成一张图,`drawPainter` + "仅人眼检视"注释 |

`image_emoji_test` 已由网络批次打 `@Tag("network")`,**本批不动**。

### `paint/TextPainterTest.kt`

| 用例 | 断言 |
|---|---|
| `textAlign_ltr_test` | 用**墨迹边界**(测试内私有 `Pixmap.inkBounds()`:扫描非透明像素的最左/最右列)做关系断言——`Alignment.RIGHT` 的右边界 ≈ 画布宽(实测 299 vs 300)、`CENTER` 的墨迹中心 ≈ 画布中点(实测 149.5 vs 150)、`LEFT` 的左边界 ≈ 0(实测 1),且左边界 `LEFT < CENTER < RIGHT` 单调 |
| `textAlign_rtl_test` | 同上,RTL 下左右语义镜像 |
| `heightModel_test` | 各 `HeightMode` 高度均 > 0;`ALL` 严格大于 `DISABLE_ALL`(实测 150 vs 141),且 `ALL` 不小于其余两档、`DISABLE_ALL` 不大于其余两档 |
| `emoji_test` | 尺寸 > 0 + 墨迹存在;若本机 emoji 字体缺失导致无墨迹,退化为"尺寸 > 0"并注释(不伪造断言) |
| `en_font_size_test` | fontSize 5..40 递增 → `maxIntrinsicWidth` **严格递增**、`height` 非降 |
| `cn_font_size_test` | 同上(CN 文本若渲染成 tofu,单调性仍成立) |
| `drawLocalFontListSample` | 改打 `@Tag("sample")`,内容不变 |
| `text_painter_artifact` | 保留一个 artifact 用例(对齐/高度模式各一行),`drawPainter` + 注释 |

**探针实测约束(必须遵守,否则断言不可观测或直接失败)**:

- `textAlign` 仅在 **`layout(boxWidth, boxWidth)`**(minWidth 与 maxWidth 都给)时可见;若只给 maxWidth,段落宽等于内容宽,六种对齐的墨迹完全相同(实测均为 `[1,5,72,14]`)。
- `HeightMode` 仅在 `TextStyle` 带**行高倍数**(如 `height = 1.5f`)时可区分;默认倍数下四档高度完全相同(实测单行与多行均为同一值)。

### `widget/RowParserTest.kt`(基线覆盖重建)

两个 `RichText`(fontSize 20 / 40)置于 `Row(crossAxisAlignment = BASELINE, textBaseline = ALPHABETIC)`:

1. 大字号子盒 `top` 更小、`height` 更大(ascent 更大);
2. 两子盒 `top` 与 `bottom` 都**不相等**(排除退化为 START/END 语义);
3. **基线真对齐**:用 `TextPainter.computeDistanceToActualBaseline(ALPHABETIC)` 独立算出两段文本的基线距离,断言 `topₐ + baselineₐ ≈ top_b + baseline_b`(两端都实测,字体无关)。

## 风险与处理

1. **关系/容差已由探针实测校准**(探针跑完即删):textAlign 的墨迹关系、HeightMode 的区分条件与高度关系、蓝色像素下界 800——均见上表与"探针实测约束"。
2. **emoji 可渲染性**:探针实测本机有墨迹(`[3,4,142,38]`),断言按"墨迹存在"写;若将来换机无墨迹则退化并注释。
3. **CN 字体缺失**:渲染成 tofu 时单调性仍成立,不影响。
4. **`drawPainter` 的唯一合法用法**:仅 artifact 用例,必须带"仅人眼检视"注释;静态核对按此口径。

## 验证标准

- `./gradlew test --offline` 全绿;`./gradlew jar --offline` BUILD SUCCESSFUL;
- `git status` 干净;
- grep 三个改动文件:无 `println` / `drawWidget`;`drawPainter` 仅出现在 artifact 用例且邻近有注释;
- `./gradlew :core:test -PincludeSamples` 能跑到 `drawLocalFontListSample`(可选)。

## 非目标

- 不引入内置字体、不建文本 golden;
- `image_emoji_test`(已标 `network`)不改;
- `paint/gradient/*`、`render/flex/RenderFlexTest`、`render/layer` 包(各自后续批)。

## 交付方式

分支 `test/font-test-migration`,base = `main`(`8c76521`)。提交顺序:docs 先行(本 spec → 实现计划),再按文件分组提交。PR 文案备好。

## 参考

- 手册:`docs/testing/README.md` §7(文本类怎么测)、§8(artifact);
- 参照实现:`core/src/test/kotlin/com/muedsa/snapshot/paint/text/TextMetricsTest.kt`;
- 前序:`docs/superpowers/specs/2026-09-09-migrate-widget-smoke-1c-design.md`、`2026-09-09-network-test-isolation-design.md`。
