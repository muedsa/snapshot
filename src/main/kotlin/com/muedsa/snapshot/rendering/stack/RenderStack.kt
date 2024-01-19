package com.muedsa.snapshot.rendering.stack

import com.muedsa.geometry.*
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.PaintingContext
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderContainerBox
import org.jetbrains.skia.paragraph.Direction
import kotlin.math.max

open class RenderStack(
    val alignment: AlignmentGeometry = AlignmentDirectional.TOP_START,
    val textDirection: Direction = Direction.LTR,
    val fit: StackFit = StackFit.LOOSE,
    val clipBehavior: ClipBehavior = ClipBehavior.HARD_EDGE,
    children: Array<RenderBox>? = null,
) : RenderContainerBox(
    children = children
) {

    var hasVisualOverflow: Boolean = false

    val resolvedAlignment: BoxAlignment
        get() = alignment.resolve(textDirection)

    override fun setupParentData(child: RenderBox) {
        if (child.parentData !is StackParentData) {
            child.parentData = StackParentData()
        }
    }

    private fun computeSize(constraints: BoxConstraints): Size {
        var hasNonPositionedChildren: Boolean = false
        if (childCount == 0) {
            return if (constraints.biggest.isFinite) constraints.biggest else constraints.smallest
        }
        var width = constraints.minWidth
        var height = constraints.minHeight

        val nonPositionedConstraints: BoxConstraints = when (fit) {
            StackFit.LOOSE -> constraints.loosen()
            StackFit.EXPAND -> BoxConstraints.tight(constraints.biggest)
            StackFit.PASSTHROUGH -> constraints
        }

        children?.forEach { child ->
            val childParentData: StackParentData = child.parentData as StackParentData
            if (!childParentData.isPositioned) {
                hasNonPositionedChildren = true

                child.layout(nonPositionedConstraints)
                val childSize: Size = child.definiteSize

                width = max(width, childSize.width)
                height = max(height, childSize.height)
            }
        }

        val size: Size
        if (hasNonPositionedChildren) {
            size = Size(width, height)
            assert(size.width == constraints.constrainWidth(width))
            assert(size.height == constraints.constrainHeight(height))
        } else {
            size = constraints.biggest
        }
        assert(size.isFinite)
        return size
    }

    override fun performLayout() {
        hasVisualOverflow = false
        size = computeSize(definiteConstraints)

        children?.forEach { child ->
            val childParentData = child.parentData as StackParentData
            if (!childParentData.isPositioned) {
                childParentData.offset = resolvedAlignment.alongOffset(definiteSize - child.definiteSize)
            } else {
                hasVisualOverflow = layoutPositionedChild(
                    child = child,
                    childParentData = childParentData,
                    size = definiteSize,
                    alignment = resolvedAlignment
                )
            }
        }
    }

    open fun paintStack(context: PaintingContext, offset: Offset) {
        defaultPaint(context, offset)
    }

    override fun paint(context: PaintingContext, offset: Offset) {
        if (clipBehavior != ClipBehavior.NONE && hasVisualOverflow) {
            context.pushClipRect(
                offset = offset,
                clipRect = Offset.ZERO combine definiteSize,
                clipBehavior = clipBehavior
            ) { c, o ->
                paintStack(c, o)
            }
        } else {
            paintStack(context, offset)
        }
    }

    companion object {
        fun layoutPositionedChild(
            child: RenderBox,
            childParentData: StackParentData,
            size: Size,
            alignment: BoxAlignment,
        ): Boolean {
            assert(childParentData.isPositioned)
            assert(child.parentData == childParentData)
            var hasVisualOverflow: Boolean = false
            var childConstraints: BoxConstraints = BoxConstraints()
            if (childParentData.left != null && childParentData.right != null) {
                childConstraints =
                    childConstraints.tighten(width = size.width - childParentData.right!! - childParentData.left!!)
            } else if (childParentData.width != null) {
                childConstraints = childConstraints.tighten(width = childParentData.width)
            }

            if (childParentData.top != null && childParentData.bottom != null) {
                childConstraints =
                    childConstraints.tighten(height = size.height - childParentData.bottom!! - childParentData.top!!)
            } else if (childParentData.height != null) {
                childConstraints = childConstraints.tighten(height = childParentData.height)
            }
            child.layout(childConstraints)
            val childSize = child.definiteSize

            val x: Float = if (childParentData.left != null) {
                childParentData.left!!
            } else if (childParentData.right != null) {
                size.width - childParentData.right!! - childSize.width
            } else {
                alignment.alongOffset(size - childSize).x
            }

            if (x < 0.0 || x + childSize.width > size.width) {
                hasVisualOverflow = true
            }

            val y: Float = if (childParentData.top != null) {
                childParentData.top!!
            } else if (childParentData.bottom != null) {
                size.height - childParentData.bottom!! - childSize.height
            } else {
                alignment.alongOffset(size - childSize).y
            }

            if (y < 0f || y + childSize.height > size.height) {
                hasVisualOverflow = true
            }

            childParentData.offset = Offset(x, y)

            return hasVisualOverflow
        }
    }
}