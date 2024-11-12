package com.muedsa.snapshot.widget

import com.muedsa.snapshot.drawWidget
import org.junit.jupiter.api.Test

class CachedNetworkImageTest {

    @Test
    fun networkImage_test() {
        println("\n\n\nCachedNetworkImageTest.networkImage_test()")
        val name = "widget/image/networkImage"
        val description = "NetworkImage"
        println("\n\ndraw: $name\n$description")
        drawWidget(imagePathWithoutSuffix = name, debugInfo = description, drawDebug = false) {
            Row {
                Column {
                    CachedNetworkImage(url = "https://samples-files.com/samples/images/jpg/1280-720-sample.jpg",
                        width = 400f,
                        height = 400f
                    )
                    CachedNetworkImage(url = "https://samples-files.com/samples/images/jpg/1280-720-sample.jpg",
                        width = 400f,
                        height = 400f
                    )
                }
                Column {
                    CachedNetworkImage(url = "https://samples-files.com/samples/images/jpg/1280-720-sample.jpg",
                        width = 400f,
                        height = 400f
                    )
                    CachedNetworkImage(url = "https://samples-files.com/samples/images/jpg/1280-720-sample.jpg",
                        width = 400f,
                        height = 400f
                    )
                }
            }
        }
    }

    @Test
    fun networkImage_noCache_test() {
        println("\n\n\nCachedNetworkImageTest.networkImage_noCache_test()")
        val name = "widget/image/networkImage_noCache"
        val description = "NetworkImageNoCache"
        println("\n\ndraw: $name\n$description")
        drawWidget(imagePathWithoutSuffix = name, debugInfo = description, drawDebug = false) {
            Row {
                Column {
                    CachedNetworkImage(url = "https://samples-files.com/samples/images/jpg/1280-720-sample.jpg",
                        width = 400f,
                        height = 400f,
                        noCache = true
                    )
                    CachedNetworkImage(url = "https://samples-files.com/samples/images/jpg/1280-720-sample.jpg",
                        width = 400f,
                        height = 400f,
                        noCache = true
                    )
                }
                Column {
                    CachedNetworkImage(url = "https://samples-files.com/samples/images/jpg/1280-720-sample.jpg",
                        width = 400f,
                        height = 400f,
                        noCache = true
                    )
                    CachedNetworkImage(url = "https://samples-files.com/samples/images/jpg/1280-720-sample.jpg",
                        width = 400f,
                        height = 400f,
                        noCache = true
                    )
                }
            }
        }
    }

}