package com.muedsa.snapshot

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.EdgeInsets
import com.muedsa.geometry.Offset
import com.muedsa.geometry.Size
import com.muedsa.snapshot.paint.decoration.Border
import com.muedsa.snapshot.paint.decoration.BorderRadius
import com.muedsa.snapshot.paint.decoration.BoxDecoration
import com.muedsa.snapshot.paint.decoration.BoxShadow
import com.muedsa.snapshot.paint.gradient.LinearGradient
import com.muedsa.snapshot.paint.text.TextStyle
import com.muedsa.snapshot.rendering.flex.CrossAxisAlignment
import com.muedsa.snapshot.rendering.flex.MainAxisSize
import com.muedsa.snapshot.widget.ChildSlot
import com.muedsa.snapshot.widget.ClipPath
import com.muedsa.snapshot.widget.Column
import com.muedsa.snapshot.widget.Container
import com.muedsa.snapshot.widget.Padding
import com.muedsa.snapshot.widget.Positioned
import com.muedsa.snapshot.widget.Row
import com.muedsa.snapshot.widget.SizedBox
import com.muedsa.snapshot.widget.Stack
import com.muedsa.snapshot.widget.text.Text
import org.jetbrains.skia.Color
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Path
import org.jetbrains.skia.PathBuilder
import org.junit.jupiter.api.Tag
import kotlin.test.Test

/** 使用项目自身的 Widget、布局、渐变和裁剪能力生成品牌 Logo。 */
@Tag("sample")
class LogoCreator {

    fun renderLogoMark(): ByteArray = SnapshotPNG(background = Color.TRANSPARENT) {
        logoMarkContent()
    }

    fun renderLogoWithName(): ByteArray = SnapshotPNG(background = Color.TRANSPARENT) {
        logoContent()
    }

    fun renderMonochromeLogoMark(): ByteArray = SnapshotPNG(background = Color.TRANSPARENT) {
        monochromeLogoMarkContent()
    }

    @Test
    fun write_logo_mark() {
        rootDirection.resolve("logo_mark.png").toFile().writeBytes(renderLogoMark())
    }

    @Test
    fun write_logo_with_name() {
        rootDirection.resolve("logo.png").toFile().writeBytes(renderLogoWithName())
    }

    @Test
    fun write_monochrome_logo_mark() {
        rootDirection.resolve("logo_mark_mono.png").toFile().writeBytes(renderMonochromeLogoMark())
    }

    companion object {

        /** 只包含图案的方形 Logo，适合头像、图标和小尺寸展示。 */
        fun ChildSlot.logoMarkContent() {
            Padding(padding = EdgeInsets.all(28f)) {
                logoMark(size = 320f)
            }
        }

        /** 单色精简版，只保留取景框和 S，适合 favicon、印刷及小尺寸场景。 */
        fun ChildSlot.monochromeLogoMarkContent() {
            Padding(padding = EdgeInsets.all(28f)) {
                SizedBox(width = 320f, height = 320f) {
                    Stack {
                        Container(width = 320f, height = 320f)
                        viewfinderCorners(unit = 1f, color = MONOCHROME)
                        Positioned(left = 72f, top = 72f, width = 176f, height = 176f) {
                            ClipPath(clipper = { bounds -> buildSPath(bounds) }) {
                                Container(width = 176f, height = 176f, color = MONOCHROME)
                            }
                        }
                    }
                }
            }
        }

        /** 带项目名称的横向 Logo；保留此名称供桌面预览入口复用。 */
        fun ChildSlot.logoContent() {
            Padding(padding = EdgeInsets.all(32f)) {
                Row(
                    mainAxisSize = MainAxisSize.MIN,
                    crossAxisAlignment = CrossAxisAlignment.CENTER,
                ) {
                    logoMark(size = 224f)
                    SizedBox(width = 38f)
                    Column(crossAxisAlignment = CrossAxisAlignment.START) {
                        Text(
                            text = "snapshot",
                            style = TextStyle(
                                color = WORDMARK,
                                fontSize = 102f,
                                fontStyle = FontStyle.BOLD,
                                fontFamilies = listOf(testFontFamily),
                                letterSpacing = -3.2f,
                                height = 1f,
                            ),
                        )
                        SizedBox(height = 8f)
                        Text(
                            text = "DECLARATIVE IMAGE ENGINE",
                            style = TextStyle(
                                color = ACCENT_PURPLE,
                                fontSize = 19f,
                                fontStyle = FontStyle.BOLD,
                                fontFamilies = listOf(testFontFamily),
                                letterSpacing = 4.1f,
                                height = 1f,
                            ),
                        )
                    }
                }
            }
        }

        /**
         * 渐变圆角框代表取景与图像输出；内部阶梯形状同时表示字母 S、Widget 层级与像素块。
         */
        private fun ChildSlot.logoMark(size: Float) {
            val unit = size / 320f
            val frame = 8f * unit
            val innerSize = size - frame * 2f
            Container(
                width = size,
                height = size,
                padding = EdgeInsets.all(frame),
                decoration = BoxDecoration(
                    gradient = LinearGradient(
                        begin = BoxAlignment.TOP_LEFT,
                        end = BoxAlignment.BOTTOM_RIGHT,
                        colors = intArrayOf(ACCENT_PURPLE, ACCENT_CYAN),
                    ),
                    borderRadius = BorderRadius.circular(82f * unit),
                    boxShadow = arrayOf(
                        BoxShadow(
                            color = 0x55_06_B6_D4,
                            offset = Offset(0f, 14f * unit),
                            blurRadius = 28f * unit,
                        ),
                    ),
                ),
            ) {
                Container(
                    width = innerSize,
                    height = innerSize,
                    decoration = BoxDecoration(
                        gradient = LinearGradient(
                            begin = BoxAlignment.TOP_LEFT,
                            end = BoxAlignment.BOTTOM_RIGHT,
                            colors = intArrayOf(MARK_BACKGROUND_LIGHT, MARK_BACKGROUND_DARK),
                        ),
                        borderRadius = BorderRadius.circular(74f * unit),
                    ),
                ) {
                    Stack {
                        Container(width = innerSize, height = innerSize)
                        viewfinderCorners(unit = unit)
                        statusIcons(unit = unit)
                        Positioned(
                            left = 76f * unit,
                            top = 76f * unit,
                            width = 152f * unit,
                            height = 152f * unit,
                        ) {
                            ClipPath(
                                clipper = { bounds -> buildSPath(bounds) },
                            ) {
                                Container(
                                    width = 152f * unit,
                                    height = 152f * unit,
                                    decoration = BoxDecoration(
                                        gradient = LinearGradient(
                                            begin = BoxAlignment.TOP_LEFT,
                                            end = BoxAlignment.BOTTOM_RIGHT,
                                            colors = intArrayOf(MARK_LAVENDER, MARK_CYAN, MARK_MINT),
                                            stops = floatArrayOf(0f, 0.56f, 1f),
                                        ),
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }

        /** 四角的取景框让图案在不依赖文字时仍能直接表达“Snapshot”。 */
        private fun Stack.viewfinderCorners(unit: Float, color: Int = VIEWFINDER) {
            val inset = 30f * unit
            val length = 36f * unit
            val thickness = 6f * unit

            Positioned(left = inset, top = inset, width = length, height = thickness) {
                Container(color = color)
            }
            Positioned(left = inset, top = inset, width = thickness, height = length) {
                Container(color = color)
            }
            Positioned(right = inset, top = inset, width = length, height = thickness) {
                Container(color = color)
            }
            Positioned(right = inset, top = inset, width = thickness, height = length) {
                Container(color = color)
            }
            Positioned(left = inset, bottom = inset, width = length, height = thickness) {
                Container(color = color)
            }
            Positioned(left = inset, bottom = inset, width = thickness, height = length) {
                Container(color = color)
            }
            Positioned(right = inset, bottom = inset, width = length, height = thickness) {
                Container(color = color)
            }
            Positioned(right = inset, bottom = inset, width = thickness, height = length) {
                Container(color = color)
            }
        }

        private fun buildSPath(bounds: Size): Path = PathBuilder().apply {
            moveTo(0f, 0f)
            lineTo(bounds.width, 0f)
            lineTo(bounds.width, bounds.height * 0.24f)
            lineTo(bounds.width * 0.29f, bounds.height * 0.24f)
            lineTo(bounds.width * 0.29f, bounds.height * 0.40f)
            lineTo(bounds.width * 0.78f, bounds.height * 0.40f)
            lineTo(bounds.width * 0.78f, bounds.height * 0.76f)
            lineTo(0f, bounds.height * 0.76f)
            lineTo(0f, bounds.height)
            lineTo(bounds.width, bounds.height)
            lineTo(bounds.width, bounds.height * 0.52f)
            lineTo(bounds.width * 0.51f, bounds.height * 0.52f)
            lineTo(bounds.width * 0.51f, bounds.height * 0.40f)
            lineTo(0f, bounds.height * 0.40f)
            lineTo(0f, 0f)
        }.detach()

        /** 右侧的电量与闪电标识表达渲染引擎已就绪，并补足取景界面的设备感。 */
        private fun Stack.statusIcons(unit: Float) {
            Positioned(
                right = 46f * unit,
                top = 42f * unit,
                width = 38f * unit,
                height = 18f * unit,
            ) {
                Stack {
                    Positioned(left = 0f, top = 0f, width = 32f * unit, height = 18f * unit) {
                        Container(
                            padding = EdgeInsets.all(3f * unit),
                            decoration = BoxDecoration(
                                border = Border.all(color = STATUS_GREEN, width = 2f * unit),
                                borderRadius = BorderRadius.circular(5f * unit),
                            ),
                        ) {
                            Container(
                                width = 22f * unit,
                                height = 8f * unit,
                                decoration = BoxDecoration(
                                    color = STATUS_GREEN,
                                    borderRadius = BorderRadius.circular(2f * unit),
                                ),
                            )
                        }
                    }
                    Positioned(right = 0f, top = 5f * unit, width = 4f * unit, height = 8f * unit) {
                        Container(
                            decoration = BoxDecoration(
                                color = STATUS_GREEN,
                                borderRadius = BorderRadius.circular(2f * unit),
                            ),
                        )
                    }
                }
            }

            Positioned(
                right = 38f * unit,
                bottom = 40f * unit,
                width = 28f * unit,
                height = 36f * unit,
            ) {
                ClipPath(
                    clipper = { bounds ->
                        PathBuilder().apply {
                            moveTo(bounds.width * 0.58f, 0f)
                            lineTo(0f, bounds.height * 0.57f)
                            lineTo(bounds.width * 0.40f, bounds.height * 0.57f)
                            lineTo(bounds.width * 0.24f, bounds.height)
                            lineTo(bounds.width, bounds.height * 0.40f)
                            lineTo(bounds.width * 0.58f, bounds.height * 0.40f)
                            lineTo(bounds.width * 0.58f, 0f)
                        }.detach()
                    },
                ) {
                    Container(
                        width = 28f * unit,
                        height = 36f * unit,
                        decoration = BoxDecoration(
                            gradient = LinearGradient(
                                begin = BoxAlignment.TOP_CENTER,
                                end = BoxAlignment.BOTTOM_CENTER,
                                colors = intArrayOf(STATUS_YELLOW, STATUS_ORANGE),
                            ),
                        ),
                    )
                }
            }
        }

        private const val MARK_BACKGROUND_LIGHT = 0xFF_16_1E_35.toInt()
        private const val MARK_BACKGROUND_DARK = 0xFF_07_0B_16.toInt()
        private const val ACCENT_PURPLE = 0xFF_7C_3A_ED.toInt()
        private const val ACCENT_CYAN = 0xFF_06_B6_D4.toInt()
        private const val MARK_LAVENDER = 0xFF_C4_B5_FD.toInt()
        private const val MARK_CYAN = 0xFF_22_D3_EE.toInt()
        private const val MARK_MINT = 0xFF_A7_F3_D0.toInt()
        private const val VIEWFINDER = 0x99_F8_FA_FC.toInt()
        private const val STATUS_GREEN = 0xFF_4A_DE_80.toInt()
        private const val STATUS_YELLOW = 0xFF_FA_CC_15.toInt()
        private const val STATUS_ORANGE = 0xFF_FB_92_3C.toInt()
        private const val MONOCHROME = 0xFF_7C_3A_ED.toInt()
        private const val WORDMARK = 0xFF_47_55_69.toInt()
    }
}
