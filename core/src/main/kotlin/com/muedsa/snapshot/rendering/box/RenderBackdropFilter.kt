package com.muedsa.snapshot.rendering.box

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.rendering.PaintingContext
import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.Rect

class RenderBackdropFilter(
    val imageFilter: ImageFilter,
    val blendMode: BlendMode = BlendMode.SRC_OVER,
) : RenderSingleChildBox() {

    // BackdropFilter 会读取子树之外的背景，不能用子节点尺寸限制它。
    internal override fun getFilterPaintBounds(): Rect? = null

    override fun paint(context: PaintingContext, offset: Offset) {
        if (child != null) {
            context.pushBackDropFilter(
                offset = offset,
                imageFilter = imageFilter,
                blendMode = blendMode
            ) { c, o ->
                super.paint(c, o)
            }
        }
    }
}
