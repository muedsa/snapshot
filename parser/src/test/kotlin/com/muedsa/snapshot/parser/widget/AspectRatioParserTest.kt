package com.muedsa.snapshot.parser.widget

import com.muedsa.geometry.Size
import com.muedsa.snapshot.expectColorAt
import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.snapshotPixels
import com.muedsa.snapshot.widget.AspectRatio
import com.muedsa.snapshot.widget.ConstrainedBox
import org.jetbrains.skia.Color
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class AspectRatioParserTest {

    @Test
    fun parses_ratio_and_renders_child_under_bounded_constraints() {
        val root = assertIs<ConstrainedBox>(
            ParserTest.parse(
                """
                <Snapshot>
                    <ConstrainedBox maxWidth="120" maxHeight="80">
                        <AspectRatio aspectRatio="2">
                            <ColoredBox color="#FF0000FF"/>
                        </AspectRatio>
                    </ConstrainedBox>
                </Snapshot>
                """.trimIndent()
            ).createWidget()
        )
        val ratio = assertIs<AspectRatio>(root.child)
        assertEquals(2f, ratio.aspectRatio)

        val box = root.createRenderBox().also { it.layout(BoxConstraints()) }
        assertEquals(Size(120f, 60f), box.definiteSize)

        val pixels = snapshotPixels { attach(root) }
        expectColorAt(pixels, 119, 59, Color.BLUE)
    }

    @Test
    fun rejects_missing_non_positive_and_non_finite_ratio() {
        listOf(null, "0", "-1", "NaN", "Infinity").forEach { value ->
            val attr = if (value == null) "" else " aspectRatio=\"$value\""
            val exception = assertFailsWith<ParseException> {
                ParserTest.parse("<Snapshot><AspectRatio$attr/></Snapshot>").createWidget()
            }
            assertContains(exception.message.orEmpty(), "aspectRatio")
        }
        assertFailsWith<ParseException> {
            ParserTest.parse("<Snapshot><AspectRatio aspectRatio=\"abc\"/></Snapshot>").createWidget()
        }
    }
}
