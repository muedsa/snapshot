package com.muedsa.snapshot.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.Offset
import com.muedsa.geometry.Radius
import com.muedsa.geometry.Size
import com.muedsa.snapshot.expectColorAt
import com.muedsa.snapshot.golden
import com.muedsa.snapshot.paint.decoration.BorderRadius
import com.muedsa.snapshot.snapshotPixels
import org.jetbrains.skia.Color
import org.jetbrains.skia.PathBuilder
import kotlin.math.cos
import kotlin.math.sin
import kotlin.test.Test

class ClipTest {

    // 300x300 白底画布;内容盒 200x200 绿色块居中占 (50,50)-(250,250)。
    // 中心 (150,150) 恒为内容绿;点 (60,60) 位于绿块范围内、但应被各裁剪形状切除 → 白底,
    // 该点用于证明裁剪确实生效(无裁剪时 (60,60) 会是绿色)。
    private fun assertClipScene(
        id: String,
        scene: Widget.() -> Unit,
    ) {
        val pixmap = snapshotPixels(content = scene)
        expectColorAt(pixmap, 150, 150, Color.GREEN)
        expectColorAt(pixmap, 60, 60, Color.WHITE)
        golden(id, content = scene)
    }

    private fun Widget.clipRectScene() {
        Container(width = 300f, height = 300f, alignment = BoxAlignment.CENTER) {
            ClipRect(
                clipper = {
                    val clipSize: Size = it / 2f
                    BoxAlignment.CENTER.alongOffset(it - clipSize) combine clipSize
                }
            ) {
                Container(width = 200f, height = 200f, color = Color.GREEN)
            }
        }
    }

    private fun Widget.clipRRectBorderRadiusScene() {
        Container(width = 300f, height = 300f, alignment = BoxAlignment.CENTER) {
            ClipRRect(
                borderRadius = BorderRadius.all(Radius.circular(50f))
            ) {
                Container(width = 200f, height = 200f, color = Color.GREEN)
            }
        }
    }

    private fun Widget.clipRRectClipperScene() {
        Container(width = 300f, height = 300f, alignment = BoxAlignment.CENTER) {
            ClipRRect(
                clipper = {
                    BorderRadius.all(Radius.circular(50f)).toRRect(Offset.ZERO combine it)
                }
            ) {
                Container(width = 200f, height = 200f, color = Color.GREEN)
            }
        }
    }

    private fun Widget.clipOvalScene() {
        Container(width = 300f, height = 300f, alignment = BoxAlignment.CENTER) {
            ClipOval(
                clipper = {
                    Offset.ZERO combine it
                }
            ) {
                Container(width = 200f, height = 200f, color = Color.GREEN)
            }
        }
    }

    private fun Widget.clipPathScene() {
        Container(width = 300f, height = 300f, alignment = BoxAlignment.CENTER) {
            ClipPath(
                clipper = {
                    val r: Float = it.width / 2f
                    val c: Float = it.height / 2f
                    PathBuilder().apply {
                        moveTo(x = c + r, y = c)
                        for (i in 1..7) {
                            val a: Float = 2.6927936f * i
                            lineTo(c + r * cos(a), c + r * sin(a))
                        }
                    }.detach()
                }
            ) {
                Container(width = 200f, height = 200f, color = Color.GREEN)
            }
        }
    }

    @Test
    fun clip_rect_golden() {
        assertClipScene("widget/clip/clip_rect") { clipRectScene() }
    }

    @Test
    fun clip_rrect_borderRadius_golden() {
        assertClipScene("widget/clip/clip_rrect_br") { clipRRectBorderRadiusScene() }
    }

    @Test
    fun clip_rrect_clipper_golden() {
        assertClipScene("widget/clip/clip_rrect_clipper") { clipRRectClipperScene() }
    }

    @Test
    fun clip_oval_golden() {
        assertClipScene("widget/clip/clip_oval") { clipOvalScene() }
    }

    @Test
    fun clip_path_golden() {
        assertClipScene("widget/clip/clip_path") { clipPathScene() }
    }
}
