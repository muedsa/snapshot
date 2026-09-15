package com.muedsa.snapshot

import com.muedsa.snapshot.paint.text.TextPainter
import org.jetbrains.skia.Data
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.Typeface
import org.jetbrains.skia.paragraph.TypefaceFontProvider

/**
 * 测试专用内置字体(Noto Sans SC,许可 OFL-1.1,见 `/fonts/NotoSansSC-OFL.txt`)。
 *
 * **为什么需要它**:文本度量依赖 OS 字体,不同平台/机器上 side bearing、ascent/descent、CJK 可用性
 * 都不同,会让"关系断言"也闪断(实测:同一条右对齐断言在 Windows 通过、在 Linux CI 失败)。
 * 在测试里显式指定 [testFontFamily]，可让 Paragraph 的字体解析和度量跨平台一致。
 *
 * 覆盖拉丁与 CJK(含完整中文字形),故中英文测试可共用。
 */
val testTypeface: Typeface
    get() = testFontResources.typeface

/** 供 Skia Paragraph 解析 [testTypeface] 的字体族名。 */
val testFontFamily: String
    get() = testFontResources.typeface.familyName

private val testFontResources: TestFontResources by lazy {
    val stream = checkNotNull(TestFonts::class.java.getResourceAsStream(FONT_RESOURCE)) {
        "缺少测试字体资源 $FONT_RESOURCE"
    }
    val bytes = stream.use { it.readBytes() }
    val typeface = checkNotNull(FontMgr.default.makeFromData(Data.makeFromBytes(bytes))) {
        "测试字体加载失败:$FONT_RESOURCE"
    }
    val provider = TypefaceFontProvider().registerTypeface(typeface, typeface.familyName)
    TextPainter.FONT_COLLECTION.setTestFontManager(provider)
    TestFontResources(typeface, provider)
}

private const val FONT_RESOURCE = "/fonts/NotoSansSC.ttf"

/** 持有字体资源的类,仅用于 [testTypeface] 取 classpath 资源。 */
private object TestFonts

/** 同时保留 Typeface 与字体提供器，避免测试期间原生资源提前进入回收流程。 */
private data class TestFontResources(
    val typeface: Typeface,
    @Suppress("unused") val provider: TypefaceFontProvider,
)
