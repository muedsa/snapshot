package com.muedsa.snapshot.widget.text

import org.jetbrains.skia.paragraph.TextStyle

inline fun TextStyle(
    build: TextStyle.() -> Unit
): TextStyle {
    return TextStyle().apply {
        build()
    }
}