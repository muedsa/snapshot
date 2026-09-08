package com.muedsa.snapshot.paint.text

import org.jetbrains.skia.FontEdging
import org.jetbrains.skia.FontHinting
import org.jetbrains.skia.paragraph.BaselineMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TextStyleTest {

    @Test
    fun isEmpty_true_when_all_null() {
        assertTrue(TextStyle().isEmpty())
    }

    @Test
    fun isEmpty_false_when_any_field_set() {
        assertFalse(TextStyle(fontSize = 12f).isEmpty())
        assertFalse(TextStyle(color = 0xFFFF0000.toInt()).isEmpty())
        assertFalse(TextStyle(fontEdging = FontEdging.ANTI_ALIAS).isEmpty())
        assertFalse(TextStyle(fontHinting = FontHinting.NORMAL).isEmpty())
        assertFalse(TextStyle(subpixel = true).isEmpty())
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
        assertEquals(true, top.subpixel)
        assertTrue(top.fontHinting == FontHinting.NONE)
    }

    @Test
    fun toSkiko_null_when_empty() {
        assertNull(TextStyle().toSkikoTextStyle())
    }

    @Test
    fun toSkiko_maps_all_key_fields() {
        val style = TextStyle(
            color = 0xFF112233.toInt(),
            fontSize = 18f,
            fontFamilies = listOf("Roboto", "Noto Sans SC"),
            height = 1.2f,
            baselineMode = BaselineMode.ALPHABETIC,
            fontEdging = FontEdging.SUBPIXEL_ANTI_ALIAS,
            fontHinting = FontHinting.FULL,
            subpixel = false,
        )
        val sk = assertNotNull(style.toSkikoTextStyle())
        assertEquals(0xFF112233.toInt(), sk.color)
        assertEquals(18f, sk.fontSize)
        assertEquals(listOf("Roboto", "Noto Sans SC"), sk.fontFamilies.toList())
        assertEquals(1.2f, sk.height)
        assertEquals(BaselineMode.ALPHABETIC, sk.baselineMode)
        assertEquals(FontEdging.SUBPIXEL_ANTI_ALIAS, sk.fontEdging)
        assertEquals(FontHinting.FULL, sk.fontHinting)
        assertEquals(false, sk.subpixel)
    }
}
