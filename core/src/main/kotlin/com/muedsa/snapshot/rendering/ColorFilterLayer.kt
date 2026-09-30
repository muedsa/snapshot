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
            // saveLayer 的 bounds 只是分配提示，必须显式裁剪，否则滤镜可能把层外透明像素染色。
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
