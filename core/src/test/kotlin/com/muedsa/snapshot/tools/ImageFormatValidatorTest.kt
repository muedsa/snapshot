package com.muedsa.snapshot.tools

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * [ImageFormatValidator] 的边界语义。
 *
 * 正例用**真实文件头字面量**(PNG 规范签名 / JPEG SOI、段起始标记 / RIFF…WEBPVP8),
 * 不复用被测常量,避免与实现同源。
 */
class ImageFormatValidatorTest {

    /** PNG 规范签名:`89 50 4E 47 0D 0A 1A 0A`。 */
    private val pngHeader = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)

    /** JPEG 的 SOI(FF D8)加段起始标记(FF)。 */
    private val jpegHeader = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())

    /** 合法的 WEBP 头:`RIFF` + 4 字节长度占位 + `WEBPVP8`,总长取给定值(默认够检)。 */
    private fun webpHeader(size: Int = ImageFormatValidator.NECESSARY_MAGIC_LENGTH): ByteArray {
        val bytes = ByteArray(size)
        "RIFF".forEachIndexed { index, c -> bytes[index] = c.code.toByte() }
        // bytes[4..7] 是长度字段,内容不参与格式校验
        "WEBPVP8".forEachIndexed { index, c -> bytes[8 + index] = c.code.toByte() }
        return bytes
    }

    private fun ByteArray.withByteAt(index: Int, value: Int): ByteArray =
        clone().also { it[index] = value.toByte() }

    @Test
    fun png_signature_recognised_and_each_changed_byte_rejected() {
        assertTrue(ImageFormatValidator.validPNG(pngHeader), "PNG 规范签名应被识别")
        pngHeader.indices.forEach { index ->
            val broken = pngHeader.withByteAt(index, pngHeader[index].toInt().xor(0xFF))
            assertFalse(ImageFormatValidator.validPNG(broken), "第 $index 字节被破坏后不应再判为 PNG")
        }
    }

    @Test
    fun jpeg_recognised_with_only_three_bytes() {
        assertTrue(ImageFormatValidator.validJPEG(jpegHeader), "FF D8 FF 即应判为 JPEG")
        assertFalse(ImageFormatValidator.validJPEG(jpegHeader.withByteAt(0, 0x00)), "SOI 首字节不符应被拒")
        assertFalse(ImageFormatValidator.validJPEG(jpegHeader.withByteAt(2, 0x00)), "第三字节不符应被拒")
    }

    @Test
    fun webp_requires_both_riff_and_webp_signatures() {
        assertTrue(ImageFormatValidator.validWEBP(webpHeader()), "RIFF…WEBPVP8 应判为 WEBP")
        assertFalse(
            ImageFormatValidator.validWEBP(webpHeader().withByteAt(0, 'X'.code)),
            "RIFF 不符应被拒"
        )
        assertFalse(
            ImageFormatValidator.validWEBP(webpHeader().withByteAt(10, 'X'.code)),
            "WEBPVP8 不符应被拒"
        )
    }

    @Test
    fun valid_accepts_every_supported_format() {
        assertTrue(ImageFormatValidator.valid(pngHeader), "PNG 应通过 valid")
        // valid 先检 PNG(需 8 字节):3 字节的 JPEG 头必须补齐后才可能通过
        assertTrue(ImageFormatValidator.valid(jpegHeader.copyOf(8)), "补齐到 8 字节后 JPEG 应通过 valid")
        assertTrue(ImageFormatValidator.valid(webpHeader()), "WEBP 应通过 valid")
        assertFalse(
            ImageFormatValidator.valid(ByteArray(ImageFormatValidator.NECESSARY_MAGIC_LENGTH)),
            "全零数据不应通过 valid"
        )
    }

    @Test
    fun short_data_throws_instead_of_returning_false() {
        // **反直觉契约**:谓词式的 valid* 对长度不足的数据抛 IllegalArgumentException,
        // 而非返回 false。调用方(LimitedImageInputStream / SimpleNoLimitedNetworkImageCache)
        // 都保证至少传入 NECESSARY_MAGIC_LENGTH 字节,故该分支在当前代码中不可达;
        // 这里显式锁定,以免将来被无声地改成"返回 false"。
        assertFailsWith<IllegalArgumentException>("PNG 检查需要 8 字节") {
            ImageFormatValidator.validPNG(ByteArray(7))
        }
        assertFailsWith<IllegalArgumentException>("JPEG 检查需要 3 字节") {
            ImageFormatValidator.validJPEG(ByteArray(2))
        }
        assertFailsWith<IllegalArgumentException>("WEBP 检查需要 15 字节") {
            ImageFormatValidator.validWEBP(ByteArray(14))
        }
        // valid 短路到 validPNG,故短数据同样抛异常而非返回 false;
        // 注意连"3 字节的合法 JPEG 头"都会抛——valid 的下限是 PNG 的签名长度(8 字节),
        // 与实际格式无关。调用方因此必须准备 NECESSARY_MAGIC_LENGTH 长度的缓冲。
        assertFailsWith<IllegalArgumentException>("valid 对 3 字节数据也应抛") {
            ImageFormatValidator.valid(ByteArray(3))
        }
        assertFailsWith<IllegalArgumentException>("即使是合法但过短的 JPEG 头也抛") {
            ImageFormatValidator.valid(jpegHeader)
        }
        assertFailsWith<IllegalArgumentException>("空数组同样抛") {
            ImageFormatValidator.valid(ByteArray(0))
        }
    }

    @Test
    fun necessary_magic_length_is_enough_for_every_format() {
        val length = ImageFormatValidator.NECESSARY_MAGIC_LENGTH
        // 该长度必须够检三种格式:把各自的有效头补齐到该长度后,检查都应通过且不抛
        fun padded(header: ByteArray) = header.copyOf(length)

        assertTrue(ImageFormatValidator.validPNG(padded(pngHeader)), "PNG 头补齐后应通过")
        assertTrue(ImageFormatValidator.validJPEG(padded(jpegHeader)), "JPEG 头补齐后应通过")
        assertTrue(ImageFormatValidator.validWEBP(webpHeader(length)), "WEBP 头应刚好够检")
        assertTrue(length >= pngHeader.size, "该长度不应小于 PNG 签名长度")
    }
}
