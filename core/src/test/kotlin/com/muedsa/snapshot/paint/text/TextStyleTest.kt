package com.muedsa.snapshot.paint.text

import org.jetbrains.skia.FontEdging
import org.jetbrains.skia.FontFeature
import org.jetbrains.skia.FontHinting
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Typeface
import org.jetbrains.skia.paragraph.BaselineMode
import org.jetbrains.skia.paragraph.DecorationStyle
import org.jetbrains.skia.paragraph.Shadow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private val TEST_SHADOW = Shadow(0xFFFF0000.toInt(), 2f, 3f, 1.5)
private val TEST_FONT_FEATURE = FontFeature("tnum", 2)

class TextStyleTest {

    @Test
    fun isEmpty_true_when_all_null() {
        assertTrue(TextStyle().isEmpty())
    }

    @Test
    fun isEmpty_false_when_any_field_set() {
        val singleFieldCases: List<Pair<String, TextStyle>> = listOf(
            "color" to TextStyle(color = 0xFF112233.toInt()),
            "foreground" to TextStyle(foreground = Paint()),
            "background" to TextStyle(background = Paint()),
            "decorationStyle" to TextStyle(decorationStyle = DecorationStyle.NONE),
            "fontStyle" to TextStyle(fontStyle = FontStyle.NORMAL),
            "shadows" to TextStyle(shadows = listOf(TEST_SHADOW)),
            "fontFeatures" to TextStyle(fontFeatures = listOf(TEST_FONT_FEATURE)),
            "fontSize" to TextStyle(fontSize = 12f),
            "fontFamilies" to TextStyle(fontFamilies = listOf("Roboto")),
            "height" to TextStyle(height = 1.2f),
            "topRatio" to TextStyle(topRatio = 0.5f),
            "letterSpacing" to TextStyle(letterSpacing = 1f),
            "wordSpacing" to TextStyle(wordSpacing = 1f),
            "typeface" to TextStyle(typeface = Typeface.makeEmpty()),
            "locale" to TextStyle(locale = "en"),
            "baselineMode" to TextStyle(baselineMode = BaselineMode.IDEOGRAPHIC),
            "fontEdging" to TextStyle(fontEdging = FontEdging.ANTI_ALIAS),
            "fontHinting" to TextStyle(fontHinting = FontHinting.NORMAL),
            "subpixel" to TextStyle(subpixel = true),
        )
        assertEquals(19, singleFieldCases.size, "all 19 nullable fields must be covered")
        singleFieldCases.forEach { (field, style) ->
            assertFalse(style.isEmpty(), "field '$field' should make isEmpty() false")
        }
        // false is also a non-null value, so it must keep the style non-empty
        assertFalse(TextStyle(subpixel = false).isEmpty())
    }

    @Test
    fun mergeFrom_child_overrides_parent_when_non_null() {
        val child = TextStyle(fontSize = 16f, color = 0xFF0000FF.toInt())
        val parent = TextStyle(fontSize = 12f, height = 1.5f)
        val merged = child.mergeFrom(parent)
        assertEquals(16f, merged.fontSize)
        assertEquals(0xFF0000FF.toInt(), merged.color)
        assertEquals(1.5f, merged.height)
    }

    @Test
    fun mergeFrom_empty_child_equals_parent() {
        val parent = TextStyle(fontSize = 12f, fontFamilies = listOf("Roboto"), subpixel = true)
        assertEquals(parent, TextStyle().mergeFrom(parent))
    }

    @Test
    fun mergeFrom_chain_and_font_raster_fields() {
        val base = TextStyle(fontEdging = FontEdging.SUBPIXEL_ANTI_ALIAS, fontHinting = FontHinting.NORMAL, subpixel = true)
        val mid = TextStyle(fontHinting = FontHinting.NONE).mergeFrom(base)
        assertEquals(FontHinting.NONE, mid.fontHinting)
        assertEquals(FontEdging.SUBPIXEL_ANTI_ALIAS, mid.fontEdging)
        val top = TextStyle(fontSize = 20f).mergeFrom(mid)
        assertEquals(20f, top.fontSize)
        assertTrue(top.subpixel ?: false)
        assertEquals(FontHinting.NONE, top.fontHinting)
    }

    @Test
    fun mergeFrom_child_false_subpixel_wins_over_parent_true() {
        val child = TextStyle(subpixel = false)
        val parent = TextStyle(subpixel = true)
        val merged = child.mergeFrom(parent)
        assertEquals(false, merged.subpixel)
    }

    @Test
    fun mergeFrom_does_not_mutate_operands() {
        val child = TextStyle(fontSize = 16f, color = 0xFF0000FF.toInt())
        val parent = TextStyle(fontSize = 12f, height = 1.5f)
        val childSnapshot = child.copy()
        val parentSnapshot = parent.copy()
        child.mergeFrom(parent)
        assertEquals(childSnapshot, child)
        assertEquals(parentSnapshot, parent)
    }

    @Test
    fun toSkiko_null_when_empty() {
        assertNull(TextStyle().toSkikoTextStyle())
    }

    @Test
    fun toSkiko_maps_key_fields_and_lists() {
        val style = TextStyle(
            color = 0xFF112233.toInt(),
            fontSize = 18f,
            fontFamilies = listOf("Roboto", "Noto Sans SC"),
            height = 1.2f,
            baselineMode = BaselineMode.IDEOGRAPHIC,
            fontEdging = FontEdging.SUBPIXEL_ANTI_ALIAS,
            fontHinting = FontHinting.FULL,
            subpixel = true,
            shadows = listOf(TEST_SHADOW),
            fontFeatures = listOf(TEST_FONT_FEATURE),
        )
        val sk = assertNotNull(style.toSkikoTextStyle())
        assertEquals(0xFF112233.toInt(), sk.color)
        assertEquals(18f, sk.fontSize)
        assertEquals(listOf("Roboto", "Noto Sans SC"), sk.fontFamilies.toList())
        assertEquals(1.2f, sk.height)
        assertEquals(BaselineMode.IDEOGRAPHIC, sk.baselineMode)
        assertEquals(FontEdging.SUBPIXEL_ANTI_ALIAS, sk.fontEdging)
        assertEquals(FontHinting.FULL, sk.fontHinting)
        assertTrue(sk.subpixel)
        assertEquals(listOf(TEST_SHADOW), sk.shadows.toList())
        // fontFeatures: skiko snapshot 的原生 getter 不能忠实还原 four-byte tag(实测只回读低字节),
        // 因此只校验条目数(证明 forEach add 已全部写入)与 value 贯通;tag 精确回读待 skiko 侧修复。
        val addedFeatures = sk.fontFeatures
        assertEquals(1, addedFeatures.size)
        assertEquals(listOf(TEST_FONT_FEATURE.value), addedFeatures.map { it.value })
    }

    @Test
    fun toSkiko_maps_subpixel_false() {
        val sk = assertNotNull(TextStyle(fontSize = 12f, subpixel = false).toSkikoTextStyle())
        assertFalse(sk.subpixel)
    }
}
