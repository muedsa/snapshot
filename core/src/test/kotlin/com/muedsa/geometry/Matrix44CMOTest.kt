package com.muedsa.geometry

import kotlin.math.PI
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * `Matrix44CMO`(列主序 4x4)的代数性质断言。
 *
 * 期望值取自**独立的教科书定义**(如列主序相乘的求和公式),不复刻被测实现的表达式。
 * 未覆盖:`transform(...)` 是 `TODO()` 未实现桩;`postmultiply` 无调用方且语义存疑(另记待议)。
 */
class Matrix44CMOTest {

    private fun approx(a: Float, b: Float, tol: Float = 1e-4f) = abs(a - b) <= tol

    private fun assertMatrixEquals(expected: Matrix44CMO, actual: Matrix44CMO, message: String = "") {
        for (i in Matrix44CMO.MATRIX44_RANGE) {
            assertTrue(
                approx(expected.mat[i], actual.mat[i]),
                "$message 第 $i 个元素期望 ${expected.mat[i]},实际 ${actual.mat[i]};实际矩阵=${actual.mat.toList()}"
            )
        }
    }

    /** 教科书定义的列主序 4x4 相乘(结果 = a·b),用于独立核对 multiply。 */
    private fun textbookMultiply(a: Matrix44CMO, b: Matrix44CMO): Matrix44CMO {
        val out = FloatArray(16)
        for (col in 0 until 4) {
            for (row in 0 until 4) {
                var sum = 0f
                for (k in 0 until 4) {
                    sum += a.mat[k * 4 + row] * b.mat[col * 4 + k]
                }
                out[col * 4 + row] = sum
            }
        }
        return Matrix44CMO(*out)
    }

    private val identityArray = listOf(
        1f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f,
        0f, 0f, 1f, 0f,
        0f, 0f, 0f, 1f
    )

    @Test
    fun identity_factory_matches_identity_matrix() {
        assertEquals(identityArray, Matrix44CMO.identity().mat.toList())
        // 工厂返回副本:改动不影响后续调用
        val first = Matrix44CMO.identity()
        first.mat[0] = 42f
        assertEquals(identityArray, Matrix44CMO.identity().mat.toList(), "identity() 应每次返回独立副本")
    }

    @Test
    fun zero_factory_returns_all_zero_matrix() {
        val zero = Matrix44CMO.zero()
        assertEquals(List(16) { 0f }, zero.mat.toList(), "zero() 应返回全零矩阵")
        // 与就地 setZero 的结果一致
        val inPlace = Matrix44CMO.identity().apply { setZero() }
        assertEquals(inPlace.mat.toList(), zero.mat.toList(), "zero() 应与 setZero() 结果一致")
    }

    @Test
    fun set_identity_and_set_zero_are_in_place() {
        val m = Matrix44CMO(*FloatArray(16))
        m.setIdentity()
        assertEquals(identityArray, m.mat.toList(), "setIdentity 应写入单位阵")
        m.setZero()
        assertEquals(List(16) { 0f }, m.mat.toList(), "setZero 应写入全零")
    }

    @Test
    fun clone_is_independent() {
        val m = Matrix44CMO.identity()
        val c = m.clone()
        c.mat[12] = 7f
        assertEquals(0f, m.mat[12], "改动克隆不应影响原矩阵")
        assertEquals(identityArray, m.mat.toList())
    }

    @Test
    fun translate_writes_translation_into_last_column() {
        val m = Matrix44CMO.identity().apply { translate(1f, 2f, 3f) }
        assertEquals(Vector3(1f, 2f, 3f), m.getTranslation(), "平移量应写入第 4 列")
        assertEquals(1f, m.mat[15], "齐次分量应保持 1")
        // 齐次坐标可为 0 以外时按矩阵乘法累计
        val again = m.clone().apply { translate(1f, 1f, 1f) }
        assertEquals(Vector3(2f, 3f, 4f), again.getTranslation(), "再次平移应累加")
    }

    @Test
    fun set_translation_and_raw() {
        val m = Matrix44CMO.identity()
        m.setTranslation(Vector3(4f, 5f, 6f))
        assertEquals(Vector3(4f, 5f, 6f), m.getTranslation())
        m.setTranslationRaw(-1f, -2f, -3f)
        assertEquals(Vector3(-1f, -2f, -3f), m.getTranslation())
    }

    @Test
    fun transpose_swaps_off_diagonal_entries_and_round_trips() {
        // 用 m[i] = i 便于逐位核对:转置交换 (1,4)(2,8)(3,12)(6,9)(7,13)(11,14)
        val m = Matrix44CMO(*(0 until 16).map { it.toFloat() }.toFloatArray())
        val transposed = m.transposed()
        val expected = floatArrayOf(
            0f, 4f, 8f, 12f,
            1f, 5f, 9f, 13f,
            2f, 6f, 10f, 14f,
            3f, 7f, 11f, 15f
        )
        assertEquals(expected.toList(), transposed.mat.toList(), "转置应交换行列")
        assertEquals(m.mat.toList(), transposed.transposed().mat.toList(), "转置两次应还原")

        // 就地版本与返回副本一致
        val inPlace = m.clone().apply { transpose() }
        assertEquals(transposed.mat.toList(), inPlace.mat.toList())
    }

    @Test
    fun scale_and_scaled_and_trace() {
        val scaled = Matrix44CMO.identity().scaled(2f, 3f, 4f)
        assertEquals(2f, scaled.mat[0])
        assertEquals(3f, scaled.mat[5])
        assertEquals(4f, scaled.mat[10])
        assertEquals(1f, scaled.mat[15], "sw 默认 1")
        assertEquals(2f + 3f + 4f + 1f, scaled.trace(), "迹应为对角线之和")

        // scaled 不改动原矩阵
        val base = Matrix44CMO.identity()
        base.scaled(5f, 5f, 5f)
        assertEquals(identityArray, base.mat.toList(), "scaled() 不应改动接收者")

        // 就地 scale:sw 默认 1,故第 4 列(含齐次分量 mat[15])不参与缩放
        val inPlace = Matrix44CMO.identity().apply { scale(2f, 2f, 2f) }
        assertEquals(
            floatArrayOf(2f, 0f, 0f, 0f, 0f, 2f, 0f, 0f, 0f, 0f, 2f, 0f, 0f, 0f, 0f, 1f).toList(),
            inPlace.mat.toList(),
            "scale 应逐列缩放,第 4 列按 sw=1 保持不变"
        )
    }

    @Test
    fun determinant_known_cases() {
        assertEquals(1f, Matrix44CMO.identity().determinant(), "单位阵行列式为 1")
        assertEquals(0f, Matrix44CMO(*FloatArray(16)).determinant(), "零阵行列式为 0")
        assertEquals(8f, Matrix44CMO.identity().scaled(2f, 2f, 2f).determinant(), "各轴 ×2 → 行列式 2³=8")
        assertEquals(1f, Matrix44CMO.identity().apply { rotateZ((PI / 3).toFloat()) }.determinant(), "旋转行列式为 1")
        // 奇异矩阵(两行相同)行列式为 0
        val singular = Matrix44CMO(
            1f, 2f, 3f, 4f,
            1f, 2f, 3f, 4f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f
        )
        assertEquals(0f, singular.determinant(), "奇异矩阵行列式应为 0")
    }

    @Test
    fun rotate_z_matches_create_z_rotation() {
        // rotateZ 与 MatrixUtil.createZRotation 是两处独立实现,布局必须一致
        val angle = (PI / 5).toFloat()
        val viaRotate = Matrix44CMO.identity().apply { rotateZ(angle) }
        val viaFactory = createZRotation(kotlin.math.sin(angle), kotlin.math.cos(angle))
        assertMatrixEquals(viaFactory, viaRotate, "rotateZ 应与 createZRotation 一致")
    }

    @Test
    fun rotations_preserve_orthonormality() {
        // 旋转矩阵的左上 3x3 应为正交阵:行列式为 1、列向量两两正交且长度为 1
        val rotated = Matrix44CMO.identity().apply {
            rotateX(0.4f)
            rotateY(-0.7f)
            rotateZ(1.1f)
        }
        assertEquals(1f, rotated.determinant(), "旋转矩阵行列式应为 1")

        val columns = (0 until 3).map { col ->
            Vector3(rotated.mat[col * 4], rotated.mat[col * 4 + 1], rotated.mat[col * 4 + 2])
        }
        columns.forEachIndexed { index, column ->
            assertTrue(approx(column.length, 1f), "第 $index 列长度应为 1,实际 ${column.length}")
        }
        for (i in 0 until 3) {
            for (j in i + 1 until 3) {
                assertTrue(
                    approx(columns[i].dot(columns[j]), 0f),
                    "第 $i、$j 列应正交,实际点积 ${columns[i].dot(columns[j])}"
                )
            }
        }
    }

    @Test
    fun multiply_matches_textbook_and_identity_behaviour() {
        val a = Matrix44CMO(
            1f, 2f, 3f, 4f,
            5f, 6f, 7f, 8f,
            9f, 10f, 11f, 12f,
            13f, 14f, 15f, 16f
        )
        val b = Matrix44CMO.identity().apply {
            translate(2f, 3f, 4f)
            rotateZ(0.5f)
        }
        assertMatrixEquals(textbookMultiply(a, b), a.multiplied(b), "multiplied 应等于教科书列主序乘积")

        // 单位阵是乘法的单位元
        assertMatrixEquals(a, a.multiplied(Matrix44CMO.identity()), "M·I 应为 M")
        assertMatrixEquals(a, Matrix44CMO.identity().multiplied(a), "I·M 应为 M")

        // 结合律:(A·B)·C 与 A·(B·C)
        val c = Matrix44CMO.identity().apply { scale(2f, 2f, 2f) }
        assertMatrixEquals(
            a.multiplied(b.multiplied(c)),
            a.multiplied(b).multiplied(c),
            "乘法应满足结合律"
        )

        // 就地版本与 multiplied 一致
        assertMatrixEquals(a.multiplied(b), a.clone().apply { multiply(b) })
    }

    @Test
    fun add_sub_negate_absolute_are_elementwise() {
        val a = Matrix44CMO(*(0 until 16).map { it.toFloat() }.toFloatArray())
        val b = Matrix44CMO(*(0 until 16).map { (it * 2).toFloat() }.toFloatArray())

        assertEquals((0 until 16).map { (it * 3).toFloat() }, a.clone().apply { add(b) }.mat.toList())
        assertEquals((0 until 16).map { it.toFloat() }, b.clone().apply { sub(a) }.mat.toList())
        // negate 会把 0 变成 -0.0(数学上等于 0),故按数值近似比较而非 equals
        val negated = a.clone().apply { negate() }
        a.mat.forEachIndexed { index, value ->
            assertTrue(approx(negated.mat[index], -value), "negate 第 $index 位应为 ${-value},实际 ${negated.mat[index]}")
        }

        val mixed = Matrix44CMO(1f, -2f, 3f, -4f, 5f, -6f, 7f, -8f, 9f, -10f, 11f, -12f, 13f, -14f, 15f, -16f)
        assertEquals((1..16).map { it.toFloat() }, mixed.absolute().mat.toList(), "absolute 应逐元素取绝对值")
    }

    @Test
    fun skew_factories_write_off_diagonal_tangents() {
        val skewX = Matrix44CMO.skewX(0.5f)
        assertEquals(kotlin.math.tan(0.5f), skewX.mat[4], "skewX 应写入 mat[4]")
        assertEquals(identityArray.toMutableList().also { it[4] = kotlin.math.tan(0.5f) }, skewX.mat.toList())

        val skewY = Matrix44CMO.skewY(0.25f)
        assertEquals(kotlin.math.tan(0.25f), skewY.mat[1], "skewY 应写入 mat[1]")

        val skew = Matrix44CMO.skew(0.5f, 0.25f)
        assertEquals(kotlin.math.tan(0.5f), skew.mat[4])
        assertEquals(kotlin.math.tan(0.25f), skew.mat[1])
    }
}
