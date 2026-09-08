# 旧渲染冒烟迁移 Batch-1a 设计(widget 确定性)

日期:2026-09-08
状态:设计(范围与形态已与用户确认:widget 确定性冒烟、重写替换)

## 背景

`testkit`(golden 三态/数值/采样)已合入 main。core widget 包仍有一批"只画 PNG 不比对"的旧冒烟测试(带 `println`、`drawWidget`+debug 文本)。本批为任务 1(全量渲染冒烟迁移)的 **Batch-1a**,先把**简单确定性**的 4 个文件走通"重写替换"闭环,确立迁移模板;DecoratedBox/ImageFiltered/BackdropFilter 等含字体/模糊/网络的较重建模归 Batch-1b。

## 目标与完成标准

把下列旧冒烟**重写替换**为 testkit 断言/golden,去除 `println` 与仅作人眼的 debug 文本;文字/网络内容在重写时替换为确定性图形。完成标准:
- [ ] 相关测试用 `golden`/`snapshotPixels`/`rootLayout` 等真实断言;旧 `drawWidget`+println 冒烟移除;
- [ ] 每个 golden 场景 record→verify 通过;基准图与测试同提交;
- [ ] `./gradlew test`(core 等)与 `./gradlew jar` 通过;
- [ ] 无 org.junit 混入(沿用 kotlin.test)。

## Batch-1a 范围与逐文件处理

### 1. `widget/OpacityTest.kt`(重写)
现 3 场景:Container 300×300(YELLOW, center)内 `Opacity(opacity)`(0/0.5/1)包 Container 200×200(GREEN)。
处理:
- 去掉 `println`/`drawWidget`/debug 文本;
- 每档一个用例,做:
  - **采样**:`snapshotPixels` 后——opacity=1 → 中心 `(150,150)`==GREEN、四角(1,1)==YELLOW;opacity=0 → 中心==YELLOW、四角==YELLOW;opacity=0.5 → 中心 ≠ GREEN 且 ≠ YELLOW(介于);
  - **golden**:id `widget/opacity/opacity_{0,0_5,1}` 三张整图入库(确定性)。

### 2. `widget/PositionedTest.kt`(重写)
- `applyParentData_test`:已断言,仅去 `println`;
- `left_top_test`:去掉 debug/println,重写为:`rootLayout { SizedBox(200){ Stack { Positioned(left=10,top=10){ Container(100×100 RED) } } } }`,断言红色容器 LayoutNode `assertGlobalRect(10,10,100,100)`(Stack 内 positioned 用 parentData 偏移,若 LayoutNode 读取不到再按实际校准并注明);并 `snapshotPixels` 采样:中心 `(60,60)`==RED、`(5,5)`==透明底上…(若白/透明默认,以实际背景断言)。建议加 golden `widget/positioned/left_top`。

### 3. `widget/ClipTest.kt`(重写)
现 5 场景(clipRect / clipRRect borderRadius / clipRRect clipper / clipOval / clipPath 星形),均 300×300 白底容器内 200×200 内容(多为纯色)。
处理:
- 去掉 `println`/debugInfo 文本;
- 场景保持纯色几何,**每场景一个用例**,用 `golden("widget/clip/clip_rect"…)` 等(沿用现有命名)整图入库;并可加 1–2 个关键采样断言(如 clip 外角为白、内部中心为内容色),坐标以实际内容为准。
- 若有场景在 Batch-1a 难以确定(如涉及 text),移除/替换并注明。

### 4. `widget/FlexibleTest.kt`(清理,非冒烟)
- `applyParentData_test` 已是断言,仅去 `println`;
- 补一个 flex **布局数值**用例(确定性):用 `rootLayout`/RenderFlex 子尺寸断言(例如 Row 内 flex=1/2 的 SizedBox 分配比例),作为数值层模板。若需更贴合既测 API,可用 `Flex(direction=HORIZONTAL)` + `createRenderBox().layout(...)` 后读 definiteSize 与 offset;避免外部约束不确定时用 unbounded 需显式 BoxConstraints——以实现期运行结果校准断言。

## 规则(迁移模板,后续批沿用)

1. 场景需**可确定复现**:移除 OS 字体文本与外部图;必要时用纯色/几何替代内容。
2. 优先数值/采样(快),确定性整图视觉加 golden。
3. 旧 `drawWidget`+`println` 冒烟删除;保留的"人眼 artifact"仅在确有需要时经 `drawPainter` 明确注释。
4. golden id 沿用 `widget/<area>/<scene>`;基准与测试同提交;record→verify。
5. 发现实现与意图语义相悖的清晰 bug → 最小修复并记录;模糊 → 不改、注释"待议"。

## 非目标(Batch-1b 或后续)

- DecoratedBoxTest / ImageFilteredTest / BackdropFilterTest(字体/模糊/外网,需替换内容后再迁);
- parse-render(ContainerParser/StackParser/RowParser)与 TextTest、ColorFiltered/CachedNetwork(network);
- render/layer 包迁移。

## 参考

- 目标文件源:`core/src/test/kotlin/com/muedsa/snapshot/widget/{OpacityTest,PositionedTest,ClipTest,FlexibleTest}.kt`。
- 迁移模板先例:已迁移 `ColoredBoxTest`/`ColumnTest`;golden 模板 `GradientGoldenTest`/`ClipGoldenTest`。
- 手册:`docs/testing/README.md`。
