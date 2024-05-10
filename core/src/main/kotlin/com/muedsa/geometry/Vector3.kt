package com.muedsa.geometry

import org.jetbrains.skia.Matrix33
import kotlin.math.acos
import kotlin.math.sqrt

class Vector3(vararg storage: Float) {
    val storage: FloatArray

    init {
        assert(storage.size == 3)
        this.storage = storage
    }

    /// Length.
    var length: Float
        get() = sqrt(length2)
        set(value) {
            if (value == 0f) {
                setZero()
            } else {
                var l = length
                if (l == 0f) {
                    return
                }
                l = value / l
                storage[0] *= l
                storage[1] *= l
                storage[2] *= l
            }
        }

    val length2: Float
        get() {
            return storage[0] * storage[0] + storage[1] * storage[1] + storage[2] * storage[2]
        }

    fun setZero() {
        storage[0] = 0f
        storage[1] = 0f
        storage[2] = 0f
    }

    fun normalize(): Float {
        val l = length
        if (l == 0f) {
            return 0f
        }
        val d = 1f / l
        storage[0] *= d
        storage[1] *= d
        storage[2] *= d
        return l
    }

    fun normalized(): Vector3 = clone().apply { normalize() }

    fun distanceTo(arg: Vector3): Float = sqrt(distanceToSquared(arg))

    fun distanceToSquared(arg: Vector3): Float {
        val dx = storage[0] - arg.storage[0]
        val dy = storage[1] - arg.storage[1]
        val dz = storage[2] - arg.storage[2]
        return dx * dx + dy * dy + dz * dz
    }

    fun angleTo(other: Vector3): Float {
        if (other == this) {
            return 0f
        }
        val d = dot(other) / (length * other.length)
        return acos(d.coerceIn(-1f, 1f))
    }

    fun angleToSigned(other: Vector3, normal: Vector3): Float {
        val angle = angleTo(other)
        val c = cross(other)
        val d = c.dot(normal)
        return if (d < 0f) -angle else angle
    }

    fun dot(other: Vector3): Float {
        return storage[0] * other.storage[0] + storage[1] * other.storage[1] + storage[2] * other.storage[2]
    }

    fun postmultiply(arg: Matrix33) {
        val v0 = storage[0]
        val v1 = storage[1]
        val v2 = storage[2]
        storage[0] = arg.mat[0] + v0 * arg.mat[1] + arg.mat[2]
        storage[1] = arg.mat[3] + v1 * arg.mat[4] + arg.mat[5]
        storage[2] = arg.mat[6] + v2 * arg.mat[7] + arg.mat[8]
    }

    fun cross(other: Vector3): Vector3 {
        val _x = storage[0]
        val _y = storage[1]
        val _z = storage[2]
        val ox = other.storage[0]
        val oy = other.storage[1]
        val oz = other.storage[2]
        return Vector3(_y * oz - _z * oy, _z * ox - _x * oz, _x * oy - _y * ox)
    }


    fun clone(): Vector3 = Vector3(*storage)

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is Vector3) return false
        return storage.contentEquals(other.storage)
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + storage.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "Vector3(storage=${storage.contentToString()})"
    }


    companion object {
        val ZERO: Vector3 = Vector3(0f, 0f, 0f)
    }


}