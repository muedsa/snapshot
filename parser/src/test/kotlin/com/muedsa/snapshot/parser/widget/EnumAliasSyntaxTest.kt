package com.muedsa.snapshot.parser.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.parser.attr.FontStyleAttrDefine
import com.muedsa.snapshot.parser.attr.nullable.NullableBoxShadowAttrDefine
import com.muedsa.snapshot.parser.attr.nullable.NullableFontFeatureListAttrDefine
import com.muedsa.snapshot.parser.attr.nullable.NullableTextDecorationAttrDefine
import com.muedsa.snapshot.parser.attr.nullable.NullableTextShadowAttrDefine
import com.muedsa.snapshot.parser.attr.required.AttrDefine
import com.muedsa.snapshot.parser.token.RawAttr
import org.jetbrains.skia.FontStyle
import java.util.Locale
import kotlin.test.*

class EnumAliasSyntaxTest {
    private fun <T> AttrDefine<T>.read(value: String): T = parseValue(RawAttr(name, value))

    @Test
    fun keyword_aliases_apply_to_composite_attributes_and_root() {
        assertEquals("png", ParserTest.parse("<Snapshot type=\" PNG \"/>").type)
        assertEquals(BoxAlignment.TOP_LEFT, CommonAttrDefine.ALIGNMENT.parseValue(RawAttr("alignment", " top-left ")))
        assertEquals(2, NullableTextDecorationAttrDefine("value").read("underline, line-through")!!.size)
        assertTrue(NullableTextShadowAttrDefine("value").read(" none ")!!.isEmpty())
        assertTrue(NullableFontFeatureListAttrDefine("value").read(" none ")!!.isEmpty())
        assertEquals(1, NullableFontFeatureListAttrDefine("value").read("liga")!!.size)
        assertFailsWith<IllegalArgumentException> { NullableFontFeatureListAttrDefine("value").read("LIGA") }
        assertTrue(NullableBoxShadowAttrDefine("value").read(" elevation-2 ")!!.isNotEmpty())
    }

    @Test
    fun aliases_are_independent_of_the_default_locale() {
        val previous = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"))
            assertEquals(FontStyle.ITALIC, FontStyleAttrDefine("value").read("italic"))
            assertEquals("FILL", ClipPathParser.FIT.read("fill"))
        } finally {
            Locale.setDefault(previous)
        }
    }

    @Test
    fun invalid_values_keep_original_input_and_restricted_choices() {
        val source = "<Snapshot><ClipPath fit=\" cover \"/></Snapshot>"
        val error = assertFailsWith<ParseException> { ParserTest.parse(source).createWidget() }
        assertEquals(" cover ", error.attributeDiagnostic?.rawValue)
        assertEquals(listOf("FILL", "CONTAIN"), error.attributeDiagnostic?.valueInfo?.allowedValues)
        assertEquals(source.indexOf(" cover "), error.pos.pos)
        assertFailsWith<ParseException> {
            ParserTest.parse("<Snapshot><Row mainAxisAlignment=\"space between\"/></Snapshot>").createWidget()
        }
        assertFailsWith<IllegalArgumentException> {
            NullableTextDecorationAttrDefine("value").read("underline,UNDERLINE")
        }
    }
}
