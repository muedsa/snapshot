package com.muedsa.snapshot.render.flex

import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import com.muedsa.snapshot.drawPainter
import com.muedsa.snapshot.paint.Axis
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderConstrainedBox
import com.muedsa.snapshot.rendering.flex.CrossAxisAlignment
import com.muedsa.snapshot.rendering.flex.FlexParentData
import com.muedsa.snapshot.rendering.flex.MainAxisAlignment
import com.muedsa.snapshot.rendering.flex.RenderFlex
import org.jetbrains.skia.Color
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PathEffect
import org.jetbrains.skia.paragraph.BaselineMode
import kotlin.test.Test

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
        val renderFlex = RenderFlex(
            children = arrayOf(child1, child2, child3)
        )
        renderFlex.children!!.forEach { child ->
            assert(child.parentData is FlexParentData)
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
            MainAxisAlignment.entries.forEachIndexed{ mainAxisAlignmentIndex, mainAxisAlignment ->
                CrossAxisAlignment.entries.forEachIndexed { crossAxisAlignmentIndex, crossAxisAlignment ->
                    val children: Array<RenderBox> = Array(childSizeArr.size) {
                        val childSize = childSizeArr[it]
                        RenderConstrainedBox(
                            BoxConstraints.expand(width = childSize.width, childSize.height)
                        )
                    }
                    val renderFlex = RenderFlex(
                        direction = direction,
                        mainAxisAlignment = mainAxisAlignment,
                        crossAxisAlignment = crossAxisAlignment,
                        children = children,
                        textBaseline = BaselineMode.ALPHABETIC
                    )
                    renderFlex.layout(BoxConstraints.expand(size.width, size.height))
                    valid_direction_mainAxisAlign_crossAxisAlign_test(renderFlex, size, children, childSizeArr, space)

                    val colors = arrayOf(Color.RED, Color.GREEN, Color.BLUE)
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
                                color = colors[childIndex % colors.size]
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
        children: Array<RenderBox>,
        childSizeArr: Array<Size>,
        space: Float
    ) {
        assert(renderFlex.definiteSize == size) {
            "$renderFlex \n${renderFlex.definiteSize} != $size"
        }
        println(renderFlex)
        children.forEachIndexed{ childIndex: Int, child: RenderBox ->
            val childOffset = child.parentData?.offset!!
            val mainAxisOffset = getMainAxisOffset(offset = childOffset, direction = renderFlex.direction)
            val crossAxisOffset = getCrossAxisOffset(offset = childOffset, direction = renderFlex.direction)

            println("child$childIndex, mainAxisOffset=$mainAxisOffset, crossAxisOffset=$crossAxisOffset")

//            when(renderFlex.mainAxisAlignment) {
//                MainAxisAlignment.START -> TODO()
//                MainAxisAlignment.END -> TODO()
//                MainAxisAlignment.CENTER -> TODO()
//                MainAxisAlignment.SPACE_BETWEEN -> TODO()
//                MainAxisAlignment.SPACE_AROUND -> TODO()
//                MainAxisAlignment.SPACE_EVENLY -> TODO()
//            }

        }
    }

    companion object {
        private fun getMainAxisOffset(offset: Offset, direction: Axis): Float =
            when(direction) {
                Axis.HORIZONTAL -> offset.x
                Axis.VERTICAL -> offset.y
            }

        private fun getCrossAxisOffset(offset: Offset, direction: Axis): Float =
            when(direction) {
                Axis.HORIZONTAL -> offset.y
                Axis.VERTICAL -> offset.x
            }
    }

}