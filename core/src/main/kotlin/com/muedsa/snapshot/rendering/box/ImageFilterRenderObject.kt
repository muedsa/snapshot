package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.rendering.PaintingContext
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.Rect

internal class ImageFilterRenderObject(
    val imageFilter: ImageFilter,
) : RenderSingleChildBox() {

    // ImageFilter 可能扩展绘制范围；无法安全估算时保留原有的不裁剪行为。
    internal override fun getFilterPaintBounds(): Rect? = null

    override fun paint(context: PaintingContext, offset: Offset) {
        if (child != null) {
            context.pushImageFilter(
                offset = offset,
                imageFilter = imageFilter
            ) { c, o -> super.paint(c, o) }
        }
    }
}
