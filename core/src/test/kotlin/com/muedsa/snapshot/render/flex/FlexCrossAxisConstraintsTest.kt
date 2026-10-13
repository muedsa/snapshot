package com.muedsa.snapshot.render.flex

import com.muedsa.geometry.Size
import com.muedsa.snapshot.findType
import com.muedsa.snapshot.paint.Axis
import com.muedsa.snapshot.paint.text.TextStyle
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.box.RenderConstrainedBox
import com.muedsa.snapshot.rendering.flex.CrossAxisAlignment
import com.muedsa.snapshot.rendering.flex.FlexFit
import com.muedsa.snapshot.rendering.flex.FlexParentData
import com.muedsa.snapshot.rendering.flex.RenderFlex
import com.muedsa.snapshot.rendering.text.RenderParagraph
import com.muedsa.snapshot.rootLayout
import com.muedsa.snapshot.testFontFamily
import com.muedsa.snapshot.widget.Column
import com.muedsa.snapshot.widget.Flexible
import com.muedsa.snapshot.widget.SizedBox
import com.muedsa.snapshot.widget.text.Text
import org.jetbrains.skia.paragraph.BaselineMode
import org.jetbrains.skia.paragraph.Direction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FlexCrossAxisConstraintsTest {
    private fun layoutChild(
        desiredSize: Size,
        alignment: CrossAxisAlignment,
        fit: FlexFit,
        direction: Axis = Axis.VERTICAL,
        textDirection: Direction = Direction.LTR,
        constraints: BoxConstraints = BoxConstraints.tight(Size(100f, 100f)),
    ): RenderConstrainedBox {
        val parent = RenderFlex(direction = direction, crossAxisAlignment = alignment,
            textDirection = textDirection, textBaseline = BaselineMode.ALPHABETIC)
        val child = RenderConstrainedBox(BoxConstraints.tight(desiredSize))
        parent.appendChild(child)
        (child.parentData as FlexParentData).apply { flex = 1; this.fit = fit }
        parent.layout(constraints)
        return child
    }

    @Test
    fun column_flex_children_keep_max_width_for_all_alignments_and_fits() {
        for (alignment in CrossAxisAlignment.entries) {
            for (fit in FlexFit.entries) {
                val child = layoutChild(Size(200f, 20f), alignment, fit)
                assertEquals(100f, child.definiteConstraints.maxWidth, "$alignment / $fit")
                assertEquals(if (alignment == CrossAxisAlignment.STRETCH) 100f else 0f,
                    child.definiteConstraints.minWidth, "$alignment / $fit")
                assertEquals(Size(100f, if (fit == FlexFit.TIGHT) 100f else 20f), child.definiteSize)
            }
        }
    }

    @Test
    fun narrow_children_keep_alignment_without_being_stretched() {
        for (direction in Direction.entries) {
            for (alignment in listOf(CrossAxisAlignment.START, CrossAxisAlignment.CENTER, CrossAxisAlignment.END)) {
                for (fit in FlexFit.entries) {
                    val child = layoutChild(Size(40f, 20f), alignment, fit, textDirection = direction)
                    val expectedX = when (alignment) {
                        CrossAxisAlignment.CENTER -> 30f
                        CrossAxisAlignment.START -> if (direction == Direction.LTR) 0f else 60f
                        else -> if (direction == Direction.LTR) 60f else 0f
                    }
                    assertEquals(40f, child.definiteSize.width)
                    assertEquals(expectedX, child.parentData!!.offset.x)
                }
            }
        }
    }

    @Test
    fun stretch_still_fills_the_cross_axis() {
        for (fit in FlexFit.entries) {
            assertEquals(100f, layoutChild(Size(40f, 20f), CrossAxisAlignment.STRETCH, fit).definiteSize.width)
        }
    }

    @Test
    fun row_keeps_its_existing_cross_axis_constraint() {
        for (fit in FlexFit.entries) {
            val child = layoutChild(Size(20f, 200f), CrossAxisAlignment.CENTER, fit, direction = Axis.HORIZONTAL)
            assertEquals(100f, child.definiteConstraints.maxHeight)
            assertEquals(Size(if (fit == FlexFit.TIGHT) 100f else 20f, 100f), child.definiteSize)
        }
    }

    @Test
    fun unbounded_cross_axis_is_not_artificially_limited() {
        val child = layoutChild(Size(200f, 20f), CrossAxisAlignment.CENTER, FlexFit.LOOSE,
            constraints = BoxConstraints(minHeight = 100f, maxHeight = 100f))
        assertEquals(Float.POSITIVE_INFINITY, child.definiteConstraints.maxWidth)
        assertEquals(200f, child.definiteSize.width)
    }

    @Test
    fun flexible_text_wraps_to_column_width_with_bundled_test_font() {
        fun paragraphSize(width: Float): Size = rootLayout {
            SizedBox(width = width, height = 500f) {
                Column {
                    Flexible {
                        Text("Column 中的弹性文本应按父宽度换行，而不是得到无限宽度。".repeat(3),
                            style = TextStyle(fontFamilies = listOf(testFontFamily), fontSize = 16f))
                    }
                }
            }
        }.findType<RenderParagraph>()!!.size
        val narrow = paragraphSize(100f)
        val wide = paragraphSize(400f)
        assertTrue(narrow.width <= 100f)
        assertTrue(narrow.height > wide.height, "窄 Column 应产生更多文本行")
    }
}
