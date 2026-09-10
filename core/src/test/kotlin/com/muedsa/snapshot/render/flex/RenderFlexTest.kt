package com.muedsa.snapshot.render.flex

import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import com.muedsa.snapshot.assertApproxEq
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
        assertMainAxisAlignmentProperties(renderFlex, size)
        assertCrossAxisAlignmentProperties(renderFlex, size)
    }

    /**
     * 主轴对齐的**性质**断言。
     *
     * 刻意不复刻 [RenderFlex] 的 `leadingSpace`/`betweenSpace` 公式(那样会与实现同源同错),
     * 只断言语义必然蕴含的关系:不重叠、贴边/居中、间距之间的关系。
     */
    private fun assertMainAxisAlignmentProperties(renderFlex: RenderFlex, size: Size) {
        val direction = renderFlex.direction
        val children = renderFlex.children
        val actualSize = getMainAxisSize(size, direction)
        val offsets = children.map { getMainAxisOffset(it.parentData!!.offset, direction) }
        val sizes = children.map { getMainAxisSize(it.definiteSize, direction) }

        // 通用:子盒按序不重叠,且都不越出容器
        assertTrue(offsets.first() >= -ALIGN_TOL, "首子盒主轴偏移为负: ${offsets.first()}")
        assertTrue(
            offsets.last() + sizes.last() <= actualSize + ALIGN_TOL,
            "末子盒越出容器: ${offsets.last() + sizes.last()} > $actualSize"
        )
        offsets.zipWithNext().forEachIndexed { index, (current, next) ->
            assertTrue(
                next >= current + sizes[index] - ALIGN_TOL,
                "主轴子盒 $index($current+${sizes[index]}) 与 ${index + 1}($next) 重叠"
            )
        }

        val leading = offsets.first()
        val trailing = actualSize - (offsets.last() + sizes.last())
        val gaps = (0 until children.size - 1).map { index ->
            offsets[index + 1] - (offsets[index] + sizes[index])
        }

        when (renderFlex.mainAxisAlignment) {
            MainAxisAlignment.START -> assertApproxEq(offsets.first(), 0f, ALIGN_TOL)

            MainAxisAlignment.END -> assertApproxEq(offsets.last() + sizes.last(), actualSize, ALIGN_TOL)

            MainAxisAlignment.CENTER -> assertApproxEq(leading, trailing, ALIGN_TOL)

            MainAxisAlignment.SPACE_BETWEEN -> {
                assertApproxEq(offsets.first(), 0f, ALIGN_TOL)
                assertApproxEq(offsets.last() + sizes.last(), actualSize, ALIGN_TOL)
                gaps.forEach { assertTrue(it > 0f, "SPACE_BETWEEN 的间距应为正,实际 $it") }
                gaps.forEach { assertApproxEq(it, gaps.first(), ALIGN_TOL) }
            }

            MainAxisAlignment.SPACE_AROUND -> {
                // Flutter 语义:free space 均分子盒之间,首尾各取其中的**一半**
                // → 间距 = 2 × 首尾留白
                assertApproxEq(leading, trailing, ALIGN_TOL)
                gaps.forEach {
                    assertTrue(it > 0f, "SPACE_AROUND 的间距应为正,实际 $it")
                    assertApproxEq(it, leading * 2f, ALIGN_TOL)
                }
            }

            MainAxisAlignment.SPACE_EVENLY -> {
                // 首尾留白与各间距彼此相等
                assertApproxEq(leading, trailing, ALIGN_TOL)
                gaps.forEach { assertApproxEq(it, leading, ALIGN_TOL) }
            }
        }
    }

    /** 交叉轴对齐的**性质**断言(同[assertMainAxisAlignmentProperties],不复刻实现公式)。 */
    private fun assertCrossAxisAlignmentProperties(renderFlex: RenderFlex, size: Size) {
        val direction = renderFlex.direction
        val children = renderFlex.children
        val crossSize = getCrossAxisSize(size, direction)
        val offsets = children.map { getCrossAxisOffset(it.parentData!!.offset, direction) }
        val sizes = children.map { getCrossAxisSize(it.definiteSize, direction) }

        when (renderFlex.crossAxisAlignment) {
            CrossAxisAlignment.START -> offsets.forEach { assertApproxEq(it, 0f, ALIGN_TOL) }

            CrossAxisAlignment.END -> offsets.zip(sizes).forEach { (offset, childSize) ->
                assertApproxEq(offset + childSize, crossSize, ALIGN_TOL)
            }

            CrossAxisAlignment.CENTER -> offsets.zip(sizes).forEach { (offset, childSize) ->
                assertApproxEq(offset + childSize / 2f, crossSize / 2f, ALIGN_TOL)
            }

            CrossAxisAlignment.STRETCH -> sizes.forEach {
                assertApproxEq(it, crossSize, ALIGN_TOL)
            }

            // 子盒是无基线的 RenderConstrainedBox:按文档"无基线者 top 对齐",交叉轴偏移为 0
            CrossAxisAlignment.BASELINE -> offsets.forEach { assertApproxEq(it, 0f, ALIGN_TOL) }
        }
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

        /** 对齐性质断言的容差:远小于一像素,又远大于浮点除法带来的噪声。 */
        private const val ALIGN_TOL = 0.01f

        private fun getMainAxisSize(size: Size, direction: Axis): Float =
            when (direction) {
                Axis.HORIZONTAL -> size.width
                Axis.VERTICAL -> size.height
            }

        private fun getCrossAxisSize(size: Size, direction: Axis): Float =
            when (direction) {
                Axis.HORIZONTAL -> size.height
                Axis.VERTICAL -> size.width
            }

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