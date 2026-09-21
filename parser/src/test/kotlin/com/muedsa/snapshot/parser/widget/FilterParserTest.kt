package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.expectColorAt
import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.snapshotPixels
import com.muedsa.snapshot.widget.BackdropFilter
import com.muedsa.snapshot.widget.ColorFiltered
import com.muedsa.snapshot.widget.ImageFiltered
import com.muedsa.snapshot.widget.Row
import com.muedsa.snapshot.widget.SizedBox
import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotEquals

class FilterParserTest {

    @Test
    fun color_filtered_applies_blend_filter() {
        val snapshot = ParserTest.parse(
            """
            <Snapshot>
                <ColorFiltered color="#FFFF0000" blendMode="MODULATE">
                    <SizedBox width="20" height="20">
                        <Container color="#FF00FF00"/>
                    </SizedBox>
                </ColorFiltered>
            </Snapshot>
            """.trimIndent()
        )

        val widget = assertIs<ColorFiltered>(snapshot.createWidget())
        assertIs<SizedBox>(widget.child)
        val pixels = snapshotPixels { attach(widget) }
        expectColorAt(pixels, 10, 10, Color.BLACK)
    }

    @Test
    fun image_filtered_applies_blur_filter() {
        val snapshot = ParserTest.parse(
            """
            <Snapshot>
                <ImageFiltered sigmaX="3" sigmaY="3" tileMode="CLAMP">
                    <Row>
                        <Container width="50" height="20" color="#FFFF0000"/>
                        <Container width="50" height="20" color="#FF0000FF"/>
                    </Row>
                </ImageFiltered>
            </Snapshot>
            """.trimIndent()
        )

        val widget = assertIs<ImageFiltered>(snapshot.createWidget())
        assertIs<Row>(widget.child)
        val pixels = snapshotPixels { attach(widget) }
        expectColorAt(pixels, 10, 10, Color.RED)
        assertNotEquals(Color.RED, pixels.getColor(49, 10))
        assertNotEquals(Color.BLUE, pixels.getColor(49, 10))
    }

    @Test
    fun backdrop_filter_parses_blur_and_blend_mode() {
        val snapshot = ParserTest.parse(
            """
            <Snapshot>
                <BackdropFilter sigmaX="4" sigmaY="5" tileMode="DECAL" blendMode="SRC">
                    <SizedBox width="20" height="10"/>
                </BackdropFilter>
            </Snapshot>
            """.trimIndent()
        )

        val widget = assertIs<BackdropFilter>(snapshot.createWidget())
        assertEquals(BlendMode.SRC, widget.blendMode)
        assertIs<SizedBox>(widget.child)

        val defaultBlendMode = ParserTest.parse(
            "<Snapshot><BackdropFilter sigmaX=\"1\" sigmaY=\"1\"><SizedBox/></BackdropFilter></Snapshot>"
        )
        assertEquals(BlendMode.SRC_OVER, assertIs<BackdropFilter>(defaultBlendMode.createWidget()).blendMode)
    }

    @Test
    fun rejects_missing_or_invalid_filter_arguments() {
        listOf(
            "<Snapshot><ColorFiltered blendMode=\"SRC\"><SizedBox/></ColorFiltered></Snapshot>",
            "<Snapshot><ColorFiltered color=\"#FFFFFFFF\"><SizedBox/></ColorFiltered></Snapshot>",
            "<Snapshot><ImageFiltered sigmaX=\"2\"><SizedBox/></ImageFiltered></Snapshot>",
            "<Snapshot><ImageFiltered sigmaX=\"-1\" sigmaY=\"2\"><SizedBox/></ImageFiltered></Snapshot>",
            "<Snapshot><ImageFiltered sigmaX=\"2\" sigmaY=\"2\" tileMode=\"UNKNOWN\"><SizedBox/></ImageFiltered></Snapshot>",
            "<Snapshot><BackdropFilter sigmaX=\"NaN\" sigmaY=\"2\"><SizedBox/></BackdropFilter></Snapshot>",
        ).forEach { text ->
            assertFailsWith<ParseException> { ParserTest.parse(text).createWidget() }
        }
    }
}
