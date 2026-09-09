package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.Matrix44CMO
import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import com.muedsa.snapshot.rendering.PaintingContext
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PathEffect
import org.jetbrains.skia.Rect
import org.jetbrains.skia.paragraph.BaselineMode

abstract class RenderBox {

    internal var parentData: BoxParentData? = null

    internal var parent: RenderBox? = null
        set(value) {
            var temp = value
            while (temp != null) {
                assert(temp != this) { "render tree circulate" }
                temp = value?.parent
            }
            if (field != value) {
                value?.setupParentData(this)
            }
            field = value
        }

    protected open fun setupParentData(child: RenderBox) {
        child.parentData = BoxParentData()
    }

    // 组件的约束,由父级传入
    protected var constraints: BoxConstraints? = null
    val definiteConstraints: BoxConstraints
        get() = constraints!!

    protected var size: Size? = null
    val definiteSize: Size
        get() {
            assert(size != null) {
                "RenderBox was not laid out: $this"
            }
            return size!!
        }

    fun getDistanceToBaseline(baseline: BaselineMode, onlyReal: Boolean = false): Float? {
        var result: Float? = null
        try {
            result = computeDistanceToActualBaseline(baseline)
        } catch (_: Throwable) {
        }
        if (result == null && !onlyReal) {
            return definiteSize.height
        }
        return result
    }

    /**
     * 返回真实基线距离,不施加 [getDistanceToBaseline] 的"无基线 → [definiteSize].height"回退。
     *
     * 回退只应在**最外层**对子盒的那一次查询上生效;内部委托([RenderSingleChildBox] 的代理委托、
     * [RenderContainerBox.defaultComputeDistanceToFirstActualBaseline] 等容器默认基线)一律走本方法。
     * 否则"无基线"会在第一层委托处就被替换成盒高,使 `CrossAxisAlignment.BASELINE` 退化为底边对齐,
     * 且行为随代理层数变化。与 Flutter 的 `RenderBox.getDistanceToActualBaseline` 对应。
     *
     * 用 internal 而非 protected:Kotlin 的 protected 不允许在声明类([RenderBox])类型的接收者上调用,
     * 而委托方持有的 `child` 正是 [RenderBox] 类型。
     */
    internal fun getDistanceToActualBaseline(baseline: BaselineMode): Float? =
        computeDistanceToActualBaseline(baseline)

    protected open fun computeDistanceToActualBaseline(baseline: BaselineMode): Float? = null


    // LAYOUT

    fun layout(constraints: BoxConstraints) {
        this.constraints = constraints
        performLayout()
        assert(size != null) { "$this no set size after layout with $constraints" }
    }

    protected abstract fun performLayout()


    // PAINTING

    fun getPaintBounds(): Rect = Offset.ZERO combine definiteSize

    /**
     * 渲染debug信息
     */
    open fun debugPaint(context: PaintingContext, offset: Offset) {
        // 使用虚线画一个边框
        context.canvas.drawRect(offset combine definiteSize,
            Paint().apply {
                setStroke(true)
                setARGB(144, 255, 0, 0)
                pathEffect = PathEffect.makeDash(floatArrayOf(3f, 3f), 0f)
            }
        )
    }

    /**
     * 渲染
     */
    open fun paint(context: PaintingContext, offset: Offset) {}

    open fun applyPaintTransform(child: RenderBox, transform: Matrix44CMO) {
        assert(child.parent == this)
    }

    fun getTransformTo(ancestor: RenderBox): Matrix44CMO {
        val rendererList = mutableListOf<RenderBox>()
        do {
            var renderer = this
            rendererList.add(renderer)
            renderer = renderer.parent as RenderBox
        } while (renderer != ancestor)
        rendererList.add(ancestor)
        val transform = Matrix44CMO.identity()
        for (index: Int in rendererList.indices.reversed()) {
            if (index > 1) {
                rendererList[index].applyPaintTransform(rendererList[index - 1], transform)
            }
        }
        return transform
    }

}