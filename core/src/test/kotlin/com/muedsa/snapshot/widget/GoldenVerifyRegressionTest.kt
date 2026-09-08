package com.muedsa.snapshot.widget

import com.muedsa.geometry.BoxAlignment
import com.muedsa.snapshot.golden
import org.jetbrains.skia.Color
import java.io.File
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class GoldenVerifyRegressionTest {

    private val mode: String
        get() = System.getProperty("snapshotTest.mode")
            ?: System.getenv("SNAPSHOT_TEST_MODE")
            ?: "verify"

    @Test
    fun verify_fails_on_mismatch_and_writes_artifacts() {
        // 仅在默认 verify 下有意义;record/update 模式会污染/覆盖基准,直接跳过
        if (mode != "verify") {
            return
        }
        // 复用已入库基准 widget/clip_oval_green(绿色),改渲红色应失配
        assertFailsWith<AssertionError> {
            golden("widget/clip_oval_green") {
                Container(width = 300f, height = 300f, alignment = BoxAlignment.CENTER, color = Color.WHITE) {
                    ClipOval {
                        Container(width = 200f, height = 200f, color = Color.RED)
                    }
                }
            }
        }
        val dir = File("build/test-results/golden/widget/clip_oval_green")
        assertTrue(File(dir, "actual.png").exists(), "应产出 actual.png")
        assertTrue(File(dir, "diff.png").exists(), "应产出 diff.png")
    }
}
