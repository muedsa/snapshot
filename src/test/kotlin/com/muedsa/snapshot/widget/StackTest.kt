package com.muedsa.snapshot.widget

import com.muedsa.geometry.Alignment
import com.muedsa.geometry.AlignmentDirectional
import com.muedsa.snapshot.drawWidget
import org.jetbrains.skia.Color
import org.jetbrains.skia.paragraph.Direction
import kotlin.test.Test

class StackTest {

    @Test
    fun alignment_direction_test() {
        println("\n\n\nStackTest.alignment_direction_test()")
        val alignmentArr = arrayOf(
            AlignmentDirectional.TOP_START,
            AlignmentDirectional.TOP_CENTER,
            AlignmentDirectional.TOP_END,
            AlignmentDirectional.CENTER_START,
            AlignmentDirectional.CENTER,
            AlignmentDirectional.CENTER_END,
            AlignmentDirectional.BOTTOM_START,
            AlignmentDirectional.BOTTOM_CENTER,
            AlignmentDirectional.BOTTOM_END,
            Alignment.TOP_CENTER,
            Alignment.TOP_RIGHT,
            Alignment.CENTER_LEFT,
            Alignment.CENTER,
            Alignment.CENTER_RIGHT,
            Alignment.BOTTOM_LEFT,
            Alignment.BOTTOM_CENTER,
            Alignment.BOTTOM_RIGHT
        )
        val directionArr = Direction.entries.toTypedArray()
        alignmentArr.forEachIndexed { alignmentIndex, alignment ->
            directionArr.forEachIndexed { directionIndex, direction ->
                val name = "stack/a${alignmentIndex}_d${directionIndex}"
                val description = "Stack(\n$alignment\n$direction\n)"
                println("\n\ndraw: $name\n$description")
                drawWidget(imagePathWithoutSuffix = name, debugInfo = description) {
                    Stack(
                        alignment = alignment,
                        textDirection = direction
                    ) { arrayOf(
                        Container(width = 200f, height = 80f, color = Color.withA(Color.RED, 128)),
                        Container(width = 50f, height = 150f, color = Color.withA(Color.YELLOW, 128)),
                        Container(width = 100f, height = 100f, color = Color.withA(Color.GREEN, 128))
                    )}
                }
            }
        }
    }
}