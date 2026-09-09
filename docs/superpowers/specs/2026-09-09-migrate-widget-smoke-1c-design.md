# 旧渲染冒烟迁移 Batch-1c 设计(parse-render 对齐矩阵 + ColorFiltered)

日期:2026-09-09
状态:设计(做法已与用户逐节确认)

## 背景

任务 1(全量旧渲染冒烟 → testkit 断言/golden)第三批。Batch-1a(PR #99)与 Batch-1b(PR #100)已合入 main;1b 顺带修复了 `RenderDecoratedBox` 子盒重复绘制的真实 bug。

本批处理 **parse-render 三件套 + ColorFiltered** 四个文件:

- `widget/ContainerParserTest.kt`(1 场景)、`widget/StackParserTest.kt`(17 对齐 × 3 方向 = 51 场景)、`widget/RowParserTest.kt`(crossAxisAlignment 5 档 + 1 个字体基线用例)、`widget/ColorFilteredTest.kt`(2 场景)。

它们的共同点是**零字体、零网络**(ColorFiltered 现内容为外网 owl 图,本批换本地几何),因此可以走与 1a/1b 相同的"确定可复现"路线:布局/采样断言为主,锐边几何辅以 golden。

**收益说明**:旧冒烟的产物落在 `build/test-results/test-image-outputs/`(不入库、不脏仓库),真正的问题是**零断言**——本批把它们换成会真正失败的断言。

## 目标与完成标准

- [ ] 四文件重写替换:去 `println`/`drawWidget`/debug 文本;外网内容 → 本地几何;
- [ ] Stack 的 51 个对齐场景由**不变量/对称性断言**覆盖 + 代表档硬编 + 3 张代表档 golden;
- [ ] `RowParserTest.baseline_test`(字体基线)本批删除,归字体批次;
- [ ] `ColorFilteredTest` 用语义期望色值 + 孪生互比,不上 golden;
- [ ] `./gradlew test --no-build-cache` 与 `./gradlew jar --no-build-cache` 通过;`git status` 干净;四文件无 `println`/`drawWidget`/`org.junit`/网络 import。

## 已确认做法决策

1. **本批范围**:parse-render 三件套 + ColorFiltered。字体类与网络类各自留批(见"非目标")。
2. **`RowParserTest.baseline_test`**:本批删除。它当前只写 PNG 不断言,删除不丢真实覆盖;字体基线语义属字体批次。
3. **Stack 期望值来源**:**不变量/对称性断言为主**,不与实现同源(避免照实现抄期望值导致同错不报),辅以少量代表档硬编坐标。
4. **ColorFiltered**:几何内容 + 语义期望色值断言 + 孪生互比,**不上 golden**(色滤合成跨平台光栅化有漂移风险)。
5. 沿用 1a/1b 规则:场景抽 builder、kotlin.test、清晰 bug 最小修复并记录、模糊只注释"待议"。

## 逐文件改动

### 1. `widget/ContainerParserTest.kt`(重写)

- 场景保留:`Container(width = 300f, height = 300f)`,无 `color`(不绘制背景)。
- 断言:`rootLayout { … }.assertSize(300f, 300f)`;`snapshotPixels` 整幅透明(`expectRegionTransparent` 覆盖全画布)。
- golden:`widget/container/sized`(纯矩形锐边)。
- 文件名 `*ParserTest` 是历史命名(实际测 widget `Container`,非 parser 模块),本批**不改名**,避免无关 diff。

### 2. `widget/StackParserTest.kt`(重写)

场景抽 `stackScene(alignment, direction)` builder:三个半透明色块(RED / YELLOW / GREEN,`Color.withA(..., 128)`,尺寸 200×80 / 50×150 / 100×100)。保留原 17×3 遍历。

- 每场景 `rootLayout` 取根,断言 Stack 尺寸(预期 200×150,**实测校准**后写死并注释),按 `children` 顺序取三个子盒。
- **不变量断言(全 51 档,17 档按性质分两组,合计 17)**:
  - **方向无关组(11 档)**:`AlignmentDirectional.{TOP,CENTER,BOTTOM}_CENTER`(3)+ `AlignmentDirectional.CENTER`(1)+ `BoxAlignment.*`(8),在 LTR/RTL 下矩形**逐点相同**;
  - **镜像组(6 档)**:`AlignmentDirectional.{TOP,CENTER,BOTTOM}_{START,END}` 的 RTL 矩形 == 同档 LTR 的**水平镜像**(`x' = W - x - w`,y 不变);
  - 三个子盒均落在 Stack 内(`0 <= x`、`x + w <= W`,y 同理)。
- **代表档硬编(3 档,实测校准)**:`AlignmentDirectional.TOP_START`(LTR) 贴左上、`AlignmentDirectional.CENTER` 居中、`AlignmentDirectional.BOTTOM_END`(LTR) 贴右下。
- golden:代表档 3 张,`widget/stack/alignment_{top_start,center,bottom_end}`。半透明纯色叠加是确定的(`OpacityTest` 先例),可以上 golden。

### 3. `widget/RowParserTest.kt`(重写,删除字体用例)

- **删除 `baseline_test`**(含 `RichText`,字体基线归字体批次)。
- `crossAxisAlignment_test` 重写为 5 档(`CrossAxisAlignment.entries`),场景 builder 复用:Row 内三个纯色盒 100×100 / 300×300 / 200×200;`STRETCH` 档外套 `LimitedBox(1000f, 1000f)`(沿用原结构)。
- 断言:
  - main 轴位置恒为 0 / 100 / 400;
  - cross 轴按档位:`START` → y=0;`END` → `y = rowH - h`;`CENTER` → `(rowH - h) / 2`;`STRETCH` → 三者等高(== rowH);`BASELINE` → **全部 y=0**。
  - `BASELINE` 的依据:`RenderFlex.kt:264` 对 `getDistanceToBaseline(...) == null` 的子盒取偏移 0,与 `CrossAxisAlignment.BASELINE` 文档"Children who report no baseline will be top-aligned."一致;纯色 `Container` 无基线,故该档是确定性可断言的。
  - **`rowH` 逐档实测校准**:非 `STRETCH` 档 Row 高度 = 最高子盒 300;`STRETCH` 档被 `LimitedBox` 撑到 maxHeight 1000,故各档 `rowH` 不同,期望值按档计算而非全局常量。
- golden:`widget/row/cross_axis_center` 1 张。

### 4. `widget/ColorFilteredTest.kt`(重写)

- 内容由外网 owl 图 → 本地 **6 色块网格**(红/绿/蓝/青/品红/黄),`colorFilteredScene(filter)` builder 供孪生复用。
- `red_modulate`: `ColorFilter.makeBlend(Color.RED, BlendMode.MODULATE)` 语义为逐通道相乘 → 期望每块中心 `(R, 0, 0)`;逐块采样断言。
- `gray_saturation`: `ColorFilter.makeBlend(0xFF9E9E9E.toInt(), BlendMode.SATURATION)` → 饱和度置 0;期望"三通道相等"的灰度,**具体数值先实测校准**再定容差;若公式不稳定则退化为弱不变量并注释。
- 孪生互比:同 builder 有/无滤镜各渲染一次,断言至少一个采样点像素不同。
- **不上 golden**。

## 规则(与 1a/1b 一致)

场景抽 builder 供复用;优先数值/采样(快),锐边几何加 golden;文字/外网内容一律换几何;坐标与色值以**实际渲染实测**校准并注释;清晰 bug 最小修复并记录,模糊注释"待议"。

## 非目标

- **字体类**:`RowParserTest.baseline_test`(本批删除)、`widget/text/TextTest`、`paint/TextPainterTest`、`paint/text/TextMetricsTest`;
- **网络类**:`widget/CachedNetworkImageTest`、`tools/Simple*NetworkImageCacheTest`(`@Tag("network")` 隔离与 `org.junit` 统一);
- **其它**:`render/flex/RenderFlexTest`(含 `Random`,需先定性)、`paint/gradient/*`(疑为刻意的 artifact 生成器,需先确认意图)、`render/layer` 包;
- 不改产品代码(除非发现清晰 bug)。

## 风险与处理

1. **坐标/尺寸需实测校准**:Stack 尺寸(预期 200×150)、`rowH`、代表档偏移,先临时打印实测再写死。
2. **不变量断言失败**:先判定是实现 bug 还是语义理解偏差——清晰 bug → 最小修复 + 回归断言并记录;模糊 → 不改、注释"待议"。
3. **SATURATION 期望公式**:先实测;不稳则退化为"三通道相等"。
4. **golden 基准**:代表档先 `-PsnapshotTest.mode=record` 生成一次,再 verify 跑绿,基准与测试同提交。

## 验证标准

- `./gradlew test --no-build-cache` 全绿;`./gradlew jar --no-build-cache` BUILD SUCCESSFUL;
- `git status` 干净;
- grep 四文件无 `println` / `drawWidget` / `org.junit` / 网络 import;
- `git log main..HEAD` 全部 `%G?` 为 `G`。

## 交付方式

分支 `test/migrate-widget-smoke-1c`,base = `origin/main`(`68907d2`,含已合并的 1b)。提交顺序:**docs 先行**(本 spec → 实现计划),再按文件分组提交实现。PR 文案备好。

## 参考

- 目标文件:`core/src/test/kotlin/com/muedsa/snapshot/widget/{ContainerParserTest,StackParserTest,RowParserTest,ColorFilteredTest}.kt`
- 模板:`OpacityTest`(1a)、`ImageFilteredTest`/`BackdropFilterTest`(1b)、`docs/testing/README.md`
- 前序设计:`docs/superpowers/specs/2026-09-08-migrate-widget-smoke-1a-design.md`、`…-1b-design.md`
