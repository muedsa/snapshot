package com.muedsa.snapshot.rendering.stack

import com.muedsa.geometry.AlignmentDirectional
import com.muedsa.geometry.AlignmentGeometry
import com.muedsa.geometry.Offset
import com.muedsa.geometry.shift
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.PaintingContext
import com.muedsa.snapshot.rendering.box.EMPTY_FILTER_PAINT_BOUNDS
import com.muedsa.snapshot.rendering.box.intersectFilterPaintBounds
import org.jetbrains.skia.Rect
import org.jetbrains.skia.paragraph.Direction

/** 布局所有子节点，但只绘制 [index] 指定的子节点；为 null 时不绘制。 */
class RenderIndexedStack(
    val index: Int? = 0,
    alignment: AlignmentGeometry = AlignmentDirectional.TOP_START,
    textDirection: Direction = Direction.LTR,
    fit: StackFit = StackFit.LOOSE,
    clipBehavior: ClipBehavior = ClipBehavior.HARD_EDGE,
) : RenderStack(
    alignment = alignment,
    textDirection = textDirection,
    fit = fit,
    clipBehavior = clipBehavior,
) {

    internal override fun getFilterPaintBounds(): Rect? {
        val selected = index?.let { children.getOrNull(it) } ?: return EMPTY_FILTER_PAINT_BOUNDS
        val bounds = selected.getFilterPaintBounds()?.shift(selected.parentData!!.offset)
        return if (clipBehavior != ClipBehavior.NONE && hasVisualOverflow) {
            bounds.intersectFilterPaintBounds(getPaintBounds())
        } else {
            bounds
        }
    }

    init {
        require(index == null || index >= 0) { "index must be null or non-negative, got $index" }
    }

    override fun performLayout() {
        require(index == null || children.isEmpty() || index < childCount) {
            "index $index is out of range for $childCount children"
        }
        super.performLayout()
    }

    override fun paintStack(context: PaintingContext, offset: Offset) {
        if (index == null || children.isEmpty()) return
        require(index < childCount) { "index $index is out of range for $childCount children" }
        val child = children[index]
        val childParentData = child.parentData!!
        context.paintChild(child, childParentData.offset + offset)
    }
}
