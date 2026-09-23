package com.muedsa.snapshot.widget

import com.muedsa.geometry.Size
import com.muedsa.snapshot.expectColorAt
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.box.RenderAspectRatio
import com.muedsa.snapshot.snapshotPixels
import org.jetbrains.skia.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AspectRatioTest {

    @Test
    fun resolves_size_from_available_width_or_height() {
        val cases = listOf(
            BoxConstraints(maxWidth = 120f, maxHeight = 100f) to Size(120f, 60f),
            BoxConstraints(maxHeight = 50f) to Size(100f, 50f),
            BoxConstraints(maxWidth = 200f, maxHeight = 50f) to Size(100f, 50f),
        )

        cases.forEach { (constraints, expected) ->
            val box = AspectRatio(2f).apply {
                attach(com.muedsa.snapshot.widget.SizedBox(width = 10f, height = 10f))
            }.createRenderBox()
            box.layout(constraints)

            assertEquals(expected, box.definiteSize)
            assertEquals(expected, (box as RenderAspectRatio).child!!.definiteSize)
        }
    }

    @Test
    fun tight_or_conflicting_constraints_take_precedence_over_ratio() {
        val tight = AspectRatio(2f).createRenderBox()
        tight.layout(BoxConstraints.tight(Size(90f, 30f)))
        assertEquals(Size(90f, 30f), tight.definiteSize)

        val conflicting = AspectRatio(2f).createRenderBox()
        conflicting.layout(BoxConstraints(minWidth = 150f, maxWidth = 200f, maxHeight = 60f))
        assertEquals(Size(150f, 60f), conflicting.definiteSize)
    }

    @Test
    fun paints_child_at_resolved_size() {
        val pixels = snapshotPixels {
            ConstrainedBox(BoxConstraints(maxWidth = 120f, maxHeight = 80f)) {
                AspectRatio(2f) {
                    ColoredBox(color = Color.BLUE)
                }
            }
        }

        assertEquals(120, pixels.info.width)
        assertEquals(60, pixels.info.height)
        expectColorAt(pixels, 119, 59, Color.BLUE)
    }

    @Test
    fun rejects_invalid_ratio_and_fully_unbounded_constraints() {
        listOf(0f, -1f, Float.NaN, Float.POSITIVE_INFINITY).forEach { ratio ->
            assertFailsWith<IllegalArgumentException> { AspectRatio(ratio) }
        }
        assertFailsWith<IllegalArgumentException> {
            AspectRatio(2f).createRenderBox().layout(BoxConstraints())
        }
    }
}
