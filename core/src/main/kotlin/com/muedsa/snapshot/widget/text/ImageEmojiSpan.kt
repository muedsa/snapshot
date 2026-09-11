package com.muedsa.snapshot.widget.text

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.Size
import com.muedsa.snapshot.kDefaultFontSize
import com.muedsa.snapshot.paint.BoxFit
import com.muedsa.snapshot.paint.ImageRepeat
import com.muedsa.snapshot.paint.text.InlineSpan
import com.muedsa.snapshot.paint.text.TextSpan
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderImage
import com.muedsa.snapshot.tools.NetworkImageCache
import com.muedsa.snapshot.tools.NetworkImageCacheManager
import com.muedsa.snapshot.widget.ProviderImage
import com.muedsa.snapshot.widget.Widget
import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.Image
import org.jetbrains.skia.Rect
import org.jetbrains.skia.paragraph.BaselineMode
import org.jetbrains.skia.paragraph.PlaceholderAlignment
import java.util.*

fun TextSpan.ImageEmojiSpan(
    url: String,
    alignment: PlaceholderAlignment = PlaceholderAlignment.BASELINE,
    baseline: BaselineMode? = null,
    width: Float? = null,
    height: Float? = null,
    fit: BoxFit? = null,
    imageAlignment: BoxAlignment = BoxAlignment.CENTER,
    repeat: ImageRepeat = ImageRepeat.NO_REPEAT,
    scale: Float = 1f,
    opacity: Float = 1f,
    color: Int? = null,
    colorBlendMode: BlendMode? = null,
    noCache: Boolean = false,
    cache: NetworkImageCache = NetworkImageCacheManager.defaultCache,
) {
    ImageEmojiSpan(
        provider = { cache.getImage(url, noCache) },
        alignment = alignment,
        baseline = baseline,
        width = width,
        height = height,
        fit = fit,
        imageAlignment = imageAlignment,
        repeat = repeat,
        scale = scale,
        opacity = opacity,
        color = color,
        colorBlendMode = colorBlendMode
    )
}

fun TextSpan.ImageEmojiSpan(
    provider: () -> Image,
    alignment: PlaceholderAlignment = PlaceholderAlignment.BOTTOM,
    baseline: BaselineMode? = null,
    width: Float? = null,
    height: Float? = null,
    fit: BoxFit? = null,
    imageAlignment: BoxAlignment = BoxAlignment.CENTER,
    repeat: ImageRepeat = ImageRepeat.NO_REPEAT,
    scale: Float = 1f,
    opacity: Float = 1f,
    color: Int? = null,
    colorBlendMode: BlendMode? = null,
) {
    WidgetSpan(
        alignment = alignment,
        baseline = baseline
    ) {
        attach(
            ImageEmoji(
                provider = provider,
                width = width,
                height = height,
                fit = fit,
                alignment = imageAlignment,
                repeat = repeat,
                scale = scale,
                opacity = opacity,
                color = color,
                colorBlendMode = colorBlendMode,
            )
        )
    }
}

class ImageEmoji(
    provider: () -> Image,
    width: Float? = null,
    height: Float? = null,
    fit: BoxFit? = null,
    alignment: BoxAlignment = BoxAlignment.CENTER,
    repeat: ImageRepeat = ImageRepeat.NO_REPEAT,
    scale: Float = 1f,
    opacity: Float = 1f,
    color: Int? = null,
    colorBlendMode: BlendMode? = null,
) : ProviderImage(
    provider = provider,
    width = width,
    height = height,
    fit = fit,
    alignment = alignment,
    repeat = repeat,
    scale = scale,
    opacity = opacity,
    color = color,
    colorBlendMode = colorBlendMode,
) {
    override fun createRenderBox(): RenderBox = RenderImageEmoji(
        image = image,
        width = width,
        height = height,
        fit = fit,
        alignment = alignment,
        repeat = repeat,
        scale = scale,
        opacity = opacity,
        color = color,
        colorBlendMode = colorBlendMode
    )
}

class RenderImageEmoji(
    image: Image?,
    width: Float? = null,
    height: Float? = null,
    scale: Float = 1f,
    color: Int? = null,
    opacity: Float = 1f,
    colorBlendMode: BlendMode? = null,
    fit: BoxFit? = null,
    alignment: BoxAlignment = BoxAlignment.CENTER,
    repeat: ImageRepeat = ImageRepeat.NO_REPEAT,
    centerSlice: Rect? = null,
    isAntiAlias: Boolean = false,
) : RenderImage(
    image = image,
    width = width,
    height = height,
    scale = scale,
    color = color,
    opacity = opacity,
    colorBlendMode = colorBlendMode,
    fit = fit,
    alignment = alignment,
    repeat = repeat,
    centerSlice = centerSlice,
    isAntiAlias = isAntiAlias
) {
    private fun findFontSize(): Float? {
        var size: Float? = null
        val pd = parentData
        if (pd is TextParentData) {
            val rootSpan = pd.rootSpan
            val targetSpan = pd.span
            if (rootSpan != null && targetSpan != null) {
                val queue: LinkedList<InlineSpan> = LinkedList()
                val parentValueMap: MutableMap<InlineSpan, Float?> = mutableMapOf()
                parentValueMap[rootSpan] = rootSpan.style?.fontSize
                queue.offer(rootSpan)
                while (!queue.isEmpty()) {
                    val currentSpan: InlineSpan = queue.poll()
                    if (currentSpan == targetSpan) {
                        size = parentValueMap[currentSpan]
                        break
                    }

                    if (currentSpan is TextSpan) {
                        for (child in currentSpan.children) {
                            val childSize = child.style?.fontSize ?: parentValueMap[currentSpan]
                            parentValueMap[child] = childSize
                            queue.offer(child)
                        }
                    }
                }
                if (size == null) {
                    size = kDefaultFontSize
                }
            }
        }
        return size
    }

    private fun sizeForConstraints(constraints: BoxConstraints): Size {
        val fontSize: Float? = findFontSize()
        val temp = BoxConstraints.tightFor(
            width = width ?: fontSize,
            height = height ?: fontSize,
        ).enforce(constraints)
        return image?.let {
            temp.constrainSizeAndAttemptToPreserveAspectRatio(
                Size(
                    it.width / scale,
                    it.height / scale,
                )
            )
        } ?: temp.smallest
    }

    override fun performLayout() {
        size = sizeForConstraints(definiteConstraints)
    }
}
