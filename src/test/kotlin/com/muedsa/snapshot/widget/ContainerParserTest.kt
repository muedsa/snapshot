package com.muedsa.snapshot.widget

import com.muedsa.snapshot.drawWidget
import kotlin.test.Test

class ContainerParserTest {

    @Test
    fun sized_test() {
        println("\n\n\nContainerTest.sized_test()")
        val name = "widget/container/sized"
        val description = "Container(300*300)"
        println("\n\ndraw: $name\n$description")
        drawWidget(imagePathWithoutSuffix = name, debugInfo = description, drawDebug = true) {
            Container(
                width = 300f,
                height = 300f
            )
        }
    }
}