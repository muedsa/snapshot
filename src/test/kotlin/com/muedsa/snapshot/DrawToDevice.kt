package com.muedsa.snapshot

import com.muedsa.snapshot.LogoCreator.Companion.logoContent
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaLayerRenderDelegate
import org.jetbrains.skiko.SkikoRenderDelegate
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.WindowConstants
import kotlin.math.ceil

fun main() {
    val renderBox = layoutWidget {
        logoContent()
    }
    val size = renderBox.definiteSize
    // 尝试使用CPU渲染
    val skiaLayer = SkiaLayer()
    skiaLayer.renderDelegate = SkiaLayerRenderDelegate(skiaLayer, object : SkikoRenderDelegate {
        override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
            canvas.drawRenderBox(renderBox = renderBox, background = Color.WHITE, debug = false)
        }
    })
    SwingUtilities.invokeLater {
        val window = JFrame("Draw To Device").apply {
            defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            preferredSize = Dimension(ceil(size.width).toInt(), ceil(size.height).toInt())
        }
        skiaLayer.attachTo(window.contentPane)
        skiaLayer.needRedraw()
        window.pack()
        window.isVisible = true
    }
}