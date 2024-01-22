package com.muedsa.snapshot.widget

import com.muedsa.snapshot.paint.Axis
import com.muedsa.snapshot.rendering.flex.FlexParentData
import com.muedsa.snapshot.rendering.flex.RenderFlex
import kotlin.test.Test
import kotlin.test.expect

class FlexibleTest {

    @Test
    fun applyParentData_test() {
        println("\n\n\nFlexibleTest.applyParentData_test()")
        val flexArr = intArrayOf(1, 2, 3)
        val flex = Flex(direction = Axis.HORIZONTAL).apply {
            flexArr.forEach {
                Flexible(
                    flex = it
                ) {
                    Container()
                }
            }
        }
        val renderFlex = flex.createRenderBox() as RenderFlex
        renderFlex.children!!.forEachIndexed { index, renderBox ->
            val flexParentData: FlexParentData = renderBox.parentData as FlexParentData
            expect(flexArr[index]) { flexParentData.flex }
        }
    }
}