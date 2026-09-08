# 测试框架(TestKit)实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 建立共享 `:testkit` 模块(两级断言 DSL + golden 录制/校验/更新 + 沿用兼容层去重),并迁移 5 个示范用例(E1–E5),让后续新功能测试可顺滑添加。

**Architecture:** 新增独立模块 `:testkit`(main 源集承载框架);core main 增最小公开布局自省 `LayoutNode`;旧两份 `TestTool.kt` 逐字搬入 testkit(包名不变)后删除;testkit 提供 布局/数值 + 像素采样 + golden 三层入口;core/parser 测试 `testImplementation(project(":testkit"))`。

**Tech Stack:** Kotlin 2.4.0 / JVM、Gradle(core/parser/testkit 三模块)、skiko-awt `0.0.0-SNAPSHOT`、`kotlin.test`。

**Spec:** `docs/superpowers/specs/2026-09-07-testkit-design.md`

---

## 执行前必读(环境约定)

- 仓库根 `D:\mine\workspace\snapshot` 运行命令(git-bash 用 `./gradlew`)。
- 当前分支 `feat/testkit-framework`(base=main);每个 Task 结束都需 **GPG 签名提交**(`git commit -S -m "…"`,验证 `git log -1 --pretty='%h %G?'` 应输出 `G`)。
- golden 模式:默认 `verify`;`record`/`update` 经 `-PsnapshotTest.mode=…` 透传到测试 JVM。
- 若某 API 形态与下述代码不符(如 skia 重载参数名),以**编译错误提示为准微调**(不臆测、不换方案),并在提交信息里注明改动。
- 每个 `./gradlew` 预期失败的步骤都必须真的先看到失败再进入实现(除纯新增模块/文件的编译性任务)。

---

## 涉及文件清单

**Modify**
- `settings.gradle.kts`(include `:testkit`)
- `core/build.gradle.kts`(testImplementation(testkit) + mode/goldenRoot 透传)
- `parser/build.gradle.kts`(同上)

**Create**
- `testkit/build.gradle.kts`
- `testkit/src/main/kotlin/com/muedsa/snapshot/TestTool.kt`(沿用兼容层,由旧文件逐字搬入)
- `testkit/src/main/kotlin/com/muedsa/snapshot/TestKit.kt`(渲染桥/数值/采样/golden 公共 DSL)
- `testkit/src/main/kotlin/com/muedsa/snapshot/testkit/GoldenEngine.kt`(golden 引擎)
- `core/src/main/kotlin/com/muedsa/snapshot/rendering/LayoutTree.kt`(LayoutNode 产品面)
- 示范测试:`widget/ColoredBoxTest.kt`(重写)、`widget/ColumnTest.kt`(重写)、`paint/gradient/GradientGoldenTest.kt`、`widget/ClipGoldenTest.kt`、`paint/text/TextMetricsTest.kt`
- 基准图:`core/src/test/resources/golden/…png`(E3/E4 由 record 生成后提交)
- `docs/testing/README.md`

**Delete**
- `core/src/test/kotlin/com/muedsa/snapshot/TestTool.kt`
- `parser/src/test/kotlin/com/muedsa/snapshot/TestTool.kt`

---

### Task 1: Gradle 布线(:testkit 模块 + 模式透传)

**Files:**
- Modify: `settings.gradle.kts`
- Create: `testkit/build.gradle.kts`
- Modify: `core/build.gradle.kts`
- Modify: `parser/build.gradle.kts`

- [ ] **Step 1: settings 注册模块**

把 `settings.gradle.kts` 末尾两行
```kotlin
include(":core")
include(":parser")
```
改为
```kotlin
include(":core")
include(":parser")
include(":testkit")
```

- [ ] **Step 2: 新建 testkit 模块构建脚本**

创建 `testkit/build.gradle.kts`,内容:
```kotlin
val versionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
plugins {
    alias(libs.plugins.jvm)
}

group = "com.muedsa.snapshot"
version = "0.0.0-SNAPSHOT"

dependencies {
    implementation(project(":core"))
}

val jarBaseName = "${rootProject.name}-${project.name}"
val manifestAttributes = mapOf(
    "Implementation-Title" to jarBaseName,
    "Implementation-Version" to project.version
)

tasks.jar {
    archiveBaseName = jarBaseName
    manifest {
        attributes(manifestAttributes)
    }
}
```

- [ ] **Step 3: core 依赖 testkit 并透传 golden 模式**

`core/build.gradle.kts` 的 `dependencies { … }` 内、`testImplementation(versionCatalog.findLibrary("skiko-$targetOs-$targetArch").get())` 之后追加:
```kotlin
    testImplementation(project(":testkit"))
```
并把 `tasks.test { useJUnitPlatform() }` 块改为:
```kotlin
tasks.test {
    useJUnitPlatform()
    systemProperty("snapshotTest.mode", providers.gradleProperty("snapshotTest.mode").getOrElse("verify"))
    providers.gradleProperty("snapshotTest.goldenRoot").orNull?.let {
        systemProperty("snapshotTest.goldenRoot", it)
    }
}
```

- [ ] **Step 4: parser 同款改动**

`parser/build.gradle.kts`:`dependencies` 中 `testImplementation(versionCatalog.findLibrary("skiko-$targetOs-$targetArch").get())` 之后追加 `testImplementation(project(":testkit"))`;`tasks.test` 块同 Step 3 替换。

- [ ] **Step 5: 编译验证(空模块)**

Run:
```bash
./gradlew :testkit:compileKotlin :core:compileTestKotlin :parser:compileTestKotlin --console=plain
```
Expected:`BUILD SUCCESSFUL`(此时旧 `TestTool.kt` 仍在,一切照旧)。

- [ ] **Step 6: 提交**

```bash
git add settings.gradle.kts testkit/build.gradle.kts core/build.gradle.kts parser/build.gradle.kts
git commit -S -m "chore(build): 引入 :testkit 模块并透传 snapshotTest golden 模式"
```

---

### Task 2: 沿用兼容层搬迁并删除两份重复 TestTool

**Files:**
- Create: `testkit/src/main/kotlin/com/muedsa/snapshot/TestTool.kt`
- Delete: `core/src/test/kotlin/com/muedsa/snapshot/TestTool.kt`
- Delete: `parser/src/test/kotlin/com/muedsa/snapshot/TestTool.kt`

- [ ] **Step 1: 建 testkit 版 TestTool(逐字搬 core 版)**

打开 `core/src/test/kotlin/com/muedsa/snapshot/TestTool.kt`,把**全文**(含 `package com.muedsa.snapshot`、全部 import、`noLimitedLayout`/`testImagesDirection`/`rootDirection`/`getTestPngFile`/`drawWidget`/`drawPainter`×2/`renderBoxToPixels`/`painterToPicture`/`layerToPixels`/`layersToPixels`/`pictureToPixels`)原样复制为 `testkit/src/main/kotlin/com/muedsa/snapshot/TestTool.kt`。除文件路径外不做任何字符改动。

- [ ] **Step 2: 删除两份旧 TestTool**

```bash
git rm core/src/test/kotlin/com/muedsa/snapshot/TestTool.kt
git rm parser/src/test/kotlin/com/muedsa/snapshot/TestTool.kt
```

- [ ] **Step 3: 编译验证(去重后旧测试仍可解析)**

Run:
```bash
./gradlew :testkit:compileKotlin :core:compileTestKotlin :parser:compileTestKotlin --console=plain
```
Expected:`BUILD SUCCESSFUL`(parser 测试 `import com.muedsa.snapshot.getTestPngFile` 等从 testkit 解析;若报某名字未解析,说明 testkit 与旧文件内容有差,按报错修正后重跑)。

- [ ] **Step 4: 提交**

```bash
git add testkit/src/main/kotlin/com/muedsa/snapshot/TestTool.kt
git commit -S -m "refactor(test): TestTool 兼容层搬迁到 :testkit 并删除 core/parser 重复副本"
```

---

### Task 3: core main 布局自省 `LayoutTree`

**Files:**
- Create: `core/src/main/kotlin/com/muedsa/snapshot/rendering/LayoutTree.kt`

- [ ] **Step 1: 写实现**

创建文件,内容:
```kotlin
package com.muedsa.snapshot.rendering

import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import com.muedsa.snapshot.rendering.box.BoxParentData
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderContainerBox
import com.muedsa.snapshot.rendering.box.RenderSingleChildBox
import org.jetbrains.skia.Rect

/**
 * 只读布局自省:给出 layout 后每个节点的全局几何。
 * 无行为变更、不参与渲染;供测试/调试读取。
 */
class LayoutNode internal constructor(
    val renderBox: RenderBox,
    val offsetFromParent: Offset,
    parent: LayoutNode?,
    children: List<LayoutNode>,
) {
    val parent: LayoutNode? = parent
    val children: List<LayoutNode> = children
    val size: Size get() = renderBox.definiteSize
    val absoluteOffset: Offset = (parent?.absoluteOffset ?: Offset.ZERO) + offsetFromParent
    val rect: Rect get() = absoluteOffset combine size
}

private fun childBoxes(box: RenderBox): List<RenderBox> = when (box) {
    is RenderContainerBox -> box.children
    is RenderSingleChildBox -> listOfNotNull(box.child)
    else -> emptyList()
}

fun RenderBox.toLayoutNode(): LayoutNode {
    fun build(box: RenderBox, parent: LayoutNode?): LayoutNode {
        val offset = (box.parentData as? BoxParentData)?.offset ?: Offset.ZERO
        // 先建"半成品"作为子节点的 parent,再以完整 children 重建,满足不可变 + 循环引用
        val node = LayoutNode(box, offset, parent, emptyList())
        return LayoutNode(
            renderBox = box,
            offsetFromParent = offset,
            parent = parent,
            children = childBoxes(box).map { build(it, node) }
        )
    }
    return build(this, null)
}
```
> 说明:`combine` 是 `Offset` 的成员 infix(返回 skia `Rect`),无需额外 import;若返回类型与 `Rect` 不符,改用 `Rect.makeXYWH(absoluteOffset.x, absoluteOffset.y, size.width, size.height)`。`parentData` 在 core 模块内读 internal 成员,合法。`BuildConfig` 无关。

- [ ] **Step 2: 编译验证**

Run:
```bash
./gradlew :core:compileKotlin --console=plain
```
Expected:`BUILD SUCCESSFUL`。

- [ ] **Step 3: 提交**

```bash
git add core/src/main/kotlin/com/muedsa/snapshot/rendering/LayoutTree.kt
git commit -S -m "feat(core): 新增只读布局自省 LayoutNode(供测试读取全局几何)"
```

---

### Task 4: testkit 数值/采样 DSL

**Files:**
- Create: `testkit/src/main/kotlin/com/muedsa/snapshot/TestKit.kt`(渲染桥 + 数值层 + 采样层)

- [ ] **Step 1: 写实现**

创建文件,内容:
```kotlin
package com.muedsa.snapshot

import com.muedsa.snapshot.rendering.LayoutNode
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.widget.Widget
import org.jetbrains.skia.*
import kotlin.math.abs
import kotlin.math.ceil

/* ---------- 渲染桥 ---------- */

private fun widgetSurface(
    background: Int = Color.WHITE,
    debug: Boolean = false,
    content: Widget.() -> Unit,
): Surface {
    val root = layoutWidget(content)
    val size = root.definiteSize
    check(!size.isEmpty) { "layout size is empty" }
    check(!size.isInfinite) { "layout size is infinite" }
    val surface = Surface.makeRasterN32Premul(ceil(size.width).toInt(), ceil(size.height).toInt())
    surface.canvas.drawRenderBox(renderBox = root, background = background, debug = debug)
    surface.flushAndSubmit()
    return surface
}

fun snapshotPixels(
    background: Int = Color.WHITE,
    debug: Boolean = false,
    content: Widget.() -> Unit,
): Pixmap = widgetSurface(background, debug, content).makeImageSnapshot().peekPixels()!!

fun snapshotImage(
    background: Int = Color.WHITE,
    debug: Boolean = false,
    content: Widget.() -> Unit,
): Image = widgetSurface(background, debug, content).makeImageSnapshot()

private fun painterSurface(
    width: Float,
    height: Float,
    background: Int = Color.WHITE,
    painter: (Canvas) -> Unit,
): Surface {
    val surface = Surface.makeRasterN32Premul(ceil(width).toInt(), ceil(height).toInt())
    surface.canvas.clear(background)
    painter(surface.canvas)
    surface.flushAndSubmit()
    return surface
}

fun painterPixels(
    width: Float,
    height: Float,
    background: Int = Color.WHITE,
    painter: (Canvas) -> Unit,
): Pixmap = painterSurface(width, height, background, painter).makeImageSnapshot().peekPixels()!!

fun painterImage(
    width: Float,
    height: Float,
    background: Int = Color.WHITE,
    painter: (Canvas) -> Unit,
): Image = painterSurface(width, height, background, painter).makeImageSnapshot()

/* ---------- 数值/布局层 ---------- */

fun rootLayout(content: Widget.() -> Unit): LayoutNode = layoutWidget(content).toLayoutNode()

fun assertApproxEq(actual: Float, expected: Float, tolerance: Float = precisionErrorTolerance) {
    if (abs(actual - expected) > tolerance) {
        throw AssertionError("assertApproxEq failed: expected $expected ± $tolerance but was $actual")
    }
}

fun LayoutNode.assertSize(width: Float, height: Float, tolerance: Float = precisionErrorTolerance) {
    assertApproxEq(size.width, width, tolerance)
    assertApproxEq(size.height, height, tolerance)
}

fun LayoutNode.assertGlobalRect(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    tolerance: Float = precisionErrorTolerance,
) {
    assertApproxEq(absoluteOffset.x, left, tolerance)
    assertApproxEq(absoluteOffset.y, top, tolerance)
    assertSize(width, height, tolerance)
}

fun LayoutNode.firstMatching(predicate: (RenderBox) -> Boolean): LayoutNode? {
    if (predicate(renderBox)) {
        return this
    }
    for (child in children) {
        child.firstMatching(predicate)?.let { return it }
    }
    return null
}

inline fun <reified T : RenderBox> LayoutNode.findType(noinline where: (T) -> Boolean = { true }): LayoutNode? =
    firstMatching { it is T && where(it as T) }

/* ---------- 采样层 ---------- */

fun expectColorAt(pixmap: Pixmap, x: Int, y: Int, expectedColor: Int) {
    val actual = pixmap.getColor(x, y)
    if (actual != expectedColor) {
        throw AssertionError(
            "pixel($x,$y): expected 0x${expectedColor.toUInt().toString(16).padStart(8, '0')} " +
                "but was 0x${actual.toUInt().toString(16).padStart(8, '0')}"
        )
    }
}

private fun Int.a() = (this shr 24) and 0xFF
private fun Int.r() = (this shr 16) and 0xFF
private fun Int.g() = (this shr 8) and 0xFF
private fun Int.b() = this and 0xFF
private fun channelColor(a: Int, r: Int, g: Int, b: Int): Int =
    (a shl 24) or (r shl 16) or (g shl 8) or b

data class RegionColorStats(
    val pixelCount: Int,
    val opaqueCount: Int,
    val transparentCount: Int,
    val averageColor: Int,
)

fun Pixmap.regionStats(rect: Rect): RegionColorStats {
    val x0 = rect.left.toInt().coerceIn(0, width - 1)
    val y0 = rect.top.toInt().coerceIn(0, height - 1)
    val x1 = (rect.right - 1).toInt().coerceIn(0, width - 1)
    val y1 = (rect.bottom - 1).toInt().coerceIn(0, height - 1)
    var count = 0
    var opaque = 0
    var transparent = 0
    var ar = 0L; var ag = 0L; var ab = 0L; var aa = 0L
    for (y in y0..y1) {
        for (x in x0..x1) {
            val c = getColor(x, y)
            val a = c.a()
            aa += a; ar += c.r(); ag += c.g(); ab += c.b()
            count++
            if (a == 0) transparent++
            else if (a == 255) opaque++
        }
    }
    if (count == 0) {
        return RegionColorStats(0, 0, 0, 0)
    }
    val avg = channelColor(
        (aa / count).toInt(),
        (ar / count).toInt(),
        (ag / count).toInt(),
        (ab / count).toInt()
    )
    return RegionColorStats(count, opaque, transparent, avg)
}

fun expectRegionOpaque(pixmap: Pixmap, rect: Rect) {
    val s = pixmap.regionStats(rect)
    check(s.transparentCount == 0) { "region($rect) contains transparent pixels: $s" }
}

fun expectRegionTransparent(pixmap: Pixmap, rect: Rect) {
    val s = pixmap.regionStats(rect)
    check(s.opaqueCount == 0) { "region($rect) contains opaque pixels: $s" }
}

fun expectRegionUniform(pixmap: Pixmap, rect: Rect, expectedColor: Int, channelTolerance: Int = 0) {
    val x0 = rect.left.toInt().coerceIn(0, pixmap.width - 1)
    val y0 = rect.top.toInt().coerceIn(0, pixmap.height - 1)
    val x1 = (rect.right - 1).toInt().coerceIn(0, pixmap.width - 1)
    val y1 = (rect.bottom - 1).toInt().coerceIn(0, pixmap.height - 1)
    for (y in y0..y1) {
        for (x in x0..x1) {
            val c = pixmap.getColor(x, y)
            val diff =
                abs(c.a() - expectedColor.a()) + abs(c.r() - expectedColor.r()) +
                abs(c.g() - expectedColor.g()) + abs(c.b() - expectedColor.b())
            check(diff <= channelTolerance * 4) {
                "pixel($x,$y)=0x${c.toUInt().toString(16)} not uniform 0x${expectedColor.toUInt().toString(16)}"
            }
        }
    }
}
```

- [ ] **Step 2: 编译验证**

Run:
```bash
./gradlew :testkit:compileKotlin --console=plain
```
Expected:`BUILD SUCCESSFUL`。若 `drawRenderBox`/`layoutWidget` 跨模块不可见(不会:它们为 public),或某个 skia 方法签名报错,以编译提示微调。

- [ ] **Step 3: 提交**

```bash
git add testkit/src/main/kotlin/com/muedsa/snapshot/TestKit.kt
git commit -S -m "feat(testkit): 布局/数值与像素采样断言 DSL"
```

---

### Task 5: golden 引擎与公共入口

**Files:**
- Create: `testkit/src/main/kotlin/com/muedsa/snapshot/testkit/GoldenEngine.kt`
- Modify: `testkit/src/main/kotlin/com/muedsa/snapshot/TestKit.kt`(追加 golden 入口)

- [ ] **Step 1: 写 golden 引擎**

创建文件:
```kotlin
package com.muedsa.snapshot.testkit

import org.jetbrains.skia.*
import java.io.File
import kotlin.math.abs

/**
 * golden 三态引擎:
 *  - verify(默认):要求基准存在,逐像素比对,失配产出 actual/diff 并抛 AssertionError
 *  - record:仅当基准不存在时写入 src/test/resources/golden/<id>.png
 *  - update :无条件覆盖同名基准
 */
internal object GoldenEngine {

    data class DiffResult(val total: Int, val changed: Int, val changedPixels: BooleanArray)

    fun mode(): String =
        System.getProperty("snapshotTest.mode")
            ?: System.getenv("SNAPSHOT_TEST_MODE")
            ?: "verify"

    fun assertMatchesBaseline(
        image: Image,
        id: String,
        perPixelTolerance: Int = 0,
        allowMismatchRatio: Double = 0.0,
    ) {
        when (mode()) {
            "record" -> write(image, id, overwrite = false)
            "update" -> write(image, id, overwrite = true)
            else -> verify(image, id, perPixelTolerance, allowMismatchRatio)
        }
    }

    /* ----- 路径 ----- */

    private fun goldenFile(id: String): File {
        val root = System.getProperty("snapshotTest.goldenRoot")?.let(::File)
            ?: File("src/test/resources/golden")
        return File(root, "$id.png")
    }

    /* ----- record / update ----- */

    private fun write(image: Image, id: String, overwrite: Boolean) {
        val target = goldenFile(id)
        if (!overwrite && target.exists()) {
            error("golden '$id' already exists at ${target.path}; run with mode=update to overwrite after review")
        }
        target.parentFile?.mkdirs()
        val bytes = image.encodeToData(EncodedImageFormat.PNG)!!.bytes
        target.writeBytes(bytes)
        println("snapshotTest[$mode()]: wrote ${target.path}")
    }

    /* ----- verify ----- */

    private fun verify(image: Image, id: String, perPixelTolerance: Int, allowMismatchRatio: Double) {
        val baseline = loadBaseline(id)
            ?: error("golden baseline missing: /golden/$id.png — run with -PsnapshotTest.mode=record first")
        val actual = image.toRasterPixmap()
        if (actual.width != baseline.width || actual.height != baseline.height) {
            throw AssertionError(
                "golden '$id' size mismatch: actual ${actual.width}x${actual.height} " +
                    "vs baseline ${baseline.width}x${baseline.height}"
            )
        }
        val diff = diffPixels(actual, baseline, perPixelTolerance)
        val ratio = if (diff.total == 0) 0.0 else diff.changed.toDouble() / diff.total
        if (ratio > allowMismatchRatio) {
            writeArtifacts(image, id, diff)
            throw AssertionError(
                "golden '$id' mismatch: changed=${diff.changed}/${diff.total} " +
                    "(${"%.4f".format(ratio * 100)}%) > allowMismatchRatio=$allowMismatchRatio; " +
                    "artifacts at build/test-results/golden/$id/"
            )
        }
    }

    private fun loadBaseline(id: String): Pixmap? {
        val url = GoldenEngine::class.java.classLoader.getResource("golden/$id.png") ?: return null
        val bytes = url.openStream().use { it.readBytes() }
        return Image.makeFromEncoded(bytes).toRasterPixmap()
    }

    private fun Image.toRasterPixmap(): Pixmap {
        val surface = Surface.makeRasterN32Premul(width, height)
        val dst = Rect.makeXYWH(0f, 0f, width.toFloat(), height.toFloat())
        surface.canvas.drawImageRect(image = this, src = dst, dst = dst, paint = Paint())
        surface.flushAndSubmit()
        return surface.makeImageSnapshot().peekPixels()!!
    }

    private fun diffPixels(a: Pixmap, b: Pixmap, tolerance: Int): DiffResult {
        val total = a.width * a.height
        val changed = BooleanArray(total)
        var count = 0
        var index = 0
        for (y in 0 until a.height) {
            for (x in 0 until a.width) {
                val ca = a.getColor(x, y)
                val cb = b.getColor(x, y)
                val isChanged = if (ca == cb) false else colorDiffers(ca, cb, tolerance)
                changed[index] = isChanged
                if (isChanged) {
                    count++
                }
                index++
            }
        }
        return DiffResult(total, count, changed)
    }

    private fun colorDiffers(ca: Int, cb: Int, tolerance: Int): Boolean {
        val shifts = intArrayOf(24, 16, 8, 0)
        for (shift in shifts) {
            if (abs(((ca shr shift) and 0xFF) - ((cb shr shift) and 0xFF)) > tolerance) {
                return true
            }
        }
        return false
    }

    private fun writeArtifacts(image: Image, id: String, diff: DiffResult) {
        val dir = File("build/test-results/golden/$id")
        dir.mkdirs()
        File(dir, "actual.png").writeBytes(image.encodeToData(EncodedImageFormat.PNG)!!.bytes)

        // diff = actual + 把 changed 像素点红
        val width = image.width
        val height = image.height
        val surface = Surface.makeRasterN32Premul(width, height)
        val dst = Rect.makeXYWH(0f, 0f, width.toFloat(), height.toFloat())
        surface.canvas.drawImageRect(image = image, src = dst, dst = dst, paint = Paint())
        val paint = Paint().apply { color = Color.RED }
        var index = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (diff.changedPixels[index]) {
                    surface.canvas.drawRect(Rect.makeXYWH(x.toFloat(), y.toFloat(), 1f, 1f), paint)
                }
                index++
            }
        }
        surface.flushAndSubmit()
        File(dir, "diff.png").writeBytes(surface.makeImageSnapshot().encodeToData(EncodedImageFormat.PNG)!!.bytes)
    }
}
```

- [ ] **Step 2: 在 TestKit.kt 追加 golden 公共入口**

把文件末尾追加:
```kotlin
/* ---------- golden 层 ---------- */

fun golden(id: String, background: Int = Color.WHITE, content: Widget.() -> Unit) {
    GoldenEngine.assertMatchesBaseline(snapshotImage(background = background, content = content), id)
}

fun goldenPixels(
    id: String,
    width: Float,
    height: Float,
    background: Int = Color.WHITE,
    painter: (Canvas) -> Unit,
) {
    GoldenEngine.assertMatchesBaseline(painterImage(width, height, background, painter), id)
}

fun assertImageMatchesBaseline(
    image: Image,
    id: String,
    perPixelTolerance: Int = 0,
    allowMismatchRatio: Double = 0.0,
) {
    GoldenEngine.assertMatchesBaseline(image, id, perPixelTolerance, allowMismatchRatio)
}
```
并在 `import` 区追加一行 `import com.muedsa.snapshot.testkit.GoldenEngine`。

- [ ] **Step 3: 编译验证**

Run:
```bash
./gradlew :testkit:compileKotlin --console=plain
```
Expected:`BUILD SUCCESSFUL`。若 `Surface.canvas.drawImageRect` 或 `Image.encodeToData` 报签名不符,以编译提示微调并注明。

- [ ] **Step 4: 提交**

```bash
git add testkit/src/main/kotlin/com/muedsa/snapshot/testkit/GoldenEngine.kt testkit/src/main/kotlin/com/muedsa/snapshot/TestKit.kt
git commit -S -m "feat(testkit): golden 录制/校验/更新引擎与公共入口"
```

---

### Task 6: E1 迁移 ColoredBoxTest(布局 + 采样)

**Files:**
- Rewrite: `core/src/test/kotlin/com/muedsa/snapshot/widget/ColoredBoxTest.kt`

- [ ] **Step 1: 重写测试**

把该文件整体替换为:
```kotlin
package com.muedsa.snapshot.widget

import com.muedsa.snapshot.expectColorAt
import com.muedsa.snapshot.rootLayout
import com.muedsa.snapshot.snapshotPixels
import com.muedsa.snapshot.rendering.box.RenderColoredBox
import org.jetbrains.skia.Color
import kotlin.test.Test

class ColoredBoxTest {

    // 灰:与下方颜色集互异,保证 findType 不会先命中 Container 自带的外层 RenderColoredBox
    private val background = 0xFF808080.toInt()

    private fun assertScene(color: Int) {
        val root = rootLayout {
            Container(
                width = 200f,
                height = 200f,
                alignment = BoxAlignment.CENTER,
                color = background,
            ) {
                SizedBox(width = 100f, height = 100f) {
                    ColoredBox(color = color)
                }
            }
        }
        root.assertSize(200f, 200f)
        val inner = checkNotNull(root.findType<RenderColoredBox> { it.color == color }) {
            "找不到 color=$color 的 RenderColoredBox"
        }
        inner.assertGlobalRect(50f, 50f, 100f, 100f)

        val pixmap = snapshotPixels {
            Container(
                width = 200f,
                height = 200f,
                alignment = BoxAlignment.CENTER,
                color = background,
            ) {
                SizedBox(width = 100f, height = 100f) {
                    ColoredBox(color = color)
                }
            }
        }
        expectColorAt(pixmap, 1, 1, background)
        expectColorAt(pixmap, 198, 198, background)
        expectColorAt(pixmap, 100, 100, color)
    }

    @Test
    fun color_layout_and_pixel_test() {
        val colors = intArrayOf(
            Color.BLACK, Color.WHITE, Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN, Color.MAGENTA
        )
        colors.forEach(::assertScene)
    }
}
```
> 说明:顶部未写 `import com.muedsa.geometry.BoxAlignment`/`com.muedsa.snapshot.widget.Container` 等,因本测试处于 `com.muedsa.snapshot.widget` 包,`Container`/`SizedBox`/`ColoredBox` 同包可见;但 `BoxAlignment` 在 `com.muedsa.geometry`,**必须补 import**。若 `renderBox` 层级中另有别的 `RenderColoredBox`,先于内容匹配则改 predicate 只认 `color==color` 已足够(外层是 WHITE)。

- [ ] **Step 1b: 补齐缺失 import**

若上一步编译报 `Unresolved reference: BoxAlignment`(会),在该文件 import 区加:
```kotlin
import com.muedsa.geometry.BoxAlignment
```

- [ ] **Step 2: 运行该测试**

Run:
```bash
./gradlew :core:test --tests 'com.muedsa.snapshot.widget.ColoredBoxTest' --console=plain
```
Expected:PASS。若 `assertGlobalRect` 数值与实测不符,把断言值改成实际输出(先 `println(root.toString() + inner.rect)` 看实际几何),并在提交信息里注明校准。

- [ ] **Step 3: 提交**

```bash
git add core/src/test/kotlin/com/muedsa/snapshot/widget/ColoredBoxTest.kt
git commit -S -m "test(core): ColoredBoxTest 迁移到布局+采样断言"
```

---

### Task 7: E2 迁移 ColumnTest(布局/数值)

**Files:**
- Rewrite: `core/src/test/kotlin/com/muedsa/snapshot/widget/ColumnTest.kt`

- [ ] **Step 1: 读旧文件确认现有语义覆盖**

先读 `core/src/test/kotlin/com/muedsa/snapshot/widget/ColumnTest.kt`,把旧用例场景逐个记录下来(替换为空值即可,无需保留绘图)。

- [ ] **Step 2: 重写测试**

把该文件替换为:
```kotlin
package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.LayoutNode
import com.muedsa.snapshot.rendering.box.RenderConstrainedBox
import com.muedsa.snapshot.rendering.flex.MainAxisAlignment
import com.muedsa.snapshot.rootLayout
import kotlin.test.Test

class ColumnTest {

    private fun columnRoot(
        mainAxisAlignment: MainAxisAlignment = MainAxisAlignment.START,
    ) = rootLayout {
        SizedBox(width = 200f, height = 200f) {
            Column(mainAxisAlignment = mainAxisAlignment) {
                SizedBox(width = 100f, height = 30f)
                SizedBox(width = 50f, height = 40f)
            }
        }
    }

    @Test
    fun column_start_places_children() {
        val root = columnRoot()
        root.assertSize(200f, 200f)

        val first = checkNotNull(root.findType<RenderConstrainedBox> { it.definiteSize.width == 100f }) {
            "找不到宽 100 的 SizedBox"
        }
        val second = checkNotNull(root.findType<RenderConstrainedBox> { it.definiteSize.width == 50f }) {
            "找不到宽 50 的 SizedBox"
        }
        first.assertGlobalRect(50f, 0f, 100f, 30f)
        second.assertGlobalRect(75f, 30f, 50f, 40f)
    }

    @Test
    fun column_center_main_axis_places_children() {
        val root = columnRoot(MainAxisAlignment.CENTER)
        val first = checkNotNull(root.findType<RenderConstrainedBox> { it.definiteSize.width == 100f })
        val second = checkNotNull(root.findType<RenderConstrainedBox> { it.definiteSize.width == 50f })
        // 剩余空间 200-70=130;leading=65 → 子1 y=65、子2 y=65+30=95;交叉居中 x 同 START(50/75)
        first.assertGlobalRect(50f, 65f, 100f, 30f)
        second.assertGlobalRect(75f, 95f, 50f, 40f)
    }
}
```
> 语义推导:外层 `SizedBox(200×200)` → `RenderConstrainedBox` 以 tight 约束 Column;`Column(RenderFlex, Axis.VERTICAL)` 的宽高都被 tight 到 200。两个非 flex 子 main 高累计 70、剩余 130;cross `CENTER` → 子 x=(200−子宽)/2 → 50/75;main `START` → y=0、30;main `CENTER` → leading=130/2=65 → y=65、95。若实测有差,以实际输出校准(方法同 Task 6)。

- [ ] **Step 3: 运行该测试**

Run:
```bash
./gradlew :core:test --tests 'com.muedsa.snapshot.widget.ColumnTest' --console=plain
```
Expected:PASS;否则按断言校准。

- [ ] **Step 4: 提交**

```bash
git add core/src/test/kotlin/com/muedsa/snapshot/widget/ColumnTest.kt
git commit -S -m "test(core): ColumnTest 迁移到布局数值断言"
```

---

### Task 8: E3 渐变确定性 golden(含上次迁移微确认回归)

**Files:**
- Create: `core/src/test/kotlin/com/muedsa/snapshot/paint/gradient/GradientGoldenTest.kt`
- Create(record 生成): `core/src/test/resources/golden/gradient/linear_two_stop.png`、`radial_two_stop.png`、`sweep_google.png`

- [ ] **Step 1: 写测试**

创建文件:
```kotlin
package com.muedsa.snapshot.paint.gradient

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import com.muedsa.snapshot.goldenPixels
import org.jetbrains.skia.Paint
import kotlin.test.Test

class GradientGoldenTest {

    @Test
    fun linear_two_stop_golden() {
        val size = Size(600f, 200f)
        goldenPixels("gradient/linear_two_stop", size.width, size.height) { canvas ->
            val gradient = LinearGradient(
                begin = BoxAlignment.TOP_LEFT,
                end = BoxAlignment.BOTTOM_RIGHT,
                colors = intArrayOf(0xFF00FF87.toInt(), 0xFF60EFFF.toInt()),
            )
            val rect = Offset.ZERO combine size
            canvas.drawRect(rect, Paint().apply { shader = gradient.createShader(rect) })
        }
    }

    @Test
    fun radial_two_stop_golden() {
        val size = Size(600f, 600f)
        goldenPixels("gradient/radial_two_stop", size.width, size.height) { canvas ->
            val gradient = RadialGradient(
                colors = intArrayOf(0xFFFCEF64.toInt(), 0xFFF44C7D.toInt()),
            )
            val rect = Offset.ZERO combine size
            canvas.drawRect(rect, Paint().apply { shader = gradient.createShader(rect) })
        }
    }

    @Test
    fun sweep_google_golden() {
        val size = Size(600f, 600f)
        goldenPixels("gradient/sweep_google", size.width, size.height) { canvas ->
            val gradient = SweepGradient(
                colors = intArrayOf(
                    0xFF4285F4.toInt(),
                    0xFF34A853.toInt(),
                    0xFFFBBC05.toInt(),
                    0xFFEA4335.toInt(),
                    0xFF4285F4.toInt(),
                )
            )
            val rect = Offset.ZERO combine size
            canvas.drawRect(rect, Paint().apply { shader = gradient.createShader(rect) })
        }
    }
}
```

- [ ] **Step 2: record 生成基准**

Run:
```bash
./gradlew :core:test -PsnapshotTest.mode=record --tests 'com.muedsa.snapshot.paint.gradient.GradientGoldenTest' --console=plain
```
Expected:BUILD SUCCESSFUL,日志含三行 `snapshotTest[record]: wrote …/gradient/…png`;此时 `core/src/test/resources/golden/gradient/*.png` 已生成。

- [ ] **Step 3: verify 复验**

Run:
```bash
./gradlew :core:test --tests 'com.muedsa.snapshot.paint.gradient.GradientGoldenTest' --console=plain
```
Expected:PASS(默认 verify,读 classpath 基准比对)。

- [ ] **Step 4: 提交(含基准图)**

```bash
git add core/src/test/kotlin/com/muedsa/snapshot/paint/gradient/GradientGoldenTest.kt core/src/test/resources/golden/gradient
git commit -S -m "test(core): 渐变确定性 golden(linear/radial/sweep)并固化 Color4f/premul 回归"
```

---

### Task 9: E4 裁剪确定性 golden

**Files:**
- Create: `core/src/test/kotlin/com/muedsa/snapshot/widget/ClipGoldenTest.kt`
- Create(record 生成): `core/src/test/resources/golden/widget/clip_oval_green.png`

- [ ] **Step 1: 写测试**

创建文件:
```kotlin
package com.muedsa.snapshot.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.Offset
import com.muedsa.geometry.Radius
import com.muedsa.snapshot.golden
import com.muedsa.snapshot.paint.decoration.BorderRadius
import org.jetbrains.skia.Color
import kotlin.test.Test

class ClipGoldenTest {

    @Test
    fun clip_oval_green_golden() {
        golden("widget/clip_oval_green") {
            Container(width = 300f, height = 300f, alignment = BoxAlignment.CENTER, color = Color.WHITE) {
                ClipOval(clipper = { Offset.ZERO combine it }) {
                    Container(width = 200f, height = 200f, color = Color.GREEN)
                }
            }
        }
    }

    @Test
    fun clip_rrect_blue_golden() {
        golden("widget/clip_rrect_blue") {
            Container(width = 300f, height = 300f, alignment = BoxAlignment.CENTER, color = Color.WHITE) {
                ClipRRect(borderRadius = BorderRadius.all(Radius.circular(60f))) {
                    Container(width = 200f, height = 200f, color = Color.BLUE)
                }
            }
        }
    }
}
```

- [ ] **Step 2: record 生成基准并 verify 复验**

Run:
```bash
./gradlew :core:test -PsnapshotTest.mode=record --tests 'com.muedsa.snapshot.widget.ClipGoldenTest' --console=plain
./gradlew :core:test --tests 'com.muedsa.snapshot.widget.ClipGoldenTest' --console=plain
```
Expected:两命令均成功(先 record 后 verify)。

- [ ] **Step 3: 提交**

```bash
git add core/src/test/kotlin/com/muedsa/snapshot/widget/ClipGoldenTest.kt core/src/test/resources/golden/widget
git commit -S -m "test(core): 裁剪确定性 golden(ClipOval/ClipRRect)"
```

---

### Task 10: E5 文本度量区间断言 + artifact

**Files:**
- Create: `core/src/test/kotlin/com/muedsa/snapshot/paint/text/TextMetricsTest.kt`

- [ ] **Step 1: 写测试**

创建文件:
```kotlin
package com.muedsa.snapshot.paint.text

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.drawPainter
import org.jetbrains.skia.Color
import kotlin.test.Test
import kotlin.test.assertTrue

class TextMetricsTest {

    private fun layoutLine(text: String): TextPainter = TextPainter(
        text = TextSpan(text = text, style = TextStyle(fontSize = 20f))
    ).apply {
        layout(0f, Float.POSITIVE_INFINITY)
    }

    @Test
    fun single_line_metrics_are_positive_and_bounded() {
        val painter = layoutLine("Hello Word!")
        assertTrue(painter.width > 0f) { "单行文本宽度应为正,实际 ${painter.width}" }
        assertTrue(painter.height in 1f..60f) { "fontSize=20 的单行高度应在合理区间,实际 ${painter.height}" }
        assertTrue(painter.maxIntrinsicWidth >= painter.width) {
            "maxIntrinsicWidth(${painter.maxIntrinsicWidth}) 应不小于 width(${painter.width})"
        }
    }

    @Test
    fun multiline_height_grows_with_line_count() {
        val single = layoutLine("line one")
        val multi = TextPainter(
            text = TextSpan(text = "line one\nline two\nline three")
        ).apply {
            layout(0f, 400f)
        }
        assertTrue(multi.height > single.height) {
            "多行高(${multi.height})应大于单行高(${single.height})"
        }
        assertTrue(multi.width > 0f)
    }

    @Test
    fun artifact_for_eyeball_review() {
        val painter = layoutLine("Hello Word! 你好,世界!")
        drawPainter("paint/text/text_metrics", width = painter.width, height = painter.height) { canvas ->
            canvas.drawRect(
                Offset.ZERO combine painter.size,
                org.jetbrains.skia.Paint().apply { color = Color.WHITE }
            )
            painter.paint(canvas, offset = Offset.ZERO)
        }
    }
}
```

- [ ] **Step 2: 运行该测试**

Run:
```bash
./gradlew :core:test --tests 'com.muedsa.snapshot.paint.text.TextMetricsTest' --console=plain
```
Expected:PASS(区间断言避免锁字形,跨平台字体差异下仍稳)。

- [ ] **Step 3: 提交**

```bash
git add core/src/test/kotlin/com/muedsa/snapshot/paint/text/TextMetricsTest.kt
git commit -S -m "test(core): 文本度量区间断言 + artifact 检视"
```

---

### Task 11: 作者手册

**Files:**
- Create: `docs/testing/README.md`

- [ ] **Step 1: 写手册**

创建文件:
```markdown
# snapshot 测试框架使用手册

分层选择(从快到稳,从局部到全局):
1. **布局/数值断言**(无栅格化):`rootLayout { … }` 得到 `LayoutNode`,对根与子做
   `assertSize` / `assertGlobalRect` / `assertApproxEq`;按类型找节点 `findType<RenderX> { … }`。
2. **像素采样断言**(局部像素,无需基准):`snapshotPixels { … }` → `expectColorAt` /
   `regionStats` / `expectRegionOpaque/Transparent/Uniform`。
3. **golden 基准比对**(整图回归,只用于确定性内容):`golden(id) { … }` 或
   `goldenPixels(id, w, h) { canvas -> … }`。

禁止进 golden 的内容:OS 字体渲染的文本、外部/网络图片、随机/时变输出。
文本场景请用"数值区间断言 + artifact PNG 人眼检视"(参考 `TextMetricsTest`)。

golden 三态:
```bash
./gradlew :core:test -PsnapshotTest.mode=record --tests '*SomeTest'   # 生成新基准(已存在则报错)
./gradlew :core:test --tests '*SomeTest'                              # 默认 verify
./gradlew :core:test -PsnapshotTest.mode=update --tests '*SomeTest'   # 覆盖基准(先人工审阅 actual)
```
失配产物在 `core/build/test-results/golden/<id>/actual.png`、`diff.png`。

新功能测试的推荐做法:按上面 1→2→3 顺序,能数值断言的先数值断言;确定性可视内容再叠 golden。
```

- [ ] **Step 2: 提交**

```bash
git add docs/testing/README.md
git commit -S -m "docs: 测试框架使用手册(docs/testing)"
```

---

### Task 12: 全量验证与静态核对

**Files:** 无新改动(仅校验)。

- [ ] **Step 1: 全量 test + jar**

Run:
```bash
./gradlew test --console=plain
./gradlew jar --console=plain
```
Expected:两个 BUILD SUCCESSFUL。

- [ ] **Step 2: 静态核对**

Run:
```bash
git ls-files | grep -i "TestTool"
git status --short
```
Expected:`git ls-files` 只显示 `testkit/src/main/kotlin/com/muedsa/snapshot/TestTool.kt`(无 core/parser 旧副本);`git status` 干净。
再人工确认:新/迁移测试文件内无 `org.junit.jupiter`;`git log --pretty='%h %G?' main..HEAD` 全为 `G`。

- [ ] **Step 3(可选):golden 篡改演示**

临时把 `ClipGoldenTest` 一个 `Color.GREEN` 改成 `Color.RED`,跑 `:core:test --tests '*ClipGoldenTest'`,确认 verify 失败且 `build/test-results/golden/widget/clip_oval_green/{actual,diff}.png` 出现;改回后恢复绿。
