package com.muedsa.geometry

import org.jetbrains.skia.Matrix44
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan


/**
 * Matrix44CMO (col-major order)
 *
 * @see org.jetbrains.skia.Matrix44
 */
class Matrix44CMO(vararg mat: Float) {

    val mat: FloatArray

    /**
     * The constructor parameters are in col-major order.
     */
    init {
        require(mat.size == 16) { "Expected 16 elements, got ${mat.size}" }
        this.mat = mat
    }

    fun translate(tx: Float, ty: Float = 0f, tz: Float = 0f) {
        val t1 = mat[0] * tx + mat[4] * ty + mat[8] * tz + mat[12]
        val t2 = mat[1] * tx + mat[5] * ty + mat[9] * tz + mat[13]
        val t3 = mat[2] * tx + mat[6] * ty + mat[10] * tz + mat[14]
        val t4 = mat[3] * tx + mat[7] * ty + mat[11] * tz + mat[15]
        mat[12] = t1
        mat[13] = t2
        mat[14] = t3
        mat[15] = t4
    }

    fun leftTranslate(tx: Float, ty: Float = 0f, tz: Float = 0f, tw: Float = 1f) {
        // Column 1
        mat[0] += tx * mat[3]
        mat[1] += ty * mat[3]
        mat[2] += tz * mat[3]
        mat[3] = tw * mat[3]

        // Column 2
        mat[4] += tx * mat[7]
        mat[5] += ty * mat[7]
        mat[6] += tz * mat[7]
        mat[7] = tw * mat[7]

        // Column 3
        mat[8] += tx * mat[11]
        mat[9] += ty * mat[11]
        mat[10] += tz * mat[11]
        mat[11] = tw * mat[11]

        // Column 4
        mat[12] += tx * mat[15]
        mat[13] += ty * mat[15]
        mat[14] += tz * mat[15]
        mat[15] = tw * mat[15]
    }

    fun transform(x: Float, y: Float, z: Float) {
        //_mat[16]
        TODO("transform")
    }

    fun clone(): Matrix44CMO {
        return Matrix44CMO(*this.mat)
    }

    fun rotate(axis: Vector3, angle: Float) {
        val len = axis.length
        val axisStorage = axis.storage
        val x = axisStorage[0] / len
        val y = axisStorage[1] / len
        val z = axisStorage[2] / len
        val c = cos(angle)
        val s = sin(angle)
        val cr = 1f - c
        val m11 = x * x * cr + c
        val m12 = x * y * cr - z * s
        val m13 = x * z * cr + y * s
        val m21 = y * x * cr + z * s
        val m22 = y * y * cr + c
        val m23 = y * z * cr - x * s
        val m31 = z * x * cr - y * s
        val m32 = z * y * cr + x * s
        val m33 = z * z * cr + c
        val t1 = mat[0] * m11 + mat[4] * m21 + mat[8] * m31
        val t2 = mat[1] * m11 + mat[5] * m21 + mat[9] * m31
        val t3 = mat[2] * m11 + mat[6] * m21 + mat[10] * m31
        val t4 = mat[3] * m11 + mat[7] * m21 + mat[11] * m31
        val t5 = mat[0] * m12 + mat[4] * m22 + mat[8] * m32
        val t6 = mat[1] * m12 + mat[5] * m22 + mat[9] * m32
        val t7 = mat[2] * m12 + mat[6] * m22 + mat[10] * m32
        val t8 = mat[3] * m12 + mat[7] * m22 + mat[11] * m32
        val t9 = mat[0] * m13 + mat[4] * m23 + mat[8] * m33
        val t10 = mat[1] * m13 + mat[5] * m23 + mat[9] * m33
        val t11 = mat[2] * m13 + mat[6] * m23 + mat[10] * m33
        val t12 = mat[3] * m13 + mat[7] * m23 + mat[11] * m33
        mat[0] = t1
        mat[1] = t2
        mat[2] = t3
        mat[3] = t4
        mat[4] = t5
        mat[5] = t6
        mat[6] = t7
        mat[7] = t8
        mat[8] = t9
        mat[9] = t10
        mat[10] = t11
        mat[11] = t12
    }

    fun rotateX(angle: Float) {
        val cosAngle = cos(angle)
        val sinAngle = sin(angle)
        val t1 = mat[4] * cosAngle + mat[8] * sinAngle
        val t2 = mat[5] * cosAngle + mat[9] * sinAngle
        val t3 = mat[6] * cosAngle + mat[10] * sinAngle
        val t4 = mat[7] * cosAngle + mat[11] * sinAngle
        val t5 = mat[4] * -sinAngle + mat[8] * cosAngle
        val t6 = mat[5] * -sinAngle + mat[9] * cosAngle
        val t7 = mat[6] * -sinAngle + mat[10] * cosAngle
        val t8 = mat[7] * -sinAngle + mat[11] * cosAngle
        mat[4] = t1
        mat[5] = t2
        mat[6] = t3
        mat[7] = t4
        mat[8] = t5
        mat[9] = t6
        mat[10] = t7
        mat[11] = t8
    }

    fun rotateY(angle: Float) {
        val cosAngle = cos(angle)
        val sinAngle = sin(angle)
        val t1 = mat[0] * cosAngle + mat[8] * -sinAngle
        val t2 = mat[1] * cosAngle + mat[9] * -sinAngle
        val t3 = mat[2] * cosAngle + mat[10] * -sinAngle
        val t4 = mat[3] * cosAngle + mat[11] * -sinAngle
        val t5 = mat[0] * sinAngle + mat[8] * cosAngle
        val t6 = mat[1] * sinAngle + mat[9] * cosAngle
        val t7 = mat[2] * sinAngle + mat[10] * cosAngle
        val t8 = mat[3] * sinAngle + mat[11] * cosAngle
        mat[0] = t1
        mat[1] = t2
        mat[2] = t3
        mat[3] = t4
        mat[8] = t5
        mat[9] = t6
        mat[10] = t7
        mat[11] = t8
    }

    fun rotateZ(angle: Float) {
        val cosAngle = cos(angle)
        val sinAngle = sin(angle)
        val t1 = mat[0] * cosAngle + mat[4] * sinAngle
        val t2 = mat[1] * cosAngle + mat[5] * sinAngle
        val t3 = mat[2] * cosAngle + mat[6] * sinAngle
        val t4 = mat[3] * cosAngle + mat[7] * sinAngle
        val t5 = mat[0] * -sinAngle + mat[4] * cosAngle
        val t6 = mat[1] * -sinAngle + mat[5] * cosAngle
        val t7 = mat[2] * -sinAngle + mat[6] * cosAngle
        val t8 = mat[3] * -sinAngle + mat[7] * cosAngle
        mat[0] = t1
        mat[1] = t2
        mat[2] = t3
        mat[3] = t4
        mat[4] = t5
        mat[5] = t6
        mat[6] = t7
        mat[7] = t8
    }

    fun scale(sx: Float, sy: Float, sz: Float, sw: Float = 1f) {
        mat[0] *= sx
        mat[1] *= sx
        mat[2] *= sx
        mat[3] *= sx

        mat[4] *= sy
        mat[5] *= sy
        mat[6] *= sy
        mat[7] *= sy

        mat[8] *= sz
        mat[9] *= sz
        mat[10] *= sz
        mat[11] *= sz

        mat[12] *= sw
        mat[13] *= sw
        mat[14] *= sw
        mat[15] *= sw
    }

    fun scaled(sx: Float, sy: Float, sz: Float, sw: Float = 1f) = clone().apply { scale(sx, sy, sz, sw) }

    fun setZero() {
        mat.fill(0f)
    }

    /**
     * 1, 0, 0, 0
     * 0, 1, 0, 0
     * 0, 0, 1, 0
     * 0, 0, 0, 1
     */
    fun setIdentity() {
        mat[0] = 1f
        mat[1] = 0f
        mat[2] = 0f
        mat[3] = 0f
        mat[4] = 0f
        mat[5] = 1f
        mat[6] = 0f
        mat[7] = 0f
        mat[8] = 0f
        mat[9] = 0f
        mat[10] = 1f
        mat[11] = 0f
        mat[12] = 0f
        mat[13] = 0f
        mat[14] = 0f
        mat[15] = 1f
    }

    fun transposed(): Matrix44CMO = clone().apply { transpose() }

    fun transpose() {
        var temp: Float = mat[4]
        mat[4] = mat[1]
        mat[1] = temp
        temp = mat[8]
        mat[8] = mat[2]
        mat[2] = temp
        temp = mat[12]
        mat[12] = mat[3]
        mat[3] = temp
        temp = mat[9]
        mat[9] = mat[6]
        mat[6] = temp
        temp = mat[13]
        mat[13] = mat[7]
        mat[7] = temp
        temp = mat[14]
        mat[14] = mat[11]
        mat[11] = temp
    }

    fun absolute(): Matrix44CMO = Matrix44CMO(
        mat[0].absoluteValue,
        mat[1].absoluteValue,
        mat[2].absoluteValue,
        mat[3].absoluteValue,
        mat[4].absoluteValue,
        mat[5].absoluteValue,
        mat[6].absoluteValue,
        mat[7].absoluteValue,
        mat[8].absoluteValue,
        mat[9].absoluteValue,
        mat[10].absoluteValue,
        mat[11].absoluteValue,
        mat[12].absoluteValue,
        mat[13].absoluteValue,
        mat[14].absoluteValue,
        mat[15].absoluteValue
    )

    fun determinant(): Float {
        val det2x01x01 = mat[0] * mat[5] - mat[1] * mat[4]
        val det2x01x02 = mat[0] * mat[6] - mat[2] * mat[4]
        val det2x01x03 = mat[0] * mat[7] - mat[3] * mat[4]
        val det2x01x12 = mat[1] * mat[6] - mat[2] * mat[5]
        val det2x01x13 = mat[1] * mat[7] - mat[3] * mat[5]
        val det2x01x23 = mat[2] * mat[7] - mat[3] * mat[6]
        val det3x201x012 = mat[8] * det2x01x12 - mat[9] * det2x01x02 + mat[10] * det2x01x01
        val det3x201x013 = mat[8] * det2x01x13 - mat[9] * det2x01x03 + mat[11] * det2x01x01
        val det3x201x023 = mat[8] * det2x01x23 - mat[10] * det2x01x03 + mat[11] * det2x01x02
        val det3x201x123 = mat[9] * det2x01x23 - mat[10] * det2x01x13 + mat[11] * det2x01x12
        return -det3x201x123 * mat[12] + det3x201x023 * mat[13] - det3x201x013 * mat[14] + det3x201x012 * mat[15]
    }

    fun trace(): Float = mat[0] + mat[5] + mat[10] + mat[15]

    fun getTranslation(): Vector3 = Vector3(mat[12], mat[13], mat[14])

    fun setTranslation(t: Vector3) {
        mat[12] = t.storage[0]
        mat[13] = t.storage[1]
        mat[14] = t.storage[2]
    }

    fun setTranslationRaw(x: Float, y: Float, z: Float) {
        mat[12] = x
        mat[13] = y
        mat[14] = z
    }

    fun add(o: Matrix44CMO) {
        for (i in MATRIX44_RANGE) {
            mat[i] += o.mat[i]
        }
    }

    fun sub(o: Matrix44CMO) {
        for (i in MATRIX44_RANGE) {
            mat[i] -= o.mat[i]
        }
    }

    fun negate() {
        for (i in MATRIX44_RANGE) {
            mat[i] = -mat[i]
        }
    }

    fun multiply(arg: Matrix44CMO) {
        val m00 = mat[0]
        val m01 = mat[4]
        val m02 = mat[8]
        val m03 = mat[12]
        val m10 = mat[1]
        val m11 = mat[5]
        val m12 = mat[9]
        val m13 = mat[13]
        val m20 = mat[2]
        val m21 = mat[6]
        val m22 = mat[10]
        val m23 = mat[14]
        val m30 = mat[3]
        val m31 = mat[7]
        val m32 = mat[11]
        val m33 = mat[15]
        val n00 = arg.mat[0]
        val n01 = arg.mat[4]
        val n02 = arg.mat[8]
        val n03 = arg.mat[12]
        val n10 = arg.mat[1]
        val n11 = arg.mat[5]
        val n12 = arg.mat[9]
        val n13 = arg.mat[13]
        val n20 = arg.mat[2]
        val n21 = arg.mat[6]
        val n22 = arg.mat[10]
        val n23 = arg.mat[14]
        val n30 = arg.mat[3]
        val n31 = arg.mat[7]
        val n32 = arg.mat[11]
        val n33 = arg.mat[15]
        mat[0] = (m00 * n00) + (m01 * n10) + (m02 * n20) + (m03 * n30)
        mat[4] = (m00 * n01) + (m01 * n11) + (m02 * n21) + (m03 * n31)
        mat[8] = (m00 * n02) + (m01 * n12) + (m02 * n22) + (m03 * n32)
        mat[12] = (m00 * n03) + (m01 * n13) + (m02 * n23) + (m03 * n33)
        mat[1] = (m10 * n00) + (m11 * n10) + (m12 * n20) + (m13 * n30)
        mat[5] = (m10 * n01) + (m11 * n11) + (m12 * n21) + (m13 * n31)
        mat[9] = (m10 * n02) + (m11 * n12) + (m12 * n22) + (m13 * n32)
        mat[13] = (m10 * n03) + (m11 * n13) + (m12 * n23) + (m13 * n33)
        mat[2] = (m20 * n00) + (m21 * n10) + (m22 * n20) + (m23 * n30)
        mat[6] = (m20 * n01) + (m21 * n11) + (m22 * n21) + (m23 * n31)
        mat[10] = (m20 * n02) + (m21 * n12) + (m22 * n22) + (m23 * n32)
        mat[14] = (m20 * n03) + (m21 * n13) + (m22 * n23) + (m23 * n33)
        mat[3] = (m30 * n00) + (m31 * n10) + (m32 * n20) + (m33 * n30)
        mat[7] = (m30 * n01) + (m31 * n11) + (m32 * n21) + (m33 * n31)
        mat[11] = (m30 * n02) + (m31 * n12) + (m32 * n22) + (m33 * n32)
        mat[15] = (m30 * n03) + (m31 * n13) + (m32 * n23) + (m33 * n33)
    }

    fun multiplied(arg: Matrix44CMO): Matrix44CMO = clone().apply { multiply(arg) }

    fun toRMO(): Matrix44 = Matrix44(
        mat[0], mat[4], mat[8], mat[12],
        mat[1], mat[5], mat[9], mat[13],
        mat[2], mat[6], mat[10], mat[14],
        mat[3], mat[7], mat[11], mat[15]
    )

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is Matrix44CMO) return false
        return mat.contentEquals(other.mat)
    }

    override fun hashCode(): Int {
        val PRIME = 58
        var result = 1
        result = result * PRIME + mat.contentHashCode()
        return result
    }

    override fun toString(): String = "Matrix44CMO\n" +
            "[${mat[0]}, ${mat[1]}, ${mat[2]}, ${mat[3]},\n" +
            "${mat[4]}, ${mat[5]}, ${mat[6]}, ${mat[7]},\n" +
            "${mat[8]}, ${mat[9]}, ${mat[10]}, ${mat[11]},\n" +
            "${mat[12]}, ${mat[13]}, ${mat[14]}, ${mat[15]}]"

    companion object {

        private val ZERO: Matrix44CMO = Matrix44CMO(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)

        private val IDENTITY: Matrix44CMO = Matrix44CMO(1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)

        @JvmStatic
        val MATRIX44_RANGE: IntRange = IntRange(0, 15)

        @JvmStatic
        fun zero() = IDENTITY.clone()

        @JvmStatic
        fun identity() = IDENTITY.clone()

        @JvmStatic
        fun skewX(alpha: Float): Matrix44CMO = identity().apply {
            mat[4] = tan(alpha)
        }

        @JvmStatic
        fun skewY(beta: Float): Matrix44CMO = identity().apply {
            mat[1] = tan(beta)
        }

        @JvmStatic
        fun skew(alpha: Float, beta: Float): Matrix44CMO = identity().apply {
            mat[4] = tan(alpha)
            mat[1] = tan(beta)
        }

        @JvmStatic
        fun translationValues(x: Float, y: Float, z: Float): Matrix44CMO = identity().apply {
            setTranslationRaw(x = x, y = y, z = z)
        }
    }
}