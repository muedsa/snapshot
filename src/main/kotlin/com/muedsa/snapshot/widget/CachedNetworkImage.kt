package com.muedsa.snapshot.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.snapshot.paint.BoxFit
import com.muedsa.snapshot.paint.ImageRepeat
import com.muedsa.snapshot.tools.NetworkImageCache
import com.muedsa.snapshot.tools.NetworkImageCacheManager
import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.Image

fun Widget.CachedNetworkImage(
    url: String,
    width: Float? = null,
    height: Float? = null,
    fit: BoxFit? = null,
    alignment: BoxAlignment = BoxAlignment.CENTER,
    repeat: ImageRepeat = ImageRepeat.NO_REPEAT,
    scale: Float = 1f,
    opacity: Float = 1f,
    color: Int? = null,
    colorBlendMode: BlendMode? = null,
    cache: NetworkImageCache = NetworkImageCacheManager.defaultCache,
) {
    buildChild(
        widget = com.muedsa.snapshot.widget.CachedNetworkImage(
            url = url,
            width = width,
            height = height,
            fit = fit,
            alignment = alignment,
            repeat = repeat,
            scale = scale,
            opacity = opacity,
            color = color,
            colorBlendMode = colorBlendMode,
            cache = cache
        ),
        content = { }
    )
}

open class CachedNetworkImage(
    val url: String,
    width: Float? = null,
    height: Float? = null,
    fit: BoxFit? = null,
    alignment: BoxAlignment = BoxAlignment.CENTER,
    repeat: ImageRepeat = ImageRepeat.NO_REPEAT,
    scale: Float = 1f,
    opacity: Float = 1f,
    color: Int? = null,
    colorBlendMode: BlendMode? = null,
    val cache: NetworkImageCache = NetworkImageCacheManager.defaultCache,
) : ProviderImage(
    provider = { Image.makeFromEncoded(cache.getImage(url)) },
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