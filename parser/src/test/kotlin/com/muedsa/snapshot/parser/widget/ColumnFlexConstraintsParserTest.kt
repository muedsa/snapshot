package com.muedsa.snapshot.parser.widget

import com.muedsa.geometry.Size
import com.muedsa.snapshot.findType
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.rendering.flex.RenderFlex
import com.muedsa.snapshot.rootLayout
import kotlin.test.Test
import kotlin.test.assertEquals

class ColumnFlexConstraintsParserTest {
    @Test
    fun expanded_and_flexible_obey_column_width() {
        for (tag in listOf("Expanded", "Flexible")) {
            for (alignment in listOf("START", "CENTER", "END", "STRETCH")) {
                val widget = ParserTest.parse("""
                    <Snapshot><SizedBox width="100" height="100">
                        <Column crossAxisAlignment="$alignment">
                            <$tag><SizedBox width="200" height="20"/></$tag>
                        </Column>
                    </SizedBox></Snapshot>
                """).createWidget()
                val column = rootLayout { attach(widget) }.findType<RenderFlex>()!!.renderBox as RenderFlex
                assertEquals(Size(100f, if (tag == "Expanded") 100f else 20f), column.children.single().definiteSize,
                    "$tag / $alignment")
            }
        }
    }
}
