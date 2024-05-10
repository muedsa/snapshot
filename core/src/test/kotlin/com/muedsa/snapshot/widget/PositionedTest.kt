package com.muedsa.snapshot.widget

import com.muedsa.snapshot.drawWidget
import com.muedsa.snapshot.rendering.stack.RenderStack
import com.muedsa.snapshot.rendering.stack.StackParentData
import org.jetbrains.skia.Color
import kotlin.test.Test
import kotlin.test.expect

class PositionedTest {

    @Test
    fun applyParentData_test() {
        println("\n\n\nPositionedTest.applyParentData_test()")
        val stack = Stack().apply {
            Positioned(
                left = 8f,
                top = 10f,
                right = 6f,
                bottom = 13f
            ) {
                SizedBox(width = 100f, height = 100f)
            }
            Positioned(
                left = 22f,
                top = 5f,
                right = 11f,
                bottom = 16f
            ) {
                SizedBox(width = 100f, height = 100f)
            }
        }
        val renderStack = stack.createRenderBox() as RenderStack
        val stackParentData1 = renderStack.children[0].parentData as StackParentData
        expect(8f) { stackParentData1.left }
        expect(10f) { stackParentData1.top }
        expect(6f) { stackParentData1.right }
        expect(13f) { stackParentData1.bottom }
        val stackParentData2 = renderStack.children[1].parentData as StackParentData
        expect(22f) { stackParentData2.left }
        expect(5f) { stackParentData2.top }
        expect(11f) { stackParentData2.right }
        expect(16f) { stackParentData2.bottom }
    }

    @Test
    fun left_top_test() {
        println("\n\n\nPositionedTest.applyParentData_test()")
        val name = "widget/positioned/left_top"
        val description = "Positioned(left=10,top=10)"
        println("\n\ndraw: $name\n$description")
        drawWidget(imagePathWithoutSuffix = name, debugInfo = description, drawDebug = true) {
            SizedBox(
                width = 200f,
                height = 200f
            ) {
                Stack {
                    Positioned(
                        left = 10f,
                        top = 10f,
                    ) {
                        Container(width = 100f, height = 100f, color = Color.RED)
                    }
                }
            }
        }
    }
}