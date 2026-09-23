package com.muedsa.snapshot.parser.image

import org.jetbrains.skia.Image

/** 将图片 Data URI 解码为可绘制的 [Image]，调用方可替换默认实现。 */
fun interface DataUriImageDecoder {
    fun decode(dataUri: String): Image
}
