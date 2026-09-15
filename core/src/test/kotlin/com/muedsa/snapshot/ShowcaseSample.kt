package com.muedsa.snapshot

import com.muedsa.geometry.BoxAlignment
import com.muedsa.geometry.EdgeInsets
import com.muedsa.geometry.Offset
import com.muedsa.geometry.computeRotation
import com.muedsa.snapshot.paint.decoration.Border
import com.muedsa.snapshot.paint.decoration.BorderRadius
import com.muedsa.snapshot.paint.decoration.BoxDecoration
import com.muedsa.snapshot.paint.decoration.BoxShadow
import com.muedsa.snapshot.paint.decoration.BoxShape
import com.muedsa.snapshot.paint.gradient.LinearGradient
import com.muedsa.snapshot.paint.gradient.RadialGradient
import com.muedsa.snapshot.paint.gradient.SweepGradient
import com.muedsa.snapshot.paint.gradient.Gradient
import com.muedsa.snapshot.paint.text.TextStyle
import com.muedsa.snapshot.rendering.ClipBehavior
import com.muedsa.snapshot.rendering.flex.CrossAxisAlignment
import com.muedsa.snapshot.rendering.flex.MainAxisAlignment
import com.muedsa.snapshot.rendering.flex.MainAxisSize
import com.muedsa.snapshot.widget.ChildSlot
import com.muedsa.snapshot.widget.Column
import com.muedsa.snapshot.widget.Container
import com.muedsa.snapshot.widget.Padding
import com.muedsa.snapshot.widget.Positioned
import com.muedsa.snapshot.widget.Row
import com.muedsa.snapshot.widget.SizedBox
import com.muedsa.snapshot.widget.Stack
import com.muedsa.snapshot.widget.text.Text
import org.jetbrains.skia.FontStyle
import org.junit.jupiter.api.Tag
import kotlin.test.Test

/** README 展示图生成器；所有素材均由本地几何、渐变和内置测试字体构成。 */
@Tag("sample")
class ShowcaseSample {

    fun renderDashboard(): ByteArray = SnapshotPNG(background = DASHBOARD_BACKGROUND) {
        dashboardScene()
    }

    fun renderPoster(): ByteArray = SnapshotPNG(background = POSTER_BACKGROUND) {
        posterScene()
    }

    @Test
    fun write_dashboard_showcase() {
        rootDirection.resolve("showcase_dashboard.png").toFile().writeBytes(renderDashboard())
    }

    @Test
    fun write_poster_showcase() {
        rootDirection.resolve("showcase_poster.png").toFile().writeBytes(renderPoster())
    }

    private fun ChildSlot.dashboardScene() {
        Container(
            width = 1200f,
            height = 720f,
            decoration = BoxDecoration(
                gradient = LinearGradient(
                    begin = BoxAlignment.TOP_LEFT,
                    end = BoxAlignment.BOTTOM_RIGHT,
                    colors = intArrayOf(DASHBOARD_BACKGROUND, 0xFF_12_16_2B.toInt(), 0xFF_18_10_31.toInt()),
                ),
            ),
        ) {
            Padding(padding = EdgeInsets.all(48f)) {
                Column(crossAxisAlignment = CrossAxisAlignment.STRETCH) {
                    dashboardHeader()
                    SizedBox(height = 24f)
                    SizedBox(height = 548f) {
                        Row(crossAxisAlignment = CrossAxisAlignment.STRETCH) {
                            heroCard()
                            SizedBox(width = 24f)
                            SizedBox(width = 408f, height = 548f) {
                                Column(crossAxisAlignment = CrossAxisAlignment.STRETCH) {
                                    performanceCard()
                                    SizedBox(height = 24f)
                                    pipelineCard()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun ChildSlot.dashboardHeader() {
        SizedBox(height = 52f) {
            Row(mainAxisAlignment = MainAxisAlignment.SPACE_BETWEEN) {
                Row(mainAxisSize = MainAxisSize.MIN) {
                    Container(
                        width = 48f,
                        height = 48f,
                        alignment = BoxAlignment.CENTER,
                        decoration = BoxDecoration(
                            gradient = LinearGradient(
                                begin = BoxAlignment.TOP_LEFT,
                                end = BoxAlignment.BOTTOM_RIGHT,
                                colors = intArrayOf(ACCENT_PURPLE, ACCENT_CYAN),
                            ),
                            borderRadius = BorderRadius.circular(16f),
                            boxShadow = arrayOf(BoxShadow(0x55_8B_5C_F6, Offset(0f, 8f), 18f)),
                        ),
                    ) {
                        Text("S", style(24f, WHITE, FontStyle.BOLD))
                    }
                    SizedBox(width = 14f)
                    Column(crossAxisAlignment = CrossAxisAlignment.START) {
                        Text("SNAPSHOT", style(21f, WHITE, FontStyle.BOLD, 2.4f))
                        Text("STRUCTURED IMAGE ENGINE", style(10f, MUTED, FontStyle.BOLD, 1.8f))
                    }
                }
                Container(
                    padding = EdgeInsets.symmetric(vertical = 9f, horizontal = 14f),
                    decoration = BoxDecoration(
                        color = 0x18_22_C5_5E,
                        border = Border.all(color = 0x88_22_C5_5E.toInt()),
                        borderRadius = BorderRadius.circular(20f),
                    ),
                ) {
                    Row(mainAxisSize = MainAxisSize.MIN) {
                        Container(
                            width = 8f,
                            height = 8f,
                            decoration = BoxDecoration(color = ACCENT_GREEN, shape = BoxShape.CIRCLE),
                        )
                        SizedBox(width = 8f)
                        Text("RENDER READY", style(11f, 0xFF_86_EF_AC.toInt(), FontStyle.BOLD, 1.2f))
                    }
                }
            }
        }
    }

    private fun ChildSlot.heroCard() {
        Container(
            width = 672f,
            height = 548f,
            decoration = glassCard(
                gradient = LinearGradient(
                    begin = BoxAlignment.TOP_LEFT,
                    end = BoxAlignment.BOTTOM_RIGHT,
                    colors = intArrayOf(0xFF_22_17_46.toInt(), 0xFF_10_22_3E.toInt(), 0xFF_0E_16_2A.toInt()),
                    stops = floatArrayOf(0f, 0.55f, 1f),
                ),
                radius = 34f,
            ),
            clipBehavior = ClipBehavior.ANTI_ALIAS,
        ) {
            Stack {
                Container(width = 672f, height = 548f)
                Positioned(right = -86f, top = -94f, width = 330f, height = 330f) {
                    Container(
                        decoration = BoxDecoration(
                            gradient = RadialGradient(
                                colors = intArrayOf(0x99_8B_5C_F6.toInt(), 0x00_8B_5C_F6),
                                stops = floatArrayOf(0f, 1f),
                            ),
                            shape = BoxShape.CIRCLE,
                        ),
                    )
                }
                Positioned(left = -120f, bottom = -170f, width = 430f, height = 430f) {
                    Container(
                        decoration = BoxDecoration(
                            gradient = RadialGradient(
                                colors = intArrayOf(0x66_06_B6_D4, 0x00_06_B6_D4),
                                stops = floatArrayOf(0f, 1f),
                            ),
                            shape = BoxShape.CIRCLE,
                        ),
                    )
                }
                Positioned(left = 42f, top = 40f, right = 42f, bottom = 38f) {
                    Column(crossAxisAlignment = CrossAxisAlignment.START) {
                        Container(
                            padding = EdgeInsets.symmetric(vertical = 7f, horizontal = 12f),
                            decoration = BoxDecoration(
                                color = 0x26_FF_FF_FF,
                                borderRadius = BorderRadius.circular(16f),
                            ),
                        ) {
                            Text("KOTLIN  ×  SKIA", style(11f, 0xFF_C4_B5_FD.toInt(), FontStyle.BOLD, 1.4f))
                        }
                        SizedBox(height = 24f)
                        Text("Compose pixels.\nShip stories.", style(48f, WHITE, FontStyle.BOLD, -1.2f))
                        SizedBox(height = 16f)
                        Text(
                            "用熟悉的 Widget 树组织复杂画面，布局、绘制与输出保持清晰可维护。",
                            style(17f, 0xFF_B8_C2_D8.toInt()),
                        )
                        SizedBox(height = 28f)
                        Row(mainAxisSize = MainAxisSize.MIN) {
                            featureChip("LAYOUT", ACCENT_PURPLE)
                            SizedBox(width = 10f)
                            featureChip("GRADIENT", ACCENT_CYAN)
                            SizedBox(width = 10f)
                            featureChip("CLIP", ACCENT_GREEN)
                        }
                        SizedBox(height = 30f)
                        Row {
                            metric("12+", "WIDGETS")
                            SizedBox(width = 14f)
                            metric("3", "FORMATS")
                            SizedBox(width = 14f)
                            metric("1", "DSL")
                        }
                    }
                }
            }
        }
    }

    private fun ChildSlot.performanceCard() {
        Container(
            width = 408f,
            height = 262f,
            padding = EdgeInsets.all(26f),
            decoration = glassCard(color = CARD, radius = 28f),
        ) {
            Column(crossAxisAlignment = CrossAxisAlignment.STRETCH) {
                Row(mainAxisAlignment = MainAxisAlignment.SPACE_BETWEEN) {
                    Column(crossAxisAlignment = CrossAxisAlignment.START) {
                        Text("RENDER PROFILE", style(11f, MUTED, FontStyle.BOLD, 1.5f))
                        SizedBox(height = 8f)
                        Text("Native canvas", style(23f, WHITE, FontStyle.BOLD))
                    }
                    Container(
                        width = 42f,
                        height = 42f,
                        alignment = BoxAlignment.CENTER,
                        decoration = BoxDecoration(color = 0x22_06_B6_D4, shape = BoxShape.CIRCLE),
                    ) {
                        Text("24", style(12f, ACCENT_CYAN, FontStyle.BOLD))
                    }
                }
                SizedBox(height = 26f)
                SizedBox(height = 94f) {
                    Row(
                        mainAxisAlignment = MainAxisAlignment.SPACE_BETWEEN,
                        crossAxisAlignment = CrossAxisAlignment.END,
                    ) {
                        bar(34f, 0xFF_31_3B_5A.toInt())
                        bar(52f, 0xFF_43_4F_76.toInt())
                        bar(43f, ACCENT_PURPLE)
                        bar(72f, 0xFF_7C_3A_ED.toInt())
                        bar(60f, ACCENT_CYAN)
                        bar(88f, ACCENT_GREEN)
                    }
                }
            }
        }
    }

    private fun ChildSlot.pipelineCard() {
        Container(
            width = 408f,
            height = 262f,
            padding = EdgeInsets.all(26f),
            decoration = glassCard(
                gradient = LinearGradient(
                    begin = BoxAlignment.TOP_LEFT,
                    end = BoxAlignment.BOTTOM_RIGHT,
                    colors = intArrayOf(0xFF_16_1E_35.toInt(), 0xFF_10_2B_32.toInt()),
                ),
                radius = 28f,
            ),
        ) {
            Column(crossAxisAlignment = CrossAxisAlignment.START) {
                Text("DECLARATIVE PIPELINE", style(11f, MUTED, FontStyle.BOLD, 1.5f))
                SizedBox(height = 18f)
                pipelineStep("01", "BUILD", "Widget tree", ACCENT_PURPLE)
                SizedBox(height = 12f)
                pipelineStep("02", "LAYOUT", "Constraints", ACCENT_CYAN)
                SizedBox(height = 12f)
                pipelineStep("03", "PAINT", "PNG · JPEG · WEBP", ACCENT_GREEN)
            }
        }
    }

    private fun ChildSlot.posterScene() {
        Container(
            width = 720f,
            height = 960f,
            decoration = BoxDecoration(
                gradient = LinearGradient(
                    begin = BoxAlignment.TOP_LEFT,
                    end = BoxAlignment.BOTTOM_RIGHT,
                    colors = intArrayOf(POSTER_BACKGROUND, 0xFF_16_0B_2C.toInt(), 0xFF_04_24_32.toInt()),
                ),
            ),
            clipBehavior = ClipBehavior.ANTI_ALIAS,
        ) {
            Stack {
                Container(width = 720f, height = 960f)
                Positioned(left = -190f, top = 180f, width = 520f, height = 520f) {
                    Container(
                        decoration = BoxDecoration(
                            gradient = RadialGradient(
                                colors = intArrayOf(0x88_7C_3A_ED.toInt(), 0x00_7C_3A_ED),
                                stops = floatArrayOf(0f, 1f),
                            ),
                            shape = BoxShape.CIRCLE,
                        ),
                    )
                }
                Positioned(right = -220f, bottom = 70f, width = 620f, height = 620f) {
                    Container(
                        decoration = BoxDecoration(
                            gradient = RadialGradient(
                                colors = intArrayOf(0x77_06_B6_D4, 0x00_06_B6_D4),
                                stops = floatArrayOf(0f, 1f),
                            ),
                            shape = BoxShape.CIRCLE,
                        ),
                    )
                }
                Positioned(left = 48f, top = 44f, right = 48f) {
                    Row(mainAxisAlignment = MainAxisAlignment.SPACE_BETWEEN) {
                        Text("SNAPSHOT / 2026", style(12f, WHITE, FontStyle.BOLD, 1.8f))
                        Text("GENERATIVE POSTER  02", style(10f, MUTED, FontStyle.BOLD, 1.4f))
                    }
                }
                Positioned(left = 46f, top = 138f, right = 34f) {
                    Column(crossAxisAlignment = CrossAxisAlignment.START) {
                        Text("SHAPE", style(96f, WHITE, FontStyle.BOLD, -4f))
                        Text("THE IMPOSSIBLE", style(54f, 0xFF_A7_F3_D0.toInt(), FontStyle.BOLD, -1.5f))
                    }
                }
                Positioned(left = 145f, top = 390f, width = 430f, height = 430f) {
                    Container(
                        alignment = BoxAlignment.CENTER,
                        decoration = BoxDecoration(
                            gradient = SweepGradient(
                                colors = intArrayOf(ACCENT_PURPLE, 0xFF_EA_58_0C.toInt(), ACCENT_GREEN, ACCENT_CYAN, ACCENT_PURPLE),
                                stops = floatArrayOf(0f, 0.24f, 0.5f, 0.76f, 1f),
                            ),
                            shape = BoxShape.CIRCLE,
                            boxShadow = arrayOf(BoxShadow(0x66_06_B6_D4, Offset(0f, 24f), 44f)),
                        ),
                    ) {
                        Container(
                            width = 310f,
                            height = 310f,
                            alignment = BoxAlignment.CENTER,
                            decoration = BoxDecoration(
                                gradient = RadialGradient(
                                    colors = intArrayOf(0xFF_1F_29_4A.toInt(), 0xFF_07_0B_16.toInt()),
                                ),
                                shape = BoxShape.CIRCLE,
                                border = Border.all(color = 0x44_FF_FF_FF, width = 2f),
                            ),
                        ) {
                            Column(
                                mainAxisAlignment = MainAxisAlignment.CENTER,
                                crossAxisAlignment = CrossAxisAlignment.CENTER,
                            ) {
                                Text("S", style(104f, WHITE, FontStyle.BOLD, -4f))
                                Text("KOTLIN CANVAS", style(11f, ACCENT_CYAN, FontStyle.BOLD, 2f))
                            }
                        }
                    }
                }
                Positioned(right = -44f, top = 338f, width = 250f, height = 56f) {
                    Container(
                        alignment = BoxAlignment.CENTER,
                        transform = computeRotation(-0.16f),
                        transformAlignment = BoxAlignment.CENTER,
                        decoration = BoxDecoration(
                            color = 0xFF_EA_58_0C.toInt(),
                            borderRadius = BorderRadius.circular(28f),
                        ),
                    ) {
                        Text("NO CANVAS CHAOS", style(12f, WHITE, FontStyle.BOLD, 1.3f))
                    }
                }
                Positioned(left = -34f, top = 720f, width = 245f, height = 54f) {
                    Container(
                        alignment = BoxAlignment.CENTER,
                        transform = computeRotation(0.14f),
                        transformAlignment = BoxAlignment.CENTER,
                        decoration = BoxDecoration(
                            color = ACCENT_PURPLE,
                            borderRadius = BorderRadius.circular(27f),
                        ),
                    ) {
                        Text("WIDGET FIRST", style(12f, WHITE, FontStyle.BOLD, 1.5f))
                    }
                }
                Positioned(left = 48f, right = 48f, bottom = 44f) {
                    Row(mainAxisAlignment = MainAxisAlignment.SPACE_BETWEEN) {
                        Column(crossAxisAlignment = CrossAxisAlignment.START) {
                            Text("LAYOUT / PAINT / EXPORT", style(11f, MUTED, FontStyle.BOLD, 1.6f))
                            SizedBox(height = 7f)
                            Text("One tree. Every pixel.", style(22f, WHITE, FontStyle.BOLD))
                        }
                        Container(
                            padding = EdgeInsets.symmetric(vertical = 10f, horizontal = 14f),
                            decoration = BoxDecoration(
                                color = 0x22_FF_FF_FF,
                                border = Border.all(color = 0x55_FF_FF_FF),
                                borderRadius = BorderRadius.circular(18f),
                            ),
                        ) {
                            Text("PNG  JPEG  WEBP", style(10f, WHITE, FontStyle.BOLD, 1.1f))
                        }
                    }
                }
            }
        }
    }

    private fun ChildSlot.featureChip(label: String, color: Int) {
        Container(
            padding = EdgeInsets.symmetric(vertical = 8f, horizontal = 12f),
            decoration = BoxDecoration(
                color = color.withAlpha(34),
                border = Border.all(color = color.withAlpha(132)),
                borderRadius = BorderRadius.circular(18f),
            ),
        ) {
            Text(label, style(10f, color, FontStyle.BOLD, 1.2f))
        }
    }

    private fun ChildSlot.metric(value: String, label: String) {
        Container(
            width = 132f,
            height = 88f,
            padding = EdgeInsets.all(14f),
            decoration = BoxDecoration(
                color = 0x18_FF_FF_FF,
                border = Border.all(color = 0x28_FF_FF_FF),
                borderRadius = BorderRadius.circular(18f),
            ),
        ) {
            Column(crossAxisAlignment = CrossAxisAlignment.START) {
                Text(value, style(27f, WHITE, FontStyle.BOLD))
                Text(label, style(9f, MUTED, FontStyle.BOLD, 1.3f))
            }
        }
    }

    private fun Row.bar(height: Float, color: Int) {
        Container(
            width = 34f,
            height = height,
            decoration = BoxDecoration(color = color, borderRadius = BorderRadius.circular(10f)),
        )
    }

    private fun ChildSlot.pipelineStep(index: String, title: String, detail: String, color: Int) {
        Row(mainAxisSize = MainAxisSize.MIN) {
            Container(
                width = 38f,
                height = 38f,
                alignment = BoxAlignment.CENTER,
                decoration = BoxDecoration(
                    color = color.withAlpha(32),
                    border = Border.all(color = color.withAlpha(116)),
                    borderRadius = BorderRadius.circular(12f),
                ),
            ) {
                Text(index, style(10f, color, FontStyle.BOLD))
            }
            SizedBox(width = 13f)
            Column(crossAxisAlignment = CrossAxisAlignment.START) {
                Text(title, style(12f, WHITE, FontStyle.BOLD, 1.1f))
                Text(detail, style(10f, MUTED))
            }
        }
    }

    private fun glassCard(
        color: Int? = null,
        gradient: Gradient? = null,
        radius: Float,
    ): BoxDecoration = BoxDecoration(
        color = color,
        gradient = gradient,
        border = Border.all(color = 0x33_FF_FF_FF, width = 1f),
        borderRadius = BorderRadius.circular(radius),
        boxShadow = arrayOf(BoxShadow(0x55_00_00_00, Offset(0f, 18f), 28f)),
    )

    private fun style(
        size: Float,
        color: Int,
        weight: FontStyle = FontStyle.NORMAL,
        spacing: Float = 0f,
    ): TextStyle = TextStyle(
        color = color,
        fontSize = size,
        fontStyle = weight,
        typeface = testTypeface,
        letterSpacing = spacing,
        height = 1.05f,
    )

    private fun Int.withAlpha(alpha: Int): Int = (this and 0x00_FF_FF_FF) or (alpha shl 24)

    companion object {
        private val DASHBOARD_BACKGROUND = 0xFF_07_0B_16.toInt()
        private val POSTER_BACKGROUND = 0xFF_05_08_12.toInt()
        private val CARD = 0xEE_15_1C_2E.toInt()
        private val WHITE = 0xFF_F8_FA_FC.toInt()
        private val MUTED = 0xFF_94_A3_B8.toInt()
        private val ACCENT_PURPLE = 0xFF_8B_5C_F6.toInt()
        private val ACCENT_CYAN = 0xFF_22_D3_EE.toInt()
        private val ACCENT_GREEN = 0xFF_22_C5_5E.toInt()
    }
}
