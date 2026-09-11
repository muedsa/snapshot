package com.muedsa.snapshot.widget

import com.muedsa.snapshot.rendering.box.RenderBox

abstract class ParentDataWidget : ProxyWidget() {
    abstract fun applyParentData(renderBox: RenderBox)
}