# 网络测试隔离实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 建立 `network` 标签与 `-PincludeNetwork` 开关,使默认 `./gradlew test` 不再执行任何联网用例;并把本批触及文件的 `org.junit` 断言换成 `kotlin.test`。

**Architecture:** 复用既有 `@Tag("sample")` + `-PincludeSamples` 模式;`core`/`parser` 各自就地改 `tasks.test`;混合类(部分用例联网)用方法级标签,整类联网用类级标签;`CachedNetworkImageTest` 顺带完成冒烟迁移(去 `drawWidget`/`println`,补布局与像素断言)。

**Tech Stack:** Gradle 9.7.1(Kotlin DSL)、JUnit 5 Platform、kotlin.test、skiko、仓库内 `:testkit`。

**Spec:** `docs/superpowers/specs/2026-09-09-network-test-isolation-design.md`

## Global Constraints

- 分支 `test/network-tag-isolation`,base = `main`(`66d74b7`);工作目录 `D:\mine\workspace\snapshot`。
- **提交必须 GPG 签名**(`git commit -S`);偶发 `No passphrase given` **原样重试一次**。提交后 `git log -1 --format="%h %G?"` 应为 `G`。
- **测试命令统一加 `--offline`**:本机到 `maven.pkg.jetbrains.space` 的 TLS 握手失败,`skiko-awt:0.0.0-SNAPSHOT` 只能走本地缓存。(若 `git fetch` 被 `SSL_ERROR_SYSCALL` 阻断,用 `git -c http.version=HTTP/1.1 fetch origin main`,该变体实测可用。)
- 断言风格统一 `kotlin.test`;**`@Tag` 是唯一例外**(来自 `org.junit.jupiter.api`,kotlin.test 无标签注解)。
- `-PincludeNetwork` 的实跑**可能因外网不可达而失败**——如实记录,不得把失败说成绿。

---

### Task 1: 构建配置与文档(开关 + 标签约定)

**Files:**
- Modify: `core/build.gradle.kts`(`tasks.test` 块)
- Modify: `parser/build.gradle.kts`(`tasks.test` 块)
- Modify: `README.md`(第 156 行那段引用块)
- Modify: `docs/testing/README.md`(§11 约定与命名)

**Interfaces:**
- Produces: Gradle 属性 `includeNetwork`;默认排除标签集合 `sample` + `network`。后续任务的标签依赖此配置生效。

- [ ] **Step 1: 改 `core/build.gradle.kts`**

把

```kotlin
tasks.test {
    useJUnitPlatform()
    val includeSamples = providers.gradleProperty("includeSamples").isPresent
    if (includeSamples) {
        useJUnitPlatform { includeTags("sample") }
    } else {
        useJUnitPlatform { excludeTags("sample") }
    }
```

替换为

```kotlin
tasks.test {
    useJUnitPlatform()
    // 默认排除需外部环境/人工触发的用例;显式 -PincludeSamples / -PincludeNetwork 时只跑对应标签
    val onlyTags = buildList {
        if (providers.gradleProperty("includeSamples").isPresent) add("sample")
        if (providers.gradleProperty("includeNetwork").isPresent) add("network")
    }
    if (onlyTags.isEmpty()) {
        useJUnitPlatform { excludeTags("sample", "network") }
    } else {
        useJUnitPlatform { includeTags(*onlyTags.toTypedArray()) }
    }
```

- [ ] **Step 2: 对 `parser/build.gradle.kts` 做同样的替换**

其 `tasks.test` 块与 core 完全同构(同样以 `val includeSamples = …` 开头),用与 Step 1 相同的 old/new 文本替换。

- [ ] **Step 3: 更新 `README.md`**

把第 156 行那段引用块的结尾

```
手动再生成:`./gradlew :core:test -PincludeSamples` 与 `./gradlew :parser:test -PincludeSamples`。注:`-PincludeSamples` 只运行这些被标注的类,不执行完整测试套件。
```

替换为

```
手动再生成:`./gradlew :core:test -PincludeSamples` 与 `./gradlew :parser:test -PincludeSamples`。注:`-PincludeSamples` 只运行这些被标注的类,不执行完整测试套件。
>
> 依赖外网的测试(网络图片缓存、`CachedNetworkImage`、含 `ImageEmojiSpan`/`<Emoji>` 的用例)统一标注 `@Tag("network")`,同样从默认 `./gradlew test` 排除;手动运行:`./gradlew :core:test -PincludeNetwork` 与 `./gradlew :parser:test -PincludeNetwork`(语义与 `-PincludeSamples` 相同:只跑该标签)。
```

- [ ] **Step 4: 更新 `docs/testing/README.md` §11**

在「断言风格」那一条之后插入一条:

```markdown
- **标签例外**:`@Tag` 来自 `org.junit.jupiter.api`(kotlin.test 无标签注解)。现有两类:`sample`(样例再生成)与 `network`(依赖外网的用例,如网络图片缓存、`CachedNetworkImage`、含 `ImageEmojiSpan`/`<Emoji>` 的测试)。默认 `./gradlew test` **排除**这两类;显式运行用 `-PincludeSamples` / `-PincludeNetwork`(可同时给出,取并集)。
```

- [ ] **Step 5: 验证配置可用(此时尚未打标签,默认套件仍会跑网络用例,属预期)**

Run: `./gradlew :core:test --offline --console=plain --tests "*LimitedInputStreamTest*" 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL(配置可编译,未打标签的本地类可正常执行)。

- [ ] **Step 6: 提交**

```bash
git add core/build.gradle.kts parser/build.gradle.kts README.md docs/testing/README.md
git commit -S -m "build(test): 增加 network 标签开关(默认排除 sample+network)"
```

---

### Task 2: `core/tools` 四个文件

**Files:**
- Modify: `core/src/test/kotlin/com/muedsa/snapshot/tools/SimpleLimitedNetworkImageCacheTest.kt`
- Modify: `core/src/test/kotlin/com/muedsa/snapshot/tools/SimpleNoLimitedNetworkImageCacheTest.kt`
- Modify: `core/src/test/kotlin/com/muedsa/snapshot/tools/LimitedImageInputStreamTest.kt`
- Modify: `core/src/test/kotlin/com/muedsa/snapshot/tools/LimitedInputStreamTest.kt`

**Interfaces:**
- Consumes: Task 1 的 `network` 标签与默认排除。
- Produces: 无(测试内部)。

- [ ] **Step 1: `SimpleLimitedNetworkImageCacheTest` —— 类级标签 + 断言转换**

- 加 import:`import org.junit.jupiter.api.Tag`;把 `import org.junit.jupiter.api.assertThrows` 改为 `import kotlin.test.assertFailsWith`
- 类上加 `@Tag("network")`:
```kotlin
@Tag("network")
class SimpleLimitedNetworkImageCacheTest {
```
- 两处 `assertThrows<Throwable> {` / `assertThrows<IllegalStateException> {` 改为 `assertFailsWith<…> {`(泛型与花括号不动)

- [ ] **Step 2: `SimpleNoLimitedNetworkImageCacheTest` —— 同上**

- 加 `import org.junit.jupiter.api.Tag`;`assertThrows` → `assertFailsWith`
- 类上加 `@Tag("network")`(5 个用例全部联网)
- `http_404_test` 的 `assertThrows<IllegalStateException>` → `assertFailsWith<IllegalStateException>`

- [ ] **Step 3: `LimitedImageInputStreamTest` —— 方法级标签 + 断言转换**

- 加 `import org.junit.jupiter.api.Tag`;`assertThrows` → `assertFailsWith`
- **只**给 `image_format_test` 打标签:
```kotlin
    @Tag("network")
    @Test
    fun image_format_test() {
```
- `header_buffer_read_test` 保持无标签(纯本地,留在默认套件);其 `assertThrows<Throwable>` → `assertFailsWith<Throwable>`

- [ ] **Step 4: `LimitedInputStreamTest` —— 仅断言转换(不加标签)**

`assertThrows` → `assertFailsWith`,加 `import kotlin.test.assertFailsWith`,删 `import org.junit.jupiter.api.assertThrows`。两处调用改写为 `assertFailsWith<IOException> { … }`。

- [ ] **Step 5: 验证标签生效**

> 注意:`--tests` 过滤到**被排除的类**会以 `No tests found for given includes` 直接 BUILD FAILED(实测),这不是失败信号,不要用它判断;改用"跑全量 + 查结果目录"。Gradle 每次运行会清空 `build/test-results/test/`,故"该类的 XML 不存在"即"未执行"。

Run: `./gradlew :core:test --offline --console=plain 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL。

Run:
```bash
ls core/build/test-results/test/ | grep -E "SimpleLimitedNetworkImageCacheTest|SimpleNoLimitedNetworkImageCacheTest" && echo "不该出现" || echo "EXCLUDED (预期)"
grep -o 'tests="[0-9]*"' core/build/test-results/test/TEST-com.muedsa.snapshot.tools.LimitedImageInputStreamTest.xml
```
Expected: 第一行输出 `EXCLUDED (预期)`;第二行 `tests="1"`(只剩 `header_buffer_read_test`)。

- [ ] **Step 6: 提交**

```bash
git add core/src/test/kotlin/com/muedsa/snapshot/tools/
git commit -S -m "test(core): tools 网络用例打 network 标签并统一 kotlin.test"
```

---

### Task 3: `core/widget` 两个文件

**Files:**
- Modify: `core/src/test/kotlin/com/muedsa/snapshot/widget/CachedNetworkImageTest.kt`(整文件替换)
- Modify: `core/src/test/kotlin/com/muedsa/snapshot/widget/text/TextTest.kt`(方法级标签)

**Interfaces:**
- Consumes: `testkit` 的 `rootLayout`/`assertSize`/`snapshotPixels`/`expectRegionOpaque`。

- [ ] **Step 1: 用下面的内容整体替换 `CachedNetworkImageTest.kt`**

```kotlin
package com.muedsa.snapshot.widget

import com.muedsa.snapshot.assertSize
import com.muedsa.snapshot.expectRegionOpaque
import com.muedsa.snapshot.rootLayout
import com.muedsa.snapshot.snapshotPixels
import org.jetbrains.skia.Color
import org.jetbrains.skia.Rect
import org.junit.jupiter.api.Tag
import kotlin.test.Test

@Tag("network")
class CachedNetworkImageTest {

    private companion object {
        const val IMAGE_URL = "https://samples-files.com/samples/images/jpg/1280-720-sample.jpg"
        const val SIDE = 400f
        const val CANVAS = 800f
    }

    // 两个 Column 各含两张 400x400 图片,并排 → 800x800(实测校准)。
    private fun Widget.networkImageScene(noCache: Boolean) {
        Row {
            Column {
                CachedNetworkImage(url = IMAGE_URL, width = SIDE, height = SIDE, noCache = noCache)
                CachedNetworkImage(url = IMAGE_URL, width = SIDE, height = SIDE, noCache = noCache)
            }
            Column {
                CachedNetworkImage(url = IMAGE_URL, width = SIDE, height = SIDE, noCache = noCache)
                CachedNetworkImage(url = IMAGE_URL, width = SIDE, height = SIDE, noCache = noCache)
            }
        }
    }

    private fun assertNetworkImageScene(noCache: Boolean) {
        rootLayout { networkImageScene(noCache) }.assertSize(CANVAS, CANVAS)
        // 透明底:图片未加载则整幅透明,区域不透明断言会失败
        val pixmap = snapshotPixels(background = Color.TRANSPARENT) { networkImageScene(noCache) }
        expectRegionOpaque(pixmap, Rect.makeXYWH(0f, 0f, CANVAS, CANVAS))
    }

    @Test
    fun networkImage_test() {
        assertNetworkImageScene(noCache = false)
    }

    @Test
    fun networkImage_noCache_test() {
        assertNetworkImageScene(noCache = true)
    }
}
```

- [ ] **Step 2: 给 `TextTest.image_emoji_test` 打方法级标签**

- 加 `import org.junit.jupiter.api.Tag`
- 在 `image_emoji_test` 上加标签:

```kotlin
    @Tag("network")
    @Test
    fun image_emoji_test() {
```

其余 4 个用例(`simple_text_test`/`text_span_test`/`widget_span_test`/`style_merge_test`)不动,继续留在默认套件。

- [ ] **Step 3: 验证**

Run: `./gradlew :core:test --offline --console=plain 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL。

Run:
```bash
ls core/build/test-results/test/ | grep CachedNetworkImageTest && echo "不该出现" || echo "EXCLUDED (预期)"
grep -o 'tests="[0-9]*"' core/build/test-results/test/TEST-com.muedsa.snapshot.widget.text.TextTest.xml
```
Expected: 第一行 `EXCLUDED (预期)`;第二行 `tests="4"`(`image_emoji_test` 已排除)。

- [ ] **Step 4: 提交**

```bash
git add core/src/test/kotlin/com/muedsa/snapshot/widget/CachedNetworkImageTest.kt core/src/test/kotlin/com/muedsa/snapshot/widget/text/TextTest.kt
git commit -S -m "test(core): CachedNetworkImageTest 迁移为断言 + network 标签(TextTest 方法级)"
```

---

### Task 4: `parser` 两个文件

**Files:**
- Modify: `parser/src/test/kotlin/com/muedsa/snapshot/parser/widget/ImageParserTest.kt`
- Modify: `parser/src/test/kotlin/com/muedsa/snapshot/parser/widget/TextParserTest.kt`

- [ ] **Step 1: `ImageParserTest` —— 类级标签**

加 `import org.junit.jupiter.api.Tag`,类上加:

```kotlin
@Tag("network")
class ImageParserTest {
```

（该文件已用 `kotlin.test.Test`,无需转换。）

- [ ] **Step 2: `TextParserTest` —— 方法级标签**

加 `import org.junit.jupiter.api.Tag`,只给 `rich_text_test` 加:

```kotlin
    @Tag("network")
    @Test
    fun rich_text_test() {
```

其余用例(`parse_text_attr_test`/`buildWidget_test`/`font_diff_test`/`text_diff_test`)不动。

- [ ] **Step 3: 验证**

Run: `./gradlew :parser:test --offline --console=plain 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL。

Run:
```bash
ls parser/build/test-results/test/ | grep ImageParserTest && echo "不该出现" || echo "EXCLUDED (预期)"
grep -o 'tests="[0-9]*"' parser/build/test-results/test/TEST-com.muedsa.snapshot.parser.widget.TextParserTest.xml
```
Expected: 第一行 `EXCLUDED (预期)`;第二行 `tests="4"`(`rich_text_test` 已排除)。

- [ ] **Step 4: 提交**

```bash
git add parser/src/test/kotlin/com/muedsa/snapshot/parser/widget/
git commit -S -m "test(parser): 网络用例打 network 标签(ImageParserTest 类级 / TextParserTest 方法级)"
```

---

### Task 5: 全量验证与实跑记录

**Files:** 无改动(仅验证)

- [ ] **Step 1: 默认套件全绿且不含网络用例**

Run: `./gradlew test --offline --console=plain 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL。

Run:
```bash
ls core/build/test-results/test/ | grep -E "Simple.*NetworkImageCacheTest|CachedNetworkImageTest" || echo "NO_NETWORK_RESULTS (预期)"
grep -o 'tests="[0-9]*"' core/build/test-results/test/TEST-com.muedsa.snapshot.widget.text.TextTest.xml
grep -o 'tests="[0-9]*"' core/build/test-results/test/TEST-com.muedsa.snapshot.tools.LimitedImageInputStreamTest.xml
grep -o 'tests="[0-9]*"' parser/build/test-results/test/TEST-com.muedsa.snapshot.parser.widget.TextParserTest.xml
```
Expected: 第一行输出 `NO_NETWORK_RESULTS (预期)`;三个计数依次为 `4`、`1`、`4`。

- [ ] **Step 2: 聚合计数(与打标签前对比)**

Run:
```bash
grep -ho 'tests="[0-9]*" skipped="[0-9]*" failures="[0-9]*" errors="[0-9]*"' core/build/test-results/test/*.xml | awk -F'"' '{t+=$2; f+=$6; e+=$8} END {print "core tests="t, "failures="f, "errors="e}'
grep -ho 'tests="[0-9]*" skipped="[0-9]*" failures="[0-9]*" errors="[0-9]*"' parser/build/test-results/test/*.xml | awk -F'"' '{t+=$2; f+=$6; e+=$8} END {print "parser tests="t, "failures="f, "errors="e}'
```
Expected: failures=0、errors=0;总用例数比打标签前**减少**(被排除的联网用例)。

- [ ] **Step 3: 打包**

Run: `./gradlew jar --offline --console=plain 2>&1 | tail -3`
Expected: BUILD SUCCESSFUL。

- [ ] **Step 4: 网络用例实跑(如实记录,不保证成功)**

Run: `./gradlew :core:test --offline --console=plain -PincludeNetwork 2>&1 | tail -15`
Expected: 若外网可达 → BUILD SUCCESSFUL;若不可达 → 记录失败用例与原因,**不得改述为通过**。该结果只用于记录,不阻塞本批(标签正确性已由 Step 1 的 `NO_NETWORK_RESULTS` 验证)。

- [ ] **Step 5: 静态核对**

Run:
```bash
grep -rn "org.junit.jupiter.api.assertThrows\|org.junit.jupiter.api.Test" \
  core/src/test/kotlin/com/muedsa/snapshot/tools/ \
  core/src/test/kotlin/com/muedsa/snapshot/widget/CachedNetworkImageTest.kt \
  core/src/test/kotlin/com/muedsa/snapshot/widget/text/TextTest.kt \
  parser/src/test/kotlin/com/muedsa/snapshot/parser/widget/ImageParserTest.kt \
  parser/src/test/kotlin/com/muedsa/snapshot/parser/widget/TextParserTest.kt
```
Expected: 无匹配(退出码 1)。

Run: `git status --short`
Expected: 无输出。

- [ ] **Step 6: 汇报**

汇总:提交清单、默认套件用例数变化、`-PincludeNetwork` 实跑结果(含失败如实说明)、任何偏差。

---

## 收尾(不在任务内,由主会话执行)

- 更新记忆:`migrate-widget-smoke-batch1` 与 `snapshot-testkit-refactor`(网络标签已落地)。
- 推送分支并备好 PR 文案(标题建议 `test: 网络测试隔离(network 标签 + 默认排除)`);PR 文案只写评审所需信息,不含流程约定与本机命令特例。
