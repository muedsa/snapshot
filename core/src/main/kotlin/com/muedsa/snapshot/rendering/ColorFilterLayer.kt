package com.muedsa.snapshot.rendering

import com.muedsa.snapshot.rendering.box.isFiniteFilterBounds
import org.jetbrains.skia.ColorFilter
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect

class ColorFilterLayer @JvmOverloads constructor(
    val filter: ColorFilter,
    /** 以当前画布坐标表示的子树绘制边界；null 表示无法安全限制。 */
    val bounds: Rect? = null,
) : ContainerLayer() {

    override fun paint(context: LayerPaintContext) {
        val clipBounds = bounds?.takeIf { it.isFiniteFilterBounds() }
        if (clipBounds != null && (clipBounds.width <= 0f || clipBounds.height <= 0f)) return
        if (clipBounds != null) {
            // Flutter 使用子图层绘制边界作为滤镜 saveLayer 的范围；Skiko 的 bounds
            // 仅是分配提示，因此这里额外裁剪，避免可改变透明像素的滤镜染色到层外。
            context.canvas.save()
            context.canvas.clipRect(clipBounds)
        }
        context.canvas.saveLayer(bounds = clipBounds, Paint().also {
            it.colorFilter = filter
        })
        super.paint(context)
        context.canvas.restore()
        if (clipBounds != null) context.canvas.restore()
    }
}
