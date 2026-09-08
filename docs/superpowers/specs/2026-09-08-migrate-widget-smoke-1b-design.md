# 旧渲染冒烟迁移 Batch-1b 设计(blur/阴影,采样断言)

日期:2026-09-08
状态:设计(做法已与用户确认)

## 背景

任务 1 第二批。`testkit` 与 Batch-1a 模板已合入 main。本批处理 `widget` 包三个"较重"冒烟:`DecoratedBoxTest`(ELEVATION_MAP 阴影)、`ImageFilteredTest`(blur)、`BackdropFilterTest`(blur + 外网图),它们都含文字或网络内容,且涉及 blur/阴影栅格化。

**关键约束(跨平台)**:golden 基准在本机 Windows 录制,CI 在 ubuntu 验证。Batch-1a 的几何/裁剪基准已通过 CI,但 blur/阴影的栅格化在不同平台可能细微不同。故本批 blur/阴影**不用整图 golden**,改用**同一次运行内两幅图互比**的采样断言(两侧都在同一平台渲染,天然可移植)。

## 目标与完成标准

- [ ] 三文件重写替换:去 `println`/`drawWidget`/debug 文本;文字→确定性几何;外网→本地几何;
- [ ] blur/阴影用采样/结构断言(见下),锐边几何(圆角装饰)仍可用 golden;
- [ ] 无外网依赖(默认 suite 不联网);
- [ ] `./gradlew test` + `jar` 通过;默认 suite 后工作树干净;无 org.junit/println 残留。

## 已确认做法决策

1. blur/阴影 → **采样断言为主**(不上整图 golden);阴影覆盖 **代表档位**。
2. 冲突统一:ELEVATION_MAP 代表档位也**不出 golden**,与 blur 策略一致。
3. 沿用 1a 模板:场景抽 builder、kotlin.test、清晰 bug 最小修复、模糊只注释"待议"。

## 逐文件改动

### 1. `widget/DecoratedBoxTest.kt`

- `border_radius_golden`(锐边几何):原 `borderRadius_test` 场景(`DecoratedBox(BoxDecoration(color=WHITE, borderRadius=circular(100)))` 包 `Container(200×200)`)去 debug/println,`golden("widget/decorated_box/borderRadius_w200_r100")`。
- `elevation_shadow_sampling`:对代表档位 `[1, 4, 12, 24]` 各渲染 `Container(500×300 WHITE, CENTER){ DecoratedBox(BoxDecoration(color=WHITE, boxShadow=ELEVATION_MAP[e])){ Container(200×100) } }`:
  - **单调变暗**:在盒正下方固定采样点(如 (250, y) 取盒底 + 阴影明显处,坐标实测校准),其亮度随 elevation 增大而**非增**(逐对比较);
  - **远处为白**:远离阴影处(如 (250, 5))== WHITE;
  - **盒内为白**:盒中心 == WHITE。
- `elevation_zero_no_shadow`:elevation=0(空阴影数组)→ 盒下方紧邻点 == WHITE。
- 说明:亮度比较用 `regionStats(...).averageColor` 取通道值自行计算(测试内私有辅助),不做整图断言。

### 2. `widget/ImageFilteredTest.kt`

原 `blur_test`/`blur_clip_test`:256×256,四角 RED/BLUE 方块 + 中央 `ImageFiltered(blur 2,2)` 内 `Container(128×128, padding 10, CENTER)` 含 `Text`。
- 把 `Text` 替换为**确定性几何**(如 3–4 条横向色带 / 小方块网格),保持"有内容可模糊"与 `blur_clip` 的"内容溢出被裁剪"意图(blur_clip 用更多/更长的色带使内容超出容器)。
- 断言(每场景):
  - **滤镜生效**:同一场景的**无滤镜孪生**(去掉 `ImageFiltered` 包裹)与有滤镜版本,在模糊带内某点像素**不同**;
  - **边缘混合**:内容边缘外一点的颜色既非纯背景也非纯内容色(即被模糊混色);
  - **未越界**:远离该区域的一点与孪生一致。
- 坐标以实际渲染实测校准并注释。

### 3. `widget/BackdropFilterTest.kt`

- `blur_test`/`blur_2_test`:文字→几何;结构保留(四角方块 + 中央 `BackdropFilter(blur 25,25)`)。
  - 断言:滤镜区域内一点与**无 BackdropFilter 孪生**同点不同;滤镜区域外一点与孪生相同。
- `blur_3_test`:外网 `DecorationImage(NetworkImageCacheManager…TEST_IMAGE_URL_2)` 换为**本地确定性几何**(如色带网格),保留 `ClipPath`(45° 弧)+ `BackdropFilter` 结构;同样做孪生比对。
- 该文件因此不再依赖网络,默认 suite 不再拉取远程图。

## 规则(与 1a 一致)

场景抽 builder 供"有滤镜/无滤镜孪生"两路复用;golden 仅用于锐边几何;文字/外网内容一律换几何;清晰 bug 最小修复并记录,模糊注释"待议"。

## 非目标

- render/layer 包、parse-render、网络类隔离(后续批);
- 不给 blur/阴影建整图基准;
- 不改产品代码(除非发现清晰 bug)。

## 参考

- 目标文件:`core/src/test/kotlin/com/muedsa/snapshot/widget/{DecoratedBoxTest,ImageFilteredTest,BackdropFilterTest}.kt`
- 模板:`OpacityTest`/`ClipTest`(1a)、`docs/testing/README.md`
- 阴影数据:`core/src/main/kotlin/com/muedsa/snapshot/material/Elevations.kt`(ELEVATION_MAP,12 键含空档 0)
