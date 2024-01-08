package com.muedsa.snapshot.widget

import com.muedsa.snapshot.drawWidget
import com.muedsa.snapshot.rendering.flex.CrossAxisAlignment
import com.muedsa.snapshot.rendering.flex.MainAxisAlignment
import org.jetbrains.skia.Color
import org.jetbrains.skia.paragraph.BaselineMode
import kotlin.test.Test

class RowTest {

    @Test
    fun crossAxisAlignmentCenterTest() {
        drawWidget(filename = "row/center") {
            Row {
                SizedBox.square(100f) {
                    ColoredBox(color = Color.RED)
                } + SizedBox.square(300f) {
                    ColoredBox(color = Color.GREEN)
                } + SizedBox.square(200f) {
                    ColoredBox(color = Color.BLUE)
                }
            }
        }
    }

    @Test
    fun crossAxisAlignmentStartTest() {
        drawWidget(filename = "row/start") {
            Row(
                crossAxisAlignment = CrossAxisAlignment.START
            ) {
                SizedBox.square(100f) {
                    ColoredBox(color = Color.RED)
                } + SizedBox.square(300f) {
                    ColoredBox(color = Color.GREEN)
                } + SizedBox.square(200f) {
                    ColoredBox(color = Color.BLUE)
                }
            }
        }
    }

    @Test
    fun crossAxisAlignmentEndTest() {
        drawWidget(filename = "row/end") {
            Row(
                crossAxisAlignment = CrossAxisAlignment.END
            ) {
                SizedBox.square(100f) {
                    ColoredBox(color = Color.RED)
                } + SizedBox.square(300f) {
                    ColoredBox(color = Color.GREEN)
                } + SizedBox.square(200f) {
                    ColoredBox(color = Color.BLUE)
                }
            }
        }
    }

    @Test
    fun crossAxisAlignmentStretchTest() {
        drawWidget(filename = "row/stretch") {
            LimitedBox(
                maxHeight = 500f
            ) {
                Row(
                    crossAxisAlignment = CrossAxisAlignment.STRETCH
                ) {
                    SizedBox.square(100f) {
                        ColoredBox(color = Color.RED)
                    } + SizedBox.square(300f) {
                        ColoredBox(color = Color.GREEN)
                    } + SizedBox.square(200f) {
                        ColoredBox(color = Color.BLUE)
                    }
                }
            }

        }
    }

    @Test
    fun crossAxisAlignmentBaselineTest() {
        drawWidget(filename = "row/baseline") {
            Row(
                crossAxisAlignment = CrossAxisAlignment.BASELINE,
                textBaseline = BaselineMode.ALPHABETIC
            ) {
                SizedBox.square(100f) {
                    ColoredBox(color = Color.RED)
                } + SizedBox.square(300f) {
                    ColoredBox(color = Color.GREEN)
                } + SizedBox.square(200f) {
                    ColoredBox(color = Color.BLUE)
                }
            }
        }
    }
}