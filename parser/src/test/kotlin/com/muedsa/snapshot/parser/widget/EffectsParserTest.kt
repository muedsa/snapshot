package com.muedsa.snapshot.parser.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.Matrix44CMO
import com.muedsa.geometry.Offset
import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.widget.Opacity
import com.muedsa.snapshot.widget.SizedBox
import com.muedsa.snapshot.widget.Transform
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull

class EffectsParserTest {

    @Test
    fun parses_opacity_with_child() {
        val snapshot = ParserTest.parse(
            """
            <Snapshot>
                <Opacity opacity="0.5"><SizedBox width="20" height="10"/></Opacity>
            </Snapshot>
            """.trimIndent()
        )

        val opacity = assertIs<Opacity>(snapshot.createWidget())
        assertEquals(0.5f, opacity.opacity)
        assertIs<SizedBox>(opacity.child)
    }

    @Test
    fun opacity_defaults_to_fully_visible() {
        val snapshot = ParserTest.parse(
            """
            <Snapshot><Opacity><SizedBox width="20" height="10"/></Opacity></Snapshot>
            """.trimIndent()
        )

        assertEquals(1f, assertIs<Opacity>(snapshot.createWidget()).opacity)
    }

    @Test
    fun rejects_opacity_outside_valid_range() {
        val snapshot = ParserTest.parse(
            """
            <Snapshot><Opacity opacity="1.1"><SizedBox/></Opacity></Snapshot>
            """.trimIndent()
        )

        assertFailsWith<ParseException> { snapshot.createWidget() }
    }

    @Test
    fun parses_transform_matrix_origin_and_alignment() {
        val snapshot = ParserTest.parse(
            """
            <Snapshot>
                <Transform matrix="(1,0,0,0,0,1,0,0,0,0,1,0,20,10,0,1)"
                           origin="(2,3)" alignment="BOTTOM_RIGHT">
                    <SizedBox width="20" height="10"/>
                </Transform>
            </Snapshot>
            """.trimIndent()
        )

        val transform = assertIs<Transform>(snapshot.createWidget())
        assertEquals(Matrix44CMO.translationValues(20f, 10f, 0f), transform.transform)
        assertEquals(Offset(2f, 3f), transform.origin)
        assertEquals(BoxAlignment.BOTTOM_RIGHT, transform.alignment)
        assertIs<SizedBox>(transform.child)
    }

    @Test
    fun transform_defaults_origin_and_alignment_to_null() {
        val snapshot = ParserTest.parse(
            """
            <Snapshot>
                <Transform matrix="(1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1)"><SizedBox/></Transform>
            </Snapshot>
            """.trimIndent()
        )

        val transform = assertIs<Transform>(snapshot.createWidget())
        assertNull(transform.origin)
        assertNull(transform.alignment)
    }

    @Test
    fun rejects_transform_without_matrix() {
        val snapshot = ParserTest.parse(
            """
            <Snapshot><Transform><SizedBox/></Transform></Snapshot>
            """.trimIndent()
        )

        assertFailsWith<ParseException> { snapshot.createWidget() }
    }

    @Test
    fun rejects_transform_matrix_with_wrong_value_count() {
        val snapshot = ParserTest.parse(
            """
            <Snapshot><Transform matrix="(1,0,0,1)"><SizedBox/></Transform></Snapshot>
            """.trimIndent()
        )

        assertFailsWith<ParseException> { snapshot.createWidget() }
    }

    @Test
    fun rejects_non_finite_transform_values() {
        val snapshot = ParserTest.parse(
            """
            <Snapshot>
                <Transform matrix="(NaN,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1)"><SizedBox/></Transform>
            </Snapshot>
            """.trimIndent()
        )

        assertFailsWith<ParseException> { snapshot.createWidget() }
    }
}
