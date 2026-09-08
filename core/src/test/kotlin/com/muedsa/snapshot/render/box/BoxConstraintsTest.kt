package com.muedsa.snapshot.render.box

import com.muedsa.geometry.EdgeInsets
import com.muedsa.geometry.Size
import com.muedsa.snapshot.rendering.box.BoxConstraints
import kotlin.test.Test
import kotlin.test.expect

class BoxConstraintsTest {

    @Test
    fun copyWithTest() {
        val source = BoxConstraints(
            minWidth = 5f,
            maxWidth = 13f,
            minHeight = 7f,
            maxHeight = 19f
        )

        expect(source) {
            source.copyWith()
        }

        expect(
            BoxConstraints(
                minWidth = 3f,
                maxWidth = 13f,
                minHeight = 7f,
                maxHeight = 19f
            )
        ) {
            source.copyWith(minWidth = 3f)
        }

        expect(
            expected = BoxConstraints(
                minWidth = 5f,
                maxWidth = 11f,
                minHeight = 7f,
                maxHeight = 19f
            )
        ) {
            source.copyWith(maxWidth = 11f)
        }

        expect(
            expected = BoxConstraints(
                minWidth = 5f,
                maxWidth = 13f,
                minHeight = 3f,
                maxHeight = 19f
            )
        ) {
            source.copyWith(minHeight = 3f)
        }

        expect(
            expected = BoxConstraints(
                minWidth = 5f,
                maxWidth = 13f,
                minHeight = 7f,
                maxHeight = 20f
            )
        ) {
            source.copyWith(maxHeight = 20f)
        }
    }

    @Test
    fun constructors_and_derived_flags() {
        val tight = BoxConstraints.tight(Size(10f, 20f))
        expect(10f) { tight.minWidth }
        expect(20f) { tight.maxHeight }
        expect(true) { tight.isTight }

        val tightFor = BoxConstraints.tightFor(width = 5f, height = 7f)
        expect(5f) { tightFor.minWidth }
        expect(7f) { tightFor.minHeight }
        expect(true) { tightFor.isTight }

        val loose = BoxConstraints.loose(Size(30f, 40f))
        expect(0f) { loose.minWidth }
        expect(30f) { loose.maxWidth }
        expect(false) { loose.hasInfiniteWidth }
        expect(true) { loose.hasBoundedWidth }

        val expand = BoxConstraints.expand(width = 12f)
        expect(12f) { expand.minWidth }
        expect(12f) { expand.maxWidth }
        expect(Float.POSITIVE_INFINITY) { expand.minHeight }

        // 默认约束 max 为 +∞、min 为 0;hasInfiniteHeight 语义为 minHeight>=+∞,故为 false。
        val infinite = BoxConstraints()
        expect(false) { infinite.hasInfiniteHeight }
        expect(Float.POSITIVE_INFINITY) { infinite.biggest.height }
        expect(0f) { infinite.smallest.width }
    }

    @Test
    fun transforms_and_constrain() {
        val c = BoxConstraints(minWidth = 1f, maxWidth = 10f, minHeight = 2f, maxHeight = 20f)
        val loosened = c.loosen()
        expect(0f) { loosened.minWidth }
        expect(10f) { loosened.maxWidth }

        val tightened = c.tighten(width = 5f, height = 30f)
        expect(5f) { tightened.minWidth }
        expect(5f) { tightened.maxWidth }
        expect(20f) { tightened.maxHeight }

        expect(Size(3f, 4f)) { c.constrain(Size(3f, 4f)) }
        expect(Size(10f, 4f)) { c.constrain(Size(30f, 4f)) } // 宽 30 被夹到 maxWidth=10
        expect(10f) { c.constrainWidth(999f) }
        expect(2f) { c.constrainHeight(0f) }

        val deflated = c.deflate(EdgeInsets.all(2f)) // horizontal = left+right = 4
        expect(6f) { deflated.maxWidth }   // max(0, 10 - 4)
        expect(0f) { deflated.minWidth }   // max(0, 1 - 4)
        expect(16f) { deflated.maxHeight } // max(0, 20 - 4)

        expect(Size(5f, 5f)) {
            BoxConstraints(minWidth = 0f, maxWidth = 100f, minHeight = 0f, maxHeight = 100f)
                .constrainSizeAndAttemptToPreserveAspectRatio(Size(5f, 5f))
        }
    }

    @Test
    fun width_and_height_only_views() {
        val c = BoxConstraints(minWidth = 1f, maxWidth = 10f, minHeight = 2f, maxHeight = 20f)
        val w = c.widthConstraints()
        expect(1f) { w.minWidth }
        expect(10f) { w.maxWidth }
        expect(0f) { w.minHeight }
        expect(Float.POSITIVE_INFINITY) { w.maxHeight }
        val h = c.heightConstraints()
        expect(0f) { h.minWidth }
        expect(Float.POSITIVE_INFINITY) { h.maxWidth }
        expect(2f) { h.minHeight }
        expect(20f) { h.maxHeight }
    }
}
