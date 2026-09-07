# paragraph FontRastrSettings 弃用迁移设计(③ FontRastr)

日期:2026-09-07
状态:设计(已与用户确认做法分叉)

## 背景

skiko `0.0.0-SNAPSHOT` 把 `org.jetbrains.skia.paragraph.FontRastrSettings` 及 `ParagraphStyle.fontRastrSettings` 标记为 **`DeprecationLevel.ERROR`** 弃用(compileKotlin 直接报错,非警告;项目未开 `-Werror`)。弃用消息指向替代:设置已下沉为 `org.jetbrains.skia.paragraph.TextStyle` 的**三个独立属性** `fontEdging` / `fontHinting` / `subpixel`(javap 实测存在 get/set)。

全库扫描确认:仅 `core/src/main/kotlin/com/muedsa/snapshot/paint/text/TextPainter.kt` 一处使用。

## 目标与完成标准

- 去掉 `ParagraphStyle.fontRastrSettings` 的赋值与 `FontRastrSettings` 常量构造,编译错误清零。
- 原 paragraph 级默认光栅化设置(`FontEdging.SUBPIXEL_ANTI_ALIAS` / `FontHinting.NORMAL` / `subpixel=true`)改为**显式设到合并基准 `DEFAULT_TEXT_STYLE`**,不依赖新 API 的默认值 → 行为确定。
- 完成标准沿用全局约定:`./gradlew test` + `./gradlew jar` 通过(需与 ①Path、②Gradient 一并完成)。

## 已确认的做法决策

1. 删除 `ParagraphStyle.apply { … fontRastrSettings = … }` 那一行。
2. 删除 `DEFAULT_FONT_RASTR_SETTINGS` 常量(整个 val 块)。
3. 把三个属性**显式设置**到 `DEFAULT_TEXT_STYLE`(`TextStyle(color=…, fontSize=…)` 之后用 `apply` 设属性;避免依赖 `TextStyle` 构造器对这三参数的命名/默认)。

## 位置与逐字改动

涉及 `TextPainter.kt`;另含 **`paint/text/TextStyle.kt`**(见位置 D,编译期勘误后补足)。

### 位置 A — `createParagraph` 内 `ParagraphStyle().apply { … }`(第 81 行)

删除这一行:

```kotlin
                fontRastrSettings = DEFAULT_FONT_RASTR_SETTINGS
```

### 位置 B — 常量定义(约第 288–292 行)

删除整个:

```kotlin
        val DEFAULT_FONT_RASTR_SETTINGS: FontRastrSettings = FontRastrSettings(
            edging = FontEdging.SUBPIXEL_ANTI_ALIAS,
            hinting = FontHinting.NORMAL,
            subpixel = true
        )
```

(删除后若 `FontEdging`/`FontHinting`/`FontRastrSettings` 在该文件不再被引用:它们来自 `org.jetbrains.skia.*` / `org.jetbrains.skia.paragraph.*` 通配 import,无单行 import 可删,保留即可。)

### 位置 C — `DEFAULT_TEXT_STYLE` 初始化(约第 294–296 行)

把:

```kotlin
        var DEFAULT_TEXT_STYLE: TextStyle = TextStyle(
            color = kDefaultTextColor,
            fontSize = kDefaultFontSize
        )
```

改为:

```kotlin
        var DEFAULT_TEXT_STYLE: TextStyle = TextStyle(
            color = kDefaultTextColor,
            fontSize = kDefaultFontSize
        ).apply {
            fontEdging = FontEdging.SUBPIXEL_ANTI_ALIAS
            fontHinting = FontHinting.NORMAL
            subpixel = true
        }
```

(`DEFAULT_TEXT_STYLE` 是**仓库 `paint/text/TextStyle.kt` 的 data class**(非 `org.jetbrains.skia.paragraph.TextStyle`);它经 `toSkikoTextStyle()` 映射成 paragraph `TextStyle`。仓库 TextStyle 需先补上这三个属性(见位置 D),此处 `.apply` 才可解析。)

### 位置 D — `paint/text/TextStyle.kt`(编译期勘误,必改)

编译证实:`paint/text` 包内 `TextStyle` 解析为仓库自己的 data class。要让 `DEFAULT_TEXT_STYLE` 的三属性经 `InlineSpan.updateMergedStyle`(空字段回填父样式)传给各 span,再经 `toSkikoTextStyle()` 落到 `org.jetbrains.skia.paragraph.TextStyle`,需给仓库 `TextStyle` 加三个可选属性并贯通:

1. import 增加 `org.jetbrains.skia.FontEdging`、`org.jetbrains.skia.FontHinting`。
2. data class 参数表末尾新增:
   ```kotlin
       var fontEdging: FontEdging? = null,
       var fontHinting: FontHinting? = null,
       var subpixel: Boolean? = null,
   ```
3. `isEmpty()` 追加 `&& fontEdging == null && fontHinting == null && subpixel == null`。
4. `mergeFrom()` 追加(子>父 `?:`):
   ```kotlin
               fontEdging = fontEdging ?: style.fontEdging,
               fontHinting = fontHinting ?: style.fontHinting,
               subpixel = subpixel ?: style.subpixel
   ```
5. `toSkikoTextStyle()` 在 `baselineMode?.let` 后追加:
   ```kotlin
           fontEdging?.let { textStyle.fontEdging = it }
           fontHinting?.let { textStyle.fontHinting = it }
           subpixel?.let { textStyle.subpixel = it }
   ```
(新参数均带默认且位于末尾,现有调用点不受影响;`mergeFrom` 的 `?:` 保证 span 未显式设置时继承 `DEFAULT_TEXT_STYLE` 的默认。)

## 语义说明与边界

- 旧模型:光栅化设置在 `ParagraphStyle`(paragraph 级,对所有 run 生效)。
- 新模型:设置在 `TextStyle`(逐样式)。`TextPainter` 在构建时 `text.updateMergedStyle(DEFAULT_TEXT_STYLE)` 把基准样式并入文本树,故把三属性放 `DEFAULT_TEXT_STYLE` 后,未单独指定这三属性的 span 会继承到旧默认值,等价于原 paragraph 级默认。若个别 span 显式构造 `TextStyle()` 且合并语义不继承这三属性,则回到新 API 的 `TextStyle` 默认(大概率同为 SUBPIXEL/NORMAL/true)——属 API 重构的固有行为,不强行兼容。
- 实现期用一次 `:core:compileKotlin` 确认该行删除后不再有 FontRastrSettings 相关错误;若 `TextStyle` 三属性名与弃用消息不一致(不太可能),以编译提示为准微调。

## 验证策略

1. `./gradlew :core:compileKotlin` → `BUILD SUCCESSFUL`(③ 部分;全绿需 ①② 一并完成)。
2. `./gradlew test` 中文本相关测试(`TextPainterTest`、各含 `Text` 的 widget 测试)不崩溃、正常输出 PNG(无像素断言)。

## 非目标

- 不保留对 `FontRastrSettings` / `ParagraphStyle.fontRastrSettings` 的任何引用(含 `@Suppress` 压制)——能删即删,避免残留弃用。
- 不改 paragraph 其余设置、不改 TextSpan/TextStyle 的整体结构。
- 不为“span 独立默认”再造 paragraph 级兼容层。

## 参考

- 新 API 事实来源:解包 `skiko-awt-0.0.0-SNAPSHOT.jar` 后 `javap org.jetbrains.skia.paragraph.{TextStyle,ParagraphStyle,FontRastrSettings}`;弃用文案来自 `compileKotlin` 输出。
- 关联:断面色诊断报告 `docs/2026-09-07-skiko-0.0.0-snapshot-api-breakage-report.md`。
