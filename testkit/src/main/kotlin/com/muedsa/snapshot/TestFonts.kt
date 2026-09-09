package com.muedsa.snapshot

import org.jetbrains.skia.Data
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.Typeface

/**
 * 测试专用内置字体(Noto Sans SC,许可 OFL-1.1,见 `/fonts/NotoSansSC-OFL.txt`)。
 *
 * **为什么需要它**:文本度量依赖 OS 字体,不同平台/机器上 side bearing、ascent/descent、CJK 可用性
 * 都不同,会让"关系断言"也闪断(实测:同一条右对齐断言在 Windows 通过、在 Linux CI 失败)。
 * 在测试里显式指定本字体,可让度量跨平台一致。
 *
 * 覆盖拉丁与 CJK(含完整中文字形),故中英文测试可共用。
 */
val testTypeface: Typeface by lazy {
    val stream = checkNotNull(TestFonts::class.java.getResourceAsStream(FONT_RESOURCE)) {
        "缺少测试字体资源 $FONT_RESOURCE"
    }
    val bytes = stream.use { it.readBytes() }
    checkNotNull(FontMgr.default.makeFromData(Data.makeFromBytes(bytes))) {
        "测试字体加载失败:$FONT_RESOURCE"
    }
}

private const val FONT_RESOURCE = "/fonts/NotoSansSC.ttf"

/** 持有字体资源的类,仅用于 [testTypeface] 取 classpath 资源。 */
private object TestFonts
