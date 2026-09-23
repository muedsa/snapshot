package com.muedsa.snapshot.parser.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.Size
import com.muedsa.snapshot.expectColorAt
import com.muedsa.snapshot.parser.ParseException
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.snapshotPixels
import com.muedsa.snapshot.widget.ConstrainedBox
import com.muedsa.snapshot.widget.FractionallySizedBox
import org.jetbrains.skia.Color
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class FractionallySizedBoxParserTest {

    @Test
    fun parses_factors_alignment_and_renders_child() {
        val root = assertIs<ConstrainedBox>(
            ParserTest.parse(
                """
                <Snapshot>
                    <ConstrainedBox maxWidth="120" maxHeight="80">
                        <FractionallySizedBox widthFactor="0.5" heightFactor="0.5" alignment="BOTTOM_RIGHT">
                            <ColoredBox color="#FF0000FF"/>
                        </FractionallySizedBox>
                    </ConstrainedBox>
                </Snapshot>
                """.trimIndent()
            ).createWidget()
        )
        val fractional = assertIs<FractionallySizedBox>(root.child)
        assertEquals(0.5f, fractional.widthFactor)
        assertEquals(0.5f, fractional.heightFactor)
        assertEquals(BoxAlignment.BOTTOM_RIGHT, fractional.alignment)

        val box = root.createRenderBox().also { it.layout(BoxConstraints()) }
        assertEquals(Size(60f, 40f), box.definiteSize)
        expectColorAt(snapshotPixels { attach(root) }, 59, 39, Color.BLUE)
    }

    @Test
    fun rejects_invalid_factors() {
        listOf("-1", "NaN", "Infinity").forEach { value ->
            val exception = assertFailsWith<ParseException> {
                ParserTest.parse(
                    "<Snapshot><FractionallySizedBox widthFactor=\"$value\"/></Snapshot>"
                ).createWidget()
            }
            assertContains(exception.message.orEmpty(), "widthFactor")
        }
        assertFailsWith<ParseException> {
            ParserTest.parse("<Snapshot><FractionallySizedBox widthFactor=\"bad\"/></Snapshot>")
                .createWidget()
        }
    }
}
