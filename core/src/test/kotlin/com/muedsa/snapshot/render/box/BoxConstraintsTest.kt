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
    fun constrain_size_preserves_aspect_ratio_when_scaling_down() {
        // 4:1(200x50)夹入 (0..100)x(0..100):先夹宽 200→100,高按比例 = 100/4 = 25
        // (实现与 Flutter 同序:先夹宽再夹高)。
        val c = BoxConstraints(minWidth = 0f, maxWidth = 100f, minHeight = 0f, maxHeight = 100f)
        expect(Size(100f, 25f)) {
            c.constrainSizeAndAttemptToPreserveAspectRatio(Size(200f, 50f))
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

    @Test
    fun flipped_swaps_width_and_height() {
        val c = BoxConstraints(minWidth = 1f, maxWidth = 10f, minHeight = 2f, maxHeight = 20f)
        val f = c.flipped()
        expect(2f) { f.minWidth }
        expect(20f) { f.maxWidth }
        expect(1f) { f.minHeight }
        expect(10f) { f.maxHeight }
    }

    @Test
    fun constructed_constraints_are_always_normalized() {
        // 构造期 assert 锁死了与 isNormalized **完全相同**的谓词
        // (BoxConstraints.kt:`minWidth in 0f..maxWidth` 与 `minHeight in 0f..maxHeight`),
        // 因此在开启断言的环境(测试即如此)下,任何构造出来的约束都必然规范——
        // isNormalized 的 false 分支不可达,它是给**关闭断言**的生产环境兜底的。
        // 故此处只覆盖"各种边界构造都规范"这一契约。
        val samples = listOf(
            BoxConstraints(),
            BoxConstraints.tight(Size(0f, 0f)),
            BoxConstraints.tight(Size(10f, 20f)),
            BoxConstraints(minWidth = 5f, maxWidth = 5f, minHeight = 0f, maxHeight = 0f),
            BoxConstraints.loose(Size(30f, 40f)),
            BoxConstraints.tightForFinite(width = 7f),
        )
        samples.forEach { expect(true) { it.isNormalized } }
    }

    @Test
    fun tight_flags_mark_axes_whose_min_reaches_max() {
        val normal = BoxConstraints(minWidth = 1f, maxWidth = 10f, minHeight = 2f, maxHeight = 20f)
        expect(false) { normal.hasTightWidth }
        expect(false) { normal.hasTightHeight }
        expect(false) { normal.isTight }

        // 仅宽轴 min == max
        val tightWidth = BoxConstraints(minWidth = 5f, maxWidth = 5f, minHeight = 2f, maxHeight = 20f)
        expect(true) { tightWidth.hasTightWidth }
        expect(false) { tightWidth.hasTightHeight }
        expect(false) { tightWidth.isTight }

        // 两轴均 min == max
        val both = BoxConstraints.tight(Size(9f, 9f))
        expect(true) { both.hasTightWidth }
        expect(true) { both.hasTightHeight }
        expect(true) { both.isTight }

        // 注:实现写作 `minWidth >= maxWidth`;因构造期已保证 min <= max,
        // 等价的 `==` 与 `>=` 不可区分,故 min > max 的情形无法(也无需)覆盖。
    }

    @Test
    fun tight_for_finite_treats_infinite_axes_as_zero_min() {
        val allInfinite = BoxConstraints.tightForFinite()
        expect(0f) { allInfinite.minWidth }
        expect(0f) { allInfinite.minHeight }
        expect(Float.POSITIVE_INFINITY) { allInfinite.maxWidth }
        expect(Float.POSITIVE_INFINITY) { allInfinite.maxHeight }
        expect(true) { allInfinite.isNormalized }

        val both = BoxConstraints.tightForFinite(width = 10f, height = 20f)
        expect(true) { both.isTight }
        expect(Size(10f, 20f)) { both.smallest }

        // 只给一轴:该轴 tight,另一轴仍为 0..+∞
        val onlyWidth = BoxConstraints.tightForFinite(width = 10f)
        expect(true) { onlyWidth.hasTightWidth }
        expect(false) { onlyWidth.hasTightHeight }
        expect(0f) { onlyWidth.minHeight }
        expect(Float.POSITIVE_INFINITY) { onlyWidth.maxHeight }
    }

    @Test
    fun constrain_dimensions_matches_per_axis_constrain() {
        val c = BoxConstraints(minWidth = 1f, maxWidth = 10f, minHeight = 2f, maxHeight = 20f)
        val dimensions = c.constrainDimensions(30f, 5f)
        expect(c.constrainWidth(30f)) { dimensions.width }
        expect(c.constrainHeight(5f)) { dimensions.height }
        // 下界同样生效
        expect(Size(1f, 20f)) { c.constrainDimensions(0f, 99f) }
    }

    @Test
    fun enforce_clamps_bounds_into_other() {
        // 收紧:自身 max 100 被 other 的 tight(50) 夹到 50
        val loose = BoxConstraints(maxWidth = 100f, maxHeight = 100f)
        val tightened = loose.enforce(BoxConstraints.tight(Size(50f, 50f)))
        expect(Size(50f, 50f)) { tightened.smallest }
        expect(Size(50f, 50f)) { tightened.biggest }

        // 放宽:自身 tight(50) 的 min/max **都**被抬到 other 的下界 80(而非保留 max=200)
        val widened = BoxConstraints.tight(Size(50f, 50f)).enforce(
            BoxConstraints(minWidth = 80f, maxWidth = 200f, minHeight = 0f, maxHeight = 200f)
        )
        expect(80f) { widened.minWidth }
        expect(80f) { widened.maxWidth }
        expect(50f) { widened.minHeight }

        // 规范性:被规范约束夹取后仍规范
        val other = BoxConstraints(minWidth = 5f, maxWidth = 5f, minHeight = 1f, maxHeight = 9f)
        expect(true) { loose.enforce(other).isNormalized }

        // 幂等:夹入 other 范围后再夹一次不再变化
        val once = loose.enforce(other)
        expect(once) { once.enforce(other) }
    }
}
