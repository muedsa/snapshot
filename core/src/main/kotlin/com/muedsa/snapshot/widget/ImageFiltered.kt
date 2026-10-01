package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.ImageFilterRenderObject
import com.muedsa.snapshot.rendering.box.RenderBox
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.Rect
import kotlin.math.ceil

/** 按高斯模糊的约 3σ 影响范围扩张子树绘制边界，供外层 ColorFiltered 合成使用。 */
fun blurImageFilterBounds(sigmaX: Float, sigmaY: Float): (Rect) -> Rect {
    require(sigmaX.isFinite() && sigmaX >= 0f && sigmaY.isFinite() && sigmaY >= 0f)
    val outsetX = ceil(3f * sigmaX)
    val outsetY = ceil(3f * sigmaY)
    return { bounds ->
        Rect.makeLTRB(
            bounds.left - outsetX,
            bounds.top - outsetY,
            bounds.right + outsetX,
            bounds.bottom + outsetY,
        )
    }
}

inline fun ChildSlot.ImageFiltered(
    imageFilter: ImageFilter,
    noinline outputBounds: ((Rect) -> Rect)? = null,
    content: ImageFiltered.() -> Unit = {},
) {
    attach(
        com.muedsa.snapshot.widget.ImageFiltered(
            imageFilter = imageFilter,
            outputBounds = outputBounds,
        ).apply(content)
    )
}

class ImageFiltered(
    var imageFilter: ImageFilter,
    var outputBounds: ((Rect) -> Rect)? = null,
) : SingleChildWidget() {
    override fun createRenderBox(child: Widget?): RenderBox = ImageFilterRenderObject(
        imageFilter = imageFilter,
        outputBounds = outputBounds,
    ).also { p ->
        child?.createRenderBox()?.let {
            p.appendChild(it)
        }
    }

}
