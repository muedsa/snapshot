package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBox

abstract class ParentDataWidget(parent: Widget? = null) : ProxyWidget(parent = parent) {
    abstract fun applyParentData(renderBox: RenderBox)
}