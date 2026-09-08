# 纯逻辑单元测试铺开设计

日期:2026-09-08
状态:设计(已与用户确认做法决策)

## 背景与动机

testkit 框架 PR 已提交(独立进行)。本子项目为**渲染无关的纯逻辑单测铺开**第二批次:为 core 中"无渲染、可确定性断言"的高信号逻辑补单测,让后续改布局/装饰/文本/渐变参数时无需肉眼核对即有回归网。

现状(2026-09-08 核对):
- `BoxConstraintsTest` 已存在但只测 `copyWith`;
- `FittedSizes/BoxFit`、`TextStyle`、渐变参数、`BorderRadius/BorderSide/BoxShadow/BorderRadiusGeometry` 均无纯单测(仅间接经渲染/绘图路径覆盖)。

## 目标与完成标准(本批)

目标:
1. 为下表四区补确定性单测,每方法覆盖主分支 + 关键边界。
2. 期望值按 **Flutter/意图语义** 书写;测出**语义清晰 bug → 最小修复**并逐条记录;歧义偏差不修、记入 spec「待议」。
3. 纯 `kotlin.test`,**不依赖 `:testkit`**(分支独立于 testkit PR;渲染类已由 E3/E5 覆盖,不在本批)。

完成标准(验收):
- [ ] `./gradlew test`(core+parser)全绿:新增纯逻辑单测全过,无既有回归。
- [ ] 静态核对:新增/修改测试文件无 `org.junit.jupiter`(统一 kotlin.test)。
- [ ] 修复清单与待议项(如有)写入分支 docs 或 PR 正文。
- [ ] 分支 `test/pure-logic-units`(base=main),docs(spec/plan)在前 + 每区一个 GPG 提交,一个 PR。

## 已确认做法决策(逐条)

1. 首批覆盖四区:BoxFit/FittedSizes + BoxConstraints 补全、TextStyle、渐变参数数学、边框/圆角/阴影几何。
2. 断言基准 = **按意图语义写期望值**;清晰 bug 最小修复并逐个汇报;模糊偏差只记录。
3. 交付 = **单分支单 PR**,按区分组 GPG 提交。
4. 浮点带容差(`precisionErrorTolerance` 或场景化 1e-3);比率类用例用比例断言。
5. 渐变 `impliedStops` 为 protected:测试内定义子类包装暴露,零产品面改动。
6. skia 值对象(RRect/Rect/Paint)断言只查字段属性,不渲染图像。

## 覆盖矩阵与逐文件改动

### A. BoxFit/FittedSizes + BoxConstraints 补全

- **Create** `core/src/test/kotlin/com/muedsa/snapshot/paint/FittedSizesTest.kt`
  - `applyBoxFit` 各枚举(FILL/CONTAIN/COVER/FIT_WIDTH/FIT_HEIGHT/NONE/SCALE_DOWN):source/destination 比例语义按 Flutter 惯例;
  - 输入任一尺寸 ≤0 → `FittedSizes.ZERO`;
  - 纵横比用例:输入 4:3、输出 16:9 等分别走 COVER/CONTAIN 两个分支方向;FIT_WIDTH/FIT_HEIGHT 的"cover 状/contain 状"分叉;NONE 截取语义;SCALE_DOWN 只缩不放。
- **Modify** `core/src/test/kotlin/com/muedsa/snapshot/render/box/BoxConstraintsTest.kt`(保留既有 copyWith 用例并扩展)
  - 构造:`tight(Size)`/`tightFor`/`tightForFinite`/`loose`/`expand`(宽/高单独);
  - 转换:`loosen`/`tighten(w/h)`/`enforce`/`deflate(EdgeInsets)`/`flipped`/`widthConstraints`/`heightConstraints`/`constrain(Size)`/`constrainDimensions`/`constrainWidth/Height`;
  - 派生:`smallest`/`biggest`/`isTight`/`hasTightWidth/Height`/`hasBoundedWidth/Height`/`hasInfiniteWidth/Height`/`isNormalized`;
  - `constrainSizeAndAttemptToPreserveAspectRatio`:非紧致下按最大/最小边保比收缩放大、宽高双向;
  - 边界:0、`Float.POSITIVE_INFINITY`、`min>max` 的 coerce 行为(构造断言打开时以 assert 语义为准;关闭断言时按 coerce 结果断言)。

### B. TextStyle

- **Create** `core/src/test/kotlin/com/muedsa/snapshot/paint/text/TextStyleTest.kt`
  - `isEmpty()`:全字段 null → true;任一字段非 null(含 fontEdging/fontHinting/subpixel)→ false;
  - `mergeFrom(style)`:子非空覆盖父;子空回填父;两级链式;互斥字段独立(不串味);
  - `toSkikoTextStyle()`:空 → null;非空 → skia paragraph.TextStyle 上对应字段映射,重点断言 `color/fontSize/fontFamilies/height/baselineMode` 与 `fontEdging/fontHinting/subpixel` 贯通;返回非空实例不抛。

### C. 渐变参数数学

- **Create** `core/src/test/kotlin/com/muedsa/snapshot/paint/gradient/GradientImpliedStopsTest.kt`
  - 测试内定义 `private class StopsProbe(colors: IntArray, stops: FloatArray? = null) : Gradient(colors, stops) { fun exposeStops() = impliedStops() }`;
  - `impliedStops()`:colors n=2..6 → 等距 `[0, 1/(n-1), …, 1]`;显式 `stops` 原样返回;等距结果与长度=colors.size;
  - Sweep 默认参数:`startAngle=0f`、`endAngle=MATH_PI*2`、`center==BoxAlignment.CENTER`(常量校验,不改产品)。

> 其余渐变行为(align resolve、Shader 构造、premul/插值)需经渲染,已由 testkit E3 golden 覆盖,本批不重复。

### D. 边框/圆角/阴影几何

- **Create** `core/src/test/kotlin/com/muedsa/snapshot/paint/decoration/BorderRadiusTest.kt`
  - 工厂:ZERO/only/all/circular/vertical/horizontal;
  - 运算符:两 BorderRadius 相加减、unaryMinus、±Float/×Float/÷Float/%Float 逐角;copyWith;
  - toRRect(rect):结果 RRect 外框尺寸正确、角半径字段与入参一致(按 skia RRect 属性读取)。
- **Create** `core/src/test/kotlin/com/muedsa/snapshot/paint/decoration/BorderRadiusGeometryTest.kt`
  - `BorderRadius add/subtract BorderRadius` → BorderRadius 逐角;
  - `BorderRadius add/subtract MixedBorderRadius`(其它子类)→ MixedBorderRadius(走基类 open);radius/值只读。
- **Create** `core/src/test/kotlin/com/muedsa/snapshot/paint/decoration/BorderSideTest.kt`
  - `strokeInset/Outset/Offset` 数学:INSIDE(-1)/CENTER(0)/OUTSIDE(1) 三档与 width;
  - `scale(t)`:t≤0 → width 0 & style NONE;t>0 → width*max(0,…) & style 保留;颜色不变;
  - `canMerge`:一方 none→true;两实边同 style 同 color→true;异 style 或异 color→false;
  - `merge`:both none→NONE;一 none→另一;两实边→宽相加、strokeAlign 取 max、色/style 保留;常量 `NONE/STROKE_ALIGN_*`。
  - `toPaint()`:SOLID→该色/宽/STROKE;NONE→黑色/0 宽/STROKE(Paint 属性)。
- **Create** `core/src/test/kotlin/com/muedsa/snapshot/paint/decoration/BoxShadowTest.kt`
  - `convertRadiusToSigma`:≤0→0;正值按 `radius*0.57735+0.5`;
  - `blurSigma` 惰性同 convert;
  - `scale(factor)`:offset/blurRadius/spreadRadius 同步缩放,color/blurStyle 不变;默认参数(spreadRadius=0、blurStyle=NORMAL)。

## 修复/记录策略

- 若某测试红因实现与意图语义不符:
  - **语义清晰为 bug**(与 Flutter 惯例或本仓库注释相悖)→ 最小修复,提交消息列"修复:类.方法 → 改动",并归入该区提交;
  - **语义模糊 / 现有注释支持现况** → 不改,记入 spec「待议项」+ PR 正文提示。
- 修复保持行为最小化;若修复影响既有测试,在该区提交一并说明。

## 待议项登记

设计定稿时无已知歧义。实施期若测试红但语义模糊(现注释支持现况),在**该区提交消息与最终 PR 正文**登记条目供讨论,不改产品;清晰 bug 按上一节走修复清单。本文件无需维护占位条目。

## 非目标

- 不迁移/不改旧渲染冒烟测试(留给后续批次)。
- 不新增产品功能;产品改动仅限"修复清单"内的最小修正。
- 不引入属性测试/覆盖门槛工具;不建 mock。
- 不覆盖 `com.muedsa.geometry.*`(外部库,非本仓库所有)。

## 验证策略

1. 逐区 TDD:先写测试(红)→ 最小修复(如需要)→ 绿 → GPG 提交。
2. 全量 `./gradlew test`(core+parser)与 `./gradlew jar` → BUILD SUCCESSFUL。
3. 静态核对:新测试无 `org.junit.jupiter`;grep 确认 product 改动仅"修复清单"内的函数。

## 参考

- 待测类源码:`paint/BoxFit.kt`、`paint/FittedSizes.kt`、`rendering/box/BoxConstraints.kt`、`paint/text/TextStyle.kt`、`paint/gradient/Gradient.kt`(impliedStops)、`paint/gradient/SweepGradient.kt`、`paint/decoration/{BorderRadius,BorderRadiusGeometry,BorderSide,BoxShadow}.kt`。
- 现有:`core/src/test/.../render/box/BoxConstraintsTest.kt`。
