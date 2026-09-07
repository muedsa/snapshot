package com.muedsa.snapshot.widget

import com.muedsa.snapshot.assertGlobalRect
import com.muedsa.snapshot.assertSize
import com.muedsa.snapshot.findType
import com.muedsa.snapshot.rendering.LayoutNode
import com.muedsa.snapshot.rendering.box.RenderConstrainedBox
import com.muedsa.snapshot.rendering.flex.MainAxisAlignment
import com.muedsa.snapshot.rootLayout
import kotlin.test.Test

class ColumnTest {

    private fun columnRoot(
        mainAxisAlignment: MainAxisAlignment = MainAxisAlignment.START,
    ): LayoutNode = rootLayout {
        SizedBox(width = 200f, height = 200f) {
            Column(mainAxisAlignment = mainAxisAlignment) {
                SizedBox(width = 100f, height = 30f)
                SizedBox(width = 50f, height = 40f)
            }
        }
    }

    private fun first(root: LayoutNode): LayoutNode =
        checkNotNull(root.findType<RenderConstrainedBox> { it.definiteSize.width == 100f }) {
            "找不到宽 100 的 SizedBox"
        }

    private fun second(root: LayoutNode): LayoutNode =
        checkNotNull(root.findType<RenderConstrainedBox> { it.definiteSize.width == 50f }) {
            "找不到宽 50 的 SizedBox"
        }

    @Test
    fun column_start_places_children() {
        val root = columnRoot()
        root.assertSize(200f, 200f)
        first(root).assertGlobalRect(50f, 0f, 100f, 30f)
        second(root).assertGlobalRect(75f, 30f, 50f, 40f)
    }

    @Test
    fun column_center_main_axis_places_children() {
        val root = columnRoot(MainAxisAlignment.CENTER)
        first(root).assertGlobalRect(50f, 65f, 100f, 30f)
        second(root).assertGlobalRect(75f, 95f, 50f, 40f)
    }
}
