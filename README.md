# Snapshot

![logo](logo.png)

抄[Flutter](https://github.com/flutter/flutter)实现了一个简单快捷构建一张图片的工具

```kotlin
val radius = 200f
val size = radius * 2
val delta = radius / 5f
val wholeSize = size + delta
Stack { arrayOf(
    Container(
        width = wholeSize,
        height = wholeSize,
        alignment = Alignment.TOP_LEFT
    ) {
        ClipPath(
            clipper = {
                Path().apply {
                    arcTo(
                        oval = Rect.Companion.makeWH(it.width, it.height),
                        startAngle = 45f,
                        sweepAngle = 180f,
                        forceMoveTo = true
                    )
                }
            }
        ) {
            Container(
                width = size,
                height = size,
                color = 0xFF_FF_D1_33.toInt()
            )
        }
    },
    Container(
        width = wholeSize,
        height = wholeSize,
        alignment =  Alignment.BOTTOM_RIGHT
    ) {
        ClipPath(
            clipper = {
                Path().apply {
                    arcTo(
                        oval = Rect.Companion.makeWH(it.width, it.height),
                        startAngle = 45f + 180f,
                        sweepAngle = 180f,
                        forceMoveTo = true
                    )
                }
            }
        ) {
            Container(
                width = size,
                height = size,
                color = 0xFF_FF_57_33.toInt()
            )
        }
    },
) }
```