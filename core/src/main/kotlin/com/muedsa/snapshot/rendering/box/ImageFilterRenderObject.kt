package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.rendering.PaintingContext
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.Rect

internal class ImageFilterRenderObject(
    val imageFilter: ImageFilter,
    val outputBounds: ((Rect) -> Rect)? = null,
) : RenderSingleChildBox() {

    internal override fun getFilterPaintBounds(): Rect? {
        val childBounds = super.getFilterPaintBounds() ?: return null
        if (childBounds.width <= 0f || childBounds.height <= 0f) return EMPTY_FILTER_PAINT_BOUNDS
        // 未知滤镜不能按布局尺寸裁剪，否则会截断模糊、位移等越界效果。
        val mappedBounds = outputBounds?.invoke(childBounds) ?: return null
        return mappedBounds.takeIf { it.isFiniteFilterBounds() && it.width >= 0f && it.height >= 0f }
    }

    override fun paint(context: PaintingContext, offset: Offset) {
        if (child != null) {
            context.pushImageFilter(
                offset = offset,
                imageFilter = imageFilter
            ) { c, o -> super.paint(c, o) }
        }
    }
}
