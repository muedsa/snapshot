package com.muedsa.geometry

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MatrixUtilTest {

    private fun approx(a: Float, b: Float, tol: Float = 1e-4f) = abs(a - b) <= tol

    private fun assertMatrixEquals(expected: Matrix44CMO, actual: Matrix44CMO, message: String = "") {
        for (i in Matrix44CMO.MATRIX44_RANGE) {
            assertTrue(
                approx(expected.mat[i], actual.mat[i]),
                "$message 第 $i 个元素期望 ${expected.mat[i]},实际 ${actual.mat[i]}"
            )
        }
    }

    @Test
    fun create_z_rotation_writes_expected_layout() {
        val theta = (PI / 6).toFloat()
        val m = createZRotation(sin(theta), cos(theta))
        // 列主序:第 1 列 (cos, sin)、第 2 列 (-sin, cos),z/w 轴不变
        assertTrue(approx(m.mat[0], cos(theta)), "m[0] 应为 cos")
        assertTrue(approx(m.mat[1], sin(theta)), "m[1] 应为 sin")
        assertTrue(approx(m.mat[4], -sin(theta)), "m[4] 应为 -sin")
        assertTrue(approx(m.mat[5], cos(theta)), "m[5] 应为 cos")
        assertEquals(1f, m.mat[10])
        assertEquals(1f, m.mat[15])
        // 其余位置必须为 0(不能残留单位阵的 1)
        listOf(2, 3, 6, 7, 8, 9, 11, 12, 13, 14).forEach {
            assertEquals(0f, m.mat[it], "m[$it] 应为 0")
        }
        assertEquals(1f, m.determinant(), "Z 旋转的行列式应为 1")
    }

    @Test
    fun compute_rotation_special_angles_use_exact_branches() {
        assertMatrixEquals(Matrix44CMO.identity(), computeRotation(0f), "0 弧度应为单位阵")

        val quarter = (PI / 2).toFloat()
        assertMatrixEquals(
            createZRotation(1f, 0f),
            computeRotation(quarter),
            "π/2 应命中 sin == 1 的精确分支"
        )
        assertMatrixEquals(
            createZRotation(-1f, 0f),
            computeRotation(-quarter),
            "-π/2 应命中 sin == -1 的精确分支"
        )
    }

    @Test
    fun compute_rotation_matches_analytic_form_for_generic_angle() {
        val theta = 0.7f
        assertMatrixEquals(
            createZRotation(sin(theta), cos(theta)),
            computeRotation(theta),
            "一般角度应与 (sin, cos) 构造一致"
        )
        assertEquals(1f, computeRotation(theta).determinant(), "旋转行列式应为 1")
    }

    @Test
    fun get_as_translation_accepts_pure_translation_only() {
        val translated = Matrix44CMO.identity().apply { translate(3f, -4f, 0f) }
        assertEquals(Offset(3f, -4f), getAsTranslation(translated), "纯平移应取出 x/y 偏移")

        // 带旋转 → 不是纯平移
        assertNull(getAsTranslation(Matrix44CMO.identity().apply { rotateZ(0.3f) }), "旋转矩阵不应判为纯平移")

        // 带 z 平移(mat[14] != 0)→ 不是 2D 纯平移
        assertNull(getAsTranslation(Matrix44CMO.identity().apply { translate(1f, 2f, 3f) }), "含 z 平移不应判为纯平移")

        // 含缩放 → 不是纯平移
        assertNull(getAsTranslation(Matrix44CMO.identity().apply { scale(2f, 2f, 1f) }), "缩放矩阵不应判为纯平移")

        // 全零矩阵 → 不是纯平移
        assertNull(getAsTranslation(Matrix44CMO(*FloatArray(16))), "零矩阵不应判为纯平移")
    }
}
