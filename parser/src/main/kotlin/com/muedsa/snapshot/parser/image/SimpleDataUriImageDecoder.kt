package com.muedsa.snapshot.parser.image

import org.jetbrains.skia.Image
import java.util.Base64
import java.util.Locale

/** 仅解析 PNG、JPEG、WebP 的 Base64 Data URI，不提供额外的限制或缓存。 */
object SimpleDataUriImageDecoder : DataUriImageDecoder {

    private val supportedHeaders = setOf(
        "data:image/png;base64",
        "data:image/jpeg;base64",
        "data:image/webp;base64",
    )

    override fun decode(dataUri: String): Image {
        val separator = dataUri.indexOf(',')
        require(separator > 0 && dataUri.substring(0, separator).lowercase(Locale.ROOT) in supportedHeaders) {
            "Expected a PNG, JPEG or WebP Base64 image Data URI"
        }
        val encoded = dataUri.substring(separator + 1)
        require(encoded.isNotEmpty()) { "Image Data URI has no Base64 payload" }
        return Image.makeFromEncoded(Base64.getDecoder().decode(encoded))
    }
}
