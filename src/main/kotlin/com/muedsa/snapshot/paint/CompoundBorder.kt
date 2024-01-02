package com.muedsa.snapshot.paint

import com.muedsa.geometry.EdgeInsets
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Path
import org.jetbrains.skia.Rect
import kotlin.math.max

internal class CompoundBorder(
    val borders: List<ShapeBorder>
) : ShapeBorder() {

    init {
        assert(borders.size >= 2)
        assert(!borders.any { it is CompoundBorder })
    }

    override val dimensions: EdgeInsets = borders.fold(EdgeInsets.ZERO) { acc, shapeBorder -> acc.add(shapeBorder.dimensions) }

    override fun add(other: ShapeBorder, reversed: Boolean): ShapeBorder? {
        if (other !is CompoundBorder) {
            val ours: ShapeBorder = if (reversed) borders.last() else borders.first()
            val merged: ShapeBorder? = ours.add(other = other, reversed = reversed)
            if (merged != null) {
                val result: MutableList<ShapeBorder> = ArrayList(borders)
                result[if (reversed) result.size - 1 else 0] = merged
                return CompoundBorder(result)
            }
        }
        val mergedBorders: List<ShapeBorder> = buildList {
            if (reversed) {
                addAll(borders)
            }
            if (other is CompoundBorder) {
                addAll(other.borders)
            } else {
                add(other)
            }
            if (!reversed) {
                addAll(borders)
            }
        }
        return CompoundBorder(mergedBorders)
    }


    override fun scale(t: Float): ShapeBorder = CompoundBorder(borders.map {
        it.scale(t)
    }.toList())


    override fun lerpFrom(a: ShapeBorder?, t: Float): ShapeBorder? = lerp(a, this, t)

    override fun lerpTo(b: ShapeBorder?, t: Float): ShapeBorder? {
        return lerp(this, b , t)
    }

    override fun getInnerPath(rect: Rect): Path {
        var r = rect
        for (index in 0 .. borders.size - 2) {
            r = borders[index].dimensions.deflateRect(rect)
        }
        return borders.last().getInnerPath(r)
    }

    override fun getOuterPath(rect: Rect): Path = borders.first().getOuterPath(rect)

    override fun paintInterior(canvas: Canvas, rect: Rect, paint: Paint) = borders.first().paintInterior(canvas, rect, paint)

    override val preferPaintInterior: Boolean = borders.all { it.preferPaintInterior }

    override fun paint(canvas: Canvas, rect: Rect) {
        var r = rect
        borders.forEach {
            it.paint(canvas, r)
            r = it.dimensions.deflateRect(r)
        }
    }

    companion object {

        @JvmStatic
        fun lerp(a: ShapeBorder?, b: ShapeBorder?, t: Float): ShapeBorder? {
            assert(a is CompoundBorder || b is CompoundBorder) // Not really necessary, but all call sites currently intend this.
            val aList: List<ShapeBorder?> = if(a is CompoundBorder) a.borders else listOf(a)
            val bList: List<ShapeBorder?> = if(b is CompoundBorder) b.borders else listOf(b)
            val results: MutableList<ShapeBorder> = mutableListOf()
            val length: Int = max(aList.size, bList.size)
            for (index in 0 ..< length) {
                val localA: ShapeBorder? = if (index < aList.size) aList[index] else null
                val localB: ShapeBorder? = if (index < bList.size) bList[index] else null
                if (localA != null && localB != null) {
                    val localResult: ShapeBorder?  = localA.lerpTo(localB, t) ?: localB.lerpFrom(localA, t)
                    if (localResult != null) {
                        results.add(localResult)
                        continue
                    }
                }
                // If we're changing from one shape to another, make sure the shape that is coming in
                // is inserted before the shape that is going away, so that the outer path changes to
                // the new border earlier rather than later. (This affects, among other things, where
                // the ShapeDecoration class puts its background.)
                if (localB != null) {
                    results.add(localB.scale(t))
                }
                if (localA != null) {
                    results.add(localA.scale(1f - t))
                }
            }
            return CompoundBorder(results)
        }
    }
}