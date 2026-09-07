# skiko Shader 渐变工厂 API 迁移设计(② Gradient/Shader)

日期:2026-09-07
状态:设计(已与用户确认做法分叉)

## 背景

skiko `0.0.0-SNAPSHOT` 重做了 Shader 渐变工厂:`GradientStyle` 类被移除,工厂签名由扁平的 `(几何, colors: IntArray, positions: FloatArray?, style: GradientStyle(tileMode, isPremul, localMatrix))` 改为

```
makeLinearGradient(x0,y0,x1,y1, gradient: org.jetbrains.skia.Gradient, localMatrix: Matrix33? = null)
makeRadialGradient(x,y,radius, gradient, localMatrix = null)
makeTwoPointConicalGradient(x0,y0,startRadius,x1,y1,endRadius, gradient, localMatrix = null)
makeSweepGradient(x,y,startAngle,endAngle, gradient, localMatrix = null)   // 另有仅 center 等重载
```

其中 `org.jetbrains.skia.Gradient` 是值对象:
- `Gradient(colors: Colors, interpolation: Interpolation)`
  - `Colors(colors: Array<Color4f>, positions: FloatArray?, tileMode: FilterTileMode, colorSpace: ColorSpace?)`
  - `Interpolation(inPremul: InPremul /* NO|YES */, colorSpace: ColorSpace = ?, hueMethod: HueMethod = ?)`(colorSpace/hueMethod 有默认;具体默认值实现期用一次编译确认)

颜色类型由 `IntArray` 变 `Array<Color4f>`;`isPremul` 语义并入 `Interpolation.InPremul`;`localMatrix` 从 style 内移到工厂独立参数(仍为 `Matrix33?`)。

`compileKotlin` 当前错误面涉及:3 个渐变子类 + `RenderCustomClip`(debug 渐变)。全库扫描确认 **无测试直接调用 Shader.make / GradientStyle**。

## 目标与完成标准

- 让 `LinearGradient/RadialGradient/SweepGradient.createShader` 与 `RenderCustomClip.debugPaint` 在新 API 下编译通过。
- **仓库公共契约不变**:`com.muedsa.snapshot.paint.gradient.Gradient.createShader(rect: Rect, textDirection: Direction?): Shader` 签名与外部调用(`BoxDecorationPainter` 等)不受影响;改动全部收敛在 5 个文件内部。
- 完成标准沿用全局约定:`./gradlew test` + `./gradlew jar` 通过(需与 ①Path、③Font 一并完成)。
- 视觉尽可能等价;无像素断言的渐变测试仅验证不崩溃 + PNG 输出。

## 已确认的做法决策

1. **共享辅助(集中)**:在仓库 `paint/gradient/Gradient.kt` 基类加一个 `protected` 辅助,集中 `IntArray → Array<Color4f>` 与 `org.jetbrains.skia.Gradient` 的构造,供 Linear/Radial/Sweep 三个子类复用;`RenderCustomClip` 按你的选择**单独内联**(它不继承该基类)。
2. **premul 镜像旧旗标**:旧 `GradientStyle.isPremul` 为 true 处(径向、two-point、扫掠)→ `Interpolation.InPremul.YES`;为 false 处(线性、`RenderCustomClip` 调试渐变)→ `InPremul.NO`。

## 转换规则(R 表)

| # | 内容 |
|---|---|
| G-R1 | 构造渐变对象:工厂参数 `colors/positions/style=` → `gradient = <Gradient 值对象>`,`localMatrix = <原 style 里的 localMatrix>`(无则省/传 null) |
| G-R2 | 颜色:每色 `Color4f(intColor)`(int 按 ARGB 非预乘解释;若该构造语义不符则用显式 `((c ushr 24)&0xFF)/255f …` 兜底并注明) |
| G-R3 | positions 原样传递(`impliedStops()`,与旧一致);tileMode 原样传递 |
| G-R4 | premul:false→`InPremul.NO`,true→`InPremul.YES` |
| G-R5 | import:删除 `import org.jetbrains.skia.GradientStyle`;`Gradient.kt` 基类新增所需 import;`org.jetbrains.skia.Gradient` 与本仓库 `Gradient` 同名冲突时用 `import … as SkiaGradient` 别名 |
| G-R6 | 不改 `createShader` 契约、不改几何/角度换算、不改 RenderCustomClip 调试用的颜色与 positions |

## 逐文件改动

### 文件 1 — `paint/gradient/Gradient.kt`(基类,新增 protected 辅助)

新增 import(注意本仓库类名 `Gradient` 与 `org.jetbrains.skia.Gradient` 冲突,使用别名):

```kotlin
import org.jetbrains.skia.Color4f
import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.Gradient as SkiaGradient
```

在类内(`impliedStops()` 之后)新增:

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

> 实现期微确认 1:若 `SkiaGradient.Interpolation` 的 `colorSpace`/`hueMethod` 非默认参数(编译报缺参),补齐 `colorSpace = ColorSpace.SRGB, hueMethod = HueMethod.SHORTER`(枚举常量名以编译提示为准)。
> 实现期微确认 2:`Color4f(int)` 是否把 int 当 ARGB 非预乘;若视觉异常改用手动逐通道换算。

### 文件 2 — `paint/gradient/LinearGradient.kt`

删除 `import org.jetbrains.skia.GradientStyle`。`createShader` 改:

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

(`GradientStyle(tileMode=…, isPremul=false, localMatrix=…)` → 拆成 `gradient` 里带 tileMode、`localMatrix` 作独立参数。)

### 文件 3 — `paint/gradient/RadialGradient.kt`

删除 `import org.jetbrains.skia.GradientStyle`。普通径向分支(`focal==null…`)改:

```kotlin
return if (focal == null || (focal == center && focalRadius == 0f)) {
    Shader.makeRadialGradient(
        x = centerOffset.x,
        y = centerOffset.y,
        radius = radius * rect.shortestSide,
        gradient = buildSkiaGradient(tileMode = tileMode, inPremul = true),
        localMatrix = transform?.transform(rect)?.toRMO()?.asMatrix33(),
    )
}
```

two-point 分支改(几何数值与旧一致:x0/y0=中心,startRadius=radius;x1/y1=focal,endRadius=focalRadius):

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

### 文件 4 — `paint/gradient/SweepGradient.kt`

删除 `import org.jetbrains.skia.GradientStyle`。`createShader` 改(角度换算 `×180/π` 保持与旧一致):

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

### 文件 5 — `rendering/box/RenderCustomClip.kt`(debug 渐变,单独内联)

`org.jetbrains.skia.*` 通配 import 已覆盖 `Gradient`/`Color4f`,无 import 改动。把 `debugPaint` 中旧调用:

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

改为:

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

(旧 `GradientStyle.DEFAULT` 视为 `isPremul=false` → `InPremul.NO`;`withTileMode(REPEAT)` → `Colors.tileMode = REPEAT`。)

## 验证策略

1. `./gradlew :core:compileKotlin` → `BUILD SUCCESSFUL`(② 部分);全绿需 ①③ 一并完成。
2. `./gradlew test` 中渐变相关测试(`LinearGradientTest/RadialGradientTest/SweepGradientTest`、`RenderCustomClip` 经 clip/backdropfilter widget 测试路径)不崩溃、正常输出 PNG。
3. 若个别渐变视觉肉眼差异明显,首选复核 `Color4f(int)` 语义与 premul 映射(见微确认),按规则修正。

## 非目标

- 不引入双版本兼容层(不打算同时支持新旧快照)。
- 不改仓库上层(`Gradient.createShader` 契约、`BoxDecorationPainter`/UI 调用)。
- 不改渐变几何、颜色集、positions、tileMode、localMatrix 的取值来源。
- 不新增文件/不拆包;复用基类即是对 `IntArray→Color4f` 唯一化,不另立工具对象。

## 参考

- 新 API 事实来源:解包 `skiko-awt-0.0.0-SNAPSHOT.jar` 后 `javap`(`Shader$Companion` 工厂、`Gradient`/`Gradient$Colors`/`Gradient$Interpolation` 构造与参数名)及 `compileKotlin` 的 “candidates not applicable” 候选签名。
- 关联:断面色诊断报告 `docs/2026-09-07-skiko-0.0.0-snapshot-api-breakage-report.md`。
