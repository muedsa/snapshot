package com.muedsa.snapshot.parser.widget

import com.muedsa.geometry.Matrix44CMO
import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.widget.Container
import com.muedsa.snapshot.widget.Transform
import kotlin.test.*

class TransformSyntaxTest {
    @Test
    fun matrix_whitespace_and_function_forms_work_for_both_widgets() {
        val expected = Matrix44CMO.translationValues(12f, 8f, 0f)
        for (value in listOf("translate(12, 8)", "TRANSLATE(1.2e1 8)", " (1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 12, 8, 0, 1) ", "1 0 0 0 0 1 0 0 0 0 1 0 12 8 0 1")) {
            assertEquals(expected, parse(value).transform)
            val container = assertIs<Container>(ParserTest.parse("<Snapshot><Container transform=\"$value\"/></Snapshot>").createWidget())
            assertEquals(expected, container.transform)
        }
        assertEquals(Matrix44CMO.identity(), parse("none").transform)
        assertEquals(Matrix44CMO.identity().apply { translate(12f) }, parse("translate(12)").transform)
    }

    @Test
    fun composed_transforms_have_documented_order_and_angle_units() {
        val translateThenScale = parse("translate(10,0) scale(2)").transform
        val scaleThenTranslate = parse("scale(2) translate(10,0)").transform
        assertEquals(12f, translateThenScale.mat[0] + translateThenScale.mat[12])
        assertEquals(22f, scaleThenTranslate.mat[0] + scaleThenTranslate.mat[12])
        val rotation = parse("rotate(90deg)").transform
        assertEquals(0f, rotation.mat[0], .00001f)
        assertEquals(1f, rotation.mat[1], .00001f)
        val radians = parse("rotate(1.57079632679rad)").transform
        assertContentEquals(rotation.mat, radians.mat)
        assertEquals(Matrix44CMO.identity().apply { scale(2f, 3f, 1f) }, parse("scale(2,3)").transform)
    }

    @Test
    fun malformed_functions_and_non_finite_compositions_report_the_attribute() {
        for (value in listOf("", "translate()", "translate(1,)", "scale(1,2,3)", "rotate(30)", "rotate(1 2deg)", "translate(1) garbage", "translate(1", "skew(1)", "none scale(2)", "translate(1px,2)", "scale(1e30) scale(1e30)", "(1,0,0,1)")) {
            val error = assertFailsWith<ParseException>(value) { parse(value) }
            assertEquals("matrix", error.attributeDiagnostic?.attributeName)
        }
    }

    private fun parse(value: String): Transform = assertIs<Transform>(ParserTest.parse(
        "<Snapshot><Transform matrix=\"$value\"/></Snapshot>"
    ).createWidget())
}
