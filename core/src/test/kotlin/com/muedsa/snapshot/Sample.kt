package com.muedsa.snapshot

import com.muedsa.snapshot.widget.*
import org.jetbrains.skia.Color
import kotlin.test.Test

class Sample {

    @Test
    fun sample_container() {
        rootDirection.resolve("sample_container.png").toFile().writeBytes(
            SnapshotPNG {
                Container(
                    width = 200f,
                    height = 200f,
                    color = Color.RED
                )
            }
        )
    }

    @Test
    fun sample_layout() {
        rootDirection.resolve("sample_layout.png").toFile().writeBytes(
            SnapshotPNG {
                Column {
                    Row {
                        Container(
                            width = 200f,
                            height = 200f,
                            color = Color.RED
                        )
                        Container(
                            width = 200f,
                            height = 200f,
                            color = Color.GREEN
                        )
                    }
                    Row {
                        Container(
                            width = 200f,
                            height = 200f,
                            color = Color.BLUE
                        )
                        Container(
                            width = 200f,
                            height = 200f,
                            color = Color.YELLOW
                        )
                    }
                }
            }
        )
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun sample_image_and_text() {
        rootDirection.resolve("sample_image_and_text.png").toFile().writeBytes(
            SnapshotPNG {
                Stack {
                    CachedNetworkImage(
                        url = "https://samples-files.com/samples/Images/jpg/1280-720-sample.jpg",
                        width = 400f,
                        height = 400f,
                    )
                    SimpleText("Hello World!", color = Color.RED, fontSize = 40f)
                }
            }
        )
    }
}