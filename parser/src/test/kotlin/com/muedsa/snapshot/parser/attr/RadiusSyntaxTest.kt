package com.muedsa.snapshot.parser.attr

import com.muedsa.geometry.Radius
import com.muedsa.snapshot.parser.attr.nullable.NullableRadiusAttrDefine
import com.muedsa.snapshot.parser.token.RawAttr
import kotlin.test.*

class RadiusSyntaxTest {
    private val definition = NullableRadiusAttrDefine("borderRadius")

    @Test
    fun slash_and_legacy_tuple_describe_the_same_ellipse() {
        for (value in listOf("12 / 6", "12/6", " (12, 6) ", "1.2e1 / 6.")) {
            assertEquals(Radius.elliptical(12f, 6f), definition.parseValue(RawAttr(definition.name, value)))
            val corner = definition.copyWith("borderRadiusTopLeft")
            assertEquals(Radius.elliptical(12f, 6f), corner.parseValue(RawAttr(corner.name, value)))
        }
        assertEquals(Radius.circular(12f), definition.parseValue(RawAttr(definition.name, " 12 ")))
    }

    @Test
    fun ambiguous_or_incomplete_axes_are_rejected() {
        for (value in listOf("12 6", "12,6", "12/", "/6", "1/2/3", "1 2/3", "(12,6", "NaN", "1e100/2")) {
            assertFailsWith<IllegalArgumentException>(value) { definition.parseValue(RawAttr(definition.name, value)) }
        }
    }
}
