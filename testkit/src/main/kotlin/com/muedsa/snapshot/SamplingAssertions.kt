package com.muedsa.snapshot

import org.jetbrains.skia.Pixmap
import org.jetbrains.skia.Rect
import kotlin.math.abs
import kotlin.math.ceil

/**
 * 计算矩形与图像水平向求交后落入的整数列区间。
 *
 * 矩形按"左含右开"处理,与像素格(像素 i 覆盖 [i, i+1))相交的列参与统计。
 * 矩形完全越界或为空时返回 null,表示没有像素落入(绝不夹取到边界单像素)。
 */
private fun Pixmap.columnRange(rect: Rect): IntRange? {
    val x0 = ceil(rect.left).toInt()
    val x1 = ceil(rect.right).toInt() - 1
    if (x0 > x1) {
        return null
    }
    val lo = maxOf(x0, 0)
    val hi = minOf(x1, info.width - 1)
    return if (lo <= hi) lo..hi else null
}

/**
 * 计算矩形与图像垂直向求交后落入的整数行区间;语义与 [columnRange] 同构。
 */
private fun Pixmap.rowRange(rect: Rect): IntRange? {
    val y0 = ceil(rect.top).toInt()
    val y1 = ceil(rect.bottom).toInt() - 1
    if (y0 > y1) {
        return null
    }
    val lo = maxOf(y0, 0)
    val hi = minOf(y1, info.height - 1)
    return if (lo <= hi) lo..hi else null
}

/**
 * 断言 (x, y) 处像素颜色与 expectedColor 精确相等。
 *
 * [Pixmap.getColor] 返回与图像颜色类型一致的 premultiplied 颜色;expectedColor 按同格式的
 * 0xAARRGGBB 传入。因此该断言适用于不透明或 alpha=0 的整色比较;若内容含部分透明/反走样像素,
 * 应改用 [expectRegionOpaque]/[expectRegionTransparent]/[expectRegionUniform] 这类区域断言。
 */
fun expectColorAt(pixmap: Pixmap, x: Int, y: Int, expectedColor: Int) {
    val actual = pixmap.getColor(x, y)
    if (actual != expectedColor) {
        throw AssertionError(
            "pixel($x,$y): expected 0x${expectedColor.toUInt().toString(16).padStart(8, '0')} " +
                "but was 0x${actual.toUInt().toString(16).padStart(8, '0')}"
        )
    }
}

private fun Int.a() = (this shr 24) and 0xFF
private fun Int.r() = (this shr 16) and 0xFF
private fun Int.g() = (this shr 8) and 0xFF
private fun Int.b() = this and 0xFF
private fun channelColor(a: Int, r: Int, g: Int, b: Int): Int =
    (a shl 24) or (r shl 16) or (g shl 8) or b

/**
 * 矩形区域像素统计:像素总数、不透明/全透明计数、以及各通道算术平均色(0xAARRGGBB)。
 */
data class RegionColorStats(
    val pixelCount: Int,
    val opaqueCount: Int,
    val transparentCount: Int,
    val averageColor: Int,
)

/**
 * 统计矩形区域落入图像的像素;区域与图像无交集(完全越界或空矩形)时返回全零统计。
 *
 * alpha 语义:alpha==0 记为 transparent,alpha==255 记为 opaque,0<alpha<255 两者皆不计。
 */
fun Pixmap.regionStats(rect: Rect): RegionColorStats {
    val columns = columnRange(rect) ?: return RegionColorStats(0, 0, 0, 0)
    val rows = rowRange(rect) ?: return RegionColorStats(0, 0, 0, 0)
    var count = 0
    var opaque = 0
    var transparent = 0
    var aa = 0L
    var ar = 0L
    var ag = 0L
    var ab = 0L
    for (y in rows) {
        for (x in columns) {
            val c = getColor(x, y)
            val a = c.a()
            aa += a
            ar += c.r()
            ag += c.g()
            ab += c.b()
            count++
            if (a == 0) {
                transparent++
            } else if (a == 255) {
                opaque++
            }
        }
    }
    if (count == 0) {
        return RegionColorStats(0, 0, 0, 0)
    }
    val avg = channelColor(
        (aa / count).toInt(),
        (ar / count).toInt(),
        (ag / count).toInt(),
        (ab / count).toInt()
    )
    return RegionColorStats(count, opaque, transparent, avg)
}

/**
 * 断言矩形区域内不含全透明像素;含 0<alpha<255 的部分透明像素仍视为通过。
 * 区域与图像无交集时直接通过。
 */
fun expectRegionOpaque(pixmap: Pixmap, rect: Rect) {
    val s = pixmap.regionStats(rect)
    if (s.transparentCount != 0) {
        throw AssertionError("region($rect) contains transparent pixels: $s")
    }
}

/**
 * 断言矩形区域内不含不透明像素(全透明或部分透明均可);区域与图像无交集时直接通过。
 */
fun expectRegionTransparent(pixmap: Pixmap, rect: Rect) {
    val s = pixmap.regionStats(rect)
    if (s.opaqueCount != 0) {
        throw AssertionError("region($rect) contains opaque pixels: $s")
    }
}

/**
 * 断言矩形区域内每个像素与 expectedColor 的 RGBA 各通道绝对差的最大值不超过 channelTolerance。
 *
 * expectedColor 语义同 [expectColorAt](0xAARRGGBB、与 premultiplied [Pixmap.getColor] 同格式比较);
 * 非不透明 alpha 也按通道参与比较。区域与图像无交集(空 region)时直接通过。
 */
fun expectRegionUniform(pixmap: Pixmap, rect: Rect, expectedColor: Int, channelTolerance: Int = 0) {
    val columns = pixmap.columnRange(rect) ?: return
    val rows = pixmap.rowRange(rect) ?: return
    val er = expectedColor.r()
    val eg = expectedColor.g()
    val eb = expectedColor.b()
    val ea = expectedColor.a()
    for (y in rows) {
        for (x in columns) {
            val c = pixmap.getColor(x, y)
            val da = abs(c.a() - ea)
            val dr = abs(c.r() - er)
            val dg = abs(c.g() - eg)
            val db = abs(c.b() - eb)
            if (maxOf(maxOf(da, dr), maxOf(dg, db)) > channelTolerance) {
                throw AssertionError(
                    "pixel($x,$y)=0x${c.toUInt().toString(16).padStart(8, '0')} " +
                        "not uniform 0x${expectedColor.toUInt().toString(16).padStart(8, '0')}: " +
                        "channel diffs a=$da r=$dr g=$dg b=$db, tolerance=$channelTolerance"
                )
            }
        }
    }
}
