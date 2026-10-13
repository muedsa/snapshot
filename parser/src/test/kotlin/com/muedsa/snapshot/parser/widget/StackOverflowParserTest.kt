package com.muedsa.snapshot.parser.widget

import com.muedsa.snapshot.expectColorAt
import com.muedsa.snapshot.parser.ParserTest
import com.muedsa.snapshot.snapshotPixels
import org.jetbrains.skia.Color
import kotlin.test.Test

class StackOverflowParserTest {
    @Test
    fun stack_and_indexed_stack_clip_earlier_positioned_overflow() {
        for (tag in listOf("Stack", "IndexedStack")) {
            val widget = ParserTest.parse("""
                <Snapshot><Padding padding="20"><SizedBox width="40" height="40">
                    <$tag clipBehavior="HARD_EDGE">
                        <Positioned left="-10" top="0" width="20" height="20"><Container color="red"/></Positioned>
                        <Positioned left="20" top="20" width="10" height="10"><Container color="blue"/></Positioned>
                    </$tag>
                </SizedBox></Padding></Snapshot>
            """).createWidget()
            val pixels = snapshotPixels { attach(widget) }
            expectColorAt(pixels, 15, 25, Color.WHITE)
            expectColorAt(pixels, 25, 25, Color.RED)
            expectColorAt(pixels, 45, 45, if (tag == "Stack") Color.BLUE else Color.WHITE)
        }
    }
}
