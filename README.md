# Snapshot

![logo](logo.png)

抄[Flutter](https://github.com/flutter/flutter)实现了一个简单快捷构建一张图片的工具

```kotlin
Stack { arrayOf(
    Container(
        width = wholeSize,
        height = wholeSize,
        alignment = leftAlignment
    ) {
        ClipPath(
            clipper = {
                Path().apply {
                    moveTo(0f, 0f)
                    lineTo(0f, it.x)
                    lineTo(it.x, it.y)
                }
            }
        ) {
            Container(
                width = size,
                height = size,
                color = 0xFF_FF_D1_33.toInt(),
                clipBehavior = ClipBehavior.ANTI_ALIAS,
                decoration = BoxDecoration(
                    shape = BoxShape.CIRCLE,
                )
            )
        }
    },
    Container(
        width = wholeSize,
        height = wholeSize,
        alignment = rightAlignment
    ) {
        ClipPath(
            clipper = {
                Path().apply {
                    moveTo(0f, 0f)
                    lineTo(it.x, 0f)
                    lineTo(it.x, it.y)
                }
            }
        ) {
            Container(
                width = size,
                height = size,
                color = 0xFF_FF_57_33.toInt(),
                clipBehavior = ClipBehavior.ANTI_ALIAS,
                decoration = BoxDecoration(
                    shape = BoxShape.CIRCLE
                ),
            )
        }
    },
) }
```