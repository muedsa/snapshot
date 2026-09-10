package com.muedsa.snapshot.render.flex

import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import com.muedsa.snapshot.drawPainter
import com.muedsa.snapshot.paint.Axis
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderConstrainedBox
import com.muedsa.snapshot.rendering.flex.*
import org.jetbrains.skia.Color
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PathEffect
import org.jetbrains.skia.paragraph.BaselineMode
import org.jetbrains.skia.paragraph.Direction
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.expect

class RenderFlexTest {

    @Test
    fun setupParentDataTest() {
        val child1 = RenderConstrainedBox(
            BoxConstraints.expand(width = 100f, 100f)
        )
        val child2 = RenderConstrainedBox(
            BoxConstraints.expand(width = 120f, 80f)
        )
        val child3 = RenderConstrainedBox(
            BoxConstraints.expand(width = 80f, 120f)
        )
        val renderFlex = RenderFlex().apply {
            appendChild(child1)
            appendChild(child2)
            appendChild(child3)
        }
        renderFlex.children.forEach { child ->
            assertTrue(child.parentData is FlexParentData, "子盒的 parentData 应为 FlexParentData")
        }
    }

    @Test
    fun direction_mainAxisAlign_crossAxisAlign_test() {
        val childSizeArr: Array<Size> = arrayOf(
            Size(120f, 80f),
            Size(100f, 100f),
            Size(80f, 120f)
        )
        val space: Float = 10f
        val size: Size = childSizeArr.reduce { acc, next ->
            acc + next + Size.square(space)
        }

        Axis.entries.forEachIndexed { directionIndex, direction ->
            MainAxisAlignment.entries.forEachIndexed { mainAxisAlignmentIndex, mainAxisAlignment ->
                CrossAxisAlignment.entries.forEachIndexed { crossAxisAlignmentIndex, crossAxisAlignment ->
                    val children: List<RenderBox> = buildList(childSizeArr.size) {
                        for (index in childSizeArr.indices) {
                            val childSize = childSizeArr[index]
                            add(
                                RenderConstrainedBox(
                                    BoxConstraints.expand(width = childSize.width, childSize.height)
                                )
                            )
                        }
                    }
                    val renderFlex = RenderFlex(
                        direction = direction,
                        mainAxisAlignment = mainAxisAlignment,
                        crossAxisAlignment = crossAxisAlignment,
                        textDirection = Direction.LTR,
                        textBaseline = BaselineMode.ALPHABETIC
                    ).apply {
                        appendChildren(children)
                    }
                    renderFlex.layout(BoxConstraints.expand(size.width, size.height))
                    valid_direction_mainAxisAlign_crossAxisAlign_test(renderFlex, size)
                    drawPainter(
                        "render/flex/d${directionIndex}_m${mainAxisAlignmentIndex}_c${crossAxisAlignmentIndex}",
                        size = size,
                        debugInfo = "Flex(\n$direction\n$mainAxisAlignment\n$crossAxisAlignment\n)"
                    ) { canvas ->

                        canvas.drawRect(Offset.ZERO combine size, Paint().apply {
                            setStroke(true)
                        })

                        children.forEachIndexed { childIndex: Int, child: RenderBox ->
                            val childSize: Size = childSizeArr[childIndex]
                            val childOffset: Offset = child.parentData!!.offset
                            canvas.drawRect(childOffset combine childSize, Paint().apply {
                                setStroke(true)
                                pathEffect = PathEffect.makeDash(floatArrayOf(3f, 3f), 0f)
                                color = COLORS[childIndex % COLORS.size]
                                strokeWidth = 2f
                            })
                        }
                    }
                }
            }
        }
    }

    private fun valid_direction_mainAxisAlign_crossAxisAlign_test(
        renderFlex: RenderFlex,
        size: Size,
    ) {
        assertTrue(
            renderFlex.definiteSize == size,
            "$renderFlex \n${renderFlex.definiteSize} != $size"
        )
        // 各子盒的 main/cross 轴偏移目前**未断言**(原先的 TODO 打算按 MainAxisAlignment 各档校验)。
        // 本用例当前只校验 flex 尺寸,并产出一张覆盖 方向×主轴对齐×交叉轴对齐 全矩阵的 artifact;
        // 补断言需按各档语义推导期望偏移,留待后续批次。
    }

    @Test
    fun parent_data_test() {
        Axis.entries.forEach { direction: Axis ->
            direction_parent_data_flex_test(direction)
            direction_parent_data_fit_test(direction)
        }
    }

    private fun direction_parent_data_flex_test(direction: Axis) {
        val children: List<RenderBox> = buildList(5) {
            for (i in 0 until 5) {
                add(RenderConstrainedBox(additionalConstraints = BoxConstraints()))
            }
        }
        val renderFlex = RenderFlex(direction = direction).apply {
            appendChildren(children)
        }
        val defaultSize = 100f
        var mainAxisSize = 0f
        children.forEachIndexed { index, child ->
            val childParentData: FlexParentData = child.parentData!! as FlexParentData
            childParentData.flex = index + 1
            mainAxisSize += childParentData.flex!! * defaultSize
        }
        renderFlex.layout(
            BoxConstraints.loose(
                Size(
                    width = if (renderFlex.direction == Axis.HORIZONTAL) mainAxisSize else defaultSize,
                    height = if (renderFlex.direction == Axis.HORIZONTAL) defaultSize else mainAxisSize,
                )
            )
        )

        var mainAxisOffset = 0f
        children.forEachIndexed { index, child ->
            val childParentData: FlexParentData = child.parentData as FlexParentData
            val childFlex: Int = childParentData.flex!!
            expect(mainAxisOffset, message = "child$index, offset=${child.parentData!!.offset}") {
                if (renderFlex.direction == Axis.HORIZONTAL) {
                    childParentData.offset.x
                } else {
                    childParentData.offset.y
                }
            }
            val childMainAxisSize = childFlex * defaultSize
            expect(childMainAxisSize, message = "child$index, size=${child.definiteSize}") {
                if (renderFlex.direction == Axis.HORIZONTAL) {
                    child.definiteSize.width
                } else {
                    child.definiteSize.height
                }
            }
            mainAxisOffset += childMainAxisSize
        }
    }


    private fun direction_parent_data_fit_test(direction: Axis) {
        // 遍历全部"可伸缩子盒下标":原先用 Random(System.currentTimeMillis()) 只随机覆盖其中一种,
        // 且每次运行的输入都不同(非确定)。全遍历既确定又覆盖更全。
        for (expandIndex in 0 until CHILDREN_COUNT) {
            direction_parent_data_fit_case(direction, expandIndex)
        }
    }

    private fun direction_parent_data_fit_case(direction: Axis, expandIndex: Int) {
        val childrenCount = CHILDREN_COUNT
        val defaultSize = DEFAULT_SIZE
        var mainAxisSize = 0f
        val expandedSpace = defaultSize * (expandIndex + 1)
        val children: List<RenderBox> = buildList(childrenCount) {
            for (index in 0 until childrenCount) {
                if (index == expandIndex) {
                    add(RenderConstrainedBox(additionalConstraints = BoxConstraints()))
                } else {
                    add(RenderConstrainedBox(additionalConstraints = BoxConstraints.expand(defaultSize, defaultSize)))
                }
            }
        }
        val renderFlex = RenderFlex(direction = direction).apply { appendChildren(children) }
        children.forEachIndexed { index, child ->
            val childParentData: FlexParentData = child.parentData!! as FlexParentData
            mainAxisSize += defaultSize
            if (index == expandIndex) {
                childParentData.flex = 1
                childParentData.fit = FlexFit.TIGHT
                mainAxisSize += expandedSpace
            }
        }
        renderFlex.layout(
            BoxConstraints.expand(
                width = if (renderFlex.direction == Axis.HORIZONTAL) mainAxisSize else defaultSize,
                height = if (renderFlex.direction == Axis.HORIZONTAL) defaultSize else mainAxisSize,
            )
        )

        var mainAxisOffset = 0f
        children.forEachIndexed { index, child ->
            val childParentData: FlexParentData = child.parentData as FlexParentData
            expect(mainAxisOffset, message = "child$index, offset=${child.parentData!!.offset}") {
                if (renderFlex.direction == Axis.HORIZONTAL) {
                    childParentData.offset.x
                } else {
                    childParentData.offset.y
                }
            }
            val childMainAxisSize = defaultSize + if (index == expandIndex) expandedSpace else 0f
            expect(childMainAxisSize, message = "child$index, size=${child.definiteSize}") {
                if (renderFlex.direction == Axis.HORIZONTAL) {
                    child.definiteSize.width
                } else {
                    child.definiteSize.height
                }
            }
            mainAxisOffset += childMainAxisSize
        }
    }

    companion object {
        private const val CHILDREN_COUNT = 5
        private const val DEFAULT_SIZE = 100f

        private fun getMainAxisOffset(offset: Offset, direction: Axis): Float =
            when (direction) {
                Axis.HORIZONTAL -> offset.x
                Axis.VERTICAL -> offset.y
            }

        private fun getCrossAxisOffset(offset: Offset, direction: Axis): Float =
            when (direction) {
                Axis.HORIZONTAL -> offset.y
                Axis.VERTICAL -> offset.x
            }

        val COLORS = arrayOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN, Color.MAGENTA)
    }

}