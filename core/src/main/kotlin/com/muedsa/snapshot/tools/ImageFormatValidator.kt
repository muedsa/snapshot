package com.muedsa.snapshot.tools

@OptIn(ExperimentalStdlibApi::class)
object ImageFormatValidator {

    /**
     * PNG file header
     * @see <a href="https://en.wikipedia.org/wiki/PNG">PNG wikipedia</a>
     */
    val PNG_MAGIC_NUMBER: ByteArray = "89504E470D0A1A0A".hexToByteArray()

    /**
     * JPEG file header
     * @see <a href="https://en.wikipedia.org/wiki/JPEG">JPEG wikipedia</a>
     */
    val JPEG_MAGIC_NUMBER: ByteArray = "FFD8FF".hexToByteArray()

    /**
     * WEBP file header
     * @see <a href="https://en.wikipedia.org/wiki/WEBP">WEBP wikipedia</a>
     */
    val WEBP_MAGIC_NUMBER_1: ByteArray = "52494646".hexToByteArray() // RIFF
    val WEBP_SKIP_LENGTH: Int = 4 // 中间的 4-7 字节代表 长度 + 12
    val WEBP_MAGIC_NUMBER_2: ByteArray = "57454250565038".hexToByteArray() // WEBP VP8(space)

    val NECESSARY_MAGIC_LENGTH: Int = maxOf(
        PNG_MAGIC_NUMBER.size,
        JPEG_MAGIC_NUMBER.size,
        WEBP_MAGIC_NUMBER_1.size + WEBP_SKIP_LENGTH + WEBP_MAGIC_NUMBER_2.size
    )

    fun valid(data: ByteArray): Boolean = validPNG(data) || validJPEG(data) || validWEBP(data)

    fun validPNG(data: ByteArray): Boolean = checkDataHeaderWithMagicNumber(data, PNG_MAGIC_NUMBER, "PNG")

    fun validJPEG(data: ByteArray): Boolean = checkDataHeaderWithMagicNumber(data, JPEG_MAGIC_NUMBER, "JPEG")

    fun validWEBP(data: ByteArray): Boolean {
        if (data.size < WEBP_MAGIC_NUMBER_1.size + WEBP_SKIP_LENGTH + WEBP_MAGIC_NUMBER_2.size) {
            throw IllegalArgumentException("Can not check data is WEBP image because there is not enough data length")
        }
        val data1: ByteArray = data.copyOfRange(0, WEBP_MAGIC_NUMBER_1.size)
        val data2Start: Int = WEBP_MAGIC_NUMBER_1.size + WEBP_SKIP_LENGTH
        val data2: ByteArray = data.copyOfRange(data2Start, data2Start + WEBP_MAGIC_NUMBER_2.size)
        return checkDataHeaderWithMagicNumber(data1, WEBP_MAGIC_NUMBER_1, "WEBP")
                && checkDataHeaderWithMagicNumber(data2, WEBP_MAGIC_NUMBER_2, "WEBP")
    }

    private fun checkDataHeaderWithMagicNumber(data: ByteArray, magic: ByteArray, type: String): Boolean {
        if (data.size < magic.size) {
            throw IllegalArgumentException("Not enough data length to check $type image format")
        }
        for (i in magic.indices) {
            if (data[i] != magic[i]) {
                return false
            }
        }
        return true
    }
}