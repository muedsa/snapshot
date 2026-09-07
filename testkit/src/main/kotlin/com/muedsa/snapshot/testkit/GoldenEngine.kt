package com.muedsa.snapshot.testkit

import org.jetbrains.skia.*
import java.io.File
import java.util.Locale
import kotlin.math.abs

private val CHANNEL_SHIFTS = intArrayOf(24, 16, 8, 0)

/**
 * golden 三态引擎:
 *  - verify(默认):要求基准存在,逐像素比对,失配产出 actual/diff 并抛 AssertionError
 *  - record:仅当基准不存在时写入 src/test/resources/golden/<id>.png
 *  - update :无条件覆盖同名基准
 *
 * 读写不对称:goldenRoot 系统属性仅用于 record/update 写入路径;verify 一律从 classpath 的
 * `golden/<id>.png` 读取基准,因此 goldenRoot 的覆盖值须指向会被打进测试 classpath 的资源根目录。
 */
internal object GoldenEngine {

    data class DiffResult(val total: Int, val changed: Int, val changedPixels: BooleanArray)

    fun mode(): String =
        System.getProperty("snapshotTest.mode")
            ?: System.getenv("SNAPSHOT_TEST_MODE")
            ?: "verify"

    fun assertMatchesBaseline(
        image: Image,
        id: String,
        perPixelTolerance: Int = 0,
        allowMismatchRatio: Double = 0.0,
    ) {
        when (val m = mode()) {
            "record" -> write(image, id, overwrite = false)
            "update" -> write(image, id, overwrite = true)
            "verify" -> verify(image, id, perPixelTolerance, allowMismatchRatio)
            else -> error("unknown snapshotTest.mode=$m: expected verify|record|update")
        }
    }

    /* ----- 路径 ----- */

    private fun goldenFile(id: String): File {
        val root = System.getProperty("snapshotTest.goldenRoot")?.let(::File)
            ?: File("src/test/resources/golden")
        return File(root, "$id.png")
    }

    /* ----- record / update ----- */

    private fun write(image: Image, id: String, overwrite: Boolean) {
        val target = goldenFile(id)
        if (!overwrite && target.exists()) {
            error("golden '$id' already exists at ${target.path}; run with mode=update to overwrite after review")
        }
        target.parentFile?.mkdirs()
        val bytes = image.encodeToData(EncodedImageFormat.PNG)!!.bytes
        target.writeBytes(bytes)
        println("snapshotTest[${mode()}]: wrote ${target.path}")
    }

    /* ----- verify ----- */

    private fun verify(image: Image, id: String, perPixelTolerance: Int, allowMismatchRatio: Double) {
        val baseline = loadBaseline(id)
            ?: throw AssertionError(
                "golden baseline missing: /golden/$id.png — run with -PsnapshotTest.mode=record first"
            )
        val actual = image.toRasterPixmap()
        if (actual.info.width != baseline.info.width || actual.info.height != baseline.info.height) {
            writeActual(image, id)
            throw AssertionError(
                "golden '$id' size mismatch: actual ${actual.info.width}x${actual.info.height} " +
                    "vs baseline ${baseline.info.width}x${baseline.info.height}"
            )
        }
        val diff = diffPixels(actual, baseline, perPixelTolerance)
        val ratio = if (diff.total == 0) 0.0 else diff.changed.toDouble() / diff.total
        if (ratio > allowMismatchRatio) {
            writeArtifacts(image, id, diff)
            throw AssertionError(
                "golden '$id' mismatch: changed=${diff.changed}/${diff.total} " +
                    "(${String.format(Locale.ROOT, "%.4f", ratio * 100)}%) > allowMismatchRatio=$allowMismatchRatio; " +
                    "artifacts at build/test-results/golden/$id/"
            )
        }
    }

    private fun loadBaseline(id: String): Pixmap? {
        val url = GoldenEngine::class.java.classLoader.getResource("golden/$id.png") ?: return null
        val bytes = url.openStream().use { it.readBytes() }
        return Image.makeFromEncoded(bytes).toRasterPixmap()
    }

    private fun Image.toRasterPixmap(): Pixmap {
        val surface = Surface.makeRasterN32Premul(width, height)
        val dst = Rect.makeXYWH(0f, 0f, width.toFloat(), height.toFloat())
        surface.canvas.drawImageRect(image = this, src = dst, dst = dst, paint = Paint())
        surface.flushAndSubmit()
        return surface.makeImageSnapshot().peekPixels()!!
    }

    private fun diffPixels(a: Pixmap, b: Pixmap, tolerance: Int): DiffResult {
        val width = a.info.width
        val height = a.info.height
        val total = width * height
        val changed = BooleanArray(total)
        var count = 0
        var index = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                val ca = a.getColor(x, y)
                val cb = b.getColor(x, y)
                val isChanged = if (ca == cb) false else colorDiffers(ca, cb, tolerance)
                changed[index] = isChanged
                if (isChanged) {
                    count++
                }
                index++
            }
        }
        return DiffResult(total, count, changed)
    }

    private fun colorDiffers(ca: Int, cb: Int, tolerance: Int): Boolean {
        for (shift in CHANNEL_SHIFTS) {
            if (abs(((ca shr shift) and 0xFF) - ((cb shr shift) and 0xFF)) > tolerance) {
                return true
            }
        }
        return false
    }

    /* ----- artifacts ----- */

    private fun artifactsDir(id: String): File {
        val dir = File("build/test-results/golden/$id")
        dir.mkdirs()
        return dir
    }

    private fun writeActual(image: Image, id: String) {
        val dir = artifactsDir(id)
        File(dir, "actual.png").writeBytes(image.encodeToData(EncodedImageFormat.PNG)!!.bytes)
    }

    private fun writeArtifacts(image: Image, id: String, diff: DiffResult) {
        val dir = artifactsDir(id)
        File(dir, "actual.png").writeBytes(image.encodeToData(EncodedImageFormat.PNG)!!.bytes)

        // diff = actual + 把 changed 像素按扫描线水平 run 点红(相邻 true 合并为一次 drawRect)
        val width = image.width
        val height = image.height
        val surface = Surface.makeRasterN32Premul(width, height)
        val dst = Rect.makeXYWH(0f, 0f, width.toFloat(), height.toFloat())
        surface.canvas.drawImageRect(image = image, src = dst, dst = dst, paint = Paint())
        val paint = Paint().apply { color = Color.RED }
        var index = 0
        for (y in 0 until height) {
            var runStart = -1
            for (x in 0 until width) {
                val isChanged = diff.changedPixels[index]
                index++
                if (isChanged && runStart < 0) {
                    runStart = x
                } else if (!isChanged && runStart >= 0) {
                    surface.canvas.drawRect(
                        Rect.makeXYWH(runStart.toFloat(), y.toFloat(), (x - runStart).toFloat(), 1f),
                        paint
                    )
                    runStart = -1
                }
            }
            if (runStart >= 0) {
                surface.canvas.drawRect(
                    Rect.makeXYWH(runStart.toFloat(), y.toFloat(), (width - runStart).toFloat(), 1f),
                    paint
                )
            }
        }
        surface.flushAndSubmit()
        File(dir, "diff.png").writeBytes(surface.makeImageSnapshot().encodeToData(EncodedImageFormat.PNG)!!.bytes)
    }
}
