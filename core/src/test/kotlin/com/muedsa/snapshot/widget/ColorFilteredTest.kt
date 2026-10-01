package com.muedsa.snapshot.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.Matrix44CMO
import com.muedsa.geometry.Offset
import com.muedsa.snapshot.expectColorAt
import com.muedsa.snapshot.layoutWidget
import com.muedsa.snapshot.snapshotPixels
import com.muedsa.snapshot.testFontFamily
import com.muedsa.snapshot.paint.decoration.BoxDecoration
import com.muedsa.snapshot.paint.decoration.BoxShadow
import com.muedsa.snapshot.paint.text.TextStyle
import com.muedsa.snapshot.widget.text.Text
import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.Color
import org.jetbrains.skia.ColorFilter
import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.Rect
import org.jetbrains.skia.paragraph.Shadow
import org.jetbrains.skia.paragraph.Direction
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ColorFilteredTest {

    // 六色块横排 50x100(总 300x100);块中心采样点 x = 25 + 50i, y = 50。
    private val blockColors = intArrayOf(
        Color.RED, Color.GREEN, Color.BLUE, Color.CYAN, Color.MAGENTA, Color.YELLOW
    )

    private fun ChildSlot.colorGrid() {
        Row(textDirection = Direction.LTR) {
            blockColors.forEach { c -> Container(width = 50f, height = 100f, color = c) }
        }
    }

    private fun ChildSlot.colorFilteredScene(filter: ColorFilter) {
        ColorFiltered(colorFilter = filter) { colorGrid() }
    }

    private fun sampleX(index: Int): Int = 25 + index * 50

    @Test
    fun red_modulate_multiplies_channels() {
        // MODULATE 逐通道相乘:结果 = src * RED = (R, 0, 0)。
        // 实测:红/品红/黄 → 0xffff0000,绿/蓝/青 → 0xff000000。
        val filter = ColorFilter.makeBlend(Color.RED, BlendMode.MODULATE)
        val pixmap = snapshotPixels { colorFilteredScene(filter) }
        val expected = intArrayOf(
            0xffff0000.toInt(), 0xff000000.toInt(), 0xff000000.toInt(),
            0xff000000.toInt(), 0xffff0000.toInt(), 0xffff0000.toInt(),
        )
        expected.forEachIndexed { i, color -> expectColorAt(pixmap, sampleX(i), 50, color) }
    }

    @Test
    fun red_modulate_differs_from_unfiltered_twin() {
        // 孪生互比:同一场景有/无滤镜,绿块中心必然不同
        val filter = ColorFilter.makeBlend(Color.RED, BlendMode.MODULATE)
        val plain = snapshotPixels { colorGrid() }
        val filtered = snapshotPixels { colorFilteredScene(filter) }
        assertNotEquals(plain.getColor(sampleX(1), 50), filtered.getColor(sampleX(1), 50))
    }

    @Test
    fun gray_saturation_produces_rec601_luma() {
        // SATURATION(灰) 把饱和度置 0 → 等通道灰度。
        // 实测灰度精确等于 0.30R + 0.59G + 0.11B 取整:红 76 / 绿 150 / 蓝 28 / 青 178 / 品红 105 / 黄 227。
        // 系数取自 skiko 当前 SATURATION 矩阵实测;若升级 skiko 改变系数,需同步更新期望值。
        val filter = ColorFilter.makeBlend(0xFF9E9E9E.toInt(), BlendMode.SATURATION)
        val pixmap = snapshotPixels { colorFilteredScene(filter) }
        val expectedGray = intArrayOf(76, 150, 28, 178, 105, 227)
        blockColors.forEachIndexed { i, source ->
            val actual = pixmap.getColor(sampleX(i), 50)
            val r = (actual shr 16) and 0xFF
            val g = (actual shr 8) and 0xFF
            val b = actual and 0xFF
            assertTrue(
                abs(r - g) <= 1 && abs(g - b) <= 1,
                "块$i(源 0x${source.toUInt().toString(16)})应为等通道灰度,实际 R=$r G=$g B=$b"
            )
            assertTrue(
                abs(r - expectedGray[i]) <= 2,
                "块$i 灰度应≈${expectedGray[i]},实际 R=$r G=$g B=$b"
            )
        }
    }

    @Test
    fun small_filtered_child_does_not_tint_outside_pixels() {
        val modes = arrayOf(
            BlendMode.MULTIPLY,
            BlendMode.SCREEN,
            BlendMode.DIFFERENCE,
            BlendMode.HUE,
            BlendMode.LUMINOSITY,
        )
        modes.forEach { mode ->
            val filter = ColorFilter.makeBlend(Color.RED, mode)
            val pixmap = snapshotPixels {
                Container(width = 100f, height = 100f, alignment = BoxAlignment.CENTER, color = Color.WHITE) {
                    SizedBox(width = 20f, height = 20f) {
                        ColorFiltered(colorFilter = filter) {
                            ColoredBox(color = Color.BLUE)
                        }
                    }
                }
            }
            expectColorAt(pixmap, 0, 0, Color.WHITE)
            assertNotEquals(Color.WHITE, pixmap.getColor(50, 50), "$mode 应继续作用于子节点")
        }
    }

    @Test
    fun nested_image_filter_with_output_bounds_does_not_tint_outside_its_painted_area() {
        val pixmap = snapshotPixels {
            Container(width = 100f, height = 100f, alignment = BoxAlignment.CENTER, color = Color.WHITE) {
                SizedBox(width = 20f, height = 20f) {
                    ColorFiltered(colorFilter = ColorFilter.makeBlend(Color.RED, BlendMode.MULTIPLY)) {
                        ImageFiltered(
                            imageFilter = ImageFilter.makeBlur(2f, 2f, FilterTileMode.CLAMP),
                            outputBounds = blurImageFilterBounds(2f, 2f),
                        ) {
                            ColoredBox(color = Color.BLUE)
                        }
                    }
                }
            }
        }
        expectColorAt(pixmap, 0, 0, Color.WHITE)
        assertNotEquals(Color.WHITE, pixmap.getColor(38, 50), "模糊内容不应被裁到子节点尺寸内")
        assertNotEquals(Color.WHITE, pixmap.getColor(50, 50))
    }

    @Test
    fun filtered_child_bounds_follow_nested_alignment() {
        val pixmap = snapshotPixels {
            ColorFiltered(colorFilter = ColorFilter.makeBlend(Color.RED, BlendMode.MULTIPLY)) {
                SizedBox(width = 100f, height = 100f) {
                    Align(alignment = BoxAlignment.CENTER) {
                        SizedBox(width = 20f, height = 20f) {
                            ColoredBox(color = Color.BLUE)
                        }
                    }
                }
            }
        }
        expectColorAt(pixmap, 0, 0, Color.WHITE)
        expectColorAt(pixmap, 50, 50, Color.BLACK)
    }

    @Test
    fun filtered_child_keeps_unconstrained_overflow() {
        val pixmap = snapshotPixels {
            Container(width = 100f, height = 100f, alignment = BoxAlignment.CENTER, color = Color.WHITE) {
                SizedBox(width = 20f, height = 20f) {
                    ColorFiltered(colorFilter = ColorFilter.makeBlend(Color.RED, BlendMode.MULTIPLY)) {
                        UnconstrainedBox {
                            Container(width = 40f, height = 40f, color = Color.BLUE)
                        }
                    }
                }
            }
        }
        expectColorAt(pixmap, 25, 50, Color.WHITE)
        expectColorAt(pixmap, 35, 50, Color.BLACK)
        expectColorAt(pixmap, 65, 50, Color.BLACK)
    }

    @Test
    fun filtered_child_bounds_follow_transform() {
        val pixmap = snapshotPixels {
            Container(width = 100f, height = 100f, alignment = BoxAlignment.CENTER, color = Color.WHITE) {
                SizedBox(width = 20f, height = 20f) {
                    ColorFiltered(colorFilter = ColorFilter.makeBlend(Color.RED, BlendMode.MULTIPLY)) {
                        Transform(
                            transform = Matrix44CMO.translationValues(x = 20f, y = 0f, z = 0f),
                            alignment = null,
                        ) {
                            ColoredBox(color = Color.BLUE)
                        }
                    }
                }
            }
        }
        expectColorAt(pixmap, 50, 50, Color.WHITE)
        expectColorAt(pixmap, 70, 50, Color.BLACK)
    }

    @Test
    fun empty_child_does_not_create_filter_color() {
        val pixmap = snapshotPixels {
            ColorFiltered(colorFilter = ColorFilter.makeBlend(Color.RED, BlendMode.SRC)) {
                SizedBox(width = 40f, height = 40f)
            }
        }
        expectColorAt(pixmap, 20, 20, Color.WHITE)
    }

    @Test
    fun source_filter_colors_transparent_gap_only_within_child_paint_bounds() {
        val pixmap = snapshotPixels {
            Container(width = 140f, height = 40f, color = Color.WHITE, alignment = BoxAlignment.CENTER) {
                SizedBox(width = 100f, height = 20f) {
                    ColorFiltered(colorFilter = ColorFilter.makeBlend(Color.RED, BlendMode.SRC)) {
                        Row(textDirection = Direction.LTR) {
                            Container(width = 20f, height = 20f, color = Color.BLUE)
                            SizedBox(width = 60f, height = 20f)
                            Container(width = 20f, height = 20f, color = Color.BLUE)
                        }
                    }
                }
            }
        }
        expectColorAt(pixmap, 10, 20, Color.WHITE)
        expectColorAt(pixmap, 70, 20, Color.RED)
        expectColorAt(pixmap, 130, 20, Color.WHITE)
    }

    @Test
    fun inner_clip_limits_filtered_paint_bounds() {
        val pixmap = snapshotPixels {
            ColorFiltered(colorFilter = ColorFilter.makeBlend(Color.RED, BlendMode.MULTIPLY)) {
                ClipRect(clipper = { Rect.makeLTRB(40f, 40f, 60f, 60f) }) {
                    SizedBox(width = 100f, height = 100f) {
                        ColoredBox(color = Color.BLUE)
                    }
                }
            }
        }
        expectColorAt(pixmap, 20, 20, Color.WHITE)
        expectColorAt(pixmap, 50, 50, Color.BLACK)
    }

    @Test
    fun filtered_decoration_keeps_shadow_outside_layout_size() {
        val pixmap = snapshotPixels {
            Container(width = 100f, height = 100f, alignment = BoxAlignment.CENTER, color = Color.WHITE) {
                SizedBox(width = 20f, height = 20f) {
                    ColorFiltered(colorFilter = ColorFilter.makeBlend(Color.RED, BlendMode.MULTIPLY)) {
                        Container(
                            width = 20f,
                            height = 20f,
                            decoration = BoxDecoration(
                                boxShadow = arrayOf(BoxShadow(Color.BLUE, Offset(15f, 0f), blurRadius = 2f))
                            ),
                        )
                    }
                }
            }
        }
        expectColorAt(pixmap, 65, 50, Color.BLACK)
        expectColorAt(pixmap, 85, 50, Color.WHITE)
    }

    @Test
    fun text_shadow_expands_filter_paint_bounds() {
        val root = layoutWidget {
            ColorFiltered(colorFilter = ColorFilter.makeBlend(Color.RED, BlendMode.MULTIPLY)) {
                SizedBox(width = 100f, height = 30f) {
                    Text(
                        "Hi",
                        style = TextStyle(
                            fontSize = 20f,
                            fontFamilies = listOf(testFontFamily),
                            shadows = listOf(Shadow(Color.BLUE, 24f, 0f, 0.0)),
                        ),
                    )
                }
            }
        }
        assertTrue(root.getFilterPaintBounds()!!.right >= 124f)
    }
}
