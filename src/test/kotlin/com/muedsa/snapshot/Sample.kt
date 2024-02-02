package com.muedsa.snapshot

import com.muedsa.snapshot.widget.*
import org.jetbrains.skia.Color
import java.io.File
import kotlin.test.Test

class Sample {

    @Test
    fun sample_container() {
        File("sample_container.png").writeBytes(
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
        File("sample_layout.png").writeBytes(
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
        File("sample_image_and_text.png").writeBytes(
            SnapshotPNG {
                Stack {
                    CachedNetworkImage("https://picsum.photos/id/201/500/")
                    SimpleText("Hello World!", color = Color.RED, fontSize = 40f)
                }
            }
        )
    }
}