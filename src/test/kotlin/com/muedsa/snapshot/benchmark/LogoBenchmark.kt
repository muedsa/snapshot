package com.muedsa.snapshot.benchmark

import com.muedsa.snapshot.LogoCreator.Companion.logoContent
import com.muedsa.snapshot.SnapshotImage
import com.muedsa.snapshot.drawWidget
import kotlin.test.Test


class LogoBenchmark {

    @Test
    fun benchmark() {
        val count = 10000
        val startTime = System.currentTimeMillis()
        for (i in 0..count) {
            SnapshotImage {
                logoContent()
            }
        }
        val totalTime = System.currentTimeMillis() - startTime
        val info = "Count: $count\nToto milliseconds: $totalTime\nAverage milliseconds: ${totalTime.toFloat() / count}"
        drawWidget("benchmark/logo", debugInfo = info) {
            logoContent()
        }
    }

}