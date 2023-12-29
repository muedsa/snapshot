package com.muedsa.snapshot

import com.muedsa.snapshot.rendering.BoxConstraints
import com.muedsa.snapshot.rendering.RenderBox

fun noLimitedLayout(renderBox: RenderBox) {
    renderBox.layout(BoxConstraints())
}