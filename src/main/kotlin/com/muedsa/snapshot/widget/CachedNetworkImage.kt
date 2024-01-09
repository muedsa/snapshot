package com.muedsa.snapshot.widget

import com.muedsa.geometry.Alignment
import com.muedsa.snapshot.annotation.MustCallSuper
import com.muedsa.snapshot.paint.BoxFit
import com.muedsa.snapshot.paint.ImageRepeat
import com.muedsa.snapshot.rendering.box.RenderBox
import com.muedsa.snapshot.rendering.box.RenderImage
import com.muedsa.snapshot.tools.NetworkImageCache
import com.muedsa.snapshot.tools.NetworkImageCacheManager
import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.Image

class CachedNetworkImage(
    val url: String,
    width: Float? = null,
    height: Float? = null,
    fit: BoxFit? = null,
    alignment: Alignment = Alignment.CENTER,
    repeat: ImageRepeat = ImageRepeat.NO_REPEAT,
    scale: Float = 1f,
    opacity: Float = 1f,
    color: Int? = null,
    colorBlendMode: BlendMode? = null,
    val cache: NetworkImageCache = NetworkImageCacheManager.defaultCache
) : LocalImage(
    image = Image.makeFromEncoded(cache.getImage(url)),
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