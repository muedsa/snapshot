package com.muedsa.snapshot.paint.decoration

import com.muedsa.geometry.Radius
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BorderRadiusGeometryTest {

    private fun r(v: Float) = Radius.circular(v)

    @Test
    fun borderRadius_op_borderRadius_stays_borderRadius() {
        val a = BorderRadius.circular(4f)
        val b = BorderRadius.only(topLeft = r(1f), topRight = r(2f))
        val sum = a.add(b)
        assertTrue(sum is BorderRadius)
        assertEquals(r(5f), sum.topLeft)
        assertEquals(r(6f), sum.topRight)
        assertEquals(r(4f), sum.bottomLeft)
        assertEquals(r(4f), sum.bottomRight)
    }

    @Test
    fun borderRadius_subtract_borderRadius_stays_borderRadius() {
        val a = BorderRadius.circular(4f)
        val diff = a.subtract(BorderRadius.circular(1f))
        assertTrue(diff is BorderRadius)
        assertEquals(r(3f), diff.topLeft)
        assertEquals(r(3f), diff.bottomRight)
    }

    @Test
    fun borderRadius_op_mixed_returns_mixed() {
        val a = BorderRadius.circular(4f)
        // MixedBorderRadius 构造: topLeft/topRight/bottomLeft/bottomRight + start/end 方向角
        val mixed = MixedBorderRadius(
            topLeft = r(1f),
            topRight = r(2f),
            bottomLeft = r(3f),
            bottomRight = r(4f),
            topStart = r(1f),
            topEnd = r(2f),
            bottomStart = r(3f),
            bottomEnd = r(4f),
        )

        val diff = a.subtract(mixed)
        assertTrue(diff is MixedBorderRadius)
        assertEquals(r(3f), diff.topLeft)
        assertEquals(r(2f), diff.topRight)
        assertEquals(r(1f), diff.bottomLeft)
        assertEquals(Radius.ZERO, diff.bottomRight)

        val sum = a.add(mixed)
        assertTrue(sum is MixedBorderRadius)
        assertEquals(r(5f), sum.topLeft)
        assertEquals(r(6f), sum.topRight)
        assertEquals(r(7f), sum.bottomLeft)
        assertEquals(r(8f), sum.bottomRight)
    }
}
