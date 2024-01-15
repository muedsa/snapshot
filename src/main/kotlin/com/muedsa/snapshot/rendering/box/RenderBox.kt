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