package com.muedsa.snapshot.rendering

import com.muedsa.geometry.Matrix44CMO
import com.muedsa.snapshot.annotation.MustCallSuper
import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PathEffect
import org.jetbrains.skia.Rect

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
                parentData = BoxParentData()
            }
            field = value
        }

    // 组件的约束,由父级传入
    protected var constraints: BoxConstraints? = null
    val definiteConstraints: BoxConstraints
        get() = constraints!!

    @MustCallSuper
    open fun getMinIntrinsicWidth(height: Float): Float {
        assert(height >= 0)
        return computeMinIntrinsicWidth(height)
    }

    protected open fun computeMinIntrinsicWidth(height: Float): Float {
        return 0f
    }

    @MustCallSuper
    open fun getMaxIntrinsicWidth(height: Float): Float {
        return computeMaxIntrinsicWidth(height)
    }

    protected open fun computeMaxIntrinsicWidth(height: Float): Float {
        return 0f
    }

    @MustCallSuper
    open fun getMinIntrinsicHeight(width: Float): Float {
        assert(width >= 0)
        return computeMinIntrinsicHeight(width)
    }

    protected open fun computeMinIntrinsicHeight(width: Float): Float {
        return 0f
    }

    @MustCallSuper
    open fun getMaxIntrinsicHeight(width: Float): Float {
        assert(width >= 0)
        return computeMaxIntrinsicHeight(width)
    }

    protected open fun computeMaxIntrinsicHeight(width: Float): Float {
        return 0f
    }

    protected var size: Size? = null
    val definiteSize: Size
        get() = size!!


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
            rendererList.add(renderer);
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