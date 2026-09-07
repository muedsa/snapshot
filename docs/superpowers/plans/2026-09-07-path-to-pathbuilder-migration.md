# Path → PathBuilder 迁移实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 把仓库 core 模块中所有直接构造/变更 `org.jetbrains.skia.Path` 的地方迁移为使用 `PathBuilder`,行为零变化。

**Architecture:** skiko `0.0.0-SNAPSHOT` 已将 `Path` 改为不可变对象(变更方法全部移除),新增链式 `PathBuilder`(用 `.detach()` 产出不可变 `Path`)。迁移只在“构造/变更端”把 `Path` 换成 `PathBuilder`;只读消费端与公共签名(`getClipPath`/`getOuterPath`/`getInnerPath`/`pushClipPath`/`clipper: (Size)->Path`)一律不动。命令序列、走向、EVEN_ODD、STROKE/FILL 语义全部原样保留。

**Tech Stack:** Kotlin 2.4.0 / JVM、Gradle(仅 core 模块受影响;parser 无 skia.Path 引用)、skiko-awt `0.0.0-SNAPSHOT`。

**Spec:** `docs/superpowers/specs/2026-09-07-path-to-pathbuilder-migration-design.md`

---

## 执行前必读(环境约定)

- 在仓库根 `D:\mine\workspace\snapshot` 运行命令;shell 为 git-bash,`./gradlew` 可用。
- **本机 git 已配置 commit 签名但 gpg 无交互可用**,任何 `git commit` 都会失败并报 `gpg: signing failed`。因此本计划所有提交统一用:
  ```bash
  git -c commit.gpgsign=false commit -m "..."
  ```
- `PathBuilder` 与 `Path` 同包 `org.jetbrains.skia`,但 Kotlin 需要显式 import(除非文件已 `import org.jetbrains.skia.*`)。
- 若某个 `Edit` 的旧串在文件中出现多次,使用行号先定位或对整块做唯一匹配;无法唯一匹配时改用 `Write` 整文件重写(Borders.kt 已给出整文件内容)。

## 转换规则速查(与 spec 一致)

| 规则 | 旧写法 | 新写法 |
|---|---|---|
| R1 链式 | `Path().lineTo(..).lineTo(..)` | `PathBuilder().lineTo(..).lineTo(..).detach()` |
| R2 apply 块 | `Path().apply { moveTo(..); lineTo(..) }` | `PathBuilder().apply { ... }.detach()`(块内方法同名同参) |
| R3 复制平移 | `Path().also { p.offset(dx, dy, it) }` | `PathBuilder(p).offset(dx, dy).detach()` |
| R4 设置填充类型 | `Path().apply { fillMode = F; ... }` | `PathBuilder(F).addRect(..)....detach()` |
| R5 import | — | 非通配文件补 `import org.jetbrains.skia.PathBuilder`;文件内不再出现 `Path` 类型标注时,把 `import ...Path` 替换为 `import ...PathBuilder` |
| R6 边界 | — | 残余 Path 变更点按 R1–R4 收敛;消费端/签名不改 |

> R3 兜底:若 `PathBuilder(existing)` 实测不是“复制续接”,改用几何等价的
> `PathBuilder().addPath(existing, dx, dy, PathAddMode.APPEND).detach()`。

## 涉及文件与职责

**生产代码 main(core)**
- `core/src/main/kotlin/com/muedsa/snapshot/paint/decoration/Borders.kt` — 每边独立 `PathBuilder`(整文件重写)
- `core/src/main/kotlin/com/muedsa/snapshot/paint/decoration/BoxDecoration.kt` — `getClipPath` 3 分支
- `core/src/main/kotlin/com/muedsa/snapshot/paint/decoration/BoxDecorationPainter.kt` — `paintBackgroundImage` 内 clipPath
- `core/src/main/kotlin/com/muedsa/snapshot/rendering/PaintingContext.kt` — `pushClipPath` offset 复制
- `core/src/main/kotlin/com/muedsa/snapshot/rendering/box/RenderClipOval.kt` — `cachedPath` + debugPaint
- `core/src/main/kotlin/com/muedsa/snapshot/rendering/box/RenderClipPath.kt` — `defaultClip` + debugPaint
- `core/src/main/kotlin/com/muedsa/snapshot/rendering/box/RenderPadding.kt` — debugPaint EVEN_ODD

**测试代码 core(test)**
- `core/src/test/kotlin/com/muedsa/snapshot/render/box/RenderClipPathTest.kt` — 链式 ×2
- `core/src/test/kotlin/com/muedsa/snapshot/render/ClipPathLayerTest.kt` — 链式 ×1
- `core/src/test/kotlin/com/muedsa/snapshot/widget/ClipTest.kt` — apply 块 ×1
- `core/src/test/kotlin/com/muedsa/snapshot/widget/BackdropFilterTest.kt` — apply 块 ×1
- `core/src/test/kotlin/com/muedsa/snapshot/LogoCreator.kt` — apply 块 ×2

> `BoxBorder.kt` 的两处(`getInnerPath/getOuterPath`)已是 `PathBuilder()...detach()`,无需再改。

---

### Task 1: 建立编译基线,确认错误面 = 计划范围

**Files:** 无(只读观察)

- [ ] **Step 1: 编译 core 主源码,记录错误清单**

Run:
```bash
./gradlew :core:compileKotlin --console=plain
```
Expected:`BUILD FAILED`。错误应集中在调用 `Path` 变更方法的 6 个文件:`BoxDecoration.kt`、`BoxDecorationPainter.kt`、`PaintingContext.kt`、`RenderClipOval.kt`、`RenderClipPath.kt`、`RenderPadding.kt`(Borders.kt/BoxBorder.kt 已迁移,不应报错)。

- [ ] **Step 2: 核对错误面**

对照 Task 2/3/4 的改动清单。**若错误面出现计划外的文件或“非 Path 相关”的新 API 错误(说明 skiko 快照还有其它破坏性变更),暂停并上报,不要臆测修改**;仅当错误全部落在上述 Path 文件内才继续。

- [ ] Step 3: 不提交(基线任务)。

---

### Task 2: 迁移 `Borders.kt`(整文件重写为每边独立 PathBuilder)

**Files:**
- Modify: `core/src/main/kotlin/com/muedsa/snapshot/paint/decoration/Borders.kt`(整文件)

- [ ] **Step 1: 用 Write 覆盖整个文件为以下内容**

```kotlin
package com.muedsa.snapshot.paint.decoration

import org.jetbrains.skia.*

fun paintBorder(
    canvas: Canvas,
    rect: Rect,
    top: BorderSide = BorderSide.NONE,
    right: BorderSide = BorderSide.NONE,
    bottom: BorderSide = BorderSide.NONE,
    left: BorderSide = BorderSide.NONE,
) {
    // We draw the borders as filled shapes, unless the borders are hairline
    // borders, in which case we use PaintingStyle.stroke, with the stroke width
    // specified here.
    val paint: Paint = Paint().apply {
        strokeWidth = 1f
    }

    when (top.style) {
        BorderStyle.SOLID -> {
            paint.color = top.color
            val builder = PathBuilder().apply {
                moveTo(rect.left, rect.top)
                lineTo(rect.right, rect.top)
                if (top.width == 0f) {
                    paint.mode = PaintMode.STROKE
                } else {
                    paint.mode = PaintMode.FILL
                    lineTo(rect.right - right.width, rect.top + top.width)
                    lineTo(rect.left + left.width, rect.top + top.width)
                }
            }
            canvas.drawPath(builder.detach(), paint)
        }

        BorderStyle.NONE -> Unit
    }

    when (right.style) {
        BorderStyle.SOLID -> {
            paint.color = right.color
            val builder = PathBuilder().apply {
                moveTo(rect.right, rect.top)
                lineTo(rect.right, rect.bottom)
                if (right.width == 0f) {
                    paint.mode = PaintMode.STROKE
                } else {
                    paint.mode = PaintMode.FILL
                    lineTo(rect.right - right.width, rect.bottom - bottom.width)
                    lineTo(rect.right - right.width, rect.top + top.width)
                }
            }
            canvas.drawPath(builder.detach(), paint)
        }

        BorderStyle.NONE -> Unit
    }

    when (bottom.style) {
        BorderStyle.SOLID -> {
            paint.color = bottom.color
            val builder = PathBuilder().apply {
                moveTo(rect.right, rect.bottom)
                lineTo(rect.left, rect.bottom)
                if (bottom.width == 0f) {
                    paint.mode = PaintMode.STROKE
                } else {
                    paint.mode = PaintMode.FILL
                    lineTo(rect.left + left.width, rect.bottom - bottom.width)
                    lineTo(rect.right - right.width, rect.bottom - bottom.width)
                }
            }
            canvas.drawPath(builder.detach(), paint)
        }

        BorderStyle.NONE -> Unit
    }

    when (left.style) {
        BorderStyle.SOLID -> {
            paint.color = left.color
            val builder = PathBuilder().apply {
                moveTo(rect.left, rect.bottom)
                lineTo(rect.left, rect.top)
                if (left.width == 0f) {
                    paint.mode = PaintMode.STROKE
                } else {
                    paint.mode = PaintMode.FILL
                    lineTo(rect.left + left.width, rect.top + top.width)
                    lineTo(rect.left + left.width, rect.bottom - bottom.width)
                }
            }
            canvas.drawPath(builder.detach(), paint)
        }

        BorderStyle.NONE -> Unit
    }
}
```

- [ ] Step 2: 不提交(模块仍未全绿)。

---

### Task 3: 迁移 decoration 两个文件

**Files:**
- Modify: `core/src/main/kotlin/com/muedsa/snapshot/paint/decoration/BoxDecoration.kt`
- Modify: `core/src/main/kotlin/com/muedsa/snapshot/paint/decoration/BoxDecorationPainter.kt`

- [ ] **Step 1: `BoxDecoration.kt` — 补 import**

在 `import org.jetbrains.skia.Path` 之后插入一行 `import org.jetbrains.skia.PathBuilder`(原 `Path` import 保留,因为 `getClipPath` 返回类型是 `Path`)。

- [ ] **Step 2: `BoxDecoration.kt` — `getClipPath` 三处(R1)**

替换 `Path().addOval(square)`(CIRCLE 分支内)为:
```kotlin
                PathBuilder().addOval(square).detach()
```
替换下面的两块为:
```kotlin
                    PathBuilder().addRRect(borderRadius.toRRect(rect)).detach()
```
与:
```kotlin
                    PathBuilder().addRect(rect).detach()
```
改后 `getClipPath` 应为:
```kotlin
    override fun getClipPath(rect: Rect): Path {
        return when (shape) {
            BoxShape.CIRCLE -> {
                val center: Offset = rect.center
                val radius: Float = rect.shortestSide / 2f
                val square: Rect = makeRectFromCircle(center, radius)
                PathBuilder().addOval(square).detach()
            }

            BoxShape.RECTANGLE -> {
                if (borderRadius != null) {
                    PathBuilder().addRRect(borderRadius.toRRect(rect)).detach()
                } else {
                    PathBuilder().addRect(rect).detach()
                }
            }
        }
    }
```

- [ ] **Step 3: `BoxDecorationPainter.kt` — 补 import**

在 `import org.jetbrains.skia.Path` 之后插入一行 `import org.jetbrains.skia.PathBuilder`(`Path` import 保留,`clipPath: Path?` 类型仍在用)。

- [ ] **Step 4: `BoxDecorationPainter.kt` — `paintBackgroundImage`(R1)**

替换 `clipPath = Path().addOval(square)` 为:
```kotlin
                clipPath = PathBuilder().addOval(square).detach()
```
替换 `clipPath = Path().addRRect(decoration.borderRadius.toRRect(rect))` 为:
```kotlin
                    clipPath = PathBuilder().addRRect(decoration.borderRadius.toRRect(rect)).detach()
```

- [ ] Step 5: 不提交。

---

### Task 4: 迁移 rendering 四个文件

**Files:**
- Modify: `core/src/main/kotlin/com/muedsa/snapshot/rendering/PaintingContext.kt`
- Modify: `core/src/main/kotlin/com/muedsa/snapshot/rendering/box/RenderClipOval.kt`
- Modify: `core/src/main/kotlin/com/muedsa/snapshot/rendering/box/RenderClipPath.kt`
- Modify: `core/src/main/kotlin/com/muedsa/snapshot/rendering/box/RenderPadding.kt`

- [ ] **Step 1: `PaintingContext.kt` — `pushClipPath` 内 offset 复制(R3)**

替换:
```kotlin
        val offsetClipPath: Path = Path().also {
            clipPath.offset(offset.x, offset.y, it)
        }
```
为:
```kotlin
        val offsetClipPath: Path = PathBuilder(clipPath).offset(offset.x, offset.y).detach()
```
该文件为 `import org.jetbrains.skia.*` 通配,无需改 import。

- [ ] **Step 2: `RenderClipOval.kt` — 补 import**

在 `import org.jetbrains.skia.Path` 之后插入一行 `import org.jetbrains.skia.PathBuilder`。

- [ ] **Step 3: `RenderClipOval.kt` — `getClipPath` 构建(R1)**

替换 `cachedPath = Path().addOval(cachedRect!!)` 为:
```kotlin
            cachedPath = PathBuilder().addOval(cachedRect!!).detach()
```

- [ ] **Step 4: `RenderClipOval.kt` — debugPaint offset 复制(R3)**

替换:
```kotlin
                context.canvas.drawPath(
                    Path().also { getClipPath(getClip()).offset(offset.x, offset.y, it) },
                    debugPaint!!
                )
```
为:
```kotlin
                context.canvas.drawPath(
                    PathBuilder(getClipPath(getClip())).offset(offset.x, offset.y).detach(),
                    debugPaint!!
                )
```

- [ ] **Step 5: `RenderClipPath.kt` — 补 import**

在 `import org.jetbrains.skia.Path` 之后插入一行 `import org.jetbrains.skia.PathBuilder`。

- [ ] **Step 6: `RenderClipPath.kt` — `defaultClip`(R1)**

替换:
```kotlin
    override val defaultClip: Path
        get() = Path().addRect(Offset.ZERO combine definiteSize)
```
为:
```kotlin
    override val defaultClip: Path
        get() = PathBuilder().addRect(Offset.ZERO combine definiteSize).detach()
```

- [ ] **Step 7: `RenderClipPath.kt` — debugPaint offset 复制(R3)**

替换:
```kotlin
                context.canvas.drawPath(Path().also { getClip().offset(offset.x, offset.y, it) }, debugPaint!!)
```
为:
```kotlin
                context.canvas.drawPath(PathBuilder(getClip()).offset(offset.x, offset.y).detach(), debugPaint!!)
```

- [ ] **Step 8: `RenderPadding.kt` — debugPaint 两段(R4)**

替换第一段:
```kotlin
            context.canvas.drawPath(
                Path().apply {
                    fillMode = PathFillMode.EVEN_ODD
                    addRect(outerRect)
                    addRect(innerRect)
                },
                Paint().apply { color = Color.makeARGB(144, 0, 144, 255) }
            )
```
为:
```kotlin
            context.canvas.drawPath(
                PathBuilder(PathFillMode.EVEN_ODD)
                    .addRect(outerRect)
                    .addRect(innerRect)
                    .detach(),
                Paint().apply { color = Color.makeARGB(144, 0, 144, 255) }
            )
```
替换第二段:
```kotlin
            context.canvas.drawPath(
                Path().apply {
                    fillMode = PathFillMode.EVEN_ODD
                    addRect(innerRect.inflate(2f).intersect(outerRect)!!)
                    addRect(innerRect)
                },
                Paint().apply { color = Color.makeARGB(255, 0, 144, 255) })
```
为:
```kotlin
            context.canvas.drawPath(
                PathBuilder(PathFillMode.EVEN_ODD)
                    .addRect(innerRect.inflate(2f).intersect(outerRect)!!)
                    .addRect(innerRect)
                    .detach(),
                Paint().apply { color = Color.makeARGB(255, 0, 144, 255) })
```
该文件为通配 import,无需改 import。

- [ ] Step 9: 不提交。

---

### Task 5: 生产代码编译通过并提交

**Files:** 无新改动(校验 Task 2–4)

- [ ] **Step 1: 编译 core 主源码**

Run:
```bash
./gradlew :core:compileKotlin --console=plain
```
Expected:`BUILD SUCCESSFUL`。

- [ ] **Step 2: 提交生产代码迁移**

```bash
git add core/src/main/kotlin/com/muedsa/snapshot/paint/decoration/Borders.kt \
        core/src/main/kotlin/com/muedsa/snapshot/paint/decoration/BoxDecoration.kt \
        core/src/main/kotlin/com/muedsa/snapshot/paint/decoration/BoxDecorationPainter.kt \
        core/src/main/kotlin/com/muedsa/snapshot/paint/decoration/BoxBorder.kt \
        core/src/main/kotlin/com/muedsa/snapshot/rendering/PaintingContext.kt \
        core/src/main/kotlin/com/muedsa/snapshot/rendering/box/RenderClipOval.kt \
        core/src/main/kotlin/com/muedsa/snapshot/rendering/box/RenderClipPath.kt \
        core/src/main/kotlin/com/muedsa/snapshot/rendering/box/RenderPadding.kt
git -c commit.gpgsign=false commit -m "refactor(core): 迁移 Path 构造到 PathBuilder(不可变 Path)"
```
> 这里把 `BoxBorder.kt` 也纳入:它是用户 WIP 的未提交改动,与本次迁移同属一个工作树,应一并提交。

---

### Task 6: 迁移渲染类测试(R1 链式)

**Files:**
- Modify: `core/src/test/kotlin/com/muedsa/snapshot/render/box/RenderClipPathTest.kt`
- Modify: `core/src/test/kotlin/com/muedsa/snapshot/render/ClipPathLayerTest.kt`

- [ ] **Step 1: `RenderClipPathTest.kt` — import**

把 `import org.jetbrains.skia.Path` 替换为 `import org.jetbrains.skia.PathBuilder`(改后文件不再出现 `Path` 类型标注)。

- [ ] **Step 2: `RenderClipPathTest.kt` — 两处 clipper 链(内容相同,共 2 次,R1)**

把下面这段(出现 2 次:`renderClip` 与 `renderNoneClip` 的 clipper)的每一处:
```kotlin
                Path()
                    .lineTo(it.x, 0f)
                    .lineTo(it.x, it.y)
                    .lineTo(0f, 0f) // clip ◥
```
替换为:
```kotlin
                PathBuilder()
                    .lineTo(it.x, 0f)
                    .lineTo(it.x, it.y)
                    .lineTo(0f, 0f) // clip ◥
                    .detach()
```

- [ ] **Step 3: `ClipPathLayerTest.kt` — `clipPath` 链(R1)**

替换:
```kotlin
            clipPath = Path()
                .lineTo(size, 0f)
                .lineTo(size, size)
                .lineTo(0f, 0f) // clip ◥
```
为:
```kotlin
            clipPath = PathBuilder()
                .lineTo(size, 0f)
                .lineTo(size, size)
                .lineTo(0f, 0f) // clip ◥
                .detach()
```
该文件为通配 import(`org.jetbrains.skia.*`),无需改 import。

- [ ] Step 4: 不提交。

---

### Task 7: 迁移 widget 类测试(R2 apply 块)

**Files:**
- Modify: `core/src/test/kotlin/com/muedsa/snapshot/widget/ClipTest.kt`
- Modify: `core/src/test/kotlin/com/muedsa/snapshot/widget/BackdropFilterTest.kt`
- Modify: `core/src/test/kotlin/com/muedsa/snapshot/LogoCreator.kt`

- [ ] **Step 1: `ClipTest.kt` — import**

把 `import org.jetbrains.skia.Path` 替换为 `import org.jetbrains.skia.PathBuilder`(改后文件不再出现 `Path` 类型标注)。

- [ ] **Step 2: `ClipTest.kt` — `clipPath_test` 的 clipper(R2)**

替换:
```kotlin
                        Path().apply {
                            moveTo(x = c + r, y = c)
                            for (i in 1..7) {
                                val a: Float = 2.6927936f * i
                                lineTo(c + r * cos(a), c + r * sin(a))
                            }
                        }
```
为:
```kotlin
                        PathBuilder().apply {
                            moveTo(x = c + r, y = c)
                            for (i in 1..7) {
                                val a: Float = 2.6927936f * i
                                lineTo(c + r * cos(a), c + r * sin(a))
                            }
                        }.detach()
```

- [ ] **Step 3: `BackdropFilterTest.kt` — clipper 里的 arcTo 块(R2)**

该文件是 `import org.jetbrains.skia.*` 通配,无需改 import。替换:
```kotlin
                        Path().apply {
                            arcTo(
                                oval = Rect.Companion.makeWH(it.width, it.height),
                                startAngle = 45f,
                                sweepAngle = 180f,
                                forceMoveTo = true
                            )
                        }
```
为:
```kotlin
                        PathBuilder().apply {
                            arcTo(
                                oval = Rect.Companion.makeWH(it.width, it.height),
                                startAngle = 45f,
                                sweepAngle = 180f,
                                forceMoveTo = true
                            )
                        }.detach()
```
> 若编译报“named argument `oval` not found”(PathBuilder.arcTo 形参名不同),把该调用改为位置参数:
> `arcTo(Rect.Companion.makeWH(it.width, it.height), 45f, 180f, true)`,其余不变。

- [ ] **Step 4: `LogoCreator.kt` — import**

把 `import org.jetbrains.skia.Path` 替换为 `import org.jetbrains.skia.PathBuilder`(`org.jetbrains.skia.Rect` import 保留,仍在使用)。

- [ ] **Step 5: `LogoCreator.kt` — 第一处 clipper(顶部橙块,arcTo startAngle=45f,R2)**

替换:
```kotlin
                                        Path().apply {
                                            arcTo(
                                                oval = Rect.Companion.makeWH(it.width, it.height),
                                                startAngle = 45f,
                                                sweepAngle = 180f,
                                                forceMoveTo = true
                                            )
                                        }
```
为:
```kotlin
                                        PathBuilder().apply {
                                            arcTo(
                                                oval = Rect.Companion.makeWH(it.width, it.height),
                                                startAngle = 45f,
                                                sweepAngle = 180f,
                                                forceMoveTo = true
                                            )
                                        }.detach()
```

- [ ] **Step 6: `LogoCreator.kt` — 第二处 clipper(底部黄块,arcTo startAngle=45f+180f,R2)**

替换:
```kotlin
                                        Path().apply {
                                            arcTo(
                                                oval = Rect.Companion.makeWH(it.width, it.height),
                                                startAngle = 45f + 180f,
                                                sweepAngle = 180f,
                                                forceMoveTo = true
                                            )
                                        }
```
为:
```kotlin
                                        PathBuilder().apply {
                                            arcTo(
                                                oval = Rect.Companion.makeWH(it.width, it.height),
                                                startAngle = 45f + 180f,
                                                sweepAngle = 180f,
                                                forceMoveTo = true
                                            )
                                        }.detach()
```

- [ ] Step 7: 不提交。

---

### Task 8: 测试代码编译通过并提交

**Files:** 无新改动(校验 Task 6–7)

- [ ] **Step 1: 编译 core 测试源码(会连带编译 main)**

Run:
```bash
./gradlew :core:compileTestKotlin --console=plain
```
Expected:`BUILD SUCCESSFUL`。

- [ ] **Step 2: 静态扫残余 Path 变更点**

Run:
```bash
grep -rnE "Path\(\)\.(addRect|addOval|addRRect|addPath|moveTo|lineTo|offset|close|reset)|fillMode\s*=|\.offset\([^)]*,\s*[^)]*\)" core/src || echo "no residual"
```
Expected:输出为空(或仅命中非 Path 的 `.offset` 误报,人工核对)。

- [ ] **Step 3: 提交测试迁移**

```bash
git add core/src/test/kotlin/com/muedsa/snapshot/render/box/RenderClipPathTest.kt \
        core/src/test/kotlin/com/muedsa/snapshot/render/ClipPathLayerTest.kt \
        core/src/test/kotlin/com/muedsa/snapshot/widget/ClipTest.kt \
        core/src/test/kotlin/com/muedsa/snapshot/widget/BackdropFilterTest.kt \
        core/src/test/kotlin/com/muedsa/snapshot/LogoCreator.kt
git -c commit.gpgsign=false commit -m "refactor(core): 测试中 Path 构造迁移到 PathBuilder"
```

---

### Task 9: 全量测试

**Files:** 无改动(校验)

- [ ] **Step 1: 运行全量测试(core + parser)**

Run:
```bash
./gradlew test --console=plain
```
Expected:`BUILD SUCCESSFUL`。其中 `RenderClipPathTest`/`RenderClipOvalTest`/`ClipPathLayerTest` 含 origin-vs-clip 像素断言,会真实跑通 clip 路径几何;若它们失败,优先怀疑 R3 语义(见 R3 兜底)。

- [ ] Step 2: 无提交(无代码改动)。

---

### Task 10: 打 jar(对齐 CI `jar.yaml`)

**Files:** 无改动(校验)

- [ ] **Step 1: 运行 jar**

Run:
```bash
./gradlew jar --console=plain
```
Expected:`BUILD SUCCESSFUL`。

- [ ] Step 2: 完成标准已达成(core/parser 编译通过 + `./gradlew test` 全绿 + `./gradlew jar` 通过)。

---

## 自检(与 spec 对照)

- spec「已迁移/待迁移/不迁移」清单 → Task 2/3/4(main)、Task 6/7(test)全部覆盖;`不迁移`列未出现在任何编辑任务中。✔
- 规则 R1–R6 → 各编辑步骤逐条落实;R3 兜底保留在 Task 9 风险说明。✔
- 完成标准(编译 + `./gradlew test` + `./gradlew jar`)→ Task 5/8/9/10 显式校验。✔
- 越界项(不改行为、不加 helper、不做 canvas.translate 优化、不动只读签名)未写入任何任务。✔
