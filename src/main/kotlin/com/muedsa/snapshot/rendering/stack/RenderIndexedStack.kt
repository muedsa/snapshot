package com.muedsa.snapshot.rendering.stack

import com.muedsa.geometry.AlignmentDirectional
import com.muedsa.geometry.AlignmentGeometry
import com.muedsa.geometry.Offset
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.PaintingContext
import com.muedsa.snapshot.rendering.box.RenderBox
import org.jetbrains.skia.paragraph.Direction

/**
 * 只渲染指定的child
 * 感觉用不到
 */
class RenderIndexedStack(
    val index: Int? = 0,
    alignment: AlignmentGeometry = AlignmentDirectional.TOP_START,
    textDirection: Direction = Direction.LTR,
    fit: StackFit = StackFit.LOOSE,
    clipBehavior: ClipBehavior = ClipBehavior.HARD_EDGE,
    children: Array<RenderBox>? = null
) : RenderStack(
    alignment = alignment,
    textDirection = textDirection,
    fit = fit,
    clipBehavior = clipBehavior,
    children = children
) {

    private fun childAtIndex(): RenderBox {
        assert(index != null)
        return children!![index!!]
    }

    override fun paintStack(context: PaintingContext, offset: Offset) {
        if (children.isNullOrEmpty() || index == null) {
            return
        }
        val child = childAtIndex()
        val childParentData = child.parentData!!
        context.paintChild(child, childParentData.offset + offset)
    }
}