package com.muedsa.snapshot.parser.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.Offset
import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.parser.attr.CommonAttrDefine
import com.muedsa.snapshot.parser.token.RawAttr
import com.muedsa.snapshot.widget.Transform
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class CoordinateSyntaxTest {
    @Test
    fun coordinates_accept_spaces_commas_and_legacy_parentheses() {
        for (value in listOf("12 -8", "12,-8", " (12, -8) ", "1.2e1\t-8.", "(12 -8)")) {
            assertEquals(BoxAlignment(12f, -8f), CommonAttrDefine.ALIGNMENT.parseValue(RawAttr("alignment", value)))
            val widget = assertIs<Transform>(ParserTest.parse(
                "<Snapshot><Transform matrix=\"(1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1)\" origin=\"$value\"/></Snapshot>"
            ).createWidget())
            assertEquals(Offset(12f, -8f), widget.origin)
        }
    }

    @Test
    fun renamed_and_nullable_alignment_share_the_syntax() {
        val definition = CommonAttrDefine.ALIGNMENT_N.copyWith("gradientBegin")
        assertEquals(BoxAlignment(-1f, .5f), definition.parseValue(RawAttr("gradientBegin", " -1, .5 ")))
        assertEquals(BoxAlignment.CENTER, CommonAttrDefine.ALIGNMENT.parseValue(RawAttr("alignment", " CENTER ")))
    }

    @Test
    fun invalid_pairs_are_not_partially_accepted() {
        for (value in listOf("1", "1 2 3", "1,,2", "1,", "(1,2", "1,2)", "((1,2))", "NaN 0", "1e100 0", "1px 0")) {
            val error = assertFailsWith<ParseException>(value) {
                ParserTest.parse("<Snapshot><Align alignment=\"$value\"/></Snapshot>").createWidget()
            }
            assertEquals("alignment", error.attributeDiagnostic?.attributeName)
            assertEquals(value, error.attributeDiagnostic?.rawValue)
        }
    }
}
