package com.muedsa.snapshot.widget

import com.muedsa.snapshot.assertGlobalRect
import com.muedsa.snapshot.assertSize
import com.muedsa.snapshot.rendering.flex.FlexFit
import com.muedsa.snapshot.rendering.flex.FlexParentData
import com.muedsa.snapshot.rendering.flex.CrossAxisAlignment
import com.muedsa.snapshot.rendering.flex.RenderFlex
import com.muedsa.snapshot.rootLayout
import org.jetbrains.skia.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class SpacerTest {

    @Test
    fun row_spacers_share_remaining_width_by_flex() {
        val root = rootLayout {
            SizedBox(width = 300f, height = 40f) {
                Row(crossAxisAlignment = CrossAxisAlignment.STRETCH) {
                    Container(width = 40f, color = Color.RED)
                    Spacer()
                    Spacer(flex = 2)
                    Container(width = 20f, color = Color.BLUE)
                }
            }
        }

        val row = root.children.single()
        row.assertSize(300f, 40f)
        row.children[0].assertGlobalRect(0f, 0f, 40f, 40f)
        row.children[1].assertGlobalRect(40f, 0f, 80f, 40f)
        row.children[2].assertGlobalRect(120f, 0f, 160f, 40f)
        row.children[3].assertGlobalRect(280f, 0f, 20f, 40f)

        val renderRow = assertIs<RenderFlex>(row.renderBox)
        val spacerData = assertIs<FlexParentData>(renderRow.children[2].parentData)
        assertEquals(2, spacerData.flex)
        assertEquals(FlexFit.TIGHT, spacerData.fit)
    }

    @Test
    fun column_spacers_share_remaining_height_by_flex() {
        val root = rootLayout {
            SizedBox(width = 80f, height = 300f) {
                Column(crossAxisAlignment = CrossAxisAlignment.STRETCH) {
                    Container(height = 30f, color = Color.RED)
                    Spacer(flex = 2)
                    Container(height = 30f, color = Color.BLUE)
                    Spacer()
                }
            }
        }

        val column = root.children.single()
        column.assertSize(80f, 300f)
        column.children[1].assertGlobalRect(0f, 30f, 80f, 160f)
        column.children[3].assertGlobalRect(0f, 220f, 80f, 80f)
    }

    @Test
    fun rejects_non_positive_flex_and_children() {
        assertFailsWith<IllegalArgumentException> { Spacer(flex = 0) }
        assertFailsWith<IllegalArgumentException> { Spacer(flex = -1) }
        assertFailsWith<IllegalStateException> {
            Spacer().attach(SizedBox.shrink())
        }
    }
}
