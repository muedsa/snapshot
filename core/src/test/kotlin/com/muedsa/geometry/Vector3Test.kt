package com.muedsa.geometry

import kotlin.math.PI
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Vector3Test {

    private fun approx(a: Float, b: Float, tol: Float = 1e-4f) = abs(a - b) <= tol

    @Test
    fun length_and_length_squared_are_consistent() {
        val v = Vector3(3f, 4f, 12f)
        assertEquals(169f, v.length2, "length2 应为 3²+4²+12²")
        assertTrue(approx(v.length, 13f), "length 应为 13,实际 ${v.length}")
        assertEquals(0f, Vector3.ZERO.length, "零向量长度应为 0")
    }

    @Test
    fun dot_matches_length_squared_and_is_bilinear() {
        val v = Vector3(1f, -2f, 3f)
        assertEquals(v.length2, v.dot(v), "自点积应等于 length2")

        val w = Vector3(4f, 5f, 6f)
        assertEquals(1f * 4f + (-2f) * 5f + 3f * 6f, v.dot(w), "点积应为逐分量乘积之和")
        assertEquals(v.dot(w), w.dot(v), "点积应可交换")
        assertEquals(0f, Vector3(1f, 0f, 0f).dot(Vector3(0f, 1f, 0f)), "正交向量点积应为 0")
    }

    @Test
    fun cross_follows_right_hand_rule() {
        val x = Vector3(1f, 0f, 0f)
        val y = Vector3(0f, 1f, 0f)
        assertEquals(Vector3(0f, 0f, 1f), x.cross(y), "x × y 应为 +z")
        assertEquals(Vector3(0f, 0f, -1f), y.cross(x), "y × x 应为 -z")
        assertEquals(Vector3.ZERO, x.cross(x), "自叉积应为零向量")
    }

    @Test
    fun length_setter_scales_to_target() {
        val v = Vector3(3f, 4f, 0f) // 长度 5
        v.length = 10f
        assertTrue(approx(v.length, 10f), "设置后长度应为 10,实际 ${v.length}")
        assertTrue(
            approx(v.storage[0], 6f) && approx(v.storage[1], 8f),
            "方向应保持,实际 ${v.storage.toList()}"
        )

        // 设为 0 → 归零
        v.length = 0f
        assertEquals(Vector3.ZERO, v, "长度设为 0 应归零")

        // 零向量上设非零长度:保持为零(无方向可缩放)
        val zero = Vector3(0f, 0f, 0f)
        zero.length = 5f
        assertEquals(Vector3.ZERO, zero, "零向量设置长度后仍应为零")
    }

    @Test
    fun normalize_scales_in_place_and_returns_previous_length() {
        val v = Vector3(0f, 3f, 4f)
        val returned = v.normalize()
        assertTrue(approx(returned, 5f), "normalize() 应返回归一前的长度,实际 $returned")
        assertTrue(approx(v.length, 1f), "归一后长度应为 1,实际 ${v.length}")

        // 零向量:返回 0 且保持为零
        val zero = Vector3.ZERO.clone()
        assertEquals(0f, zero.normalize())
        assertEquals(Vector3.ZERO, zero)
    }

    @Test
    fun normalized_leaves_receiver_untouched() {
        val v = Vector3(0f, 3f, 4f)
        val normalized = v.normalized()
        assertEquals(Vector3(0f, 3f, 4f), v, "normalized() 不应改动原向量")
        assertTrue(approx(normalized.length, 1f), "结果长度应为 1,实际 ${normalized.length}")
    }

    @Test
    fun distance_matches_length_of_difference() {
        val a = Vector3(1f, 2f, 3f)
        val b = Vector3(4f, 6f, 3f)
        val diff = Vector3(
            b.storage[0] - a.storage[0],
            b.storage[1] - a.storage[1],
            b.storage[2] - a.storage[2]
        )
        assertEquals(diff.length2, a.distanceToSquared(b), "distanceToSquared 应等于差值向量长度平方")
        assertTrue(approx(a.distanceTo(b), 5f), "距离应为 5,实际 ${a.distanceTo(b)}")
        assertEquals(0f, a.distanceTo(a), "同点距离应为 0")
    }

    @Test
    fun angle_to_known_angles_and_signedness() {
        val x = Vector3(1f, 0f, 0f)
        val y = Vector3(0f, 1f, 0f)
        assertTrue(approx(x.angleTo(x), 0f), "同向量夹角应为 0")
        assertTrue(approx(x.angleTo(y), (PI / 2).toFloat()), "正交夹角应为 π/2")
        assertTrue(approx(x.angleTo(Vector3(-1f, 0f, 0f)), PI.toFloat()), "反向夹角应为 π")

        // 有符号:以 +Z 为法线,x→y 为正、y→x 为负
        val z = Vector3(0f, 0f, 1f)
        assertTrue(x.angleToSigned(y, z) > 0f, "x→y 绕 +Z 应为正")
        assertTrue(y.angleToSigned(x, z) < 0f, "y→x 绕 +Z 应为负")
        assertTrue(approx(abs(x.angleToSigned(y, z)), (PI / 2).toFloat()), "有符号夹角绝对值应与无符号一致")
    }

    @Test
    fun clone_is_independent_and_equality_is_structural() {
        val v = Vector3(1f, 2f, 3f)
        val c = v.clone()
        assertEquals(v, c, "克隆应与原向量相等")
        c.storage[0] = 99f
        assertEquals(Vector3(1f, 2f, 3f), v, "改动克隆不应影响原向量")
        assertTrue(v != c, "改动后应不再相等")
    }
}
