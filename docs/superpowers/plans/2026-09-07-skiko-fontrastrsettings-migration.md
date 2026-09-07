# paragraph FontRastrSettings 弃用迁移实现计划(③)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 消除 `TextPainter.kt` 中 `FontRastrSettings` / `ParagraphStyle.fontRastrSettings` 的 ERROR 级弃用错误,并把原默认光栅化设置(SUBPIXEL_ANTI_ALIAS / NORMAL / subpixel=true)显式落到合并基准 `DEFAULT_TEXT_STYLE`。

**Architecture:** 删除 `ParagraphStyle.apply{ fontRastrSettings = … }` 行与 `DEFAULT_FONT_RASTR_SETTINGS` 常量;在 `DEFAULT_TEXT_STYLE` 初始化上 `apply` 三个 `TextStyle` 属性(`fontEdging`/`fontHinting`/`subpixel`),行为确定、不依赖新默认值。

**Tech Stack:** Kotlin 2.4.0 / JVM、Gradle(core 模块)、skiko-awt `0.0.0-SNAPSHOT`(`org.jetbrains.skia.paragraph.*`)。

**Spec:** `docs/superpowers/specs/2026-09-07-skiko-fontrastrsettings-migration-design.md`

---

## 执行前必读

- 仓库根 `D:\mine\workspace\snapshot` 运行命令(git-bash 用 `./gradlew`)。
- 所有提交统一 `git -c commit.gpgsign=false commit -m "…"`(gpg 不可用)。
- 本计划编译验证以“`TextPainter.kt` 的错误从编译输出消失”为准;module 级 `BUILD SUCCESSFUL`/`test`/`jar` 需 ①Path 与 ②Gradient 计划一并执行。

## 涉及文件

- Modify: `core/src/main/kotlin/com/muedsa/snapshot/paint/text/TextPainter.kt`
- Modify: `core/src/main/kotlin/com/muedsa/snapshot/paint/text/TextStyle.kt`(编译期勘误补足,见 spec 位置 D;把三属性贯通到 `org.jetbrains.skia.paragraph.TextStyle`)

---

### Task 1: 删除 paragraph 级 `fontRastrSettings` 与常量

**Files:**
- Modify: `core/src/main/kotlin/com/muedsa/snapshot/paint/text/TextPainter.kt`

- [ ] **Step 1: 删除 `ParagraphStyle` 赋值行**

删除 `createParagraph` 内 `ParagraphStyle().apply { … }` 块中的这一行:

```kotlin
                fontRastrSettings = DEFAULT_FONT_RASTR_SETTINGS
```

(该块中上一行为 `textHeightMode?.let { this.heightMode = it }`。)

- [ ] **Step 2: 删除 `DEFAULT_FONT_RASTR_SETTINGS` 常量**

删除整个 val 块:

```kotlin
        val DEFAULT_FONT_RASTR_SETTINGS: FontRastrSettings = FontRastrSettings(
            edging = FontEdging.SUBPIXEL_ANTI_ALIAS,
            hinting = FontHinting.NORMAL,
            subpixel = true
        )
```

(该块位于 `val FONT_COLLECTION = …` 之后、`var DEFAULT_TEXT_STYLE` 之前。删除后 `FontRastrSettings`/`FontEdging`/`FontHinting` 若在文件内不再出现,由 `org.jetbrains.skia.*`/`org.jetbrains.skia.paragraph.*` 通配 import 覆盖,无需改 import。)

- [ ] Step 3: 不提交。

---

### Task 2: 把默认设置显式落到 `DEFAULT_TEXT_STYLE`

**Files:**
- Modify: `core/src/main/kotlin/com/muedsa/snapshot/paint/text/TextPainter.kt`

- [ ] **Step 1: 改 `DEFAULT_TEXT_STYLE` 初始化**

把:

```kotlin
        var DEFAULT_TEXT_STYLE: TextStyle = TextStyle(
            color = kDefaultTextColor,
            fontSize = kDefaultFontSize
        )
```

替换为:

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

(`TextStyle` 三属性为可变 `var`,`apply` 合法。若 `fontEdging` 等属性名与编译提示不符——概率极低——按编译提示调整。)

---

### Task 3: 编译验证(③ 部分)并提交

**Files:** 无新改动(校验 Task 1–2)

- [ ] **Step 1: 编译并核对 TextPainter 错误消失**

Run:
```bash
./gradlew :core:compileKotlin --console=plain 2>&1 | grep "TextPainter"
```
Expected:输出为空(不再出现 `TextPainter.kt` 的任何错误)。
仍允许出现:Path 文件与 `gradient/`、`RenderCustomClip` 的错误——那些属 ①/② 计划范围。

- [ ] **Step 2: 提交**

```bash
git add core/src/main/kotlin/com/muedsa/snapshot/paint/text/TextPainter.kt \
        core/src/main/kotlin/com/muedsa/snapshot/paint/text/TextStyle.kt
git -c commit.gpgsign=false commit -m "refactor(core): 移除 FontRastrSettings 弃用,默认光栅化设置落到 TextStyle"
```

> `TextStyle.kt` 必须同 commit(实现"显式落到 DEFAULT_TEXT_STYLE"的贯通管线;漏掉会导致编译不过)。

---

### Task 4: 说明(交给执行者而非本计划单独验证)

- module 级 `BUILD SUCCESSFUL` 与 `./gradlew test`/`jar` 需 ①Path、②Gradient 计划一并执行后达成。
- `TextPainterTest` 与各含 `Text` 的 widget 测试无像素断言;若肉眼字形光栅化差异明显,复核 `TextStyle` 三属性是否被 span 继承(合并基准机制),必要时在具体 span 样式中显式补齐。

## 自检(与 spec 对照)

- spec 位置 A/B/C → Task 1(两处删除)+ Task 2(一处赋值)。✔
- spec 做法决策(显式设到 DEFAULT_TEXT_STYLE)→ Task 2。✔
- spec 边界(能删即删,不做 @Suppress、不造兼容层)→ 无冲突任务。✔
