package com.muedsa.snapshot

import com.muedsa.snapshot.rendering.LayoutNode
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.toLayoutNode
import com.muedsa.snapshot.testkit.GoldenEngine
import com.muedsa.snapshot.widget.Widget
import org.jetbrains.skia.*
import kotlin.math.abs
import kotlin.math.ceil

/* ---------- 渲染桥 ---------- */

/**
 * 将 widget 内容布局并渲染到与根布局尺寸一致的 raster [Surface]。
 *
 * 直接委托 core 的 [Snapshot],避免在 testkit 中重复 layout、尺寸检查与建 surface 逻辑。
 */
private fun widgetSurface(
    background: Int = Color.WHITE,
    debug: Boolean = false,
    content: Widget.() -> Unit,
): Surface = Snapshot(background = background, debug = debug, content = content)

/**
 * 渲染 widget 内容并返回像素快照。
 *
 * 注意:返回的 [Pixmap] 引用底层快照内存,其有效性与本方法内部创建的 Surface/Image 生命周期一致;
 * 请勿在底层 Surface/Image 释放后继续读取该 Pixmap。
 */
fun snapshotPixels(
    background: Int = Color.WHITE,
    debug: Boolean = false,
    content: Widget.() -> Unit,
): Pixmap = widgetSurface(background, debug, content).makeImageSnapshot().peekPixels()!!

/**
 * 渲染 widget 内容并返回 [Image] 快照。
 */
fun snapshotImage(
    background: Int = Color.WHITE,
    debug: Boolean = false,
    content: Widget.() -> Unit,
): Image = widgetSurface(background, debug, content).makeImageSnapshot()

/**
 * 创建指定尺寸的 raster [Surface],先以背景色清空再执行 painter 绘制。
 */
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

/**
 * 在给定画布尺寸上执行 painter 绘制并返回像素快照。
 *
 * 注意:返回的 [Pixmap] 引用底层快照内存,其有效性与本方法内部创建的 Surface/Image 生命周期一致。
 */
fun painterPixels(
    width: Float,
    height: Float,
    background: Int = Color.WHITE,
    painter: (Canvas) -> Unit,
): Pixmap = painterSurface(width, height, background, painter).makeImageSnapshot().peekPixels()!!

/**
 * 在给定画布尺寸上执行 painter 绘制并返回 [Image] 快照。
 */
fun painterImage(
    width: Float,
    height: Float,
    background: Int = Color.WHITE,
    painter: (Canvas) -> Unit,
): Image = painterSurface(width, height, background, painter).makeImageSnapshot()

/* ---------- 数值/布局层 ---------- */

/**
 * 对 widget 内容完成一次根布局,返回只读的布局自省树根 [LayoutNode]。
 * 前提:widget 树的尺寸不受外部约束(等价于 core 的 layoutWidget 于 BoxConstraints 无穷约束)。
 */
fun rootLayout(content: Widget.() -> Unit): LayoutNode = layoutWidget(content).toLayoutNode()

/**
 * 断言 actual 与 expected 之差的绝对值不超过 tolerance;失败抛 [AssertionError]。
 */
fun assertApproxEq(actual: Float, expected: Float, tolerance: Float = precisionErrorTolerance) {
    if (abs(actual - expected) > tolerance) {
        throw AssertionError("assertApproxEq failed: expected $expected ± $tolerance but was $actual")
    }
}

/**
 * 断言该节点尺寸与期望宽高在 tolerance 内一致。
 */
fun LayoutNode.assertSize(width: Float, height: Float, tolerance: Float = precisionErrorTolerance) {
    assertApproxEq(size.width, width, tolerance)
    assertApproxEq(size.height, height, tolerance)
}

/**
 * 断言该节点全局几何(absoluteOffset 与 size)与期望的 left/top/宽高在 tolerance 内一致。
 */
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

/**
 * 深度优先返回首个(含自身)满足 predicate 的节点;无匹配返回 null。
 */
fun LayoutNode.firstMatching(predicate: (RenderBox) -> Boolean): LayoutNode? {
    if (predicate(renderBox)) {
        return this
    }
    for (child in children) {
        child.firstMatching(predicate)?.let { return it }
    }
    return null
}

/**
 * 深度优先返回首个节点类型为 [T] 且满足 where 谓词的节点;无匹配返回 null。
 */
inline fun <reified T : RenderBox> LayoutNode.findType(noinline where: (T) -> Boolean = { true }): LayoutNode? =
    firstMatching { it is T && where(it) }

/* ---------- golden 层 ---------- */

/**
 * 将 widget 内容渲染为像素快照并与 golden 基准比对(默认 verify 模式)。
 *
 * **确定性原则**:仅用于可确定复现的内容(纯几何/渐变/本地位图/纯 shader)。
 * 文本依赖操作系统字体,以及任何外网/随机/时变内容在跨机器、跨平台上不可复现,一律禁止进入 golden。
 *
 * 模式由系统属性 `-PsnapshotTest.mode`(或环境变量 `SNAPSHOT_TEST_MODE`)控制:
 *  - verify(默认):要求 `src/test/resources/golden/<id>.png` 存在并逐像素比对,失配抛 AssertionError;
 *  - record:仅当基准不存在时写入,已存在则报错;
 *  - update:无条件覆盖同名基准。
 */
fun golden(id: String, background: Int = Color.WHITE, content: Widget.() -> Unit) {
    GoldenEngine.assertMatchesBaseline(snapshotImage(background = background, content = content), id)
}

/**
 * 在指定画布尺寸上执行 painter 绘制并与 golden 基准比对(默认 verify 模式)。
 *
 * 确定性原则与模式说明同 [golden]:仅接受可确定性复现的纯绘制内容,文本/外网/随机/时变内容禁止进入 golden。
 */
fun goldenPixels(
    id: String,
    width: Float,
    height: Float,
    background: Int = Color.WHITE,
    painter: (Canvas) -> Unit,
) {
    GoldenEngine.assertMatchesBaseline(painterImage(width, height, background, painter), id)
}

/**
 * 直接比对一张已渲染 [Image] 与 golden 基准(默认 verify 模式),支持逐像素容差与失配比例上限。
 *
 * 确定性原则与模式说明同 [golden];供绕过 widget/尺寸 DSL、直接构造像素的场景使用。
 */
fun assertImageMatchesBaseline(
    image: Image,
    id: String,
    perPixelTolerance: Int = 0,
    allowMismatchRatio: Double = 0.0,
) {
    GoldenEngine.assertMatchesBaseline(image, id, perPixelTolerance, allowMismatchRatio)
}
