package com.muedsa.snapshot.paint

import com.muedsa.geometry.Size
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FittedSizesTest {

    private fun approxEq(a: Float, b: Float, tol: Float = 1e-3f) = abs(a - b) <= tol

    private fun assertSizeApprox(actual: Size, w: Float, h: Float, tol: Float = 1e-3f) {
        assertTrue(approxEq(actual.width, w, tol), "width expected $w but was ${actual.width}")
        assertTrue(approxEq(actual.height, h, tol), "height expected $h but was ${actual.height}")
    }

    // 输入 4:3(400x300),输出 16:9(1600x900)
    private val input = Size(400f, 300f)
    private val output = Size(1600f, 900f)

    @Test
    fun zero_when_any_dimension_non_positive() {
        listOf(BoxFit.FILL, BoxFit.CONTAIN, BoxFit.COVER, BoxFit.FIT_WIDTH, BoxFit.FIT_HEIGHT, BoxFit.NONE, BoxFit.SCALE_DOWN)
            .forEach { fit ->
                assertEquals(FittedSizes.ZERO, FittedSizes.applyBoxFit(fit, Size(0f, 300f), output))   // input.width = 0
                assertEquals(FittedSizes.ZERO, FittedSizes.applyBoxFit(fit, Size(1600f, 0f), output))  // input.height = 0
                assertEquals(FittedSizes.ZERO, FittedSizes.applyBoxFit(fit, Size(-1f, 300f), output))  // 负宽
                assertEquals(FittedSizes.ZERO, FittedSizes.applyBoxFit(fit, input, Size(1600f, 0f)))   // output.height = 0
            }
    }

    @Test
    fun fill_stretches_source_to_output() {
        val r = FittedSizes.applyBoxFit(BoxFit.FILL, input, output)
        assertEquals(input, r.source)
        assertEquals(output, r.destination)
    }

    @Test
    fun contain_fits_whole_image() {
        val r = FittedSizes.applyBoxFit(BoxFit.CONTAIN, input, output)
        assertEquals(input, r.source)
        assertSizeApprox(r.destination, 1200f, 900f)
        val narrow = FittedSizes.applyBoxFit(BoxFit.CONTAIN, input, Size(400f, 400f))
        assertSizeApprox(narrow.destination, 400f, 300f)
    }

    @Test
    fun cover_fills_output_cropping_source() {
        val r = FittedSizes.applyBoxFit(BoxFit.COVER, input, output)
        assertEquals(output, r.destination)
        assertSizeApprox(r.source, 400f, 225f)
    }

    @Test
    fun cover_reverse_aspect_crops_width() {
        // 反向比例:输入 16:9(1600x900),输出 4:3(400x300)。
        // output 比例 1.33 < input 比例 1.78 → COVER else 分支:source = (input.h*output.w/output.h, input.h)
        // = (900*400/300, 900) = (1200,900):横向裁掉 1600→1200,dest 填满 400x300。
        val wideInput = Size(1600f, 900f)
        val narrowOutput = Size(400f, 300f)
        val r = FittedSizes.applyBoxFit(BoxFit.COVER, wideInput, narrowOutput)
        assertEquals(narrowOutput, r.destination)
        assertSizeApprox(r.source, 1200f, 900f)
    }

    @Test
    fun fit_width_and_fit_height_split_branches() {
        val fw = FittedSizes.applyBoxFit(BoxFit.FIT_WIDTH, input, output)
        assertEquals(output, fw.destination)
        assertSizeApprox(fw.source, 400f, 225f)

        val fh = FittedSizes.applyBoxFit(BoxFit.FIT_HEIGHT, input, output)
        assertEquals(input, fh.source)
        assertSizeApprox(fh.destination, 1200f, 900f)
    }

    @Test
    fun none_crops_to_smaller_side() {
        val r = FittedSizes.applyBoxFit(BoxFit.NONE, Size(1600f, 1200f), output)
        assertEquals(Size(1600f, 900f), r.source)
        assertEquals(Size(1600f, 900f), r.destination)
    }

    @Test
    fun scale_down_only_downscales() {
        val up = FittedSizes.applyBoxFit(BoxFit.SCALE_DOWN, input, output)
        assertEquals(input, up.destination)
        // 3200x2400(4:3) 缩到 16:9(1600x900)输出内:高度受限,结果 1200x900(而非 1600x1200,后者高方向超出输出)。
        val big = Size(3200f, 2400f)
        val down = FittedSizes.applyBoxFit(BoxFit.SCALE_DOWN, big, output)
        assertSizeApprox(down.destination, 1200f, 900f)
    }
}
