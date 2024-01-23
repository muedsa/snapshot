package com.muedsa.snapshot.benchmark

import com.muedsa.snapshot.LogoCreator.Companion.logoContent
import com.muedsa.snapshot.SnapshotImage
import com.muedsa.snapshot.drawWidget
import com.muedsa.snapshot.rendering.box.BoxConstraints
import com.muedsa.snapshot.widget.ProxyWidget


class LogoBenchmark {

    //@Test
    fun image_benchmark() {
        println("\n\nimage_benchmark")
        System.gc()
        val count = 10000
        val startTime = System.currentTimeMillis()
        for (i in 0..count) {
            SnapshotImage {
                logoContent()
            }
        }
        val totalTime = System.currentTimeMillis() - startTime
        val info = "Count: $count\nToto milliseconds: $totalTime\nAverage milliseconds: ${totalTime.toFloat() / count}"
        println(info)
        drawWidget("benchmark/logo", debugInfo = info) {
            logoContent()
        }
    }

    //@Test
    fun renderTree_benchmark() {
        println("\n\nrenderTree_benchmark")
        System.gc()
        val widget = ProxyWidget().apply {
            logoContent()
        }
        val count = 1000000
        val startTime = System.currentTimeMillis()
        for (i in 0..count) {
            widget.createRenderBox()
        }
        val totalTime = System.currentTimeMillis() - startTime
        val info = "Count: $count\nToto milliseconds: $totalTime\nAverage milliseconds: ${totalTime.toFloat() / count}"
        println(info)
    }

    //@Test
    fun layout_benchmark() {
        println("\n\nlayout_benchmark")
        System.gc()
        val widget = ProxyWidget().apply {
            logoContent()
        }
        val renderRoot = widget.createRenderBox()
        val count = 1000000
        val startTime = System.currentTimeMillis()
        for (i in 0..count) {
            renderRoot.layout(BoxConstraints())
        }
        val totalTime = System.currentTimeMillis() - startTime
        val info = "Count: $count\nToto milliseconds: $totalTime\nAverage milliseconds: ${totalTime.toFloat() / count}"
        println(info)
    }

}