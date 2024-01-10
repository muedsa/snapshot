package com.muedsa.snapshot.rendering.flex

import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import com.muedsa.snapshot.VerticalDirection
import com.muedsa.snapshot.paint.Axis
import com.muedsa.snapshot.precisionErrorTolerance
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.PaintingContext
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderContainerBox
import org.jetbrains.skia.paragraph.BaselineMode
import org.jetbrains.skia.paragraph.Direction
import kotlin.math.max

open class RenderFlex(
    val direction: Axis = Axis.HORIZONTAL,
    val mainAxisSize: MainAxisSize = MainAxisSize.MAX,
    val mainAxisAlignment: MainAxisAlignment = MainAxisAlignment.START,
    val crossAxisAlignment: CrossAxisAlignment = CrossAxisAlignment.CENTER,
    val textDirection: Direction? = null,
    val verticalDirection: VerticalDirection = VerticalDirection.DOWN,
    val textBaseline: BaselineMode? = null,
    val clipBehavior: ClipBehavior = ClipBehavior.NONE,
    children: Array<RenderBox>? = null,
) : RenderContainerBox(
    children = children
) {

    override fun setupParentData(child: RenderBox) {
        if (child.parentData !is FlexParentData) {
            child.parentData = FlexParentData()
        }
    }

    protected var overflow = 0f
        private set

    protected open val hasOverflow: Boolean
        get() = overflow > precisionErrorTolerance

    private fun getMainSize(size: Size): Float = when (direction) {
        Axis.HORIZONTAL -> size.width
        Axis.VERTICAL -> size.height
    }

    private fun getCrossSize(size: Size): Float = when (direction) {
        Axis.HORIZONTAL -> size.height
        Axis.VERTICAL -> size.width
    }

    private fun computeSizes(constraints: BoxConstraints): LayoutSizes {
        // Determine used flex factor, size inflexible items, calculate free space.
        var totalFlex: Int = 0
        val maxMainSize: Float = if (direction == Axis.HORIZONTAL) constraints.maxWidth else constraints.maxHeight
        val canFlex: Boolean = maxMainSize < Float.POSITIVE_INFINITY

        var crossSize: Float = 0f
        var allocatedSize: Float = 0f

        var lastFlexChild: RenderBox? = null
        children?.forEach { child ->
            val childParentData: FlexParentData = child.parentData as FlexParentData
            val flex: Int = childParentData.flex ?: 0
            if (flex > 0) {
                totalFlex += flex
                lastFlexChild = child
            } else {
                val innerConstraints: BoxConstraints = if (crossAxisAlignment == CrossAxisAlignment.STRETCH) {
                    when (direction) {
                        Axis.HORIZONTAL -> BoxConstraints.tightFor(height = constraints.maxHeight)
                        Axis.VERTICAL -> BoxConstraints.tightFor(width = constraints.maxWidth)
                    }
                } else {
                    when (direction) {
                        Axis.HORIZONTAL -> BoxConstraints(maxHeight = constraints.maxHeight)
                        Axis.VERTICAL -> BoxConstraints(maxWidth = constraints.maxWidth)
                    }
                }
                child.layout(innerConstraints)
                val childSize: Size = child.definiteSize
                allocatedSize += getMainSize(childSize)
                crossSize = max(crossSize, getCrossSize(childSize))
            }
        }

        // Distribute free space to flexible children.
        val freeSpace: Float = max(0f, (if (canFlex) maxMainSize else 0f) - allocatedSize)
        var allocatedFlexSpace: Float = 0f
        if (totalFlex > 0) {
            val spacePerFlex: Float = if (canFlex) freeSpace / totalFlex else Float.NaN
            children?.forEach { child ->
                val childParentData: FlexParentData = (child.parent as FlexParentData?)!!
                val flex: Int = childParentData.flex ?: 0
                if (flex > 0) {
                    val maxChildExtent: Float = if (canFlex)
                        if (child == lastFlexChild) freeSpace - allocatedFlexSpace else spacePerFlex * flex
                    else Float.POSITIVE_INFINITY
                    val minChildExtent: Float = when (childParentData.fit ?: FlexFit.TIGHT) {
                        FlexFit.TIGHT -> {
                            assert(maxChildExtent < Float.POSITIVE_INFINITY)
                            maxChildExtent
                        }

                        FlexFit.LOOSE -> 0f
                    }
                    val innerConstraints: BoxConstraints = if (crossAxisAlignment == CrossAxisAlignment.STRETCH) {
                        when (direction) {
                            Axis.HORIZONTAL -> BoxConstraints(
                                minWidth = minChildExtent,
                                maxWidth = maxChildExtent,
                                minHeight = constraints.maxHeight,
                                maxHeight = constraints.maxHeight,
                            )

                            Axis.VERTICAL -> BoxConstraints(
                                minWidth = constraints.maxWidth,
                                maxWidth = constraints.maxWidth,
                                minHeight = minChildExtent,
                                maxHeight = maxChildExtent,
                            )
                        }
                    } else {
                        when (direction) {
                            Axis.HORIZONTAL -> BoxConstraints(
                                minWidth = minChildExtent,
                                maxWidth = maxChildExtent,
                                maxHeight = constraints.maxHeight,
                            )

                            Axis.VERTICAL -> BoxConstraints(
                                minHeight = minChildExtent,
                                maxHeight = maxChildExtent,
                            )
                        }
                    }
                    child.layout(innerConstraints)
                    val childSize: Size = child.definiteSize
                    val childMainSize: Float = getMainSize(childSize)
                    assert(childMainSize <= maxChildExtent)
                    allocatedSize += childMainSize
                    allocatedFlexSpace += maxChildExtent
                    crossSize = max(crossSize, getCrossSize(childSize))
                }
            }

        }
        val idealSize: Float = if (canFlex && mainAxisSize == MainAxisSize.MAX) maxMainSize else allocatedSize
        return LayoutSizes(
            mainSize = idealSize,
            crossSize = crossSize,
            allocatedSize = allocatedSize
        )
    }

    override fun performLayout() {
        // debugCheckConstraints()
        val sizes: LayoutSizes = computeSizes(constraints = definiteConstraints)
        val allocatedSize: Float = sizes.allocatedSize
        var actualSize: Float = sizes.mainSize
        var crossSize: Float = sizes.crossSize
        var maxBaselineDistance: Float = 0f
        if (crossAxisAlignment == CrossAxisAlignment.BASELINE) {
            var maxSizeAboveBaseline: Float = 0f
            var maxSizeBelowBaseline: Float = 0f
            children?.forEach { child ->
                assert(textBaseline != null) { "To use CrossAxisAlignment.baseline, you must also specify which baseline to use using the \"BaselineMode\" argument." }
                val distance: Float? = child.getDistanceToBaseline(textBaseline!!, onlyReal = true)
                if (distance != null) {
                    maxBaselineDistance = max(maxBaselineDistance, distance)
                    maxSizeAboveBaseline = max(distance, maxSizeAboveBaseline)
                    maxSizeBelowBaseline = max(child.definiteSize.height - distance, maxSizeBelowBaseline)
                    crossSize = max(maxSizeAboveBaseline + maxSizeBelowBaseline, crossSize)
                }
            }
        }
        when (direction) {
            Axis.HORIZONTAL -> {
                size = definiteConstraints.constrain(Size(actualSize, crossSize))
                actualSize = definiteSize.width
                crossSize = definiteSize.height
            }

            Axis.VERTICAL -> {
                size = definiteConstraints.constrain(Size(crossSize, actualSize))
                actualSize = definiteSize.height
                crossSize = definiteSize.width
            }
        }
        val actualSizeDelta: Float = actualSize - allocatedSize
        overflow = max(0f, -actualSizeDelta)

        val remainingSpace: Float = max(0f, actualSizeDelta)

        // flipMainAxis is used to decide whether to lay out
        // left-to-right/top-to-bottom (false), or right-to-left/bottom-to-top
        // (true). The _startIsTopLeft will return null if there's only one child
        // and the relevant direction is null, in which case we arbitrarily decide
        // to flip, but that doesn't have any detectable effect.
        val flipMainAxis: Boolean = !(startIsTopLeft(direction, textDirection, verticalDirection) ?: true)

        val leadingSpace: Float
        val betweenSpace: Float
        when (mainAxisAlignment) {
            MainAxisAlignment.START -> {
                leadingSpace = 0f
                betweenSpace = 0f
            }
            MainAxisAlignment.END -> {
                leadingSpace = remainingSpace
                betweenSpace = 0f
            }
            MainAxisAlignment.CENTER -> {
                leadingSpace = remainingSpace / 2f
                betweenSpace = 0f
            }
            MainAxisAlignment.SPACE_BETWEEN -> {
                leadingSpace = 0f
                betweenSpace =  if (childCount > 1) remainingSpace / (childCount - 1) else 0f
            }
            MainAxisAlignment.SPACE_AROUND -> {
                leadingSpace = if (childCount > 0) remainingSpace / childCount else 0f
                betweenSpace = leadingSpace / 2
            }
            MainAxisAlignment.SPACE_EVENLY -> {
                val bs = if (childCount > 0) remainingSpace / (childCount + 1) else 0f
                leadingSpace = if (childCount > 0) remainingSpace / (childCount + 1) else 0f
                betweenSpace = leadingSpace
            }
        }

        // Position elements
        var childMainPosition: Float = if (flipMainAxis) actualSize - leadingSpace else leadingSpace
        children?.forEach { child ->
            val childParentData: FlexParentData = child.parentData as FlexParentData
            val childCrossPosition: Float = when (crossAxisAlignment) {
                CrossAxisAlignment.START, CrossAxisAlignment.END -> {
                    if (startIsTopLeft(direction.flipAxis(), textDirection, verticalDirection)
                        == (crossAxisAlignment == CrossAxisAlignment.START)
                    ) {
                        0f
                    } else {
                        crossSize - getCrossSize(child.definiteSize)
                    }
                }

                CrossAxisAlignment.CENTER -> crossSize / 2f - getCrossSize(child.definiteSize) / 2f
                CrossAxisAlignment.STRETCH -> 0f
                CrossAxisAlignment.BASELINE -> {
                    if (direction == Axis.HORIZONTAL) {
                        assert(textBaseline != null)
                        val distance: Float? = child.getDistanceToBaseline(textBaseline!!, true)
                        if (distance != null) {
                            maxBaselineDistance - distance
                        } else {
                            0f
                        }
                    } else {
                        0f
                    }
                }
            }

            if (flipMainAxis) {
                childMainPosition -= getMainSize(child.definiteSize)
            }
            when (direction) {
                Axis.HORIZONTAL -> childParentData.offset = Offset(childMainPosition, childCrossPosition)
                Axis.VERTICAL -> childParentData.offset = Offset(childCrossPosition, childMainPosition)
            }
            if (flipMainAxis) {
                childMainPosition -= betweenSpace
            } else {
                childMainPosition += getMainSize(child.definiteSize) + betweenSpace
            }
        }
    }

    override fun paint(context: PaintingContext, offset: Offset) {
        if (!hasOverflow) {
            defaultPaint(context, offset)
            return
        }
        // There's no point in drawing the children if we're empty.
        if (definiteSize.isEmpty) {
            return
        }

        context.doClipRect(
            offset = offset,
            clipRect = Offset.ZERO combine definiteSize,
            clipBehavior = clipBehavior,
        ) { c, o ->
            defaultPaint(c, o)
        }
    }

}