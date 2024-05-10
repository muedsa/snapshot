package com.muedsa.snapshot.paint.decoration

import com.muedsa.geometry.EdgeInsets
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Path
import org.jetbrains.skia.Rect

internal class CompoundBorder(
    val borders: List<ShapeBorder>,
) : ShapeBorder() {

    init {
        assert(borders.size >= 2)
        assert(!borders.any { it is CompoundBorder })
    }

    override val dimensions: EdgeInsets =
        borders.fold(EdgeInsets.ZERO) { acc, shapeBorder -> acc.add(shapeBorder.dimensions) }

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

    override fun getInnerPath(rect: Rect): Path {
        var r = rect
        for (index in 0..borders.size - 2) {
            r = borders[index].dimensions.deflateRect(rect)
        }
        return borders.last().getInnerPath(r)
    }

    override fun getOuterPath(rect: Rect): Path = borders.first().getOuterPath(rect)

    override fun paintInterior(canvas: Canvas, rect: Rect, paint: Paint) =
        borders.first().paintInterior(canvas, rect, paint)

    override val preferPaintInterior: Boolean = borders.all { it.preferPaintInterior }

    override fun paint(canvas: Canvas, rect: Rect) {
        var r = rect
        borders.forEach {
            it.paint(canvas, r)
            r = it.dimensions.deflateRect(r)
        }
    }
}