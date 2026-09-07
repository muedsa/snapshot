# skiko Gradient/Shader 工厂 API 迁移实现计划(②)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 把仓库 3 个渐变子类 + `RenderCustomClip` 的 debug 渐变迁移到新 skiko Shader 工厂 API(`gradient: org.jetbrains.skia.Gradient` 值对象 + 独立 `localMatrix`),使这些文件的编译错误清零、行为尽力等价。

**Architecture:** 在仓库 `paint/gradient/Gradient.kt` 基类新增 `protected buildSkiaGradient(tileMode, inPremul)` 共享辅助(集中 `IntArray→Array<Color4f>` 与 `Gradient` 值对象构造),供 3 子类复用;`RenderCustomClip` 单独内联。删除 `GradientStyle` 用法/import。`createShader` 公共契约不变。

**Tech Stack:** Kotlin 2.4.0 / JVM、Gradle(core 模块)、skiko-awt `0.0.0-SNAPSHOT`。

**Spec:** `docs/superpowers/specs/2026-09-07-skiko-gradient-shader-api-migration-design.md`

---

## 执行前必读(环境约定)

- 仓库根 `D:\mine\workspace\snapshot` 运行命令(git-bash 用 `./gradlew`)。
- 本机 gpg 无交互可用 → 所有提交统一 `git -c commit.gpgsign=false commit -m "…"`。
- **本计划的编译验证以“这些文件的错误从编译输出中消失”为准**,而非 `BUILD SUCCESSFUL`——因 module 级全绿还需 ①Path 与 ③Font 计划一并执行。
- 遇到编译提示与下述代码不一致时,以编译错误信息为准微调,不要臆测。

## 转换规则(G-R,详见 spec)

G-R1 `colors/positions/style=` → `gradient = SkiaGradient(...)` + `localMatrix = …`
G-R2 颜色:每色 `Color4f(intColor)`
G-R3 `positions`/`tileMode` 原样传递
G-R4 premul:false→`InPremul.NO`,true→`InPremul.YES`
G-R5 删 `import org.jetbrains.skia.GradientStyle`;基类用 `import org.jetbrains.skia.Gradient as SkiaGradient`
G-R6 不改几何/角度换算/契约

## 涉及文件

- Modify: `core/src/main/kotlin/com/muedsa/snapshot/paint/gradient/Gradient.kt`
- Modify: `core/src/main/kotlin/com/muedsa/snapshot/paint/gradient/LinearGradient.kt`
- Modify: `core/src/main/kotlin/com/muedsa/snapshot/paint/gradient/RadialGradient.kt`
- Modify: `core/src/main/kotlin/com/muedsa/snapshot/paint/gradient/SweepGradient.kt`
- Modify: `core/src/main/kotlin/com/muedsa/snapshot/rendering/box/RenderCustomClip.kt`

---

### Task 1: 基类 `Gradient.kt` 加共享辅助

**Files:**
- Modify: `core/src/main/kotlin/com/muedsa/snapshot/paint/gradient/Gradient.kt`

- [ ] **Step 1: 加 import**

把文件头部 import 块

```kotlin
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Shader
import org.jetbrains.skia.paragraph.Direction
```

替换为:

```kotlin
import org.jetbrains.skia.Color4f
import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.Gradient as SkiaGradient
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Shader
import org.jetbrains.skia.paragraph.Direction
```

- [ ] **Step 2: 加 protected 辅助**

在 `impliedStops()` 方法结束的 `}` 与 `abstract fun createShader(...)` 之间插入:

```kotlin
    protected fun buildSkiaGradient(
        tileMode: FilterTileMode,
        inPremul: Boolean,
    ): SkiaGradient = SkiaGradient(
        colors = SkiaGradient.Colors(
            colors = Array(colors.size) { Color4f(colors[it]) },
            positions = impliedStops(),
            tileMode = tileMode,
            colorSpace = null,
        ),
        interpolation = SkiaGradient.Interpolation(
            inPremul = if (inPremul) {
                SkiaGradient.Interpolation.InPremul.YES
            } else {
                SkiaGradient.Interpolation.InPremul.NO
            }
        )
    )
```

- [ ] **Step 3: 定位微确认锚点(暂不编译,供 Task 6 验证)**

记下两处“实现期微确认”:`Interpolation` 是否需显式 `colorSpace`/`hueMethod`;`Color4f(int)` 语义。若 Task 6 编译报缺参/异常,据此回改(见 Task 6 Step 2)。

---

### Task 2: `LinearGradient.kt`

**Files:**
- Modify: `core/src/main/kotlin/com/muedsa/snapshot/paint/gradient/LinearGradient.kt`

- [ ] **Step 1: 删 import**

删除行 `import org.jetbrains.skia.GradientStyle`。

- [ ] **Step 2: 改 `createShader`**

把:

```kotlin
    override fun createShader(rect: Rect, textDirection: Direction?): Shader {
        val beginOffset = begin.resolve(textDirection).withinRect(rect)
        val endOffset = end.resolve(textDirection).withinRect(rect)
        return Shader.makeLinearGradient(
            x0 = beginOffset.x,
            y0 = beginOffset.y,
            x1 = endOffset.x,
            y1 = endOffset.y,
            colors = colors,
            positions = impliedStops(),
            style = GradientStyle(
                tileMode = tileMode,
                isPremul = false,
                localMatrix = transform?.transform(rect)?.toRMO()?.asMatrix33()
            )
        )
    }
```

替换为:

```kotlin
    override fun createShader(rect: Rect, textDirection: Direction?): Shader {
        val beginOffset = begin.resolve(textDirection).withinRect(rect)
        val endOffset = end.resolve(textDirection).withinRect(rect)
        return Shader.makeLinearGradient(
            x0 = beginOffset.x,
            y0 = beginOffset.y,
            x1 = endOffset.x,
            y1 = endOffset.y,
            gradient = buildSkiaGradient(tileMode = tileMode, inPremul = false),
            localMatrix = transform?.transform(rect)?.toRMO()?.asMatrix33(),
        )
    }
```

---

### Task 3: `RadialGradient.kt`

**Files:**
- Modify: `core/src/main/kotlin/com/muedsa/snapshot/paint/gradient/RadialGradient.kt`

- [ ] **Step 1: 删 import**

删除行 `import org.jetbrains.skia.GradientStyle`。

- [ ] **Step 2: 改普通径向分支**

把:

```kotlin
        return if (focal == null || (focal == center && focalRadius == 0f)) {
            Shader.makeRadialGradient(
                x = centerOffset.x,
                y = centerOffset.y,
                r = radius * rect.shortestSide,
                colors = colors,
                positions = impliedStops(),
                style = GradientStyle(
                    tileMode = tileMode,
                    isPremul = true,
                    localMatrix = transform?.transform(rect)?.toRMO()?.asMatrix33()
                )
            )
        } else {
```

替换为:

```kotlin
        return if (focal == null || (focal == center && focalRadius == 0f)) {
            Shader.makeRadialGradient(
                x = centerOffset.x,
                y = centerOffset.y,
                radius = radius * rect.shortestSide,
                gradient = buildSkiaGradient(tileMode = tileMode, inPremul = true),
                localMatrix = transform?.transform(rect)?.toRMO()?.asMatrix33(),
            )
        } else {
```

- [ ] **Step 3: 改 two-point 分支**

把:

```kotlin
            val focalOffset: Offset = focal.resolve(textDirection).withinRect(rect)
            Shader.makeTwoPointConicalGradient(
                x0 = centerOffset.x,
                y0 = centerOffset.y,
                r0 = radius * rect.shortestSide,
                x1 = focalOffset.x,
                y1 = focalOffset.y,
                r1 = focalRadius * rect.shortestSide,
                colors = colors,
                positions = impliedStops(),
                style = GradientStyle(
                    tileMode = tileMode,
                    isPremul = true,
                    localMatrix = transform?.transform(rect)?.toRMO()?.asMatrix33()
                )
            )
```

替换为:

```kotlin
            val focalOffset: Offset = focal.resolve(textDirection).withinRect(rect)
            Shader.makeTwoPointConicalGradient(
                x0 = centerOffset.x,
                y0 = centerOffset.y,
                startRadius = radius * rect.shortestSide,
                x1 = focalOffset.x,
                y1 = focalOffset.y,
                endRadius = focalRadius * rect.shortestSide,
                gradient = buildSkiaGradient(tileMode = tileMode, inPremul = true),
                localMatrix = transform?.transform(rect)?.toRMO()?.asMatrix33(),
            )
```

---

### Task 4: `SweepGradient.kt`

**Files:**
- Modify: `core/src/main/kotlin/com/muedsa/snapshot/paint/gradient/SweepGradient.kt`

- [ ] **Step 1: 删 import**

删除行 `import org.jetbrains.skia.GradientStyle`。

- [ ] **Step 2: 改 `createShader`**

把:

```kotlin
        return Shader.makeSweepGradient(
            x = centerOffset.x,
            y = centerOffset.y,
            startAngle = startAngle / MATH_PI * 180f,
            endAngle = endAngle / MATH_PI * 180f,
            colors = this.colors,
            positions = impliedStops(),
            style = GradientStyle(
                tileMode = tileMode,
                isPremul = true,
                localMatrix = transform?.transform(rect)?.toRMO()?.asMatrix33()
            )
        )
```

替换为:

```kotlin
        return Shader.makeSweepGradient(
            x = centerOffset.x,
            y = centerOffset.y,
            startAngle = startAngle / MATH_PI * 180f,
            endAngle = endAngle / MATH_PI * 180f,
            gradient = buildSkiaGradient(tileMode = tileMode, inPremul = true),
            localMatrix = transform?.transform(rect)?.toRMO()?.asMatrix33(),
        )
```

---

### Task 5: `RenderCustomClip.kt`(debug 渐变,单独内联)

**Files:**
- Modify: `core/src/main/kotlin/com/muedsa/snapshot/rendering/box/RenderCustomClip.kt`

- [ ] **Step 1: 改 debug `shader`**

把 `debugPaint` 中:

```kotlin
                shader = Shader.makeLinearGradient(
                    x0 = 0f,
                    y0 = 0f,
                    x1 = 10f,
                    y1 = 10f,
                    colors = intArrayOf(0x00000000, 0xFFFF00FF.toInt(), 0xFFFF00FF.toInt(), 0x00000000),
                    positions = floatArrayOf(0.25f, 0.25f, 0.75f, 0.75f),
                    style = GradientStyle.DEFAULT.withTileMode(FilterTileMode.REPEAT)
                )
```

替换为:

```kotlin
                shader = Shader.makeLinearGradient(
                    x0 = 0f,
                    y0 = 0f,
                    x1 = 10f,
                    y1 = 10f,
                    gradient = Gradient(
                        colors = Gradient.Colors(
                            colors = arrayOf(
                                Color4f(0x00000000),
                                Color4f(0xFFFF00FF.toInt()),
                                Color4f(0xFFFF00FF.toInt()),
                                Color4f(0x00000000)
                            ),
                            positions = floatArrayOf(0.25f, 0.25f, 0.75f, 0.75f),
                            tileMode = FilterTileMode.REPEAT,
                            colorSpace = null,
                        ),
                        interpolation = Gradient.Interpolation(
                            inPremul = Gradient.Interpolation.InPremul.NO
                        )
                    ),
                    localMatrix = null,
                )
```

(该文件已是 `import org.jetbrains.skia.*` 通配,`Gradient`/`Color4f`/`FilterTileMode` 均可用。)

- [ ] Step 2: 不提交。

---

### Task 6: 编译验证(② 部分)并提交

**Files:** 无新改动(校验 Task 1–5)

- [ ] **Step 1: 编译并核对渐变文件错误消失**

Run:
```bash
./gradlew :core:compileKotlin --console=plain 2>&1 | grep -E "gradient/|RenderCustomClip"
```
Expected:输出为空(**不再出现** 5 个渐变相关文件的任何错误)。
仍允许出现:Path 文件(`BoxDecoration`/`PaintingContext`/`RenderClip*`/`RenderPadding`)与 `TextPainter` 的错误——那些属 ①③ 计划范围。

- [ ] **Step 2(条件):微确认回改**

若上一步对 ② 报错,按下述处理:
- `Gradient.Interpolation` 报“no value passed for parameter colorSpace/hueMethod”等缺参 → 给 `Interpolation` 补 `colorSpace = ColorSpace.SRGB, hueMethod = HueMethod.SHORTER`(枚举常量名以编译提示为准),重跑 Step 1。
- 其它参数名/类型不符 → 以编译提示的候选签名对齐(保持几何值与规则不变)。
- `Color4f(int)` 语义问题无法在编译期暴露,留到测试阶段(见 Task 7 注)。

- [ ] **Step 3: 提交**

```bash
git add core/src/main/kotlin/com/muedsa/snapshot/paint/gradient/Gradient.kt \
        core/src/main/kotlin/com/muedsa/snapshot/paint/gradient/LinearGradient.kt \
        core/src/main/kotlin/com/muedsa/snapshot/paint/gradient/RadialGradient.kt \
        core/src/main/kotlin/com/muedsa/snapshot/paint/gradient/SweepGradient.kt \
        core/src/main/kotlin/com/muedsa/snapshot/rendering/box/RenderCustomClip.kt
git -c commit.gpgsign=false commit -m "refactor(core): 迁移 Shader 渐变工厂到 Gradient 值对象 API"
```

---

### Task 7: 说明(交给执行者而非本计划单独验证)

- module 级 `BUILD SUCCESSFUL` 与 `./gradlew test`/`jar` 需在 ①Path、③Font 计划一并执行后达成;② 只保证自身文件错误清零。
- `Color4f(int)` 若引入肉眼可见的渐变差异(渐变测试无像素断言,不自动失败),在后续联调时核对 `Color4f(int)` 语义(premul 与否),必要时改为手动逐通道换算,并同步更新 spec 微确认 2。

## 自检(与 spec 对照)

- spec「逐文件改动」5 处 → Task 1–5 全覆盖。✔
- spec 做法决策(基类共享辅助、RenderCustomClip 内联、premul 镜像)→ Task 1 辅助 + Task 2/3/4 用 `inPremul`(线性 false / 径向/扫掠 true)、Task 5 用 `InPremul.NO`。✔
- 微确认 1/2 → Task 1 Step 3 记录、Task 6 Step 2 / Task 7 处理。✔
- 越界项(不改契约/几何/不建双版本层)→ 无相关任务。✔
