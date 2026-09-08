package com.muedsa.snapshot.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.Radius
import com.muedsa.snapshot.golden
import com.muedsa.snapshot.paint.decoration.BorderRadius
import org.jetbrains.skia.Color
import kotlin.test.Test

class ClipGoldenTest {

    @Test
    fun clip_oval_green_golden() {
        golden("widget/clip_oval_green") {
            Container(width = 300f, height = 300f, alignment = BoxAlignment.CENTER, color = Color.WHITE) {
                ClipOval {
                    Container(width = 200f, height = 200f, color = Color.GREEN)
                }
            }
        }
    }

    @Test
    fun clip_rrect_blue_golden() {
        golden("widget/clip_rrect_blue") {
            Container(width = 300f, height = 300f, alignment = BoxAlignment.CENTER, color = Color.WHITE) {
                ClipRRect(borderRadius = BorderRadius.all(Radius.circular(60f))) {
                    Container(width = 200f, height = 200f, color = Color.BLUE)
                }
            }
        }
    }
}
