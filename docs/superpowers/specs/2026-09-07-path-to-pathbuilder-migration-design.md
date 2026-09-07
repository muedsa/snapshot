# Path → PathBuilder 迁移设计(不可变 Path)

日期:2026-09-07
状态:已获用户批准(方案 B)

## 背景与目标

仓库依赖的 `skiko 0.0.0-SNAPSHOT`(gradle 缓存内核对)已将 `org.jetbrains.skia.Path` 改为**不可变对象**:

- `Path` 上的变更方法(`moveTo/lineTo/addRect/addOval/addRRect/arcTo/offset` 等)已全部移除,仅保留只读方法。
- 新增可链式调用的 `PathBuilder`(`org.jetbrains.skia.PathBuilder`),同名方法均返回 `PathBuilder`,用 `.detach()`(或 `.snapshot()`)产出不可变 `Path`;支持 `PathBuilder(Path)` 以现有不可变 Path 为起点续接;构造器支持指定填充类型 `PathBuilder(PathFillMode)`。

**目标**:把仓库(core main + test,含 parser 若有引用)所有“直接构造/变更 Path”的地方迁移为使用 `PathBuilder`,保证编译通过、行为零变化。

**成功标准(用户确认)**:`./gradlew test` 全绿(`:core:compileTestKotlin` 覆盖全部 main+test 漏网点),`./gradlew jar` 通过。渲染输出不要求像素比对(仓库单测仅输出 PNG、无像素断言基线)。

## 范围盘点

### 已迁移(当前 WIP,未经编译验证)
- `paint/decoration/Borders.kt`(共享 builder + `reset()` 复用)→ 按第 2 节重写为每边独立 builder
- `paint/decoration/BoxBorder.kt`(`getInnerPath/getOuterPath` 已用 `PathBuilder()...detach()`)

### 待迁移(生产代码 main)
| 文件 | 点 | 规则 |
|---|---|---|
| `paint/decoration/Borders.kt` | 4 个 border 分支 | 见上,重写 |
| `paint/decoration/BoxDecoration.kt` | `getClipPath` 3 分支 | R1 |
| `paint/decoration/BoxDecorationPainter.kt` | `paintBackgroundImage` 内 clipPath | R1 |
| `rendering/PaintingContext.kt` | `pushClipPath` offset 复制 | R3 |
| `rendering/box/RenderClipOval.kt` | `getClipPath` 构建 + debugPaint offset | R1 + R3 |
| `rendering/box/RenderClipPath.kt` | `defaultClip` + debugPaint offset | R1 + R3 |
| `rendering/box/RenderPadding.kt` | debugPaint EVEN_ODD 双矩形 | R4 |

### 待迁移(测试代码)
| 文件 | 规则 |
|---|---|
| `widget/ClipTest.kt` | R2(apply 块) |
| `widget/BackdropFilterTest.kt` | R2(apply 块,arcTo) |
| `LogoCreator.kt` | R2(两处 apply 块) |
| `render/box/RenderClipPathTest.kt` | R1(链式 ×2) |
| `render/ClipPathLayerTest.kt` | R1(链式) |

### 不迁移(仅消费 Path,签名/实现均不动)
`Decoration.kt`、`ShapeBorder.kt`、`CompoundBorder.kt`、`DecorationImagePainter.kt`、`InternalDecorationImagePainter.kt`、`ClipContext.kt`、`ClipPathLayer.kt`、`widget/ClipPath.kt`、`widget/ClipOval.kt`、`rendering/box/RenderCustomClip.kt`、`widget/Container.kt`。

## 统一转换规则

| 规则 | 旧写法(可变 Path) | 新写法(不可变) |
|---|---|---|
| R1 链式构造 | `Path().lineTo(x,y).lineTo(x2,y2)` | `PathBuilder().lineTo(x,y).lineTo(x2,y2).detach()` |
| R2 apply 块构造 | `Path().apply { moveTo(..); lineTo(..); arcTo(..) }` | `PathBuilder().apply { ... }.detach()`(块内方法同名同参,几乎逐字保留) |
| R3 复制平移 | `Path().also { p.offset(dx, dy, it) }` | `PathBuilder(p).offset(dx, dy).detach()` |
| R4 设置填充类型 | `Path().apply { fillMode = PathFillMode.EVEN_ODD; ... }` | `PathBuilder(PathFillMode.EVEN_ODD).addRect(..)....detach()` |
| R5 import | — | 非 `org.jetbrains.skia.*` 通配文件补 `import org.jetbrains.skia.PathBuilder`;仅当仍有 `Path` 类型标注时才保留 `import ...Path` |
| R6 边界 | — | 任何残余 Path 变更点按 R1–R4 收敛;只读消费点/签名不改 |

行为零变化保证:命令序列、走向、EVEN_ODD 等填充语义、STROKE/FILL 分支全部原样保留。`RenderClipOval.cachedPath` 缓存保留——Path 不可变后缓存仅更安全。

## 设计细节

### 1. Borders.kt 收尾(重写为每边独立 builder)

当前 WIP 用“共享 `PathBuilder()` + 各边 `reset()` 复用”。重写为每边独立 builder,逻辑逐字保留:

```kotlin
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
```

right / bottom / left 分支同构替换。hairline(`width==0`)分支行为不变。

### 2. R3 唯一风险点与兜底

`PathBuilder(Path)` 按“复制现有不可变 Path 内容为起点”使用。若实现阶段实测其不复制或行为不符,退化为几何等价的:

```kotlin
PathBuilder().addPath(p, dx, dy, PathAddMode.APPEND).detach()
```

### 3. import 卫生

补 `import org.jetbrains.skia.PathBuilder` 的文件:`BoxDecoration.kt`、`BoxDecorationPainter.kt`、`RenderClipOval.kt`、`RenderClipPath.kt`、`widget/ClipTest.kt`、`widget/BackdropFilterTest.kt`、`LogoCreator.kt`、`render/box/RenderClipPathTest.kt`、`render/ClipPathLayerTest.kt`。`Borders.kt`、`PaintingContext.kt`、`RenderPadding.kt`、`BoxBorder.kt` 已是 `org.jetbrains.skia.*` 通配或已引用,无需处理。

## 验证策略

1. `./gradlew test` 全绿(含 clip 相关的真实像素断言:`RenderClipPathTest`、`ClipPathLayerTest`、`RenderClipOvalTest` 中 origin-vs-clip 比较会实际跑通 clip 路径几何)。
2. `./gradlew jar` 通过(编译层面与上面同一结果;对齐 CI `test.yaml` / `jar.yaml`)。
3. 若编译期暴露清单外漏网点,一律按 R1–R4 收敛,最终以编译全绿为准。

## 非目标(避免越界)

- 不做渲染行为的任何改动。
- 不引入新 helper/DSL(如 `PathBuilder{}` 扩展),避免新增仓库级 API(YAGNI)。
- 不把 offset 复制改造成 canvas.translate 等“省拷贝”优化。
- 不动只读消费端签名(`getClipPath/getOuterPath/getInnerPath/pushClipPath/clipper: (Size)->Path`)。

## 提交结构

改动仅触及 ~13 个源文件 + import,单次 commit 即可;如需按 CI 惯例走 PR,在实现阶段开分支。
