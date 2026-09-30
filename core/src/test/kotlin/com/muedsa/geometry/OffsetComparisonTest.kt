package com.muedsa.geometry

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OffsetComparisonTest {

    @Test
    fun comparisons_require_both_components_to_match() {
        val small = Offset(1f, 2f)
        val large = Offset(3f, 4f)
        val crossed = Offset(3f, 1f)

        assertTrue(small lessThan large)
        assertTrue(small lessThanOrEqual large)
        assertTrue(small lessThanOrEqual Offset(1f, 3f))
        assertTrue(large greaterThan small)
        assertTrue(large greaterThanOrEqual small)
        assertTrue(large greaterThanOrEqual Offset(3f, 2f))
        assertFalse(small greaterThan crossed)
        assertFalse(small lessThan crossed)
        assertFalse(small greaterThanOrEqual crossed)
        assertFalse(small lessThanOrEqual crossed)
    }

    @Test
    fun equal_components_only_satisfy_inclusive_comparisons() {
        val size = Size(0f, 10f)
        val same = Size(0f, 10f)

        assertFalse(size lessThan same)
        assertFalse(size greaterThan same)
        assertTrue(size lessThanOrEqual same)
        assertTrue(size greaterThanOrEqual same)
        assertFalse(size greaterThan Size.ZERO)
        assertFalse(size lessThan Size.ZERO)
    }

    @Test
    fun nan_does_not_satisfy_any_comparison() {
        val invalid = Offset(Float.NaN, 1f)

        assertFalse(invalid lessThan Offset.ZERO)
        assertFalse(invalid lessThanOrEqual Offset.ZERO)
        assertFalse(invalid greaterThan Offset.ZERO)
        assertFalse(invalid greaterThanOrEqual Offset.ZERO)
    }

    @Test
    fun offset_and_size_are_not_comparable() {
        assertFalse((Offset.ZERO as Any) is Comparable<*>)
        assertFalse((Size.ZERO as Any) is Comparable<*>)
    }
}
