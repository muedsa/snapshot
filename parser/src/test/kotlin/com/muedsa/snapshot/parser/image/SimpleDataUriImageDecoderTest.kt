package com.muedsa.snapshot.parser.image

import com.muedsa.snapshot.painterImage
import org.jetbrains.skia.Color
import org.jetbrains.skia.EncodedImageFormat
import java.util.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SimpleDataUriImageDecoderTest {

    @Test
    fun decodes_png_jpeg_and_webp_data_uris() {
        val source = painterImage(2f, 3f, Color.RED) { }
        try {
            for ((mime, format) in listOf(
                "image/png" to EncodedImageFormat.PNG,
                "image/jpeg" to EncodedImageFormat.JPEG,
                "image/webp" to EncodedImageFormat.WEBP,
            )) {
                val encoded = Base64.getEncoder().encodeToString(source.encodeToData(format)!!.bytes)
                val decoded = SimpleDataUriImageDecoder.decode("data:$mime;base64,$encoded")
                try {
                    assertEquals(2, decoded.width)
                    assertEquals(3, decoded.height)
                } finally {
                    decoded.close()
                }
            }
        } finally {
            source.close()
        }
    }

    @Test
    fun rejects_unsupported_or_malformed_data_uris() {
        for (dataUri in listOf(
            "https://example.com/image.png",
            "data:image/svg+xml;base64,PHN2Zz4=",
            "data:image/png,AAAA",
            "data:image/png;base64,",
            "data:image/png;base64,!not-base64!",
            "data:image/png;base64,aGVsbG8=",
        )) {
            assertFailsWith<Exception>("应拒绝 $dataUri") {
                SimpleDataUriImageDecoder.decode(dataUri)
            }
        }
    }
}
