# 测试内置字体设计(跨平台确定性文本度量)

日期:2026-09-09
状态:设计(做法已与用户确认)

## 背景

字体类测试迁移(分支 `test/font-test-migration`,PR #104)在 **Windows 本机全绿、Linux CI 失败**:

```
TextPainterTest > textAlign_ltr_test() FAILED        (TextPainterTest.kt:66)
TextPainterTest > cn_font_size_monotonic_test() FAILED (TextPainterTest.kt:162)
```

这两条恰好是本批**唯一两条隐含字形/字体假设**的断言:前者依赖"末尾字形的右侧留白 < 2px",后者依赖"机器装有可缩放的 CJK 字体"。其余断言(区间、英文单调、`top + baseline` 等式、墨迹存在性、HeightMode 序关系)在 CI 全部通过——**根因是文本度量依赖 OS 字体**,而非实现缺陷。

仓库既有策略(`docs/testing/README.md` §7)是"只做区间/单调断言、不锁字形",但事实证明:**连"关系断言"也会因字体差异闪断**(对齐的墨迹边界取决于 side bearing)。

## 目标与完成标准

- [ ] 仓库内置一个**覆盖拉丁与 CJK** 的测试字体,由 `:testkit` 提供加载入口;
- [ ] 所有文本测试显式指定该字体,使度量跨平台一致;
- [ ] 两条 CI 失败断言修复,且中文单调用例**保留**(不再删除);
- [ ] `./gradlew test` 全绿;文档同步(手册 §7 + 字体迁移 spec)。

## 已确认做法决策

1. **字体选型**:`Noto Sans SC`(可变字体,17.8MB,OFL-1.1,含完整中文字形 + 拉丁),来源 `google/fonts` 仓库;随仓附带 `OFL.txt`。用户明确:一个字体覆盖多语种,体积无所谓。
2. **放置与加载**:放 `testkit/src/main/resources/fonts/`,由 `:testkit` 暴露 `testTypeface: Typeface`。理由:`core`/`parser` 测试都依赖 `:testkit`,后续新测试零成本复用;`:testkit` 未发布到 Maven(根构建无 publishing 配置),不会把大包发出去。
3. **适用范围**:**所有文本测试**显式用该字体(`TextTest`/`TextPainterTest`/`TextMetricsTest`/`RowParserTest` 的文本用例),而非只修两条失败断言——否则同类测试仍会零星闪断。
4. **断言形态不变**:仍是区间/单调/关系断言,但因为度量确定,容差可以收紧并有据可依。

## 实现要点

### 1. 资产

```
testkit/src/main/resources/fonts/NotoSansSC.ttf      (17,772,300 B)
testkit/src/main/resources/fonts/NotoSansSC-OFL.txt  (OFL-1.1)
```

### 2. 加载入口(`testkit/src/main/kotlin/com/muedsa/snapshot/TestFonts.kt`)

```kotlin
val testTypeface: Typeface by lazy {
    val stream = checkNotNull(...getResourceAsStream("/fonts/NotoSansSC.ttf")) { "缺少测试字体资源" }
    val bytes = stream.use { it.readBytes() }
    checkNotNull(FontMgr.default.makeFromData(Data.makeFromBytes(bytes))) { "测试字体加载失败" }
}
```

注:skiko 的 `Typeface` **没有** `makeFromData`;须走 `FontMgr.default.makeFromData(Data)`(实测)。

### 3. 测试改动

| 文件 | 改动 |
|---|---|
| `paint/TextPainterTest.kt` | 所有 `TextStyle` 加 `typeface = testTypeface`;`cn_font_size_monotonic_test` 保留并改用该字体(去掉 `fontFamilies`) |
| `widget/text/TextTest.kt` | 同上;`widget_span_test` 的蓝像素下界按新度量复测 |
| `paint/text/TextMetricsTest.kt` | 同上(保持宽松区间,但度量确定) |
| `widget/RowParserTest.kt` | 基线用例的 `RichText` 样式加 `typeface`,使 `top + baseline` 等式精确成立 |
| `emoji_test` | **不加** `typeface`(Noto Sans SC 不含 emoji 字形,加了反而会在无 emoji 字体的 CI 上失去墨迹);断言降为"尺寸 > 0 + 可产出 artifact",并注释说明 emoji 字形来自 OS |

### 4. 探针实测(内置字体,Windows)

- `Noto Sans SC` 加载成功,拉丁与 CJK 均有墨迹;
- 对齐(`layout(300,300)`,文本 `Hello Word!`):`LEFT [1,72]`、`CENTER [114,185]`、`RIGHT [227,298]`、`JUSTIFY/START [1,72]`、`END [227,298]`;
- 中文单调:fontSize 5..40 时 `maxIntrinsicWidth` 严格递增(37.08 / 44.50 / 51.92 / …)。

> 断言容差按上表实测值留 2px 余量(如 RIGHT 右边界 ≥ 296)。

### 5. Linux 度量量化(第二轮 CI 失败与修订)

内置字体修复了对齐断言,但 CI 仍在 `cn_font_size_monotonic_test` 失败:

```
fontSize=29 的中文 maxIntrinsicWidth(169.0) 应大于上一档(169.0)
```

两值**完全相等**,且为**整数**(本机为 37.08/44.50 等小数)——**Linux 上 skia 把字形推进量化到整数像素**,故 fontSize=28 与 29 宽度相同。结论:**"逐档严格递增"不是文本度量的真实不变量**。

修订为**"随字号非降 + 端点严格递增"**:前者容忍逐档舍入,后者仍能抓住"宽度不随字号增长"这一原始缺陷(若度量不随字号变化,`widths.last() > widths.first()` 必失败)。英文单调用例同样脆弱,一并改写。

## 验证标准

- `./gradlew test --offline` 全绿(本机);
- **CI(Linux)必须绿**——这是本批的验收条件,推送后需确认;
- `git status` 干净;`drawPainter` 仍仅出现在 artifact 用例。

## 风险与处理

1. **资产体积**:17.8MB 入仓。用户已确认可接受;若将来要瘦身,可换子集化字体(需工具链)。
2. **emoji**:OS 字体依赖未消除,`emoji_test` 断言相应弱化并注释,不做假断言。
3. **skiko 升级**:字形栅格化若变化,收紧的容差可能需复测;注释中已写明实测来源。

## 非目标

- 不做文本 golden(栅格化跨平台仍可能不同);
- 不引入 emoji 字体;
- 不改 `TextMetricsTest` 之外的既有断言口径。

## 参考

- 手册 `docs/testing/README.md` §7(文本类怎么测);
- 字体迁移 spec `docs/superpowers/specs/2026-09-09-font-test-migration-design.md`;
- CI 失败:`TextPainterTest.kt:66`、`:162`。
